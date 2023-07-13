/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

public class Table extends ImValuesImpl
{
    public final ImList<ImList<AbstractTextBox>> rowsReversed;
    private final LeafTextBox empty = LeafTextBox.with("");

    /**
     * <p> Add a row to the table
     */
    public Table()
    {
        this(ImList.on());
    }

    public Table(ImList<ImList<AbstractTextBox>> rowsReversed)
    {
        this.rowsReversed = rowsReversed;
    }

    /**
     * <p> Add a row to the table
     */
    public static Table on(Object... cols)
    {
        return new Table().row(cols);
    }

    /**
     * <p> Add a row to the table
     */
    public Table row(Object... cols)
    {
        return new Table(rowsReversed.push(ImList.on(cols).map(o -> Table.textBox(o))));
    }

    private static AbstractTextBox textBox(Object o)
    {
        return o instanceof AbstractTextBox
               ? (AbstractTextBox) o
               : TextUtils.getBoxFrom(o);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(rowsReversed);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("rowsReversed");
    }

    @Override
    public AbstractTextBox getTextBox()
    {
        // Get the max width
        int maxWidth = rowsReversed.foldl(0, (z, i) -> Math.max(z, i.size()));

        ImList<AbstractTextBox> e = ImList.on();

        ImList<ImList<AbstractTextBox>> cols = ImList.repeat(e, maxWidth);

        ImList<ImList<AbstractTextBox>> cols2 = rowsReversed.foldl(cols, (z, i) -> this.addRow(i, z));

        return LeftRightBox.withAllBoxes(cols2.map(c -> TopDownBox.withAllBoxes(c)));

    }

    /**
     * <p> Add row to the list of boxes
     * <p> box[n] of row is pushed onto the front of the list at cols[n]
     * <p> x x x x o o            x x x x o o
     * -->   x x o o o o
     * x o o o o o
     * x x o o o o            x x x x o o
     * x o o o o o
     * x x x x o o
     *
     */
    private ImList<ImList<AbstractTextBox>> addRow(ImList<AbstractTextBox> row, ImList<ImList<AbstractTextBox>> cols)
    {
        // pad the row to the correct size
        ImList<AbstractTextBox> padded = row.append(ImList.repeat(empty, cols.size() - row.size()));

        return padded.zipWith(cols, (b, c) -> c.push(b));
    }
}