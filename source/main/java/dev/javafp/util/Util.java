/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.lst.ImList;

/**
 * <p> Utilities for various things..
 */

public class Util
{
    /**
     * <p> Given a starting value
     * {@code start}
     * , apply
     * {@code step}
     *  to get n, until
     * {@code pred}
     *  applied to n is
     * {@code true}
     * <p> until (>1000) (*2) 1  => 1024
     *
     */
    public static <A> A until(Fn<A, Boolean> pred, Fn<A, A> step, A start)
    {
        return pred.of(start)
               ? start
               : until(pred, step, step.of(start));
    }

    /**
     * <p> print a double as compactly as possible with "px" on the end
     */
    public static String toPx(double d)
    {
        return String.format("%.1fpx", d);
    }

    public static double sum(ImList<Double> ds)
    {
        return ds.foldl(0.0, (z, i) -> z + i);
    }

    public static <A> double sum(ImList<A> ds, Fn<A, Double> f)
    {
        return ds.foldl(0.0, (z, a) -> z + f.of(a));
    }

    public static long sumLong(ImList<Long> ds)
    {
        return ds.foldl(0L, (z, i) -> z + i);
    }

    public static int sumInt(ImList<Integer> ds)
    {
        return ds.foldl(0, (z, i) -> z + i);
    }

    public static double max(ImList<Double> ds)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return ds.tail().foldl(ds.head(), (z, i) -> Math.max(z, i));
    }

    public static <A> double min(ImList<A> ds, Fn<A, Double> f)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return min(ds.map(f));
    }

    public static double min(ImList<Double> ds)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return ds.tail().foldl(ds.head(), (z, i) -> Math.min(z, i));
    }

    public static <A> double max(ImList<A> ds, Fn<A, Double> f)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return max(ds.map(f));
    }

    public static int maxInt(ImList<Integer> is)
    {
        Throw.Exception.ifTrue(is.isEmpty(), "list cannot be empty");
        return is.tail().foldl(is.head(), (z, i) -> Math.max(z, i));
    }

    public static int minInt(ImList<Integer> is)
    {
        Throw.Exception.ifTrue(is.isEmpty(), "list cannot be empty");
        return is.tail().foldl(is.head(), (z, i) -> Math.min(z, i));
    }

    /**
     * <p> Start with an accumulator
     * {@code z}
     *  and iterate over
     * {@code iterable}
     * , applying
     * {@code f}
     *  to the
     * {@code z}
     *  and
     * {@code e}
     *  to get a new
     * {@code z}
     * <p> One way to visualise this is to imagine that the function that we are using is the function that adds two numbers - ie
     * the infix
     * {@code +}
     *  operator
     * <p> Then
     *
     * <pre>{@code
     * foldl (+) z [e1, e2, ... en] == [ (...((z + e1) + e2) + ... ) + en ]
     * }</pre>
     * <p> Note that the accumulator,
     * {@code z}
     *  is the first argument to the function.
     * <p> If we extend this to imagine that the function is called * and can be applied using infix notation - like
     * {@code +}
     *  then
     *
     * <pre>{@code
     * foldl (*) z [e1, e2, ... en] == [ (...((z * e1) * e2) * ... ) * en ]
     * }</pre>
     * <p> Note that we are <em>not</em> assuming that
     * {@code *}
     *  is commutative
     *
     */
    public static <A, B> B foldl(Iterable<A> iterable, B z, Fn2<B, A, B> f)
    {
        for (A i : iterable)
            z = f.of(z, i);

        return z;
    }

    //    public ImList<Character> getBadChars()
    //    {
    //        /**
    //         * <p> I would like to test with a larger set of 'bad' characters, but this causes problems
    //         * <p> I tried generating all the characters between 0 and 0xFFFF that are invisible.
    //         * <p> This included the surrogate characters when I first tried it. This turned out to be a bad idea
    //         * since when you try to encode a surrogate to UTF-8 it silently refuses to do it and gives you a
    //         * question mark (0x3F) instead. This confused me for a few hours.
    //         * <p> So we have to be careful when dealing with tests for 'bad' Unicode characters
    //         * <p> We want to allow all reasonable Unicode characters - but we want to remove the ones that will cause the
    //         * CSV parsing to fail in a strange way - before we give it to the CSV parser
    //         * <p> For unicode characters that are invisible, I think we should map them to a space
    //         * <p> Testing the unicode characters that are surrogates is hard because, using Java, it is very difficult to
    //         * write them into a file. They just get translated silently as ? (3F) characters - sigh -
    //         * <p> from https://en.wikipedia.org/wiki/Plane_(Unicode)#Basic_Multilingual_Plane
    //         * <p> Surrogates:
    //         * High Surrogates (D800–DBFF)
    //         * Low Surrogates (DC00–DFFF)
    //         * Private Use Area (E000–F8FF)
    //         * <p> We are using this Guava function
    //         * public static CharMatcher invisible()
    //         * <p> Determines whether a character is invisible; that is, if its Unicode category is any of
    //         * SPACE_SEPARATOR, LINE_SEPARATOR, PARAGRAPH_SEPARATOR, CONTROL, FORMAT, SURROGATE, and PRIVATE_USE according to ICU4J.
    //         * <p> Let's get the invisible chars that are not surrogates or private use
    //         * <p> I can't even print some of these without strange effects!
    //         * <p> Eg
    //         * 8234 202A <<‪>>, 8235 202B <<‫>>, 8236 202C <<‬>>, 8237 202D <<‭>>,
    //         * <p> I think one or more of the characters has kicked the display into printing right to left
    //         * <p> Let's restrict our tests to 0 - 0x200C to prevent strangeness
    //         *
    //         */
    //        ImList<Integer> bad = ImRange.inclusive(0x0, 0x200C).filter(c -> CsvRow.isBad((char) c.intValue()));
    //        //        ImList<Integer> bad2 = ImList.on(); //ImRange.inclusive(0xF8FF + 1, 0xFFFF).filter(c -> CsvRow.isBad((char) c.intValue()));
    //
    //        //        System.out.println("bad.size() " + bad.size());
    //        //        System.out.println(bad);
    //        //
    //        //        System.out.println("bad is " + bad.map(i -> String.format("%d %X <<%c>>", i, i, (char) i.intValue())));
    //
    //        return bad.map(i -> Character.valueOf((char) i.intValue()));
    //    }

}