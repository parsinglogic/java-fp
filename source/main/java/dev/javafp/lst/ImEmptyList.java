/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.ex.ImNotAllowedOnEmptyList;

import java.util.ArrayList;

class ImEmptyList<A> extends ImLazyList<A>
{
    @SuppressWarnings("rawtypes")
    private static final int hashCodeOfEmpty = new ArrayList().hashCode();

    protected ImEmptyList()
    {
        super(0);
    }

    /**
     * <p>The first element in
     * {@code this}
     * .
     *
     * <p>Throws {@link ImNotAllowedOnEmptyList} because this list is empty.
     */
    @Override
    public A head()
    {
        throw new ImNotAllowedOnEmptyList();
    }

    /**
     * `this` without the first element.
     *
     * Throws {@link ImNotAllowedOnEmptyList} because this list is empty.
     */
    @Override
    public ImList<A> tail()
    {
        throw new ImNotAllowedOnEmptyList();
    }

    @Override
    public A last()
    {
        throw new ImNotAllowedOnEmptyList();
    }

    @Override
    public A at(int indexStartingAtOne)
    {
        throw new ImNotAllowedOnEmptyList();
    }

    @Override
    public A at(int indexStartingAtOne, A defaultValue)
    {
        return defaultValue;
    }

    /**
     *
     */
    public ImList<A> put(int indexStartingAtOne, A thingToPut)
    {
        throw new ImNotAllowedOnEmptyList();
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    /**
     * <p> The number of elements in
     * {@code this}
     *
     * In this case,
     * {@code 0}
     *
     */
    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public int hashCode(int count)
    {
        return hashCodeOfEmpty;
    }

    // To maintain the Empty singleton
    private Object readResolve()
    {
        return ImList.on();
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
        return LeafTextBox.with("[]");
    }

}