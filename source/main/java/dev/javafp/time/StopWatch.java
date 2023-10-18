/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import java.util.concurrent.locks.LockSupport;

/**
 * <p> A timer that uses {@link System#nanoTime()}.
 * <p> It is intended to act like a physical stopwatch - in that it has:
 * <ol>
 * <li>
 * <p> start
 * </li>
 * <li>
 * <p> resume
 * </li>
 * <li>
 * <p> pause
 * </li>
 * </ol>
 * <p> However, the class is immutable so that each of the methods above will return a new stopwatch
 * <p> This is implemented by having two classes tha implement
 * {@code StopWatch}
 *  -
 * {@code PausedStopWatch}
 *  and
 * {@code RunningStopWatch}
 * <p> When a stopwatch is paused:
 * <p> time t1: s = StopWatch().start;
 * <p> time t2: StopWatch p = s.pause();
 * <p> time t2: StopWatch r = p.resume();
 * <p> time t2: s.getElapsedNanos()
 * <p> {@code p}
 *  refers to a
 * {@code PausedStopWatch}
 * .
 * <p> {@code r}
 *  refers to a
 * {@code RunningStopWatch}
 * <p> {@code s}
 *  is still a running stopwatch and the elapsed nanoseconds can still be obtained from it.
 * <p> A running stopwatch created from a paused stopwatch will remember the time taken so far by the paused stopwatch
 *
 */
public abstract class StopWatch
{

    /**
     * Create a running stopwatch
     */
    public static StopWatch start()
    {
        return RunningStopWatch.start();
    }

    /**
     * The total elapsed nanoseconds for this stopwatch
     */
    abstract long getElapsedNanos();

    /**
     * Create a paused stopwatch from the running stopwatch
     */
    abstract StopWatch pause();

    /**
     * Create a running stopwatch from a paused stopwatch
     */
    abstract StopWatch resume();

    /**
     * The total elapsed microseconds for this stopwatch
     */
    public long getElapsedMicroseconds()
    {
        return getElapsedNanos() / 1000;
    }

    /**
     * The total elapsed milliseconds for this stopwatch
     */
    public long getElapsedMilliseconds()
    {
        return getElapsedMicroseconds() / 1000;
    }

    /**
     * The total elapsed seconds for this stopwatch
     */
    public long getElapsedSeconds()
    {
        return getElapsedMilliseconds() / 1000;
    }

    /**
     * <p> Sleep the current thread for
     * {@code millisToSleep}
     *  milliseconds
     *
     */
    public static void sleep(int millisToSleep)
    {
        LockSupport.parkNanos(millisToSleep * 1000 * 1000);
    }

    /**
     * <p> Sleep the current thread for
     * {@code seconds}
     *  seconds
     *
     */
    public static void sleepSeconds(int secondsToSleep)
    {
        sleep(secondsToSleep * 1000);
    }

}