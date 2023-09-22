/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.time;

import dev.javafp.func.FnBlock;
import dev.javafp.func.FnProducer;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;

/**
 * <p> A timer that measures elapsed time in nanoseconds.
 */
public class Timer
{
    // The elapsed time in nanos
    private long nanos;

    private Timer(long nanos)
    {
        this.nanos = nanos;
    }

    /**
     * <p> Time the block
     * {@code block}
     *
     */
    public static Timer time(FnBlock block)
    {
        long startTime = System.nanoTime();
        block.doit();
        return new Timer(System.nanoTime() - startTime);
    }

    /**
     * <p> Time the producer
     * {@code fn}
     *  and return a pair with the timer and the result of
     * {@code fn}
     *
     */
    public static <A> ImPair<Timer, A> time(FnProducer<A> fn)
    {
        long startTime = System.nanoTime();
        A result = fn.doit();
        return Pai.r(new Timer(System.nanoTime() - startTime), result);

    }

    /**
     *
     * <p> The number of milliseconds that
     * {@code this}
     *  represents, rounded to a
     * {@code long}
     *
     */
    public long getMillis()
    {
        return Math.round(getNanos() * 0.000001);
    }

    /**
     * <p> The number of milliseconds that
     * {@code this}
     *  represents, as a
     * {@code Double}
     * .
     *
     */
    public Double getMs()
    {
        return getNanos() * 0.000001;
    }

    /**
     * <p> The number of nanoseconds that
     * {@code this}
     *  represents.
     *
     */
    public long getNanos()
    {
        return nanos;
    }
}