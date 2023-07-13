/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tuple;

import dev.javafp.func.Fn4;
import dev.javafp.lst.ImList;
import dev.javafp.lst.TupleListBuilder;
import dev.javafp.val.ImValuesImpl;

public class ImQuartet<A, B, C, D> extends ImValuesImpl
{
    public final A e1;
    public final B e2;
    public final C e3;
    public final D e4;

    public ImQuartet(A e1, B e2, C e3, D e4)
    {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
        this.e4 = e4;

    }

    public static <A, B, C, D> ImQuartet<A, B, C, D> on(A e1, B e2, C e3, D e4)
    {
        return new ImQuartet(e1, e2, e3, e4);
    }

    public static <A, B, C, D> ImList<ImQuartet<A, B, C, D>> zip(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        return TupleListBuilder.on4(as, bs, cs, ds);
    }

    public static <A, B, C, D, E> ImList<E> zipWith(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds, Fn4<A, B, C, D, E> fn)
    {
        return zip(as, bs, cs, ds).map(t -> fn.of(t.e1, t.e2, t.e3, t.e4));
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(e1, e2, e3, e4);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("e1", "e2", "e3", "e4");
    }
}