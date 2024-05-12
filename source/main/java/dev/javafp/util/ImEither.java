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
 * <p> The equivalent of the Haskell Either (well y'know - as far as possible)
 */
public class ImEither<L, R> extends ImValuesImpl
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
    public final L left;

    /**
     * <p> The right value (or
     * {@code null}
     *  if
     * {@code isLeft()}
     *  is
     * {@code true}
     * )
     *
     */
    public final R right;

    /**
     * Does this object have a left value?
     */
    public final boolean isLeft;

    private ImEither(L left, R right, boolean isLeft)
    {
        this.left = left;
        this.right = right;
        this.isLeft = isLeft;

    }

    /**
     * <p> An
     * {@code ImEither}
     *  with
     * {@code left}
     *  set to
     * {@code left}
     *  and
     * {@code isLeft}
     *  set to
     * {@code true}
     *
     */
    public static <L, R> ImEither<L, R> Left(L left)
    {
        return new ImEither(left, null, true);
    }

    /**
     * <p> An
     * {@code ImEither}
     *  with
     * {@code right}
     *  set to
     * {@code right}
     *  and
     * {@code isLeft}
     *  set to
     * {@code false}
     *
     */
    public static <L, R> ImEither<L, R> Right(R right)
    {
        return new ImEither(null, right, false);
    }

    /**
     * <p> If this.isLeft() then
     * {@code f1(left)}
     *  else
     * {@code f2(right)}
     *
     */
    public <A> A match(Fn<L, A> f1, Fn<R, A> f2)
    {
        return isLeft
               ? f1.of(left)
               : f2.of(right);
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
        return ImList.on(left, right, isLeft);
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
        return ImList.on("left", "right", "isLeft");
    }

    public <B> ImEither<L, B> flatMap(Fn<R, ImEither<L, B>> fn)
    {
        // Hmm - this line won't compile in javac. I can't understand why though - Van - jul-16
        // Is it related to https://bugs.openjdk.java.net/browse/JDK-8043926
        // return match( l -> ImEither.Left(l), r -> fn.of(r));

        return isLeft
               ? ImEither.Left(left)
               : fn.of(right);
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return isLeft
               ? "Left " + left
               : "Right " + right;
    }

}