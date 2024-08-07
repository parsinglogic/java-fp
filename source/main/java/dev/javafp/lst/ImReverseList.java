/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.eq.Eq;
import dev.javafp.ex.FunctionNotAllowedOnEmptyList;
import dev.javafp.ex.Throw;

/**
 * <p> A list that is the reverse of its source - which is an array.
 */
class ImReverseList<A> extends ImEagerList<A> implements Eq
{
    private final A[] source;
    private final int skipCount;

    private ImReverseList(A[] source, int skipCount, int size)
    {
        super(size);
        this.source = source;
        this.skipCount = skipCount;
    }

    static <A> ImList<A> on(A[] source, int skipCount, int size)
    {
        if (size == 0)
            return ImList.empty();
        else
        {
            Throw.Exception.ifOutOfRange("skipCount + size", skipCount + size, 1, source.length);
            Throw.Exception.ifOutOfRange("ssize", size, 1, source.length);

            return new ImReverseList(source, skipCount, size);
        }
    }

    static <A> ImList<A> on(A[] source)
    {
        return on(source, 0, source.length);
    }

    /**
     * The first element in
     * {@code this}
     * .
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public A head()
    {
        return at(1);
    }

    /**
     * `this` without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public ImList<A> tail()
    {
        return on(source, skipCount, size() - 1);
    }

    /**
     * <p> We can optimise if we are an array list
     */
    @Override
    public A at(int indexStartingAtOne)
    {
        Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());
        return source[skipCount + size() - indexStartingAtOne];
    }

    @Override
    public ImList<A> drop(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);

        return count == 0
               ? this
               : count >= size()
                 ? ImList.on()
                 : on(source, skipCount, size() - count);
    }

    @Override
    public ImList<A> take(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);

        return count == 0
               ? ImList.on()
               : count >= size()
                 ? this
                 : on(source, skipCount + size() - count, count);
    }

    @Override
    public ImList<A> reverse()
    {
        // return a list on the source
        return ImListOnArray.on(source, skipCount, size());
    }
}