/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import dev.javafp.util.Say;

import static dev.javafp.util.Say.say;

/**
 * <p> Utility for diagnostic output.
 */
public class Clock
{
    public static long time = 0;
    private static boolean isRealTime = false;

    private static int sleepCount;
    private static int activeCount;

    public static synchronized long currentTimeMillis()
    {
        //        System.out.printf("Getting time %s%n", time);

        return isRealTime
               ? getRealTime()
               : time++;
    }

    public static void sleep(long msToSleep)
    {
        if (isRealTime)
            realSleep(msToSleep);
        else
            new Sleeper().sleep(msToSleep);
    }

    public static void realSleep(long msToSleep)
    {
        try
        {
            Thread.sleep(msToSleep);

        } catch (InterruptedException e)
        {
        }
    }

    public synchronized static long getRealTime()
    {
        return System.currentTimeMillis();
    }

    public static void beReal(boolean beReal)
    {
        isRealTime = beReal;
    }

    public static synchronized void jump(long msToJump)
    {
        time += msToJump;
    }

    public static synchronized void incSleepCount()
    {
        sleepCount++;
        //Say.printf("inc sleep count %s%n", getSleepCount());
    }

    public static synchronized void decSleepCount()
    {
        sleepCount--;
        //Say.printf("dec sleep count %s%n", getSleepCount());
    }

    public static synchronized int getSleepCount()
    {
        return sleepCount;
    }

    public static synchronized void incActiveCount()
    {
        activeCount++;
    }

    public static synchronized void decActiveCount()
    {
        activeCount--;
        Say.printf("dec active count %s%n", getActiveCount());
    }

    public static synchronized int getActiveCount()
    {
        return activeCount;
    }

    public synchronized static int getAwakeCount()
    {
        return activeCount - sleepCount;
    }

    public static void sleep(int seconds, String message)
    {
        say("Sleeping for", seconds, "seconds", message);
        realSleep(seconds * 1000);
        say("Finished sleeping for", seconds, "seconds", message);
    }

    public static void sleepMs(int ms, String message)
    {
        say("Sleeping for", ms, "ms", message);
        realSleep(ms);
        say("Finished sleeping for", ms, "ms", message);
    }

    public static void sleepSeconds(int seconds)
    {
        realSleep(seconds * 1000);
    }

}