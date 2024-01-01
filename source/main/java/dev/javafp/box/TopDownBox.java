/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.box;

import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A composite text-box that lays out its children top down where each child box is a left-right box.
 *
 * <p> <img src="{@docRoot}/dev/doc-files/top-down-box.png"  width=300/>
 */
public class TopDownBox extends AbstractTextBox
{

    private final ImList<AbstractTextBox> boxes;

    /**
     * <p> A
     * {@code TopDownBox}
     *  with children obtained from
     * {@code things}
     * using {@link TextUtils#getBoxFrom(Object)}.
     *
     */
    public static TopDownBox with(Object... things)
    {
        return withAll(ImList.on(things));
    }

    /**
     * <p> A
     * {@code TopDownBox}
     *  with children obtained from
     * {@code things}
     * using {@link TextUtils#getBoxFrom(Object)}.
     *
     */
    public static TopDownBox withAll(ImList<?> things)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (Object t : things)
        {
            AbstractTextBox b = TextUtils.getBoxFrom(t);
            w = Math.max(w, b.width);
            h = h + b.height;
            boxes.add(b);
        }

        return new TopDownBox(w, h, ImList.on(boxes));
    }

    /**
     * <p> A
     * {@code TopDownBox}
     *  with children
     * {@code boxes}
     *
     *
     */
    public static TopDownBox withBoxes(AbstractTextBox... boxes)
    {
        return withAllBoxes(ImList.on(boxes));
    }

    /**
     * <p> A
     * {@code TopDownBox}
     *  with children
     * {@code boxes}
     *
     */
    public static TopDownBox withAllBoxes(ImList<AbstractTextBox> boxes)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> bs = new ArrayList<>();

        for (AbstractTextBox b : boxes)
        {
            w = Math.max(w, b.width);
            h = h + b.height;
            bs.add(b);
        }

        return new TopDownBox(w, h, ImList.on(bs));
    }

    private TopDownBox(int w, int h, ImList<AbstractTextBox> boxes)
    {
        super(w, h);
        this.boxes = boxes;
    }

    @Override
    public String getLine(int n)
    {
        return n > height
               ? TextUtils.padOrTrimToWidth("", width)
               : getLine$(n, boxes);

    }

    private String getLine$(int n, ImList<AbstractTextBox> boxes)
    {
        if (boxes.isEmpty())
            return TextUtils.padOrTrimToWidth("", width);
        else
        {
            AbstractTextBox box = boxes.head();

            return n <= box.height
                   ? TextUtils.padOrTrimToWidth(box.getLine(n), width)
                   : getLine$(n - box.height, boxes.tail());
        }
    }

    @Override
    public AbstractTextBox leftJustifyIn(int width)
    {
        return width == this.width
               ? this
               : new TopDownBox(width, height, boxes);
    }
}