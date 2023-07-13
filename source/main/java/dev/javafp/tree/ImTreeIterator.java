/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tree;

import dev.javafp.util.ImMaybe;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> An iterator on an {@link ImTree}.
 * <p> Because
 * {@code ImTrees}
 *  are immutable, {@link #remove} will throw {@link UnsupportedOperationException}
 *
 */
public class ImTreeIterator<T> implements Iterator<T>
{
    private ImMaybe<ImTreeZipper<T>> maybeNextPath;

    public ImTreeIterator(final ImTree<T> tree)
    {
        if (tree == ImTree.Nil())
            maybeNextPath = ImMaybe.nothing();
        else
            maybeNextPath = ImMaybe.just(ImTreeZipper.onLeftmost(tree));
    }

    public boolean hasNext()
    {
        return maybeNextPath.isPresent();
    }

    public T next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        ImTreeZipper<T> zipper = maybeNextPath.get();

        try
        {
            return zipper.getFocus().getElement();
        } finally
        {
            maybeNextPath = zipper.next();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}