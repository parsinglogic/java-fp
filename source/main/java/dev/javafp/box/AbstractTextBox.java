/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.box;

import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;

import java.io.Serializable;

/**
 * <p> The top level class for text-boxes.
 *
 * <p> The classes in the
 * {@code box}
 *  package allow you to easily output text that is formatted in some way.
 * <p> We assume a fixed width font.
 * <p> The basic element is the box - which is a rectangular grid of characters that can be manipulated as a unit.
 * <p> We can create a some boxes and then stack them side by side - horizontally - or we could stack them vertically.
 * <p> Having done that we can regard the resulting stack of boxes as a box itself and then stack that box with other boxes - and so on.
 * <p> To actually write out a text box, we convert it to a String and then write that String.
 *
 * <p> <img src="{@docRoot}/dev/doc-files/textbox-hierarchy.png"  width=400/>
 *
 * <p> All of the classes that extend
 * {@link dev.javafp.val.ImValuesImpl}
 * will have a function called
 * {@code getTextBox}
 * which will generate a text representation of the object as a text box
 * <p> Then the default
 * {@code toString}
 * method will use this method
 * and convert the resulting text box to a
 * {@code String}
 *
 */
public abstract class AbstractTextBox implements Serializable
{
    public static final LeafTextBox empty = LeafTextBox.with("");

    /**
     * The width of the box in characters
     */
    public final int width;

    /**
     * The height of the box in lines
     */
    public final int height;

    AbstractTextBox(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * The line of text at line number `n` where `n ∈ [1, height]`
     */
    public abstract String getLine(int n);

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        if (height == 1)
            return TextUtils.trimRight(getLine(1));
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= height; i++)
            {
                sb.append(TextUtils.trimRight(getLine(i)));
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    public AbstractTextBox indentBy(int spaces)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.padToWidth("", spaces)), this);
    }

    public AbstractTextBox above(AbstractTextBox other)
    {
        return TopDownBox.with(this, other);
    }

    public AbstractTextBox before(AbstractTextBox other)
    {
        return LeftRightBox.with(this, other);
    }

    public AbstractTextBox boxed()
    {
        /**
         *
         * <pre>{@code
         * ┌──────┐
         * │ text │
         * └──────┘
         * }</pre>
         *
         */

        LeafTextBox vert = LeafTextBox.with("│");

        LeafTextBox space = LeafTextBox.with(" ");

        TopDownBox side = TopDownBox.withAll(ImList.repeat(vert, height));
        LeftRightBox b = LeftRightBox.with(side, space, this, space, side);

        String line = "─".repeat(width + 2);
        LeafTextBox top = LeafTextBox.with("┌" + line + "┐");

        LeafTextBox bottom = LeafTextBox.with("└" + line + "┘");

        return TopDownBox.with(top, b, bottom);
    }

}