/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import java.util.concurrent.locks.LockSupport;

/**
 * 
 */
public class StopWatch
{
    // Start time in nanoseconds from System.nanoTime()
    private long startTime;

    // Elapsed time in nanoseconds
    private long accruedNanos;

    private boolean paused;

    public StopWatch()
    {
        startTime = System.nanoTime();
    }

    public long getElapsedNanos()
    {
        return paused
               ? accruedNanos
               : accruedNanos + System.nanoTime() - startTime;
    }

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

    public void reset()
    {
        startTime = System.nanoTime();
        accruedNanos = 0;
    }

    public void pause()
    {
        accruedNanos += System.nanoTime() - startTime;
        paused = true;
    }

    public void resume()
    {
        paused = false;
        startTime = System.nanoTime();
    }

    public static void sleep(int millisToSleep)
    {
        LockSupport.parkNanos(millisToSleep * 1000 * 1000);
    }

}