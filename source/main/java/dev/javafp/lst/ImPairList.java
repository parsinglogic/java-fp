/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.tuple.ImPair;

class ImPairList<A, B> extends ImCachingLazyList<ImPair<A, B>>
{

    public final ImList<A> as;
    public final ImList<B> bs;

    private ImPairList(ImList<A> as, ImList<B> bs)
    {
        super(Sz.zipWith(as.getSz(), bs.getSz()));

        this.as = as;
        this.bs = bs;
    }

    public static <A, B> ImList<ImPair<A, B>> on(ImList<A> as, ImList<B> bs)
    {
        return as.isEmpty() || bs.isEmpty()
               ? ImList.on()
               : new ImPairList(as, bs);
    }

    @Override
    protected ImPair<A, B> hd()
    {
        return ImPair.on(as.head(), bs.head());
    }

    @Override
    protected ImList<ImPair<A, B>> tl()
    {
        return ImPairList.on(as.tail(), bs.tail());
    }
}