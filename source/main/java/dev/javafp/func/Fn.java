/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.func;

import java.io.Serializable;

/**
 * <p> A function that takes an input argument of type
 * {@code I}
 *  and produces a value of type
 * {@code O}
 *
 */

@FunctionalInterface
public interface Fn<I, O> extends Serializable
{

    /**
     * <p> Apply the function to
     * {@code in}
     *
     */
    public O of(I in);

    /**
     * <p> The composition of
     * {@code this}
     *  and
     * {@code other}
     * <p> f1.then(f2).of(in) => f2.of(f1.of(in))
     *
     */
    default <U> Fn<I, U> then(Fn<O, U> other)
    {
        return new ComposedFunction<I, O, U>(this, other);
    }

    /**
     * <p> The negation of the function
     * {@code f}
     *
     * <p> The implementation is
     * <pre>{@code
     * i -> !f.of(i);
     * }</pre>
     *
     */
    static <IN> Fn<IN, Boolean> not(Fn<IN, Boolean> f)
    {
        return i -> !f.of(i);
    }

    /**
     * The identity function
     */
    static <IN> Fn<IN, IN> id()
    {
        return i -> i;
    }

}