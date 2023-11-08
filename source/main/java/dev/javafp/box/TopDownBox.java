/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.box;

import dev.javafp.ex.InvalidState;
import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A composite text-box that lays out its children top down.
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
        if (n > height)
        {
            return TextUtils.repeatString(" ", width);
        }

        int count = 0;
        for (AbstractTextBox b : boxes)
        {
            for (int i = 1; i <= b.height; i++)
            {
                count++;
                if (count == n)
                {
                    return TextUtils.padToWidth(b.getLine(i), width);
                }
            }
        }
        throw new InvalidState("You can't get here");
    }
}