/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.func.FnProducer;
import dev.javafp.time.StopWatch;

import java.util.concurrent.ExecutorService;

import static dev.javafp.util.Say.say;

/**
 * <p> Utility for working with {@link Thread}s.
 */
public class ThreadUtils
{

    /**
     * <p> Stop any threads in
     * {@code executorService}
     * , checking for null and log a message about what is going on
     * <ul>
     * <li>
     * <p> using this tells the user more about what is happening in tests
     * </li>
     * </ul>
     *
     */
    public static void stopThread(String name, ExecutorService executorService)
    {
        if (executorService == null)
            say("not stopping thread " + name + " because it is not running");
        else
        {
            say("stopping thread", name);
            executorService.shutdown();
        }
    }

    /**
     * <p> Sleep for
     * {@code seconds}
     *  seconds and log the message
     * {@code message}
     *  - using this tells the user more about what is happening in tests
     *
     */
    public static void sleep(int seconds, String message)
    {
        say("sleeping for", seconds, "seconds", message);
        StopWatch.sleep(seconds * 1000);
        say("Finished sleeping for", seconds, "seconds", message);
    }

    public static void addShutdownHook(FnProducer func)
    {
        Say.say("Adding shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> func.doit()));
    }
}