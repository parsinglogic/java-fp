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
 * <p>A text-box that lays out its children left to right.
 */
public class LeftRightBox extends AbstractTextBox
{
    private final ImList<AbstractTextBox> boxes;

    public static LeftRightBox with(Object... boxes)
    {
        return withAll(ImList.on(boxes));
    }

    public static LeftRightBox withAll(ImList<?> things)
    {

        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (Object t : things)
        {
            AbstractTextBox b = TextUtils.getBoxFrom(t);
            h = Math.max(h, b.getHeight());
            w = w + b.getWidth();
            boxes.add(b);
        }
        return new LeftRightBox(w, h, ImList.on(boxes));
    }

    public static LeftRightBox withAllBoxes(ImList<AbstractTextBox> things)
    {
        int w = 0;
        int h = 0;
        List<AbstractTextBox> boxes = new ArrayList<>();

        for (AbstractTextBox b : things)
        {
            h = Math.max(h, b.getHeight());
            w = w + b.getWidth();
            boxes.add(b);
        }

        return new LeftRightBox(w, h, ImList.on(boxes));
    }

    private LeftRightBox(int w, int h, ImList<AbstractTextBox> boxes)
    {
        super(w, h);
        this.boxes = boxes;
    }

    @Override
    public String getLine(int index)
    {
        StringBuilder sb = new StringBuilder();
        for (AbstractTextBox b : boxes)
        {
            sb.append(b.getLine(index));
        }
        return sb.toString();
    }

    public static LeftRightBox indent(int indent, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.repeat(" ", indent)), box);
    }

    public static LeftRightBox indent(int indent, Object thing)
    {
        return indent(indent, LeafTextBox.with("" + thing));
    }

    public static LeftRightBox withMargins(int leftMargin, int width, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(leftMargin, 1, ""), box, LeafTextBox.with(width - box.getWidth(), 1, ""));
    }

}