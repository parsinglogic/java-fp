/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.box.LeafTextBox;
import dev.javafp.ex.InvalidState;
import dev.javafp.ex.SizeOnInfiniteList;
import dev.javafp.ex.ThreadInterrupted;
import dev.javafp.ex.Throw;
import dev.javafp.util.Say;

import java.util.Iterator;

/**
 *
 * <p>A lazy list. This can, unlike eager lists, be infinite.
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
abstract class ImLazyList<A> extends ImAbstractList<A>
{

    /**
     * <p> The values for these a are designed to make it easy to calculate the Sz when we are appending lists
     * see Sx::append and Sx::join
     *
     */

    /**
     * The list size is not known - but it must be finite
     */
    public static final int UNKNOWN_FINITE = -1;

    /**
     * The list size is not known - it might be finite or infinite
     */
    public static final int UNKNOWN_UNKNOWN = -2;

    /**
     * The list size is known to be infinite
     */
    public static final int KNOWN_INFINITE = -3;

    /**
     * The list size is not known - but it must be finite
     */
    static final int UU_BOX_LIMIT = 10_000;

    /**
     * The list size is not known - but it must be finite
     */
    static LeafTextBox UU_MESSAGE = LeafTextBox.with(String.format(" (showing the first %d elements - list could be larger or infinite)", UU_BOX_LIMIT));

    /**
     * The size of the list
     */
    private final int sz;

    /**
     * The size of a list before we start warning the user it might be infinite
     */
    static final int START_SHOW = 20_000_000;

    /**
     * The interval between warning the user the list might be infinite
     */
    static final int SHOW_INTERVAL = 10_000_000;

    ImLazyList(int size)
    {
        this.sz = size;
    }

    /**
     * The internal size of the list
     */

    int getSz()
    {
        return sz;
    }

    @Override
    /**
     * <p> The size of the list. This is our best guess at the size. Throws
     * {@code SizeOnInfiniteList}
     * if the size is infinite.
     *
     */
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

    /**
     * This method should have implementations lower down in the class hierarchy
     */
    protected int calculateSize()
    {
        throw new InvalidState("This method should not have been called");
    }

    /**
     * <p> Calculate the size of this list - in constant memory
     *
     * <p> If the list is "large" - ie greater than
     * {@code START_SHOW}
     *  then this function starts to output warning messages to the standard
     * output. It also uses the value
     * {@code SHOW_INTERVAL}
     * <p> At the time of writing,
     * {@code START_SHOW}
     *  is 20 million and
     * {@code SHOW_INTERVAL}
     *  is 10 million.
     * <p> If the list is not infinite and is larger than
     * {@code START_SHOW}
     * , then it will eventually complete (unless any other limiting
     * factor in the environment prevents it)
     * <p> I should make these values more settable - Van 2024.
     *
     */
    @Override
    public int resolveSize()
    {
        if (Sz.getSz(this) == KNOWN_INFINITE)
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

    /**
     * <p> An iterator on the elements of
     * {@code this}
     * .
     *
     * <p> If this list is known infinite then throw
     * {@code SizeOnInfiniteList}
     *
     */
    @Override
    public Iterator<A> iterator()
    {
        return Sz.getSz(this) == KNOWN_INFINITE
               ? Throw.wrap(new SizeOnInfiniteList())
               : super.iterator();
    }

}