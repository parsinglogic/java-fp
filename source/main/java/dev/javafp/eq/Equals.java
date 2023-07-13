/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.eq;

import dev.javafp.ex.Throw;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class Equals
{

    /**
     * <p> true iff
     * {@code one}
     *  "is equal to"
     * {@code two}
     * <p> For all our objects - the ones that extend Values, we have each equals method doing a nested equals anyway.
     * <p> Before, we could not deal with Values objects that contained arrays and so some of them had their own implementation
     * of Values::eq. Now we don't require this method so I have removed it.
     * <p> So Objects.deepEquals does the right thing:
     * <h3>Objects::deepEquals</h3>
     *
     * <pre>{@code
     *  public static boolean deepEquals(Object a, Object b) {
     *      if (a == b)
     *          return true;
     *      else if (a == null || b == null)
     *          return false;
     *      else
     *          return Arrays.deepEquals0(a, b);
     *  }
     * }</pre>
     *
     * <h3>Arrays::deepEquals0</h3>
     *
     * <pre>{@code
     *  static boolean deepEquals0(Object e1, Object e2) {
     *      assert e1 != null;
     *      boolean eq;
     *      if (e1 instanceof Object[] && e2 instanceof Object[])
     *          eq = deepEquals ((Object[]) e1, (Object[]) e2);
     *      else if (e1 instanceof byte[] && e2 instanceof byte[])
     *          eq = equals((byte[]) e1, (byte[]) e2);
     *      else if (e1 instanceof short[] && e2 instanceof short[])
     *          eq = equals((short[]) e1, (short[]) e2);
     *      else if (e1 instanceof int[] && e2 instanceof int[])
     *          eq = equals((int[]) e1, (int[]) e2);
     *      else if (e1 instanceof long[] && e2 instanceof long[])
     *          eq = equals((long[]) e1, (long[]) e2);
     *      else if (e1 instanceof char[] && e2 instanceof char[])
     *          eq = equals((char[]) e1, (char[]) e2);
     *      else if (e1 instanceof float[] && e2 instanceof float[])
     *          eq = equals((float[]) e1, (float[]) e2);
     *      else if (e1 instanceof double[] && e2 instanceof double[])
     *          eq = equals((double[]) e1, (double[]) e2);
     *      else if (e1 instanceof boolean[] && e2 instanceof boolean[])
     *          eq = equals((boolean[]) e1, (boolean[]) e2);
     *      else
     *          eq = e1.equals(e2);
     *      return eq;
     *  }
     * }</pre>
     *
     */
    public static boolean isEqual(Object one, Object two)
    {
        return Objects.deepEquals(one, two);
    }

    /**
     * <p> Convenience function to do pairwise equality tests.
     * <p> true iff
     *
     * <pre>{@code
     * isEqual(objects[0], objects[1]) &&
     * isEqual(objects[2], objects[3]) &&
     * ...
     * isEqual(objects[n-1], objects[n])
     * }</pre>
     *
     */
    public static boolean isEqualPairwise(Object... objects)
    {
        if (objects.length % 2 != 0)
        {
            // Wrapped in an if to prevent Arrays.toString() being invoked all the time
            Throw.Exception.ifTrue(true,
                    "Number of objects to compare pairwise is not even: " + Arrays.toString(objects));
        }

        for (int i = 0; i < objects.length; i += 2)
            if (!isEqual(objects[i], objects[i + 1]))
                return false;

        return true;
    }

    public static boolean isEqualSet(Collection<?> expected, Collection<?> actual)
    {
        return getDifferences(expected, actual).isEmpty();
    }

    public static String getDifferences(Collection<?> expected, Collection<?> actual)
    {

        StringBuilder sb = new StringBuilder();

        for (Object thing : expected)
        {
            if (!actual.contains(thing))
            {
                sb.append("present in expected but not in actual\n" + thing + "\n");
            }
        }

        for (Object thing : actual)
        {
            if (!expected.contains(thing))
                sb.append("present in actual but not in expected\n" + thing + "\n");
        }

        return sb.toString();
    }

}