package groovy

class TryCatchTest extends GroovyTestCase {

    def exceptionCalled
    def finallyCalled
	
    void testTryCatch() {
        try {
            failingMethod()
        }
        catch (AssertionError e) {
            onException(e)
        }
        finally {
            onFinally()
        }
        afterTryCatch()
        assert exceptionCalled , "should have invoked the catch clause"
        assert finallyCalled , "should have invoked the finally clause"
        println("After try/catch")
     }


     void testTryFinally() {
         Boolean touched = false;
         
         try {
         }
         finally {
             touched = true;
         }

         assert touched , "finally not called with empty try"
     }



     void testWorkingMethod() {
         /** @todo causes inconsistent stack height
          assert exceptionCalled == false , "should not invoked the catch clause"
          */
         
         try {
	    	 workingMethod()
	     }
	     catch (AssertionError e) {
		     onException(e)
	     }
	     finally {
		     onFinally()
	     }
	     assert exceptionCalled == false , "should not invoked the catch clause"
	     assert finallyCalled , "should have invoked the finally clause"
	     println("After try/catch")
    }
    
    void failingMethod() {
        assert false , "Failing on purpose"
	}
	
    void workingMethod() {
        assert true , "Should never fail"
    }
    
    void onException(e) {
	    assert e != null
	    exceptionCalled = true
	}
	
    void onFinally() {
        finallyCalled = true
	}

    void afterTryCatch() {
        assert exceptionCalled , "should have invoked the catch clause"        
        assert finallyCalled , "should have invoked the finally clause"
        println("After try/catch")
    }
    
    protected void setUp() {
        exceptionCalled = false
        finallyCalled = false
    }
    
    void testTryWithReturnWithPrimitiveTypes() {
      assert intTry() == 1
      assert longTry() == 2
      assert byteTry() == 3
      assert shortTry() == 4
      assert charTry() == "c"
      assert floatTry() == 1.0
      assert doubleTry() == 2.0
    }
    
    int intTry(){
      try {
        return 1
      } finally {}
    }
    
    long longTry(){
      try {
        return 2
      } finally {}
    }
    
    byte byteTry(){
      try {
        return 3
      } finally {}
    }
    
    short shortTry(){
      try {
        return 4
      } finally {}
    }
    
    char charTry(){
      try {
        return 'c'
      } finally {}
    }
    
    float floatTry(){
      try {
        return 1.0
      } finally {}
    }
    
    double doubleTry(){
      try {
        return 2.0
      } finally {}
    }
    
    void testTryCatchWithUntyped(){
      try {
        throw new Exception();
      } catch(e) {
        assert true
        return
      }
      assert false
    }
}
