/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import java.util.Iterator;

class ImIteratorList<A> extends ImCachingLazyList<A>
{

    private transient final Iterator<A> source;
    private final A head;

    private ImIteratorList(A head, Iterator<A> source)
    {
        super(UNKNOWN_UNKNOWN);
        this.head = head;
        this.source = source;
    }

    public static <A> ImList<A> on(Iterator<? extends A> source)
    {
        if (source.hasNext())
        {
            A s = source.next();
            return new ImIteratorList(s, source);
        }
        else
            return ImList.on();
    }

    @Override
    public A hd()
    {
        return head;
    }

    @Override
    public ImList<A> tl()
    {
        return on(source);
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