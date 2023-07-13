/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.box.LeafTextBox;
import dev.javafp.ex.IllegalState;
import dev.javafp.ex.SizeOnInfiniteList;
import dev.javafp.ex.ThreadInterrupted;
import dev.javafp.util.Say;

import java.util.Iterator;

/**
 *
 * <p>A lazy list can, unlike eager lists, be infinite.
 *
 * <p> For example, an `ImUnfoldList` starts with a value and the next value is determined by the step function. This list never ends.
 *
 *
 *
 * <p> Generally we use infinite lists to easily determine a pattern for what each element should be and then we take a finite subset of the
 * list to actually work with.
 *
 * <p> But what happens if you try to display an infinite list - or try to iterate over one?
 *
 * <p> In an earlier version of this library your program would just loop - normally it would end after memory was exhausted.
 *
 * <p> Now, you could argue that any program can loop forever, even if it is not trying to process and infinite list. This is true.
 * It's not the end of the world - although it is the end of your running program.
 *
 * However, it would be useful if we could do something to help when this happens.
 *
 * toString
 * iterator
 */
public abstract class ImLazyList<A> implements ImList<A>
{

    /**
     * <p> The values for these a are designed to make it easy to calculate the Sz when we are appending lists
     * see Sx::append and Sx::join
     *
     */
    public static final int UNKNOWN_FINITE = -1;
    public static final int UNKNOWN_UNKNOWN = -2;
    public static final int KNOWN_INFINITE = -3;
    public static final int UU_BOX_LIMIT = 10_000;

    public static LeafTextBox UU_MESSAGE = LeafTextBox.with(String.format(" (showing the first %d elements - list could be larger or infinite)", UU_BOX_LIMIT));

    public final int sz;
    public static final int START_SHOW = 20_000_000;
    public static final int SHOW_INTERVAL = 10_000_000;

    public ImLazyList(int size)
    {
        this.sz = size;
    }

    @Override
    public int getSz()
    {
        return sz;
    }

    @Override
    public int size()
    {
        if (sz > 0)
        {
            return sz;
        }
        else if (sz == KNOWN_INFINITE)
        {
            throw new SizeOnInfiniteList();
        }
        else if (sz == UNKNOWN_FINITE)
        {
            // Will never be _|_
            return calculateSize();
        }
        else
        {
            // Could be _|_
            return calculateSize();
        }
    }

    protected int calculateSize()
    {
        throw new IllegalState("This method should not have been called");
    }

    @Override
    public String toString()
    {
        return toS();
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof ImList
               ? equalsList((ImList<A>) other)
               : false;
    }

    /**
     * <p> Calculate the size of this list - in constant memory
     *
     */
    @Override
    public int resolveSize()
    {
        if (getSz() == KNOWN_INFINITE)
            throw new SizeOnInfiniteList();

        ImList<A> l = this;
        int count = 0;

        while (!l.isEmpty())
        {
            l = l.tail();
            count++;

            if (count > START_SHOW && count % SHOW_INTERVAL == 0)
            {
                Say.printf("LazyList might be infinite - size now is %d", count);
                if (Thread.currentThread().isInterrupted())
                    throw new ThreadInterrupted();
            }
        }

        return count;
    }

    @Override
    public Iterator<A> iterator()
    {
        if (getSz() == KNOWN_INFINITE)
            throw new SizeOnInfiniteList();

        return ImList.super.iterator();
    }

    /**
     * <p> When serialising, we turn every ImList into a LstOnArray
     */
    protected Object writeReplace()
    {
        return ImList.on(toArray(Object.class));
    }

    /**
     * <p> Get the hash of the first few elements
     */
    public int hashCode()
    {
        return hashCode(10);
    }

}