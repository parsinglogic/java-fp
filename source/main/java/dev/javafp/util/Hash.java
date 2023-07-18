/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import java.util.Arrays;

/**
 * <p> We used to calculate the hash when the object was created.
 * <p> Now we are doing it on demand and then caching the value.
 *
 */
public class Hash
{
    //    public final static int listHashCode = 10;
    //    public final static int arrayHashCode = 11;

    public static int hash(Object... values)
    {
        return hashCodeOfArray(values);
    }

    public static int hashCodeOfArray(Object a[])
    {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a)
            result = 31 * result + hashCodeOf(element);

        return result;
    }

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
                (componentType == byte.class) ? Arrays.hashCode((byte[]) a) :
                (componentType == int.class) ? Arrays.hashCode((int[]) a) :
                (componentType == long.class) ? Arrays.hashCode((long[]) a) :
                (componentType == char.class) ? Arrays.hashCode((char[]) a) :
                (componentType == short.class) ? Arrays.hashCode((short[]) a) :
                (componentType == boolean.class) ? Arrays.hashCode((boolean[]) a) :
                (componentType == double.class) ? Arrays.hashCode((double[]) a) :
                hashCodeOfArray((Object[]) a);

    }

}