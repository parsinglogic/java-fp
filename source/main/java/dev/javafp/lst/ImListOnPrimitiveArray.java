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

/**
 * <p> Sometimes we have a primitive array in our hands and we need to diddle with it so that is why we have this class.
 * <p> It creates a ImList - but the component type is the Object version of the primitive.
 * <p> It is mainly lazy
 * <p> The reason for needing this class is essentially the fact that java is so ugly when it comes to handling arrays and arrays
 * of primitives and other collections - eher we have to wrap the primitives in objects.
 * <p> A list whose source is a
 * <em>primitive</em>
 *  array
 * <p> The resulting ImList has the Object version of the primitive as its component type
 * <p> int     -> Integer
 * long    -> Long
 * char    -> Character
 * boolean -> Boolean
 * <p> etc
 * <p> The ImListOnArray class takes its source to be an array - but an array of
 * <em>Objects</em>
 *  rather
 * than
 * <em>primitives</em>
 * <p> So this is ok:
 *
 * <pre>{@code
 *     Integer[] ints = { 1, 2, 3 };
 *     ImList<Integer> list = on(ints);
 * }</pre>
 * <p> but this does not compile of course:
 *
 * <pre>{@code
 *     int[] ints = { 1, 2, 3 };
 *     ImList<int> list = on(ints);
 * }</pre>
 * <p> I needed to display an array of bytes - but it was so painful
 * <p> Unfortunately I can't make this class typesafe. This code will fail at runtime with a class cast exception
 *
 * <pre>{@code
 *     int[] ints = {2};
 *     ImList<Boolean> is = ImListOnPrimitiveArray.on(ints, Integer.TYPE);
 *     Boolean b = is.head();
 * }</pre>
 *
 */

class ImListOnPrimitiveArray<A> extends ImEagerList<A> implements Eq
{
    private final Object source;
    private final int skipCount;

    private ImListOnPrimitiveArray(Object source, int skipCount, int size)
    {
        super(size);
        this.source = source;
        this.skipCount = skipCount;
    }

    private static <A> ImList<A> on(Object source, int skipCount, int size)
    {
        return size == 0
               ? ImList.empty()
               : new ImListOnPrimitiveArray(source, skipCount, size);
    }

    /**
     * <p> Create a ImList from primitive array
     * {@code source}
     *  assuming that its members are of class
     * {@code clazz}
     *
     */
    static <A> ImList<A> on(Object source, Class<?> clazz)
    {
        if (!source.getClass().getComponentType().equals(clazz))
        {
            throw new ClassCastException("You specified component type " + clazz + " but source has component type " + source.getClass().getComponentType());
        }

        int len = Array.getLength(source);

        return len == 0
               ? ImList.empty()
               : new ImListOnPrimitiveArray(source, 0, len);
    }

    /**
     * <p> Create a ImList from primitive array
     * {@code source}
     *  assuming that its members are of class
     * {@code clazz}
     *
     */
    static <A> ImList<A> on(Object source)
    {
        if (!source.getClass().isArray())
        {
            throw new ClassCastException("source has " + source.getClass() + " but it should be an array");
        }

        if (!source.getClass().getComponentType().isPrimitive())
        {
            throw new ClassCastException("Source has component type " + source.getClass().getComponentType() + " but it should be primitive");
        }

        int len = Array.getLength(source);

        return len == 0
               ? ImList.empty()
               : new ImListOnPrimitiveArray(source, 0, len);
    }

    @Override
    public A head()
    {
        return (A) Array.get(source, skipCount);
    }

    @Override
    public ImList<A> tail()
    {
        return on(source, skipCount + 1, size - 1);
    }

    public A[] toArray()
    {
        return copyToObjectArray(size);
    }

    private A[] copyToObjectArray(int newSize)
    {
        Object[] os = new Object[newSize];
        for (int i = 0; i < size; i++)
        {
            os[i] = Array.get(source, i + skipCount);
        }

        return (A[]) os;
    }

    /**
     * <p> We can optimise if we are a primitive list
     */
    @Override
    public A at(int indexStartingAtOne)
    {
        Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());
        return (A) Array.get(source, skipCount + indexStartingAtOne - 1);
    }

    /**
     * <p> So this is a fast way (I hope) to drop elements
     */
    @Override
    public ImList<A> drop(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);
        return count == 0
               ? this
               : count >= size()
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
     * <p> Create a copy of the array and then reverse it in place
     * and then create a ImList on that array
     *
     */
    public ImList<A> reverse()
    {
        return ImReverseList.on(copyToObjectArray(size));
    }

    @Override
    public ImList<A> flush()
    {
        return this;
    }

}