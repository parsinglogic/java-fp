/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import java.util.Collection;

/**
 * <p> Thrown when a value that is expected to be a (1-based) collection index is too small or too large. 
 * {@code null}
 * .
 *
 * <p> Thrown (for example) in {@link Throw.Exception#ifIndexNotInCollection(int, Collection, String)}
 */
@SuppressWarnings("serial")
public class InvalidCollectionIndex extends RuntimeException
{

    private String message;

    public InvalidCollectionIndex(int index, int size, String collectionName)
    {

        if (index < 0)
        {
            message = "You tried to access index " + index + " of " + collectionName + " but index should be >= 0";
            return;

        }

        if (size == 0)
        {
            message = "You tried to access index " + index + " of " + collectionName + " but there are none";
            return;

        }

        if (index >= size)
        {
            message = "You tried to access index " + index + " of " + collectionName + " but the largest valid index is " + size;
            return;

        }

    }

    public String getMessage()
    {
        return message;
    }
}