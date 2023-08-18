/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.func.Fn2;

class ImScanlList<A, B> extends ImCachingLazyList<A>
{
    private final ImList<B> source;
    private final Fn2<A, B, A> fn;
    private final A start;

    private ImScanlList(ImList<B> source, A start, Fn2<A, B, A> fn)
    {
        super(Sz.addOne(source.getSz()));
        this.source = source;
        this.start = start;
        this.fn = fn;
    }

    static <A, B> ImList<A> on(ImList<B> source, A start, Fn2<A, B, A> fn)
    {
        return new ImScanlList<A, B>(source, start, fn);
    }

    @Override
    protected A hd()
    {
        return start;
    }

    @Override
    protected ImList<A> tl()
    {
        return source.isEmpty()
               ? ImList.on()
               : on(source.tail(), fn.of(start, source.head()), fn);
    }

    @Override
    protected int calculateSize()
    {
        return 1 + source.size();
    }

}