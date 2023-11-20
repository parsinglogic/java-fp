/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.InvalidArgument;
import dev.javafp.func.FnBlock;

/**
 *
 * <p> A factory that creates
 * {@code ImRangeList}s
 * (mainly)
 * which are ranges of integers
 */
public class ImRange
{

    private ImRange()
    {
    }

    /**
     * <p> A list of integers
     * {@code [min, min + 1, min + 2, ... max]}
     * <p> If
     * {@code min > max}
     *  then return
     * {@code []}
     * <p> If
     * {@code min == max}
     *  then return
     * {@code [min]}
     *
     */
    public static ImList<Integer> inclusive(int min, int max)
    {
        return inclusive(min, max, 1);
    }

    /**
     * <p> A list of integers
     * {@code [min, min + step, min + 2*step, ... max]}
     * <p> If
     * {@code min > max}
     *  then return
     * {@code []}
     * <p> If
     * {@code step}
     * is not a factor of
     * {@code max - min}
     *  then throw
     * {@link InvalidArgument}
     *
     */
    public static ImList<Integer> inclusive(int min, int max, int step)
    {

        if (min > max)
            return ImList.on();

        if ((max - min) % step != 0)
            throw new InvalidArgument("step", step, String.format("value must divide max - min but step = %d, min = %d, max = %d", step, min, max));

        return ImRangeList.inclusive(min, max, step);
    }

    /**
     * <p> A list of integers
     * {@code [0, 1, 2, ... maxIndexPlusOne - 1]}
     * <p> If
     * {@code maxIndexPlusOne <= 0}
     *  then return
     * {@code []}
     * <p> If
     * {@code maxIndexPlusOne == 1 }
     *  then return
     * {@code [0]}
     *
     */
    public static ImList<Integer> zeroTo(int maxIndexPlusOne)
    {
        return inclusive(0, maxIndexPlusOne - 1);
    }

    /**
     * The same as
     * {@code inclusive(1, max)}
     */
    public static ImList<Integer> oneTo(int max)
    {
        return inclusive(1, max);
    }

    /**
     * <p> A list of integers (an
     * <strong>infinite</strong>
     *  list in fact)
     *
     * {@code [start, start + step, start + 2*step, ... ]}
     *
     *
     */
    public static ImList<Integer> step(int start, int step)
    {
        return ImList.unfold(start, i -> i + step);
    }

    /**
     * <p> Do
     * {@code fn}
     * {@code count}
     *  times
     *
     */
    public static void nTimesDo(int count, FnBlock fn)
    {
        for (int i = 0; i < count; i++)
        {
            fn.doit();
        }
    }
}