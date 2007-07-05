package groovy.time

import org.codehaus.groovy.runtime.TimeCategory
import java.util.Date

class DurationTest extends GroovyTestCase {
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000): \
                "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"
            
            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000): \
                "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"
        }
   }

    void fixme_testDurationArithmetic() {
        use(TimeCategory) {
            //def nowOffset = (new Date()).daylightSavingsOffset
            def nowOffset = 0.months.from.now.daylightSavingsOffset

            // add two durations
            def twoMonthsA = 1.month + 1.month
            def offsetA = twoMonthsA.daylightSavingsOffset - nowOffset
            // subtract dates which are two months apart
            def offsetB = 2.months.from.now.daylightSavingsOffset - 0.months.from.now.daylightSavingsOffset
            def twoMonthsB = 2.months.from.now + offsetB - 0.months.from.now
            assertEquals "Two months absolute duration should be the same as the difference between two dates two months apart\n",
                (twoMonthsA + offsetA).toMilliseconds(), twoMonthsB.toMilliseconds()

            // add two durations
            def monthAndWeekA = 1.month + 1.week
            offsetA = monthAndWeekA.daylightSavingsOffset - nowOffset
            // subtract absolute date and a duration from another absolute date
            offsetB = (1.month.from.now + 1.week).daylightSavingsOffset - 0.months.from.now.daylightSavingsOffset
            def monthAndWeekB = 1.month.from.now + 1.week + offsetB - 0.months.from.now
            assertEquals "A week and a month absolute duration should be the same as the difference between two dates that far apart\n",
                (monthAndWeekA + offsetA).toMilliseconds(), monthAndWeekB.toMilliseconds()
        }
    }

    void testDatumDependantArithmetic() {
        use(TimeCategory) {
            def now = new Date()
            def then = (now + 1.month) + 1.week
            def week = then - (now + 1.month)
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000): \
                "Expected ${7 * 24 * 60 * 60 * 1000} but was ${week.toMilliseconds()}"
        }
    }
}
