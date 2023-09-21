/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.rand;

import dev.javafp.ex.Throw;

import java.security.SecureRandom;
import java.util.NoSuchElementException;

/**
 * <p> Convenience wrapper for SecureRandom
 */
public class Rando
{
    public static SecureRandom random = new SecureRandom();

    /**
     * The next element in the iterator. Throws {@link NoSuchElementException} if no such element exists.
     */
    public static int nextIntFromZeroToExclusive(int maxExclusive)
    {
        Throw.Exception.ifLessThanOrEqualTo("maxExclusve", maxExclusive, 0);
        return random.nextInt(maxExclusive);
    }

    /**
     * <p> A random int
     * {@code i}
     *  where
     * {@code minInclusive <= i < maxExclusive}
     *
     */
    public static int nextInt(int minInclusive, int maxExclusive)
    {
        Throw.Exception.ifLessThanOrEqualTo("maxExclusve", maxExclusive, minInclusive);
        return random.nextInt(maxExclusive - minInclusive) + minInclusive;
    }

    /**
     * <p> A random int
     * {@code i}
     *  where
     * {@code minInclusive <= i < maxExclusive}
     *
     */
    public static int nextIntInclusive(int minInclusive, int maxInclusive)
    {
        Throw.Exception.ifLessThan("maxInclusive", maxInclusive, minInclusive);
        return nextInt(minInclusive, maxInclusive + 1);
    }

    /**
     * <p> A random double
     * {@code d}
     *  where
     * {@code min <= i < max}
     *
     */
    public static double nextDouble(double min, double max)
    {
        return random.nextDouble() * (max - min) + min;
    }

    /**
     * <p> An array of bytes containing
     * {@code count}
     * random bytes
     *
     */
    public static byte[] nextBytes(int count)
    {
        byte[] bs = new byte[count];
        random.nextBytes(bs);
        return bs;
    }
}