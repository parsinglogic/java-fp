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
 * <p>A text-box that lays out its children top downt.
 */
public class TopDownBox extends AbstractTextBox
{

    private final ImList<AbstractTextBox> boxes;

    public static TopDownBox with(Object... things)
    {
        return withAll(ImList.on(things));
    }

    public static TopDownBox withAll(ImList<?> things)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (Object t : things)
        {
            AbstractTextBox b = TextUtils.getBoxFrom(t);
            w = Math.max(w, b.getWidth());
            h = h + b.getHeight();
            boxes.add(b);
        }

        return new TopDownBox(w, h, ImList.on(boxes));
    }

    public static TopDownBox withBoxes(AbstractTextBox... things)
    {
        return withAllBoxes(ImList.on(things));
    }

    public static TopDownBox withAllBoxes(ImList<AbstractTextBox> things)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (AbstractTextBox b : things)
        {
            w = Math.max(w, b.getWidth());
            h = h + b.getHeight();
            boxes.add(b);
        }

        return new TopDownBox(w, h, ImList.on(boxes));
    }

    private TopDownBox(int w, int h, ImList<AbstractTextBox> boxes)
    {
        super(w, h);
        this.boxes = boxes;
    }

    @Override
    public String getLine(int index)
    {
        if (index > getHeight())
        {
            return TextUtils.repeat(" ", getWidth());
        }

        int count = 0;
        for (AbstractTextBox b : boxes)
        {
            for (int i = 1; i <= b.getHeight(); i++)
            {
                count++;
                if (count == index)
                {
                    return TextUtils.padToWidth(b.getLine(i), getWidth());
                }
            }
        }
        throw new IllegalStateException("You can't get here");
    }
}