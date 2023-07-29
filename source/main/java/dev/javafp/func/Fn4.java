/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.func;

import java.io.Serializable;

/**
 * <p> A function that takes four input arguments of type
 * {@code A}
 * ,
 * {@code B}
 * ,
 * {@code C}
 * and
 * {@code D}
 *  and produces a value of type
 * {@code E}
 *
 */
@FunctionalInterface
public interface Fn4<A, B, C, D, E> extends Serializable
{
    /**
     * <p> Apply the function to
     * {@code a}
     * ,
     * {@code b}
     * ,
     * {@code c}
     * and
     * {@code d}
     *
     */
    E of(A a, B b, C c, D d);

    /**
     * <p> Partially apply the function to its first argument
     */
    default Fn3<B, C, D, E> ofFirst(A a)
    {
        return (b, c, d) -> of(a, b, c, d);
    }
}