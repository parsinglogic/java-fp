/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.func;

/**
 * <p> ComposedFunction implements function
 * <strong>composition</strong>
 *
 */
public class ComposedFunction<I, O, U> implements Fn<I, U>
{

    // The function to apply first
    private Fn<I, O> first;

    // The function to apply second
    private Fn<O, U> second;

    /**
     * <p> The composition of
     * {@code first}
     *  and
     * {@code second}
     * <p> first.then(second).of(in) => second.of(first.of(in))
     *
     */
    public ComposedFunction(Fn<I, O> first, Fn<O, U> second)
    {
        this.first = first;
        this.second = second;
    }

    @Override
    public U of(I in)
    {
        return second.of(first.of(in));
    }

}