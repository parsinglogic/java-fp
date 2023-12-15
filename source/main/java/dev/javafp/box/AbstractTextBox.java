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
    /**
     * The empty text box
     */
    public static final LeafTextBox empty = new LeafTextBox(0, 1, ImList.on(""));

    /**
     * The text box containing a single space
     */
    public static final LeafTextBox spaceBox = new LeafTextBox(1, 1, ImList.on(" "));

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
     * <p> The line of text at line number
     * {@code n}
     *  where
     * {@code n ∈ [1, height]}
     *
     * <p> The string that is returned is padded or trimmed to the width of this box.
     *
     */
    public abstract String getLine(int n);

    /**
     * <p> The String representation of this box.
     *
     * <p> The lines are not trimmed
     *
     */
    @Override
    public String toString()
    {
        return toBuilder(false).toString();
    }

    /**
     * <p> The
     * {@link StringBuilder}
     *  representation of this box, trimming the lines if
     * {@code trimLines}
     *  is true.
     * <p> This is used by
     * {@link dev.javafp.util.Say}
     *  to write stuff
     *
     */
    public StringBuilder toBuilder(boolean trimLines)
    {
        StringBuilder sb = new StringBuilder(128);

        sb.append(getLine(trimLines, 1));

        for (int i = 2; i <= height; i++)
        {
            sb.append("\n");
            sb.append(getLine(trimLines, i));
        }

        return sb;
    }

    private String getLine(boolean trimLines, int i)
    {
        return trimLines
               ? getLine(i).stripTrailing()
               : getLine(i);
    }

    /**
     * <p> A
     * {@link LeftRightBox}
     * with a
     * {@link LeafTextBox}
     * with
     * {@code count}
     *  spaces on the left and
     * {@code this}
     *  on the right
     *
     */
    public AbstractTextBox indentBy(int count)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.padOrTrimToWidth("", count)), this);
    }

    /**
     * <p> A
     * {@link TopDownBox}
     * <p> with
     * {@code this}
     *  above
     * {@code other}
     *
     *
     */
    public AbstractTextBox above(AbstractTextBox other)
    {
        return TopDownBox.with(this, other);
    }

    /**
     * <p> A
     * {@link LeftRightBox}
     * <p> with
     * {@code this}
     *  to the left of
     * {@code other}
     *
     *
     */
    public AbstractTextBox before(AbstractTextBox other)
    {
        return LeftRightBox.with(this, other);
    }

    /**
     * <p> A composite box that adds vertical line and horizontal line characters and "corner" characters around
     * {@code this}
     *  to draw
     * a box around it.
     *
     * <pre>{@code
     * LeafTextBox.with("text").boxed()
     * }</pre>
     * <p> would look like this:
     *
     *
     * <pre>{@code
     * ┌──────┐
     * │ text │
     * └──────┘
     * }</pre>
     *
     */
    public AbstractTextBox boxed()
    {

        LeafTextBox vert = LeafTextBox.with("│");

        LeafTextBox space = LeafTextBox.with(" ");

        TopDownBox side = TopDownBox.withAll(ImList.repeat(vert, height));
        LeftRightBox b = LeftRightBox.with(side, space, this, space, side);

        String line = "─".repeat(width + 2);
        LeafTextBox top = LeafTextBox.with("┌" + line + "┐");

        LeafTextBox bottom = LeafTextBox.with("└" + line + "┘");

        return TopDownBox.with(top, b, bottom);
    }

    public abstract AbstractTextBox leftJustifyIn(int width);

}