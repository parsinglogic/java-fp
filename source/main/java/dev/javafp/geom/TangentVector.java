/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImTriple;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A 2D tangent vector
 */
public class TangentVector extends ImValuesImpl
{

    public final Point start;
    public final Point offset;

    public TangentVector(Point start, Point offset)
    {
        this.start = start;
        this.offset = offset;

    }

    public TangentVector(double x, double y, double ox, double oy)
    {
        this(new Point(x, y), new Point(ox, oy));
    }

    public static TangentVector fromTo(Point p1, Point p2)
    {
        return new TangentVector(p1, p2.minus(p1));
    }

    public ImList<TangentVector> repeat(Point offset, int count)
    {
        return ImList.unfold(this, i -> new TangentVector(i.start.plus(offset), i.offset)).take(count);
    }

    /**
     * <p> Draw a grid with
     * {@code countX}
     *  columns and
     * {@code countY}
     *  rows. Each cell to have a size of
     * {@code width}
     *  and
     * {@code height}
     * <p> So grid(1,1, 5, 2)
     * <p> would produce 9 vectors looking like this:
     *
     * <pre>{@code
     *     ┌───┬───┬───┬───┬───┐
     *     │   │   │   │   │   │
     *     ├───┼───┼───┼───┼───┤
     *     │   │   │   │   │   │
     *     └───┴───┴───┴───┴───┘
     * }</pre>
     *
     */
    public static ImList<TangentVector> grid(double width, double height, int countX, int countY)
    {
        return new TangentVector(0, 0, width * countX, 0).repeat(new Point(0, height), countY + 1)
                .append(new TangentVector(0, 0, 0, height * countY).repeat(new Point(width, 0), countX + 1));
    }

    public TangentVector move(Point move)
    {
        return new TangentVector(start.plus(move), offset);
    }

    public Point getCorner()
    {
        return start.plus(offset);
    }

    public ImTriple<Double, Double, Double> intersection(TangentVector other)
    {
        // get the homegeneous line equations
        LineEquation one = this.lineEquation();
        LineEquation two = other.lineEquation();

        return one.intersection(two);
    }

    public LineEquation lineEquation()
    {
        return new LineEquation(offset.y, -offset.x, start.y * offset.x - start.x * offset.y);
    }

    public LineEquation perpThruCentre()
    {
        // generate the line equation

        LineEquation eq = this.lineEquation();

        // Get the vector from the mid point that is perpendicular ( a, b )
        // Get the line equation from that

        return new TangentVector(start.midPoint(getCorner()), new Point(eq.a, eq.b)).lineEquation();

    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(start, offset);
    }

    @Override public ImList<String> getNames()
    {
        return ImList.on("start", "offset");
    }
}