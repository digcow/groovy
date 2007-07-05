/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen;

import groovy.lang.MetaMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Code generates a Reflector
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ReflectorGenerator implements Opcodes {

    private List methods;
    private ClassVisitor cv;
    private BytecodeHelper helper = new BytecodeHelper(null);
    private String classInternalName;

    public ReflectorGenerator(List methods) {
        this.methods = methods;
    }

    public void generate(ClassVisitor cv, String className) {
        this.cv = cv;

        classInternalName = BytecodeHelper.getClassInternalName(className);
        cv.visit(ClassGenerator.asmJDKVersion, ACC_PUBLIC + ACC_SUPER, classInternalName, (String) null, "org/codehaus/groovy/runtime/Reflector", null);

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/Reflector", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);

        generateInvokeMethod();

        cv.visitEnd();
    }

    protected void generateInvokeMethod() {
        int methodCount = methods.size();

        MethodVisitor mv = cv.visitMethod(
                ACC_PUBLIC,
                "invoke",
                "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                null,
                null);

        // load parameters for the helper method call
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);

        // get method number for switch
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "groovy/lang/MetaMethod", "getMethodIndex", "()I");

        // init meta methods with number
        Label defaultLabel = new Label();
        Label[] labels = new Label[methodCount];
        int[] indices = new int[methodCount];
        for (int i = 0; i < methodCount; i++) {
            labels[i] = new Label();
            MetaMethod method = (MetaMethod) methods.get(i);
            method.setMethodIndex(i + 1);
            indices[i] = method.getMethodIndex();
        }

        // do switch
        mv.visitLookupSwitchInsn(defaultLabel, indices, labels);
        // create switch cases
        for (int i = 0; i < methodCount; i++) {
            // call helper for invocation
            mv.visitLabel(labels[i]);
            mv.visitMethodInsn(
                    INVOKESPECIAL,
                    classInternalName,
                    "m" + i,
                    "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(ARETURN);
        }

        // call helper for error
        mv.visitLabel(defaultLabel);
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                classInternalName,
                "noSuchMethod",
                "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        // end method
        mv.visitMaxs(4, 4);
        mv.visitEnd();

        // create helper methods m*
        for (int i = 0; i < methodCount; i++) {
            mv = cv.visitMethod(
                    ACC_PRIVATE,
                    "m" + i,
                    "(Lgroovy/lang/MetaMethod;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
                    null,
                    null);
            helper = new BytecodeHelper(mv);

            MetaMethod method = (MetaMethod) methods.get(i);
            invokeMethod(method, mv);
            if (method.getReturnType() == void.class) {
                mv.visitInsn(ACONST_NULL);
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    protected void invokeMethod(MetaMethod method, MethodVisitor mv) {
        // compute class to make the call on
        Class callClass = method.getInterfaceClass();
        boolean useInterface = false;
        if (callClass == null) {
            callClass = method.getCallClass();
        } else {
            useInterface = true;
        }
        // get bytecode information
        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes());

        // make call
        if (method.isStatic()) {
            loadParameters(method, 3, mv);
            mv.visitMethodInsn(INVOKESTATIC, type, method.getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 2);
            helper.doCast(callClass);
            loadParameters(method, 3, mv);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, method.getName(), descriptor);
        }

        helper.box(method.getReturnType());
    }

    protected void loadParameters(MetaMethod method, int argumentIndex, MethodVisitor mv) {
        Class[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i];
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }
}
