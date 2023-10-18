package dev.javafp.time;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StopWatchTest
{

    @Test
    public void testBasic()
    {
        long startTime = 5;
        long endTime = 17;
        long pauseTime = 345;
        long resumeTime = 324324;
        long finalTime = 56765757;
        long lastTime = 878798898;

        FakeNanoTimeProvider tp = new FakeNanoTimeProvider(ImList.on(startTime, endTime, pauseTime, resumeTime, finalTime, lastTime));
        StopWatch s = RunningStopWatch.start(tp);

        assertEquals(endTime - startTime, s.getElapsedNanos());

        StopWatch p = s.pause();

        assertEquals(pauseTime - startTime, p.getElapsedNanos());
        assertEquals(pauseTime - startTime, p.getElapsedNanos());

        StopWatch r = p.resume();

        assertEquals(pauseTime - startTime + finalTime - resumeTime, r.getElapsedNanos());

        assertEquals(lastTime - startTime, s.getElapsedNanos());

    }

    @Test
    public void testPause()
    {
        StopWatch timer0 = StopWatch.start();
        StopWatch timer1 = StopWatch.start();
        StopWatch.sleep(100);

        StopWatch pauseTimer = timer0.pause();
        long t1 = timer1.getElapsedMilliseconds();
        long pt = pauseTimer.getElapsedMilliseconds();

        StopWatch.sleep(50);

        // The paused timer should not have changed its elapsed time
        assertEquals(pt, pauseTimer.getElapsedMilliseconds());

        // The other timer should have changed its elapsed time by about 50
        assertDiffIsLessThan(timer1.getElapsedMilliseconds() - t1, 50, 10);

        StopWatch.sleep(50);

        // Start a new timer
        StopWatch timer2 = StopWatch.start();

        // resume the paused timer
        StopWatch resumed = pauseTimer.resume();
        StopWatch.sleep(100);

        long pauseTime = resumed.getElapsedMilliseconds();
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