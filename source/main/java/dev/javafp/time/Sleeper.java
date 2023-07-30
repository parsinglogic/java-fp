/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import dev.javafp.util.Say;

/**
 * <p> Utility to "sleep" a thread - used by {@link Clock}.
 */
public class Sleeper
{

    public void sleep(long msToSleep)
    {
        Clock.incSleepCount();
        try
        {
            sleep_(msToSleep);
        } finally
        {
            Clock.decSleepCount();
        }

    }

    public void sleep_(long msToSleep)
    {

        if (Clock.getAwakeCount() <= 0)
            return;

        int count = 0;
        long endTime = Clock.time + msToSleep;
        //Say.printf("Sleep for %s, time = %s, endTime = %s%n", msToSleep, Clock.time, endTime);

        //System.out.printf("activeCount = %s, sleepCount = %s%n", Clock.getActiveCount(), Clock.getSleepCount());

        while (Clock.time < endTime)
        {
            //            Say.printf(".%n");

            //            System.out.printf(".");
            //            System.out.flush();
            Clock.realSleep(10);

            //Clock.currentTimeMillis();
            count += 10;

            if (Clock.getAwakeCount() <= 0)
                break;

            if (count > 100)
                break;
        }

        Say.printf("Slept for %s, count %s%n", msToSleep, count);

    }
}