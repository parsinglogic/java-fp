package dev.javafp.time;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StopWatchTest
{
    @Test
    public void testPause()
    {
        StopWatch pauseTimer = new StopWatch();
        StopWatch timer1 = new StopWatch();
        StopWatch.sleep(100);

        pauseTimer.pause();
        long t1 = timer1.getElapsedMilliseconds();
        long pt = pauseTimer.getElapsedMilliseconds();

        StopWatch.sleep(50);

        // The paused timer should not have changed its elapsed time
        assertEquals(pt, pauseTimer.getElapsedMilliseconds());

        // The other timer should have changed its elapsed time by about 50
        assertDiffIsLessThan(timer1.getElapsedMilliseconds() - t1, 50, 10);

        StopWatch.sleep(50);

        StopWatch timer2 = new StopWatch();
        pauseTimer.resume();
        StopWatch.sleep(100);

        long pauseTime = pauseTimer.getElapsedMilliseconds();
        long t2 = timer2.getElapsedMilliseconds();

        // Pause time should be almost the same as t1 + t2

        assertDiffIsLessThan(t1 + t2, pauseTime, 10);
    }

    private void assertDiffIsLessThan(long one, long two, long expectedDiff)
    {
        long diff = Math.abs(one - two);
        assertTrue("The difference between " + one + " and " + two + " is " + diff
                + " which is not < " + expectedDiff, diff < expectedDiff);
    }

}