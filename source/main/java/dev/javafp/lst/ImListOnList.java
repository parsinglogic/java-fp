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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

class ImListOnList<A> extends ImEagerList<A> implements Eq
{

    private final int skipCount;
    private final List<A> source;

    private ImListOnList(List<A> source, int skipCount, int size)
    {
        super(size);
        this.source = source;
        this.skipCount = skipCount;
    }

    static <A> ImList<A> on(List<A> source, int skipCount, int size)
    {
        return size == 0
               ? ImList.on()
               : new ImListOnList(source, skipCount, size);
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
        return source.get(skipCount);
    }

    /**
     * <p> {@code this}
     *  without the first element.
     *
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public ImList<A> tail()
    {
        return on(source, skipCount + 1, size() - 1);
    }

    @Override
    public List<A> toList()
    {
        return skipCount == 0 && size() == source.size()
               ? source
               : source.subList(skipCount, size());
    }

    @Override
    public A[] toArray(Class<?> clazz)
    {
        return skipCount == 0
               ? source.toArray(((A[]) (Array.newInstance(clazz, size()))))
               : super.toArray(clazz);
    }

    /**
     * <p> We can optimise if we are a list list
     */
    @Override
    public A at(int indexStartingAtOne)
    {
        Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());
        return source.get(skipCount + indexStartingAtOne - 1);

    }

    /**
     * <p> So this is a fast way (I hope) to append an element
     * <p> We copy the underlying array with an extra slot
     *
     */
    @Override
    public ImList<A> appendElement(A element)
    {
        // Create a copy of the whole list - as an array
        A[] whole = (A[]) source.toArray();

        // Copy a range of this, and add a slot at the end
        A[] newArray = Arrays.copyOfRange(whole, skipCount, whole.length + 1);

        // Add the element
        newArray[newArray.length - 1] = element;

        // Return the new ImList - this time as LstOnArray
        return ImListOnArray.on(newArray, 0, newArray.length);

    }

}