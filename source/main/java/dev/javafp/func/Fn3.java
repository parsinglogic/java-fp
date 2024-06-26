/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.func;

import java.io.Serializable;

/**
 * <p> A function that takes three input arguments of type
 * {@code A}
 * ,
 * {@code B}
 * and
 * {@code C}
 *  and produces a value of type
 * {@code D}
 *
 */
@FunctionalInterface
public interface Fn3<A, B, C, D> extends Serializable
{
    /**
     * <p> Apply the function to
     * {@code a}
     * ,
     * {@code b}
     * and
     * {@code c}
     *
     */
    D of(A a, B b, C c);

    /**
     * <p> Partially apply the function to its first argument
     */
    default Fn2<B, C, D> ofFirst(A a)
    {
        return (b, c) -> of(a, b, c);
    }

    /**
     * <p> Partially apply the function to its second argument
     */
    default Fn2<A, C, D> ofSecond(B b)
    {
        return (a, c) -> of(a, b, c);
    }
}