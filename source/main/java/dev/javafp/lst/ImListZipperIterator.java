/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> An iterator on an {@link ImListZipper}.
 */
public class ImListZipperIterator<T> implements Iterator<ImListZipper<T>>
{

    private ImListZipper<T> zipper;

    ImListZipperIterator(ImListZipper<T> zipper)
    {
        this.zipper = zipper;
    }

    /**
     * <p> {@code true}
     *  if the iterator has more elements. (In other words, returns
     * {@code true}
     *  if
     * {@code next()}
     *  would return an element rather than throwing an exception.)
     *
     */
    public boolean hasNext()
    {
        return zipper.hasNext();
    }

    /**
     * The next element in the iterator. Throws {@link NoSuchElementException} if no such element exists.
     */
    public ImListZipper<T> next()
    {
        zipper = zipper.next();
        return zipper;
    }

    /**
     * <p> Throws
     * {@code UnsupportedOperationException}
     * . You can't modify
     * {@code ImListZippers}
     *  in this way.
     *
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}