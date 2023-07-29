/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.func;

import java.io.Serializable;

/**
 * <p> A function that takes two input arguments of type
 * {@code A}
 * and
 * {@code B}
 * and produces a value of type
 * {@code C}
 *
 */
@FunctionalInterface
public interface Fn2<A, B, C> extends Serializable
{
    /**
     * <p> Apply the function to
     * {@code a}
     * and
     * {@code b}
     *
     */
    C of(A a, B b);

    /**
     * <p> Partially apply the function to its first argument
     */
    default Fn<B, C> ofFirst(A a)
    {
        return b -> of(a, b);
    }

    /**
     * <p> Partially apply the function to its second argument
     */
    default Fn<A, C> ofSecond(B b)
    {
        return a -> of(a, b);
    }

    /**
     * <p> The function that is the same as
     * {@code this}
     * except that the order of the arguments is flipped
     */
    default Fn2<B, A, C> flip()
    {
        return (b, a) -> of(a, b);
    }

}