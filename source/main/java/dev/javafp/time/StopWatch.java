/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import java.util.concurrent.locks.LockSupport;

/**
 * A timer that uses {@link System#nanoTime()}.
 *
 * It is intended to act like a physical stopwatch - in that it has:
 *
 * 1. start
 * 2. resume
 * 3. pause
 *
 * However, the class is immutable so that eaach of the methods above will return a new StopWatch
 *
 *
 * This is implemented by having two classes tha implement `StopWatch` - `PausedStopWatch` and `RunningStopWatch`
 *
 * When a stopwatch is paused:
 *
 *    time t1: s = StopWatch().start;
 *
 *    time t2: StopWatch p = s.pause();
 *
 *    time t2: StopWatch r = p.resume();
 *
 *    time t2: s.getElapsedNanos()
 *
 * `p` refers to a `PausedStopWatch`.
 *
 * `r` refers to a `RunningStopWatch`
 *
 * `s` is still a running stopwatch and the elapsed nanoseconds can still be obtained from it.
 *
 *
 * A running stopwatch created from a paused stopwatch will remember the time taken so far by the paused stopwatch
 */
public abstract class StopWatch
{

    public static StopWatch start()
    {
        return RunningStopWatch.start();
    }

    abstract long getElapsedNanos();

    abstract StopWatch pause();

    abstract StopWatch resume();

    public long getElapsedMicroseconds()
    {
        return getElapsedNanos() / 1000;
    }

    public long getElapsedMilliseconds()
    {
        return getElapsedMicroseconds() / 1000;
    }

    public long getElapsedSeconds()
    {
        return getElapsedMilliseconds() / 1000;
    }

    public double getElapsedMs()
    {
        return getElapsedMilliseconds();
    }

    public static void sleep(int millisToSleep)
    {
        LockSupport.parkNanos(millisToSleep * 1000 * 1000);
    }

    public static void sleepSeconds(int seconds)
    {
        sleep(seconds * 1000);
    }

}