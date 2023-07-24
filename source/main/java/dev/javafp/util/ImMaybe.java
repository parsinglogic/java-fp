/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.eq.Equals;
import dev.javafp.ex.MaybeCannotHaveANullValueException;
import dev.javafp.ex.NothingThereException;
import dev.javafp.func.Fn;
import dev.javafp.func.FnConsumer;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

import java.util.Objects;

/**
 * <p> A
 * wrapper around a value that might be missing.
 * <p> You can test if there is a value using {@link #isPresent}.
 * <p> If you try to get the value from a
 * {@code ImMaybe}
 *  and it is missing, it will throw an exception.
 *
 */
public class ImMaybe<T> extends ImValuesImpl
{
    
    private final T value;

    private static final LeafTextBox justBox = LeafTextBox.with("Just ");

    private static final LeafTextBox nothingBox = LeafTextBox.with("Nothing");

    @SuppressWarnings("unchecked")
    public static <A> ImMaybe<A> nothing()
    {
        return new ImMaybe(null);
    }

    protected ImMaybe(T value)
    {
        this.value = value;
    }

    public static <U> ImMaybe<U> just(U value)
    {
        if (value == null)
            throw new MaybeCannotHaveANullValueException();
        else
            return new ImMaybe(value);
    }

    public static <T> ImMaybe<T> with(T valueOrNull)
    {
        return with(null, valueOrNull);
    }

    public static <T> ImMaybe<T> with(T bad, T valueOrBad)
    {
        return Equals.isEqual(bad, valueOrBad)
               ? nothing()
               : just(valueOrBad);
    }

    public T get()
    {
        if (value == null)
            throw new NothingThereException("ImMaybe contains nothing");
        else
            return value;
    }

    public static <A> ImMaybe<A> join(ImMaybe<ImMaybe<A>> m)
    {
        return m.orElse(ImMaybe.nothing());
    }

    public boolean isPresent()
    {
        return value != null;
    }

    public <A> A ifPresentElse(Fn<T, A> yes, A not)
    {
        return isPresent()
               ? yes.of(value)
               : not;
    }

    public void ifPresentDo(FnConsumer<T> block)
    {
        if (isPresent())
            block.doit(value);
    }

    public <U> ImMaybe<U> map(Fn<T, U> fn)
    {
        return !isPresent()
               ? nothing()
               : ImMaybe.with(fn.of(value));
    }

    public <U> ImMaybe<U> flatMap(Fn<T, ImMaybe<U>> fn)
    {
        Objects.requireNonNull(fn);

        return join(this.map(fn));
    }

    public T orElse(T other)
    {
        return value != null
               ? value
               : other;
    }

    public AbstractTextBox getTextBox()
    {
        return ifPresentElse(v -> justBox.before(TextUtils.getBoxFrom(v)), nothingBox);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(value);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("value");
    }

}