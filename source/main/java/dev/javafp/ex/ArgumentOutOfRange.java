/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

/**
 * <p> Thrown when an int argument to a function is not within a range.
 *
 * <p> Thrown (for example) in {@link dev.javafp.lst.ImList#at(int)}
 */
@SuppressWarnings("serial")
public class ArgumentOutOfRange extends ImException
{

    /**
     * The argument with name
     * {@code name}
     * and value
     * {@code value}
     * having
     * {@code value < min}
     * or
     * {@code value > max}
     */
    public ArgumentOutOfRange(String name, int value, int min, int max)
    {
        super("Argument " + name + " with value " + value + " is out of range. It should be in [" + min + "," + max + "]");
    }
    
}