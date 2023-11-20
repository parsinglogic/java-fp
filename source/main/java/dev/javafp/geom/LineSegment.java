/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A 2D line segment
 *
 *
 *
 */
public class LineSegment extends ImValuesImpl
{

    /**
     * The start point
     */
    public final Point start;

    /**
     * The point defining the offset relative to the start point
     */
    public final Point offset;

    private LineSegment(Point start, Point offset)
    {
        this.start = start;
        this.offset = offset;

    }

    private LineSegment(double x, double y, double ox, double oy)
    {
        this(Point.on(x, y), Point.on(ox, oy));
    }

    /**
     * <p> A line segment starting at
     * {@code p1}
     *  and ending at
     * {@code p2}
     *
     */
    public static LineSegment fromTo(Point p1, Point p2)
    {
        return new LineSegment(p1, p2.minus(p1));
    }

    /**
     * <p> A line segment starting at
     * {@code (x, y)}
     *  with an offset of
     * {@code (ox, oy}
     */
    public static LineSegment originOffset(double x, double y, double ox, double oy)
    {
        return new LineSegment(Point.on(x, y), Point.on(ox, oy));
    }

    /**
     * <p> {@code count}
     *  line segments, each one with a start that is offset from the previous one by
     * {@code offset}
     * .
     * <p> The first one equal to
     * {@code this}
     *
     */
    public ImList<LineSegment> repeat(Point offset, int count)
    {
        return ImList.unfold(this, i -> new LineSegment(i.start.plus(offset), i.offset)).take(count);
    }

    /**
     * <p> Draw a grid using fixed line segments
     * with
     * {@code countX}
     *  columns and
     * {@code countY}
     *  rows. Each cell to have a size of
     * {@code width}
     *  and
     * {@code height}
     * <p> So grid(1,1, 5, 2)
     * <p> would produce 9 line segments looking like this:
     *
     * <p> <img src="{@docRoot}/dev/doc-files/grid.png"  width=200/>
     *
     *
     */
    public static ImList<LineSegment> grid(double width, double height, int countX, int countY)
    {
        return new LineSegment(0, 0, width * countX, 0).repeat(Point.on(0, height), countY + 1)
                .append(new LineSegment(0, 0, 0, height * countY).repeat(Point.on(width, 0), countX + 1));
    }

    /**
     * <p> The point
     * {@code start + offset}
     * .
     *
     */
    public Point getCorner()
    {
        return start.plus(offset);
    }

    /**
     * The line equation of the infinite line colinear with this line segment
     */
    public LineEquation lineEquation()
    {
        return new LineEquation(offset.y, -offset.x, start.y * offset.x - start.x * offset.y);
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

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("start", "offset");
    }
}