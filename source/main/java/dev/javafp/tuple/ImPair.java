/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tuple;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.func.FnPairConsumer;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

import java.util.Map;

@SuppressWarnings("serial")
public class ImPair<U, V> extends ImValuesImpl
{

    public final U fst;
    public final V snd;

    public ImPair(U fst, V snd)
    {
        this.fst = fst;
        this.snd = snd;
    }

    public static <U, V> ImPair<U, V> on(U fst, V snd)
    {
        return new ImPair<>(fst, snd);
    }

    public static <U, V> ImPair<U, V> on(Map.Entry<U, V> e)
    {
        return ImPair.on(e.getKey(), e.getValue());
    }

    /**
     * <p> The list created by mapping ImPair::fst ofer
     * {@code ps}
     *
     */
    public static <A, B> ImList<A> fst(ImList<ImPair<A, B>> ps)
    {
        return ps.map(p -> p.fst);
    }

    /**
     * <p> The list created by mapping ImPair::snd ofer
     * {@code ps}
     *
     */
    public static <A, B> ImList<B> snd(ImList<ImPair<A, B>> ps)
    {
        return ps.map(p -> p.snd);
    }

    /**
     * <p> The representation of
     * {@code this}
     * as an {@link AbstractTextBox}
     * <p> If the class extends {@link dev.javafp.val.ImValuesImpl} then the default
     * {@code toString}
     *  method will use this method
     * and then convert the result to a
     * {@code String}
     *
     */
    @Override
    public AbstractTextBox getTextBox()
    {
        return LeafTextBox.with("(" + fst + ", " + snd + ")");
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
        return ImList.on(fst, snd);
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
        return ImList.on("fst", "snd");
    }

    /**
     * Apply function `f` to the components of this pair.
     *
     * This is used to allow code that easily accesses the components of a pair with each component having
     * a variable name. Eg:
     *
     *
     */
    public <C> C useIn(Fn2<U, V, C> f)
    {
        return f.of(fst, snd);
    }

    /**
     * Apply function `pairConsumer` to the components of this pair.
     *
     * This is used to allow code that easily accesses the components of a pair with each component having
     * a variable name. Eg:
     *
     *
     */
    public void consumeIn(FnPairConsumer<U, V> pairConsumer)
    {
        pairConsumer.doit(fst, snd);
    }

    public <UU, VV> ImPair<UU, VV> map(Fn<U, UU> f1, Fn<V, VV> f2)
    {
        return ImPair.on(f1.of(fst), f2.of(snd));
    }

    /**
     * <p> Map over a list of pairs
     */
    public static <A, B, C> ImList<C> map(ImList<ImPair<A, B>> pairs, Fn2<A, B, C> fn)
    {
        return pairs.map(p -> fn.of(p.fst, p.snd));
    }

}