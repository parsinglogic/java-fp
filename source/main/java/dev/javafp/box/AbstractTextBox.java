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

public abstract class AbstractTextBox implements Serializable
{
    public static final LeafTextBox empty = LeafTextBox.with("");

    public final int width;
    public final int height;

    public AbstractTextBox(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public abstract String getLine(int count);

    @Override
    public String toString()
    {

        if (getHeight() == 1)
            return TextUtils.trimRight(getLine(1));
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= getHeight(); i++)
            {
                sb.append(TextUtils.trimRight(getLine(i)));
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
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

        TopDownBox side = TopDownBox.withAll(ImList.repeat(vert, this.getHeight()));
        LeftRightBox b = LeftRightBox.with(side, space, this, space, side);

        String line = "─".repeat(this.getWidth() + 2);
        LeafTextBox top = LeafTextBox.with("┌" + line + "┐");

        LeafTextBox bottom = LeafTextBox.with("└" + line + "┘");

        return TopDownBox.with(top, b, bottom);
    }

}