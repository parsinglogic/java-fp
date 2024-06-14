/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A sum type with three options
 */
public class ImOneOfThree<A, B, C> extends ImValuesImpl
{
    /**
     * <p> The left value (or
     * {@code null}
     *  if
     * {@code isLeft()}
     *  is
     * {@code false}
     * )
     *
     */
    public final Object value;
    public final ThreeType type;

    public enum ThreeType
    {
        A,
        B,
        C
    }

    private ImOneOfThree(A a, B b, C c, ThreeType t)
    {
        if (t == ThreeType.A)
            this.value = a;
        else if (t == ThreeType.B)
            this.value = b;
        else
            this.value = c;

        this.type = t;
    }

    public static <A, B, C> ImOneOfThree<A, B, C> a(A a)
    {
        return new ImOneOfThree(a, null, null, ThreeType.A);
    }

    public static <A, B, C> ImOneOfThree<A, B, C> b(B b)
    {
        return new ImOneOfThree(null, b, null, ThreeType.B);
    }

    public static <A, B, C> ImOneOfThree<A, B, C> c(C c)
    {
        return new ImOneOfThree(null, null, c, ThreeType.C);
    }

    public A a()
    {
        return (A) value;
    }

    public B b()
    {
        return (B) value;
    }

    public C c()
    {
        return (C) value;
    }

    /**
     * <p> If this.isLeft() then
     * {@code f1(left)}
     *  else
     * {@code f2(right)}
     *
     */
    public <D> D match(Fn<A, D> f1, Fn<B, D> f2, Fn<C, D> f3)
    {
        if (type == ThreeType.A)
            return f1.of(a());
        else if (type == ThreeType.B)
            return f2.of(b());
        else
            return f3.of(c());
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(value, type);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("value", "type");
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return "" + value;
    }

}