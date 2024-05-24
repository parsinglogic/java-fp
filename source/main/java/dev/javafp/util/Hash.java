/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.func.Fn;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * <p> A utility to help with hash calculations.
 * <p> We used to calculate the hash when the object was created.
 * <p> Now we are doing it on demand and then caching the value.
 *
 * <p>When we are calculating the hashCode of an array, we are calculating the hashCode of each element and combining them using
 * the same algorithm as java.util.List
 *
 * <p> In particular,
 * {@code null}
 * s have a hashCode of
 * {@code 0}
 * .
 *
 */
public class Hash
{

    public static final int multiplier = 31;
    public static final int sizeMultiplier = 997;
    public static final int sampleSize = 10;

    public static int hash(Object... xs)
    {
        int result = 1;

        for (Object element : xs)
            result = multiplier * result + hashCodeOf(element);

        return result;
    }

    public static int hashCodeOfIterable(Iterable<?> xs)
    {
        int result = 1;

        for (Object element : xs)
            result = multiplier * result + hashCodeOf(element);

        return result;
    }

    public static int hashCodeOfIterableWithFirstElements(int count, Iterable<?> xs)
    {
        int result = 1;
        int i = 0;

        Iterator<?> it = xs.iterator();

        while (it.hasNext())
        {
            if (i >= count)
                return result;

            i++;

            result = multiplier * result + hashCodeOf(it.next());
        }

        return result;
    }

    public static <T> int hashCodeOfIterableUsing(Iterable<T> xs, Fn<T, Integer> hashFn)
    {
        int result = 1;

        for (T element : xs)
            result = multiplier * result + hashFn.of(element);

        return result;
    }

    /**
     * <p> The hash code of
     * {@code element}
     *  - which is typed here as
     * {@code Object}
     * .
     * <p> If
     * {@code element}
     *  turns out to be a primitive array then, instead of just returning the Java default hash code (which is taken from the memory
     * address of the object), we use the appropriate overloaded version of
     *  {@code Arrays.hashCode()}
     *
     * <p> This means we use:
     * {@link Arrays#hashCode(boolean[])}
     *  for arrays of booleans.
     *
     * <p> These methods look inside the array and "do the right thing"
     * <p> In fact, we consider the size of the primitive array as well.
     *
     */
    public static int hashCodeOf(Object element)
    {
        if (element == null)
            return 0;
        else
        {
            Class<?> aClass = element.getClass();

            return aClass.isArray()
                   ? primitiveArrayHashCode(element, aClass.getComponentType())
                   : element.hashCode();
        }
    }

    private static int primitiveArrayHashCode(Object a, Class<?> componentType)
    {
        return
                (componentType == byte.class) ? addLengthToHash(Arrays.hashCode((byte[]) a), ((byte[]) a).length) :
                (componentType == int.class) ? addLengthToHash(Arrays.hashCode((int[]) a), ((int[]) a).length) :
                (componentType == long.class) ? addLengthToHash(Arrays.hashCode((long[]) a), ((long[]) a).length) :
                (componentType == char.class) ? addLengthToHash(Arrays.hashCode((char[]) a), ((char[]) a).length) :
                (componentType == short.class) ? addLengthToHash(Arrays.hashCode((short[]) a), ((short[]) a).length) :
                (componentType == boolean.class) ? addLengthToHash(Arrays.hashCode((boolean[]) a), ((boolean[]) a).length) :
                (componentType == double.class) ? addLengthToHash(Arrays.hashCode((double[]) a), ((double[]) a).length) :
                addLengthToHash(Arrays.hashCode((Object[]) a), ((Object[]) a).length);
    }

    private static int addLengthToHash(int hashCode, int length)
    {
        return sizeMultiplier * length + hashCode;
    }
}