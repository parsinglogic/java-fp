/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.ThreadInterrupted;
import dev.javafp.util.Say;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over ImLists
 *
 */
public class ImListIterator<T> implements Iterator<T>, Serializable
{

    private int count = 0;
    public ImList<T> list;

    public ImListIterator(ImList<T> list)
    {
        this.list = list;
    }

    @Override
    public boolean hasNext()
    {
        return !list.isEmpty();
    }

    @Override
    public T next()
    {
        if (hasNext())
        {
            try
            {
                count++;

                if (count > ImLazyList.START_SHOW && count % ImLazyList.SHOW_INTERVAL == 0)
                {
                    Say.printf("List iterator might be infinite - count now is %d", count);
                    if (Thread.currentThread().isInterrupted())
                        throw new ThreadInterrupted();
                }

                return list.head();

            } finally
            {
                list = list.tail();
            }
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    /**
     * Throws UnsupportedOperationException
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}