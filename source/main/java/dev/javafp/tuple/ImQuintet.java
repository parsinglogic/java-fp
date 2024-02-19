/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tuple;

import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A tuple of length 5.
 */
public class ImQuintet<A, B, C, D, E> extends ImValuesImpl
{
    public final A e1;
    public final B e2;
    public final C e3;
    public final D e4;
    public final E e5;

    public ImQuintet(A e1, B e2, C e3, D e4, E e5)
    {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
        this.e4 = e4;
        this.e5 = e5;

    }

    public static <A, B, C, D, E> ImQuintet<A, B, C, D, E> on(A e1, B e2, C e3, D e4, E e5)
    {
        return new ImQuintet(e1, e2, e3, e4, e5);
    }

    public static <A, B, C, D, E> ImList<ImQuintet<A, B, C, D, E>> zip(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds, ImList<E> es)
    {
        return as.zip(bs).zip(cs).zip(ds).zip(es).map(p -> ImQuintet.on(p.fst.fst.fst.fst, p.fst.fst.fst.snd, p.fst.fst.snd, p.fst.snd, p.snd));
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(e1, e2, e3, e4, e5);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("e1", "e2", "e3", "e4", "e5");
    }
}