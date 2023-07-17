/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.eq.Eq;

/**
 * <p> The classic functional list implementation.
 */
class ImConsList<A> extends ImLazyList<A> implements Eq
{
    private final A head;

    private final ImList<A> tail;

    // TODO - remove this? - Van sep-2015
    //    // To maintain the Empty singleton
    //    private Object readResolve() throws Exception
    //    {
    //        return head == null
    //                ? empty
    //                : this;
    //    }

    public ImConsList(A head, ImList<A> tail)
    {
        super(Sz.addOne(tail.getSz()));
        this.head = head;
        this.tail = tail;
    }

    public A head()
    {
        return head;
    }

    public ImList<A> tail()
    {
        return tail;
    }

    @Override
    protected int calculateSize()
    {
        return 1 + tail.size();
    }

}