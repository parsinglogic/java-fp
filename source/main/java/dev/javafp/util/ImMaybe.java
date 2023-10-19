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
import dev.javafp.ex.MaybeHasNothing;
import dev.javafp.func.Fn;
import dev.javafp.func.FnConsumer;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

import java.util.Objects;

/**
 * <p> A wrapper around a value that might be missing - in which case it is
 * {@code ImMaybe.nothing}
 * .
 *
 * <p> You can test if there is a value using {@link #isPresent}.
 * <p> If you try to
 * {@link #get()}.
 * the value from an
 * {@code ImMaybe}
 * and it is missing, it will throw a
 * {@code MaybeHasNothing}
 * exception.
 *
 * <p> {@code ImMaybe}
 *  ia a Monad so it implements
 * {@code map}
 * ,
 * {@code flatmap}
 *  and
 * {@code join}
 * .
 *
 *
 */
public class ImMaybe<T> extends ImValuesImpl
{
    private final T value;

    private static final LeafTextBox justBox = LeafTextBox.with("Just ");

    private static final LeafTextBox nothingBox = LeafTextBox.with("Nothing");

    /**
     * <p> The singleton
     * <strong>nothing</strong>
     *  maybe
     *
     */
    public static final ImMaybe nothing = new ImMaybe(null);

    protected ImMaybe(T value)
    {
        this.value = value;
    }

    /**
     * <p> A
     * {@code Maybe}
     *  with the value
     * {@code value}
     * .
     * <p> Throws {@link MaybeCannotHaveANullValueException} if
     * {@code value == null}
     * .
     *
     */
    public static <U> ImMaybe<U> just(U value)
    {
        if (value == null)
            throw new MaybeCannotHaveANullValueException();
        else
            return new ImMaybe(value);
    }

    /**
     * <p> An
     * {@code ImMaybe}
     *  with the value
     * {@code valueOrNull}
     * or
     * {@code ImMaybe.nothing}
     *  if
     * {@code value == null}
     * .
     *
     */
    public static <T> ImMaybe<T> with(T valueOrNull)
    {
        return with(null, valueOrNull);
    }

    /**
     * <p> An
     * {@code ImMaybe}
     *  with the value
     * {@code valueOrBad}
     * or
     * {@code ImMaybe.nothing}
     *  if
     * {@code Equals.isEqual(bad, valueOrBad)}
     * .
     *
     */
    public static <T> ImMaybe<T> with(T bad, T valueOrBad)
    {
        return Equals.isEqual(bad, valueOrBad)
               ? nothing
               : just(valueOrBad);
    }

    /**
     * <p> The value in this
     * {@code ImMaybe}
     * .
     * <p> If there is no value, throw {@link MaybeHasNothing}
     *
     */
    public T get()
    {
        if (value == null)
            throw new MaybeHasNothing("ImMaybe contains nothing");
        else
            return value;
    }

    /**
     * <p> Given an
     * {@code ImMaybe}
     *  whose value is itself an
     * {@code ImMaybe}
     * <p> return
     *
     * <pre>{@code
     * m.flatMap(Fn.id())
     * }</pre>
     *
     */
    public static <A> ImMaybe<A> join(ImMaybe<ImMaybe<A>> m)
    {
        return m.orElse(nothing);
    }

    /**
     * <p> Map the function
     * {@code fn}
     *  to  this
     * {@code ImMaybe}
     *  and then run
     * {@link #join(ImMaybe)} on the result
     *
     *
     */
    public <U> ImMaybe<U> flatMap(Fn<T, ImMaybe<U>> fn)
    {
        Objects.requireNonNull(fn);

        return join(this.map(fn));
    }

    /**
     * <p> {@code True}
     *  if this
     * {@code ImMaybe}
     *  is not
     * {@code ImMaybe.nothing}
     * ,
     * {@code false}
     *  otherwise
     *
     */
    public boolean isPresent()
    {
        return value != null;
    }

    /**
     * <p> If there is a value in this
     * {@code ImMaybe}
     *  then apply the function
     * {@code fn}
     *  to the value and return the result.
     * <p> If there is no value then return
     * {@code not}
     *
     */
    public <A> A ifPresentElse(Fn<T, A> fn, A not)
    {
        return isPresent()
               ? fn.of(value)
               : not;
    }

    /**
     * <p> If there is a value in this
     * {@code ImMaybe}
     *  then apply the consumer function
     * {@code block}
     *  to the value
     * <p> If there is no value then do nothing
     *
     */
    public void ifPresentDo(FnConsumer<T> block)
    {
        if (isPresent())
            block.doit(value);
    }

    /**
     * <p> Apply the function
     * {@code fn}
     *  to the value and return an
     * {@code ImMaybe}
     *  with that value.
     * <p> If that value is null then return
     * {@code ImMaybe.nothing}
     * .
     * <p> If there is no value in this
     * {@code ImMaybe}
     *  then return
     * {@code ImMaybe.nothing}
     *
     */
    public <U> ImMaybe<U> map(Fn<T, U> fn)
    {
        return !isPresent()
               ? nothing
               : ImMaybe.with(fn.of(value));
    }

    /**
     *
     * <p> If there is a value in this
     * {@code ImMaybe}
     *  then return it, otherwise return
     * {@code other}
     */
    public T orElse(T other)
    {
        return value != null
               ? value
               : other;
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
    public AbstractTextBox getTextBox()
    {
        return ifPresentElse(v -> justBox.before(TextUtils.getBoxFrom(v)), nothingBox);
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
        return ImList.on(value);
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
        return ImList.on("value");
    }

}