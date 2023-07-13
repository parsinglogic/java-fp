/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> An iterator on an {@link ImRoseTreeZipper}.
 * <p> Because
 * {@code ImRoseTreeZippers}
 *  are immutable, {@link #remove} will throw {@link UnsupportedOperationException}.
 * @see ImRoseTree
 * @see ImRoseTreeZipper
 * @see ImRoseTreeIterator
 *
 */
public class ImRoseTreeZipperIterator<T> implements Iterator<ImRoseTreeZipper<T>>
{

    private ImRoseTreeZipper<T> zipper;
    private boolean offLeft = true;

    public ImRoseTreeZipperIterator(final ImRoseTreeZipper<T> zipper)
    {
        this.zipper = zipper;
    }

    public boolean hasNext()
    {
        return offLeft
               ? true
               : zipper.hasNext();
    }

    public ImRoseTreeZipper<T> next()
    {
        if (offLeft)
        {
            offLeft = false;
            return zipper;
        }

        if (!hasNext())
            throw new NoSuchElementException();

        zipper = zipper.next();
        return zipper;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

}