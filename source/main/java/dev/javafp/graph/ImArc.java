/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.graph;

import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A labeled arc between nodes in a graph
 */
public class ImArc<KEY, LABEL> extends ImValuesImpl
{

    public final LABEL label;
    public final KEY start;
    public final KEY end;

    ImArc(LABEL label, KEY start, KEY end)
    {
        this.label = label;
        this.start = start;
        this.end = end;

    }

    public static <KEY, LABEL> ImArc<KEY, LABEL> on(LABEL label, KEY start, KEY end)
    {
        return new ImArc<KEY, LABEL>(label, start, end);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(label, start, end);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("label", "start", "end");
    }

    /**
     * <p> Return either start or end depending on the value of dir
     */
    public KEY getSlot(ImGraph.Dir dir)
    {
        return dir == ImGraph.Dir.Out
               ? end
               : start;
    }
}