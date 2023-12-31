/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.tuple.ImQuintet;

class ImQuintetList<A, B, C, D, E> extends ImCachingLazyList<ImQuintet<A, B, C, D, E>>
{

    final ImList<A> as;
    final ImList<B> bs;
    final ImList<C> cs;
    final ImList<D> ds;
    final ImList<E> es;

    private ImQuintetList(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds, ImList<E> es)
    {
        // The size of this list will be like a zip
        super(Sz.zipWith(Sz.zipWith(Sz.zipWith(Sz.getSz(as), Sz.getSz(bs)), Sz.zipWith(Sz.getSz(cs), Sz.getSz(ds))), Sz.getSz(es)));

        this.as = as;
        this.bs = bs;
        this.cs = cs;
        this.ds = ds;
        this.es = es;
    }

    static <A, B, C, D, E> ImList<ImQuintet<A, B, C, D, E>> on(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds, ImList<E> es)
    {
        return as.isEmpty() || bs.isEmpty() || cs.isEmpty() || ds.isEmpty() || es.isEmpty()
               ? ImList.on()
               : new ImQuintetList<>(as, bs, cs, ds, es);
    }

    @Override
    protected ImQuintet<A, B, C, D, E> hd()
    {
        return ImQuintet.on(as.head(), bs.head(), cs.head(), ds.head(), es.head());
    }

    @Override
    protected ImList<ImQuintet<A, B, C, D, E>> tl()
    {
        return ImQuintetList.on(as.tail(), bs.tail(), cs.tail(), ds.tail(), es.tail());
    }

}