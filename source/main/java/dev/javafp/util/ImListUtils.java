/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.tuple.ImPair;

public class ImListUtils
{

    /**
     * <p> Split
     * {@code things}
     *  into two subsequences with the first having size
     * {@code count}
     * , The elements for the subsequences are
     * chosen randomly.
     *
     * <p> An exception is thrown if this is not true:
     *
     * <pre>{@code
     * 0 <= count <= things.size
     * }</pre>
     *
     */
    public static <A> ImPair<ImList<A>, ImList<A>> randomSubSeq(ImList<A> things, int count)
    {
        Throw.Exception.ifOutOfRange("count", count, 0, things.size());

        ImList<ImPair<Integer, A>> pairs = Range.oneTo(things.size()).zip(things).shuffle();

        return pairs.splitAfterIndex(count).map(ImListUtils::reorder, ImListUtils::reorder);

    }

    private static <A> ImList<A> reorder(ImList<ImPair<Integer, A>> pairs)
    {
        return pairs.sort(i -> i.fst).map(i -> i.snd);
    }
}