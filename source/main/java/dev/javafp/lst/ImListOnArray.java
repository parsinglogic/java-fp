/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

class ImListOnArray<A> extends ImEagerList<A>
{
    private final A[] source;
    private final int skipCount;

    private ImListOnArray(A[] source, int skipCount, int size)
    {
        super(size);
        this.source = source;
        this.skipCount = skipCount;
    }

    @Override
    public A head()
    {
        return source[skipCount];
    }

    public static <A> ImList<A> on(A[] source, int skipCount, int size)
    {
        return size == 0
               ? ImList.empty()
               : new ImListOnArray(source, skipCount, size);
    }

    public static <A> ImList<A> on(A[] source)
    {
        return source.length == 0
               ? ImList.empty()
               : new ImListOnArray(source, 0, source.length);
    }

    @Override
    public ImList<A> tail()
    {
        return on(source, skipCount + 1, size - 1);
    }

    @Override
    public A[] toArray(Class<?> clazz)
    {
        Class cl = Array.newInstance(clazz, 0).getClass();

        // We already have the elements in an array. All we have to do is copy the array.

        // If source array is being "used completely" and its component type is the same as clazz then we don't even need to copy it

        return skipCount == 0 && size() == source.length && Eq.uals(clazz, source.getClass().getComponentType())
               ? source
               : Arrays.copyOfRange(source, skipCount, skipCount + size, (Class<? extends A[]>) cl); //clazz.arrayType());
    }

    /**
     * <p> We can optimise if we are an array list
     */
    @Override
    public A at(int indexStartingAtOne)
    {
        Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());
        return source[skipCount + indexStartingAtOne - 1];
    }

    /**
     * <p> So this is a fast way (I hope) to append an element
     * <p> We copy the underlying array with an extra slot
     *
     */
    @Override
    public ImList<A> appendElement(A element)
    {
        // Create a copy
        A[] newArray = Arrays.copyOfRange(source, skipCount, skipCount + size() + 1);

        // Add the element
        newArray[newArray.length - 1] = element;

        // Return the new ImList
        return ImListOnArray.on(newArray, 0, size() + 1);
    }

    /**
     * <p> So this is a fast way (I hope) to drop elements
     */
    @Override
    public ImList<A> drop(int count)
    {
        return count >= size()
               ? ImList.on()
               : on(source, skipCount + count, size - count);
    }

    /**
     * <p> So this is a fast way (I hope) to take elements
     */
    @Override
    public ImList<A> take(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);

        return count == 0
               ? ImList.on()
               : count >= size()
                 ? this
                 : on(source, skipCount, count);
    }

    /**
     * <p> Create a ImReverseList
     */
    public ImList<A> reverse()
    {
        //        // We can't just use toArray here because that does not actually copy anything
        //        A[] target = Arrays.copyOfRange(source, skipCount, skipCount + size());
        //
        //        return ImList.on(reverseArrayInPlace(target));

        return ImReverseList.on(source, skipCount, size);
    }

    /**
     * <p> {@code true}
     *  iff
     * {@code this}
     * equals
     * {@code other}
     *
     * <p> Equality for lists means that both lists have the same size and the
     * {@code i}
     * th element of
     * {@code this}
     *  equals the
     * {@code i}
     * th element of
     * {@code other}
     *
     * This version is optimised for this class and uses {@link Objects#deepEquals(Object, Object)}
     */
    @Override
    public boolean equals(Object other)
    {
        if (skipCount == 0 && other instanceof ImListOnArray)
        {
            ImListOnArray loa = (ImListOnArray) other;
            return loa.skipCount == 0 && loa.source.length == source.length
                   ? Objects.deepEquals(source, loa.source)
                   : super.equals(other);
        }
        else
            return super.equals(other);
    }

    @Override
    public ImList<A> flush()
    {
        return this;
    }

}