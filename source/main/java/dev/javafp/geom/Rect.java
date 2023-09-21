/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.util.Util;
import dev.javafp.val.ImValuesImpl;

import java.awt.geom.Rectangle2D;

import static dev.javafp.geom.Point.pt;

/**
 * <p> A Rectangle.
 *
 * <p> A rectangle
 * with its north-west point
 * {@code origin}
 * and its south-east point
 * {@code corner}
 *  so its size is
 * {@code corner - origin}
 *
 * <p> <img src="{@docRoot}/dev/doc-files/rectangle-origin-corner.png"  width=300/>
 *
 * <p> This is used to layout widgets (and other things).
 *
 * @see Point
 *
 */
public class Rect extends ImValuesImpl
{

    /**
     * The north-west point
     */
    public final Point origin;

    /**
     * The size -
     * {@code corner - origin}
     */
    public final Point size;

    /**
     * The south-east point
     */
    public final Point corner;

    private Rect(Point origin, Point size)
    {
        this.origin = origin;
        this.size = size;
        this.corner = origin.plus(size);
    }

    private Rect(double x, double y, double width, double height)
    {
        this(Point.on(x, y), Point.on(width, height));
    }

    /**
     * <p> The rectangle with origin
     * {@code origin}
     *  and corner
     * {@code corner}
     *
     * <p> If it is not the case that
     * {@code origin.le(corner)}
     *  then
     * {@code origin}
     *  and
     * {@code corner}
     *  are
     * {@code swapped}
     *
     */
    public static Rect originCorner(Point origin, Point corner)
    {
        return origin.le(corner)
               ? originSize(origin, corner.minus(origin))
               : originSize(origin, origin.minus(corner));
    }

    /**
     * <p> The rectangle with origin
     * {@code origin}
     *  and size
     * {@code size}
     *
     */
    public static Rect originSize(Point origin, Point size)
    {
        return new Rect(origin, size);
    }

    /**
     * <p> The rectangle with origin
     * {@code (0, 0)}
     *  and size
     * {@code size}
     *
     */
    public static Rect size(Point size)
    {
        return new Rect(Point.zero, size);
    }

    /**
     * <p> The rectangle with origin
     * {@code (0, 0)}
     *  and size
     * {@code (x, y)}
     *
     */
    public static Rect size(double x, double y)
    {
        return Rect.size(pt(x, y));
    }

    /**
     * <p> The rectangle with origin
     * {@code (x, y)}
     *  and size
     * {@code size}
     *
     */
    public static Rect on(double x, double y, Point size)
    {
        return new Rect(pt(x, y), size);
    }

    /**
     * <p> The rectangle with origin
     * {@code (x, y)}
     *  and size
     * {@code (width, height)}
     *
     */
    public static Rect on(double x, double y, double width, double height)
    {
        return new Rect(pt(x, y), pt(width, height));
    }

    /**
     * <p> The rectangle with the same origin and size as
     * {@code r2d}
     *
     */
    public static Rect from(Rectangle2D r2d)
    {
        return new Rect(pt(r2d.getX(), r2d.getY()), pt(r2d.getWidth(), r2d.getHeight()));
    }

    /**
     * <p> The representation of
     * {@code this}
     * as an {@link AbstractTextBox}
     * <p> If the class extends {@link dev.javafp.val.ImValuesImpl} then the default
     * {@code toString}
     *  method will use this method
     * and then convert the result to a
     * {@code String}
     *
     */
    @Override
    public AbstractTextBox getTextBox()
    {
        return LeafTextBox.with("|" + origin + " " + size + "|");
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
        return ImList.on(origin, size);
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
        return ImList.on("origin", "size");
    }

    /**
     * <p> The centre point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     */
    public Point centre()
    {
        return Point.mid(northWest(), southEast());
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose centre point is
     * {@code p}
     *
     */
    public Rect centre(Point p)
    {
        return this.moveTo(p).moveBy(size.times(-0.5));
    }

    /**
     * <p> The north point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point north()
    {
        return Point.mid(northWest(), northEast());
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose north point is
     * {@code p}
     *
     */
    public Rect north(Point p)
    {
        return this.moveTo(p).moveByX(size.x * -0.5);
    }

    /**
     * <p> The south point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point south()
    {
        return Point.mid(southWest(), southEast());
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose south point is
     * {@code p}
     *
     */
    public Rect south(Point p)
    {
        return this.moveTo(p).moveBy(pt(size.x * -0.5, -size.y));
    }

    /**
     * <p> The east point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point east()
    {
        return Point.mid(northEast(), southEast());
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose east point is
     * {@code p}
     *
     */
    public Rect east(Point p)
    {
        return this.moveTo(p).moveBy(pt(-size.x, size.y * -0.5));
    }

    /**
     * <p> The west point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point west()
    {
        return Point.mid(northWest(), southWest());
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose west point is
     * {@code p}
     *
     */
    public Rect west(Point p)
    {
        return this.moveTo(p).moveByY(size.y * -0.5);
    }

    /**
     * <p> The north-west point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point northWest()
    {
        return origin;
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose north-west point is
     * {@code p}
     *
     */
    public Rect northWest(Point p)
    {
        return this.moveTo(p);
    }

    /**
     * <p> The north-east point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     */
    public Point northEast()
    {
        return pt(corner.x, origin.y);
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose north-east point is
     * {@code p}
     *
     */
    public Rect northEast(Point p)
    {
        return this.moveTo(p).moveByX(-size.x);
    }

    /**
     * <p> The south-west point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point southWest()
    {
        return pt(origin.x, corner.y);
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose south-west point is
     * {@code p}
     *
     */
    public Rect southWest(Point p)
    {
        return this.moveTo(p).moveByY(-size.y);
    }

    /**
     * <p> The south-east point of the rectangle.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-points.png"  width=300/>
     *
     *
     */
    public Point southEast()
    {
        return corner;
    }

    /**
     * <p> The rectangle that has the same size as
     * {@code this}
     *  and whose south-east point is
     * {@code p}
     *
     */
    public Rect southEast(Point p)
    {
        return this.moveTo(p).moveBy(pt(-size.x, -size.y));
    }

    /**
     * <p> {@code true}
     *  if
     * {@code p}
     *  lies inside (or on the boundary of)
     * {@code this}
     * .
     *
     */
    public boolean contains(Point p)
    {
        return p.ge(origin) && corner.ge(p);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code newOrigin}
     * .
     */
    public Rect moveTo(Point newOrigin)
    {
        return new Rect(newOrigin, size);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code origin + (dx, 0)}
     * .
     */
    public Rect moveByX(double dx)
    {
        return new Rect(pt(origin.x + dx, origin.y), size);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code origin + (0, dy)}
     * .
     */
    public Rect moveByY(double dy)
    {
        return new Rect(pt(origin.x, origin.y + dy), size);
    }

    /**
     * <p> The width of
     * {@code this}
     *
     */
    public double getWidth()
    {
        return size.x;
    }

    /**
     * <p> The height of
     * {@code this}
     *
     */
    public double getHeight()
    {
        return size.y;
    }

    /**
     * <p> The rectangle with the same
     * {@code origin}
     *  and
     * {@code height}
     *  as
     * {@code this}
     *  but with width
     * {@code width}
     * .
     *
     */
    public Rect setWidth(double w)
    {
        return new Rect(origin, pt(w, size.y));
    }

    /**
     * <p> The rectangle with the same
     * {@code origin}
     *  and
     * {@code width}
     *  as
     * {@code this}
     *  but with height
     * {@code height}
     * .
     *
     */
    public Rect setHeight(double h)
    {
        return new Rect(origin, pt(size.x, h));
    }

    /**
     * <p> A list with
     * {@code this}
     *  repeated
     * {@code count}
     *  times with each element having an offset of
     * {@code offset}
     *  from the previous element.
     *
     * <p> The list of rectangles:
     *
     * <pre>{@code
     * [this, this.moveBy(offset), this.moveBy(offset*2).., this.moveBy(offset*count) ]
     * }</pre>
     *
     * or
     * {@code []' if }
     * count== 0`
     * <p> If
     * {@code count < 0}
     *  the
     * {@code ArgumentShouldNotBeLessThan}
     *  is thrown
     *
     */
    public ImList<Rect> repeat(Point offset, int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);
        return ImList.unfold(this, i -> new Rect(i.origin.plus(offset), i.size)).take(count);
    }

    /**
     *
     * <p> A shortcut for:
     *
     * {@code layoutVertically(gap, ImList.on(sizes)}
     *
     */
    public static ImList<Rect> layoutVertically(double gap, Point... sizes)
    {
        return layoutVertically(gap, ImList.on(sizes));
    }

    /**
     * <p> {@code sizes.size}
     *  rectangles
     * laid out top down with the first one having
     * {@code origin=(0,0)}
     *  with
     * a vertical gap between them of
     * {@code gap}
     * .
     * <p> The
     * {@code ith}
     *  rectangle has
     * {@code size=sizes.at(i)}
     *
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-layout-vertically.png"  width=300/>
     *
     *
     */
    public static ImList<Rect> layoutVertically(double gap, ImList<Point> sizes)
    {
        ImList<Double> tops = sizes.map(p -> p.y + gap).scanl(0.0, (z, y) -> z + y);
        return tops.zipWith(sizes, (y, size) -> Rect.on(0.0, y, size));
    }

    /**
     *
     * <p> A shortcut for:
     *
     * {@code layoutHorizontally(gap, ImList.on(sizes)}
     *
     */
    public static ImList<Rect> layoutHorizontally(double gap, Point... sizes)
    {
        return layoutHorizontally(gap, ImList.on(sizes));
    }

    /**
     * <p> {@code sizes.size}
     *  rectangles
     * laid out left to right with the first one having
     * {@code origin=(0,0)}
     *  with
     * a horizontal gap between them of
     * {@code gap}
     *
     * <p> The
     * {@code ith}
     *  rectangle has
     * {@code size=sizes.at(i)}
     *
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-layout-horizontally.png"  width=300/>
     *
     *
     */
    public static ImList<Rect> layoutHorizontally(double gap, ImList<Point> sizes)
    {
        ImList<Double> lefts = sizes.scanl(0.0, (z, i) -> z + i.x + gap);
        return lefts.zipWith(sizes, (x, size) -> Rect.on(x, 0, size));
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code container.center()}
     * .
     */
    public Rect centreIn(Rect container)
    {
        return centreOn(container.centre());
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code centre}
     * .
     */
    public Rect centreOn(Point centre)
    {
        return new Rect(centre.minus(size.times(0.5)), size);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code (origin.x, y)}
     * .
     */
    public Rect moveToY(double y)
    {
        return Rect.on(origin.x, y, size);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code (x, origin.y)}
     * .
     */
    public Rect moveToX(double x)
    {
        return Rect.on(x, origin.y, size);
    }

    /**
     * <p> The rectangle with the same
     * {@code centre}
     *  as
     * {@code this}
     *  but with size
     * {@code size - 2*(delta, delta)}
     * .
     *
     *  <p> <img src="{@docRoot}/dev/doc-files/rectangle-inset.png"  width=300/>
     */
    public Rect inset(double delta)
    {
        Point p = pt(delta, delta);
        return new Rect(origin.plus(p), size.minus(p.times(2)));
    }

    /**
     * <p> The rectangle
     * {@code this.inset(-delta)}
     *
     *  <p> <img src="{@docRoot}/dev/doc-files/rectangle-outset.png"  width=300/>
     */
    public Rect outset(double delta)
    {
        return inset(-delta);
    }

    /**
     * <p> Cut this rectangle into columns - Return
     * {@code n}
     *  rectangles with same height as
     * {@code this}
     *  with each
     * <br/>
     * width taken from
     * {@code widths}
     *  where
     * {@code n}
     *  is the size of
     * {@code widths}
     * .
     * <p> They are laid left to right with the first rectangle having the same origin as
     * {@code this}
     * .
     * The sum of the widths of the rectangles will not necessarily be the width of
     * {@code this}
     * .
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-create-columns.png"  width=300/>
     */
    public ImList<Rect> createColumns(ImList<Double> widths)
    {
        ImList<Double> startXs = widths.scanl(0.0, (z, i) -> z + i);
        return startXs.zipWith(widths, (start, width) -> new Rect(origin.plus(Point.xOffset(start)), pt(width, size.y)));
    }

    /**
     * <p> A rectangle representing
     * {@code this}
     * after it has been justified in
     * {@code container}
     * based on orientation
     * {@code orient}
     * and then offset by
     * {@code offset}
     *
     * <p> <img src="{@docRoot}/dev/doc-files/rectangle-orientations.png"  width=600/>
     */
    public Rect justifyIn(Orient2 orient, Point offset)
    {

        double xOff = xOff(this.getWidth(), offset.x, orient.ox);
        double yOff = yOff(this.getHeight(), offset.y, orient.oy);

        return this.setWidth(offset.x).setHeight(offset.y).moveBy(pt(xOff, yOff));
    }

    private static double xOff(double containerWidth, double myWidth, Orient1 ox)
    {
        switch (ox)
        {
        case Right:
            return containerWidth - myWidth;

        case Centre:
            return (containerWidth - myWidth) * 0.5;

        default:
            return 0;
        }
    }

    private static double yOff(double containerHeight, double myHeight, Orient1 oy)
    {
        switch (oy)
        {
        case Bottom:
            return containerHeight - myHeight;

        case Centre:
            return (containerHeight - myHeight) * 0.5;

        default:
            return 0;
        }
    }

    /**
     * <p> Expand by a border of borderWidth, keeping the origin the same
     */
    public Rect expandBy(double borderWidth)
    {
        return new Rect(origin, size.plus(Point.square(borderWidth * 2)));
    }

    /**
     * <p> Expand by a border of x, y, keeping the origin the same
     */
    public Rect expandBy(double x, double y)
    {
        return new Rect(origin, size.plus(pt(x, y).times(2)));
    }

    /**
     * <p> The rectangle that minimally contains
     * {@code rs}
     *
     */
    public static Rect extent(ImList<Rect> rs)
    {
        Point origin = pt(Util.min(rs.map(r -> r.origin.x)), Util.min(rs.map(r -> r.origin.y)));
        Point corner = pt(Util.max(rs.map(r -> r.corner.x)), Util.max(rs.map(r -> r.corner.y)));

        return Rect.originCorner(origin, corner);
    }

    /**
     * <p> The rectangle with the same
     * {@code size}
     *  as
     * {@code this}
     *  but with origin
     * {@code origin + point}
     * .
     */
    public Rect moveBy(Point point)
    {
        return moveTo(origin.plus(point));
    }

}