/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

/**
 * <p> Thrown when you attempt to access an indexed collection
 * using an index (0-based or 1-based)  that is not valid for that collection.
 *
 */
@SuppressWarnings("serial")
public class ImIndexOutOfBounds extends ImException
{
    private ImIndexOutOfBounds(String message)
    {
        super(message);
    }

    /**
     * Check that a collection with size
     * {@code size}
     * can be accessed with a 1-based index with name
     * {@code name}
     * and value
     * {@code index}
     * and throw an
     * {@code ImIndexOutOfBounds}
     * with an explanatory message if the index is out of bounds
     */
    public static void check(int index, int size, String name)
    {
        if (index < 1)
        {
            throw new ImIndexOutOfBounds(name + " should be >= 1  but was " + index);
        }

        if (size == 0)
        {
            throw new ImIndexOutOfBounds("The collection is empty but " + name + " was " + index);
        }

        if (index > size)
        {
            throw new ImIndexOutOfBounds("the size of the collection is " + size + " but " + name
                    + " was " + index);
        }
    }

    /**
     * Check that a collection with size
     * {@code size}
     * can be accessed with a 0-based index with name
     * {@code name}
     * and value
     * {@code index}
     * and throw an
     * {@code ImIndexOutOfBounds}
     * with an explanatory message if the index is out of bounds
     */
    public static void check0(int index, int size, String argumentName)
    {
        if (index < 0)
        {
            throw new ImIndexOutOfBounds(argumentName + " should be >= 0  but was " + index);
        }

        if (size == 0)
        {
            throw new ImIndexOutOfBounds("The collection is empty but " + argumentName + " was " + index);
        }

        if (index >= size)
        {
            throw new ImIndexOutOfBounds("the size of the collection is " + size + " but " + argumentName
                    + " was " + index);
        }
    }

    public static void check(int index, int min, int max, String argumentName)
    {
        if ((index < min) || (index > max))
        {
            throw new ImIndexOutOfBounds(argumentName + " should be in the range [" + min + ", " + max
                    + "]  but was " + index);
        }
    }
}