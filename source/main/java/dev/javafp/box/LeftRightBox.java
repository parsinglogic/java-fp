/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.box;

import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;
import dev.javafp.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A composite text-box that lays out its children left to right.
 *
 * <p> <img src="{@docRoot}/dev/doc-files/left-right-box.png"  width=300/>
 */
public class LeftRightBox extends AbstractTextBox
{
    private final ImList<AbstractTextBox> boxes;

    /**
     * <p> A
     * {@code LeftRightBox}
     *  with children obtained from
     * {@code things}
     * using {@link TextUtils#getBoxFrom(Object)}.
     *
     */
    public static LeftRightBox with(Object... things)
    {
        return withAll(ImList.on(things));
    }

    /**
     * <p> A
     * {@code LeftRightBox}
     *  with children obtained from
     * {@code things}
     * using {@link TextUtils#getBoxFrom(Object)}.
     *
     */
    public static LeftRightBox withAll(ImList<?> things)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (Object t : things)
        {
            AbstractTextBox b = TextUtils.getBoxFrom(t);
            h = Math.max(h, b.height);
            w = w + b.width;
            boxes.add(b);
        }
        return new LeftRightBox(w, h, ImList.onList(boxes));
    }

    /**
     * <p> A
     * {@code LeftRightBox}
     *  with children
     * {@code boxes}
     *
     */
    public static LeftRightBox withAllBoxes(ImList<AbstractTextBox> boxes)
    {
        return new LeftRightBox(Util.sumInt(boxes.map(i -> i.width)), Util.maxInt(boxes.map(i -> i.height)), boxes);
    }

    private LeftRightBox(int w, int h, ImList<AbstractTextBox> boxes)
    {
        super(w, h);
        this.boxes = boxes;
    }

    @Override
    public String getLine(int n)
    {
        StringBuilder sb = new StringBuilder();

        for (AbstractTextBox b : boxes)
            sb.append(b.getLine(n));

        // Should I use padOrTrim here?
        // Adjust for the width if necessary
        if (sb.length() > width)
            sb.delete(width, sb.length());
        else if (sb.length() < width)
            sb.append(" ".repeat(width - sb.length()));

        return sb.toString();
    }

    @Override
    public AbstractTextBox leftJustifyIn(int width)
    {
        return width == this.width
               ? this
               : new LeftRightBox(width, height, boxes);
    }

    /**
     * <p> A
     * {@code LeftRightBox}
     * obtained by indenting
     * {@code box}
     *  by
     * {@code indent}
     *  spaces.
     *
     */
    public static LeftRightBox indent(int indent, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.repeatString(" ", indent)), box);
    }

    /**
     * <p> A
     * {@code LeftRightBox}
     * obtained by indenting the text-box obtained from
     * {@code thing}
     * using {@link TextUtils#getBoxFrom(Object)}.
     *  by
     * {@code indent}
     *  spaces.
     *
     */
    public static LeftRightBox indent(int indent, Object thing)
    {
        return indent(indent, LeafTextBox.with("" + thing));
    }

    /**
     * <p> A
     * {@code LeftRightBox}
     * obtained by adding a margin of
     * {@code leftMargin}
     * <p> spaces and then adding a box to make the resulting box have width
     * {@code width}
     *
     *
     */
    public static LeftRightBox withMargins(int leftMargin, int width, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(leftMargin, 1, ""), box, LeafTextBox.with(width - box.width - leftMargin, 1, ""));
    }

}