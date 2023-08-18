/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import dev.javafp.func.Fn;

/**
 * <p> Thrown when a function used in a flatMap operation returned
 * {@code null}
 * .
 *
 * <p> Thrown (for example) in {@link dev.javafp.util.Chat#flatMap(Fn)}
 */
public class FlatMapFunctionReturnedNull extends ImException
{
    /**
     * A function used in flatMap returned null when applied to:
     * {@code monad}
     */
    public <T> FlatMapFunctionReturnedNull(T monad)
    {
        super("A function used in flatMap returned null when applied to: " + monad);
    }
}