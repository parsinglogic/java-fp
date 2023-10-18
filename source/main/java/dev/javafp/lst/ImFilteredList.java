/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.func.Fn;

class ImFilteredList<A> extends ImCachingLazyList<A>
{

    private final ImList<A> source;
    private final Fn<A, Boolean> filterFn;

    private ImFilteredList(ImList<A> source, Fn<A, Boolean> filterFn)
    {
        super(Sz.filter(Sz.getSz(source)));

        this.filterFn = filterFn;
        this.source = source;
    }

    static <A> ImList<A> on(ImList<A> source, Fn<A, Boolean> fn)
    {
        // I need to eagerly calculate the next list that has a head that passes the filter
        // so that isEmpty will work
        // TODO make this lazy - may-15 - Van 
        ImList<A> next = findNextSubList(source, fn);

        return next.isEmpty()
               ? ImList.on()
               : new ImFilteredList<A>(next, fn);
    }

    /**
     * <p> Find the next sublist for which fn of the head is true.
     * If no such list is found then we return the empty list.
     * <p> Note that this method is not recursive
     *
     */
    static <A> ImList<A> findNextSubList(ImList<A> s, Fn<A, Boolean> fn)
    {
        ImList<A> ss = s;
        while (true)
        {
            if (ss.isEmpty())
                return ImList.on();
            else
            {
                if (fn.of(ss.head()))
                    return ss;
                else
                    ss = ss.tail(); // mutation - yuck
            }
        }
    }

    @Override
    protected A hd()
    {
        return source.head();
    }

    @Override
    protected ImList<A> tl()
    {
        return on(source.tail(), filterFn);
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> If source is an infinite list and filterFn never returns true then I will loop
         */
        return resolveSize();
    }

}