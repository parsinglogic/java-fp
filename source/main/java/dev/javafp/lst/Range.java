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
 * {@code RangeList}s
 * (mainly)
 * which are ranges of integers
 */
public class Range
{

    public static ImList<Integer> inclusive(int min, int max)
    {
        return inclusive(min, max, 1);
    }

    public static ImList<Integer> inclusive(int min, int max, int step)
    {

        if (min > max)
            return ImList.on();

        if ((max - min) % step != 0)
            throw new InvalidArgument(String.format("step value must divide max - min but step = %d, min = %d, max = %d", step, min, max));

        return RangeList.inclusive(min, max, step);
    }

    public static ImList<Integer> zeroTo(int maxIndexPlusOne)
    {
        return inclusive(0, maxIndexPlusOne - 1);
    }

    public static ImList<Integer> oneTo(int max)
    {
        return inclusive(1, max);
    }

    public static ImList<Integer> step(int start, int step)
    {
        return ImList.unfold(start, i -> i + step);
    }

    public static void nTimesDo(int count, FnBlock fn)
    {
        for (int i = 0; i < count; i++)
        {
            fn.doit();
        }
    }
}