/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.val;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.util.TextUtils;

import java.io.Serializable;

/**
 * <p> The main class to subclass to get the "value object" functionality.
 *
 * @see Values
 */
public abstract class ImValuesImpl implements Serializable, Values
{
    private static final long serialVersionUID = 1L;

    protected int hashCode = 0;

    private boolean hashed;

    /**
     * The (cached) hashcode for this object.
     */
    @Override
    public int hashCode()
    {
        if (hashCode == 0)
            hashCode = computeHash2();

        return hashCode;
    }

    protected int computeHash2()
    {
        return getValues().hashCode();
    }

    /**
     * <p> {@code true}
     *  iff this equals
     * {@code other}
     * <p> This implementation uses
     * {@code getValues}
     * and checks that the values are equal pairwise
     */
    @Override
    public boolean equals(Object obj)
    {
        return defaultEquals(obj);
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return getTextBox().toString();
    }

    /**
     * <p> The representation of
     * {@code this}
     * as an {@link AbstractTextBox}
     * The default
     * {@code toString}
     *  method will use this method
     * and then convert the result to a
     * {@code String}
     *
     */
    @Override
    public AbstractTextBox getTextBox()
    {
        return LeftRightBox.with(
                LeafTextBox.with(getClass().getSimpleName() + ": "),
                TextUtils.getBoxFromNamesAndValues(getNames(), getValues()));
    }
}

