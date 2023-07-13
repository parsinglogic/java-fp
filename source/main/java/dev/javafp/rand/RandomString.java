/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.rand;

import dev.javafp.ex.Throw;

import java.util.Locale;

/**
 * <p> Generate random strings from an alphabet
 * <p> adapted from https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 * 
 */
public class RandomString
{

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final String lower = upper.toLowerCase(Locale.ROOT);

    public static final String digits = "0123456789";

    public static final String alphanum = upper + lower + digits;

    private final char[] symbols;

    public RandomString(String symbols)
    {
        if (symbols.length() < 2)
            throw new IllegalArgumentException();

        this.symbols = symbols.toCharArray();

    }

    /**
     * <p> Create an alphanumeric string generator.
     */
    public RandomString()
    {
        this(alphanum);
    }

    /**
     * <p> Generate a random string.
     */
    public String next(int len)
    {
        Throw.Exception.ifLessThan("len", len, 0);

        char[] buf = new char[len];

        for (int idx = 0; idx < len; ++idx)
            buf[idx] = symbols[Rando.nextIntFromZeroToExclusive(symbols.length)];

        return new String(buf);
    }
}