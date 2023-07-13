package dev.javafp.time;

import dev.javafp.tuple.ImPair;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TimerTest
{
    @Test
    public void testTimer2()
    {
        Timer t1 = Timer.time(() -> StopWatch.sleep(50));
        System.out.println(t1.getNanos());
        System.out.println(t1.getMillis());

        StopWatch.sleep(50);

        Timer t2 = Timer.time(() -> StopWatch.sleep(50));
        System.out.println(t2.getNanos());

        long elapsed = (t1.getNanos() + t2.getNanos());
        System.out.println("elapsed time in nanos " + elapsed);

        assertTrue("" + elapsed, elapsed > 100 * 1000 * 1000 && elapsed < 120 * 1000 * 1000);
    }

    @Test
    public void testSimple()
    {
        int millisToSleep = 50;
        Timer timer = Timer.time(() -> StopWatch.sleep(millisToSleep));

        System.out.println(timer.getMillis());
        assertTrue("" + timer.getMillis(), timer.getMillis() - millisToSleep <= 10);
    }

    @Test
    public void testPair()
    {
        int millisToSleep = 50;

        ImPair<Timer, String> p = Timer.time(() ->
        {
            StopWatch.sleep(millisToSleep);
            return "foo";
        });

        System.out.println(p.fst.getMillis());

        assertDiffIsLessThan(p.fst.getMillis(), millisToSleep, 10);

    }

    private void assertDiffIsLessThan(long one, long two, long expectedDiff)
    {
        long diff = Math.abs(one - two);
        assertTrue("The difference between " + one + " and " + two + " is " + diff
                + " which is not < " + expectedDiff, diff < expectedDiff);
    }

}