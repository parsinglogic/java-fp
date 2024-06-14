/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.lst.ImList;

import java.math.BigInteger;

public class Sums
{

    /**
     * <p> The value of
     * {@code n!}
     *  as an
     * {@code int}
     *  - strictly
     * {@code Math.min(n!, Integer.MAX_VALUE)}
     *
     */
    public static int factorial(int n)
    {
        return (n < factorials.length)
               ? factorials[n]
               : Integer.MAX_VALUE;
    }

    private static final int[] factorials = { 1, 1, //
            1 * 2, //
            1 * 2 * 3, //
            1 * 2 * 3 * 4, // 
            1 * 2 * 3 * 4 * 5, //
            1 * 2 * 3 * 4 * 5 * 6, //
            1 * 2 * 3 * 4 * 5 * 6 * 7, //
            1 * 2 * 3 * 4 * 5 * 6 * 7 * 8, //
            1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9, //
            1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10, //
            1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11, //
            1 * 2 * 3 * 4 * 5 * 6 * 7 * 8 * 9 * 10 * 11 * 12 };

    /**
     *
     * <pre>{@code
     * Return -1 if l < 0
     *         0 if l == 0
     *         1 if l >0
     * }</pre>
     *
     */
    public static int sign(long l)
    {
        return l < 0
               ? -1
               : l == 0
                 ? 0
                 : 1;
    }

    /**
     *
     * <pre>{@code
     * Return -1 if l < 0
     *         0 if l == 0
     *         1 if l >0
     * }</pre>
     *
     */
    public static int sign(double l)
    {
        return l < 0
               ? -1
               : l == 0
                 ? 0
                 : 1;
    }

    public static ImList<BigInteger> convertToDigitsUsingRadix(BigInteger radix, BigInteger number)
    {
        ImList<BigInteger> digits = ImList.on();
        BigInteger t = number;

        while (true)
        {
            if (t.equals(BigInteger.ZERO))
                return digits.isEmpty() ? ImList.on(BigInteger.ZERO) : digits;

            BigInteger d = t.mod(radix);

            digits = digits.push(d);

            t = t.subtract(d).divide(radix);
        }

    }

}