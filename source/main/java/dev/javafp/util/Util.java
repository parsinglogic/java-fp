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

    /**
     * <p> Map
     * {@code f}
     *  over
     * {@code ds}
     *  to get a list of
     * {@code Double}
     *  and then calculate the sum of those
     *
     */
    public static <A> double sum(ImList<A> ds, Fn<A, Double> f)
    {
        return ds.foldl(0.0, (z, a) -> z + f.of(a));
    }

    /**
     * <p> The (
     * {@code double}
     * ) sum of
     *
     * {@code ds}
     *
     */
    public static double sum(ImList<Double> ds)
    {
        return ds.foldl(0.0, (z, i) -> z + i);
    }

    /**
     * <p> The (
     * {@code long}
     * ) sum of
     *
     * {@code ds}
     *
     */
    public static long sumLong(ImList<Long> ds)
    {
        return ds.foldl(0L, (z, i) -> z + i);
    }

    /**
     * <p> The (
     * {@code int}
     * ) sum of
     *
     * {@code ds}
     *
     */
    public static int sumInt(ImList<Integer> ds)
    {
        return ds.foldl(0, (z, i) -> z + i);
    }

    /**
     * <p> The (
     * {@code double}
     * ) largest of
     *
     * {@code ds}
     *
     */
    public static double max(ImList<Double> ds)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return ds.tail().foldl(ds.head(), (z, i) -> Math.max(z, i));
    }

    /**
     * `min(ds.map(f))`
     *
     */
    public static <A> double min(ImList<A> ds, Fn<A, Double> f)
    {
        Throw.Exception.ifTrue(ds.isEmpty(), "list cannot be empty");
        return min(ds.map(f));
    }

    /**
     * <p> The (
     * {@code double}
     * ) smallest of
     *
     * {@code ds}
     *
     */
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

    public static <A, B> A maxElement(ImList<A> is, Fn<A, Comparable> f)
    {
        Throw.Exception.ifTrue(is.isEmpty(), "list cannot be empty");
        return is.tail().foldl(is.head(), (z, i) -> f.of(i).compareTo(f.of(z)) > 0 ? i : z);
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

}