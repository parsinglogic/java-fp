/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.tuple.ImQuartet;

class ImQuartetList<A, B, C, D> extends ImCachingLazyList<ImQuartet<A, B, C, D>>
{

    public final ImList<A> as;
    public final ImList<B> bs;
    public final ImList<C> cs;
    public final ImList<D> ds;

    private ImQuartetList(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        // The size of this list will be like a zip
        super(Sz.zipWith(Sz.zipWith(as.getSz(), bs.getSz()), Sz.zipWith(cs.getSz(), ds.getSz())));

        this.as = as;
        this.bs = bs;
        this.cs = cs;
        this.ds = ds;
    }

    static <A, B, C, D> ImList<ImQuartet<A, B, C, D>> on(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        return as.isEmpty() || bs.isEmpty() || cs.isEmpty() || ds.isEmpty()
               ? ImList.on()
               : new ImQuartetList<>(as, bs, cs, ds);
    }

    @Override
    protected ImQuartet<A, B, C, D> hd()
    {
        return ImQuartet.on(as.head(), bs.head(), cs.head(), ds.head());
    }

    @Override
    protected ImList<ImQuartet<A, B, C, D>> tl()
    {
        return ImQuartetList.on(as.tail(), bs.tail(), cs.tail(), ds.tail());
    }

}