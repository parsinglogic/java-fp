/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.lst.ImList;
import dev.javafp.util.Util;
import dev.javafp.val.ImValuesImpl;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import static dev.javafp.geom.Point.pt;

/**
 * <p> A Rectangle.
 * <p> This is to be used to layout widgets.
 *
 */
public class Rect extends ImValuesImpl
{

    public final Point origin;
    public final Point size;
    public final Point corner;

    public Rect(Point origin, Point size)
    {
        this.origin = origin;
        this.size = size;
        this.corner = origin.plus(size);

    }

    public Rect(double x, double y, double width, double height)
    {
        this(new Point(x, y), new Point(width, height));
    }

    public static Rect originCorner(Point origin, Point corner)
    {
        return new Rect(origin, corner.minus(origin));
    }

    public static Rect size(Point size)
    {
        return new Rect(Point.zero, size);
    }

    public static Rect size(double x, double y)
    {
        return Rect.size(pt(x, y));
    }

    public static Rect on(double x, double y, Point size)
    {
        return new Rect(pt(x, y), size);
    }

    public static Rect on(double x, double y, double width, double height)
    {
        return new Rect(pt(x, y), pt(width, height));
    }

    public static Rect from(Rectangle2D r2d)
    {
        return new Rect(pt(r2d.getX(), r2d.getY()), pt(r2d.getWidth(), r2d.getHeight()));
    }

    @Override
    public AbstractTextBox getTextBox()
    {
        return LeafTextBox.with("|" + origin + " " + size + "|");
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(origin, size);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("origin", "size");
    }

    /**
     *
     * <pre>{@code
     * NW       N       NE
     *  +-------+-------+
     *  |               |
     *  |               |
     * }</pre>
     * <p> W +       +       + E
     * |       C       |
     * |               |
     * +-------+-------+
     * SW       S       SE
     *
     */

    public Point centre()
    {
        return Point.mid(northWest(), southEast());
    }

    public Point north()
    {
        return Point.mid(northWest(), northEast());
    }

    public Rect north(Point p)
    {
        return this.moveTo(p).moveLeft(size.x * 0.5);
    }

    public Point south()
    {
        return Point.mid(southWest(), southEast());
    }

    public Rect south(Point p)
    {
        return this.moveTo(p).moveLeft(size.x * 0.5).moveUp(size.y);
    }

    public Point east()
    {
        return Point.mid(northEast(), southEast());
    }

    public Rect east(Point p)
    {
        return this.moveTo(p).moveLeft(size.x).moveUp(size.y * 0.5);
    }

    public Point west()
    {
        return Point.mid(northWest(), southWest());
    }

    public Rect west(Point p)
    {
        return this.moveTo(p).moveUp(size.y * 0.5);
    }

    public Point northWest()
    {
        return origin;
    }

    public Rect northWest(Point p)
    {
        return this.moveTo(p);
    }

    public Point northEast()
    {
        return pt(corner.x, origin.y);
    }

    public Rect northEast(Point p)
    {
        return this.moveTo(p).moveLeft(size.x);
    }

    public Point southWest()
    {
        return pt(origin.x, corner.y);
    }

    public Rect southWest(Point p)
    {
        return this.moveTo(p).moveUp(size.y);
    }

    public Point southEast()
    {
        return corner;
    }

    public Rect southEast(Point p)
    {
        return this.moveTo(p).moveLeft(size.x).moveUp(size.y);
    }

    public boolean contains(Point p)
    {
        return p.ge(origin) && corner.ge(p);
    }

    public Rect moveTo(Point newOrigin)
    {
        return new Rect(newOrigin, size);
    }

    public Rect moveRight(double amountToMoveRight)
    {
        return new Rect(pt(origin.x + amountToMoveRight, origin.y), size);
    }

    public Rect moveLeft(double amountToMoveLeft)
    {
        return new Rect(pt(origin.x - amountToMoveLeft, origin.y), size);
    }

    public Rect moveDown(double amountToMoveDown)
    {
        return new Rect(pt(origin.x, origin.y + amountToMoveDown), size);
    }

    public Rect moveUp(double amountToMoveUp)
    {
        return new Rect(pt(origin.x, origin.y - amountToMoveUp), size);
    }

    public Rect moveBelow(Rect other, double verticalOffset)
    {
        return moveTo(other.southWest().plus(pt(0, verticalOffset)));
    }

    public double getWidth()
    {
        return size.x;
    }

    public double getHeight()
    {
        return size.y;
    }

    public Rect setWidth(double w)
    {
        return new Rect(origin, pt(w, size.y));
    }

    public Rect setHeight(double h)
    {
        return new Rect(origin, pt(size.x, h));
    }

    public ImList<Rect> repeat(Point offset, int count)
    {
        return ImList.unfold(this, i -> new Rect(i.origin.plus(offset), i.size)).take(count);
    }

    public static ImList<Rect> layoutVertically(double spaceBetween, Point... sizes)
    {
        return layoutVertically(spaceBetween, ImList.on(sizes));
    }

    public static ImList<Rect> layoutVertically(double spaceBetween, ImList<Point> sizes)
    {
        ImList<Double> tops = sizes.map(p -> p.y + spaceBetween).scanl(0.0, (z, y) -> z + y);
        return tops.zipWith(sizes, (y, size) -> Rect.on(0.0, y, size));
    }

    public static ImList<Rect> layoutHorizontally(double spaceBetween, Point... sizes)
    {
        return layoutHorizontally(spaceBetween, ImList.on(sizes));
    }

    public static ImList<Rect> layoutHorizontally(double spaceBetween, ImList<Point> sizes)
    {
        ImList<Double> lefts = sizes.map(p -> p.x + spaceBetween).scanl(0.0, (z, x) -> z + x);
        return lefts.zipWith(sizes, (x, size) -> Rect.on(x, 0, size));
    }

    public Rect centreIn(Rect container)
    {
        return centreOn(container.getCentre());
    }

    public Rect centreOn(Point centre)
    {
        return new Rect(centre.minus(size.times(0.5)), size);
    }

    public Rect centreY(Point centre)
    {
        return moveYTo(centre.y - size.y * 0.5);
    }

    public Rect moveYTo(double y)
    {
        return Rect.on(origin.x, y, size);
    }

    public Rect centreX(Point centre)
    {
        return moveXTo(centre.x - size.x * 0.5);
    }

    public Rect moveXTo(double x)
    {
        return Rect.on(x, origin.y, size);
    }

    public Rect inset(double inset)
    {
        Point p = pt(inset, inset);
        return new Rect(origin.plus(p), size.minus(p.times(2)));
    }

    public Rect outset(double outset)
    {
        return inset(-outset);
    }

    public Point getCentre()
    {
        return centre();
    }

    public ImList<Rect> createColumns(Double... widths)
    {
        return createColumns(ImList.on(widths));
    }

    public ImList<Rect> cutInto(int i)
    {
        return createColumns(ImList.repeat(size.x / i).take(i));
    }

    /**
     * <p> Cut this rectangle into columns - rectangles with the given widths where they are laid out in a row
     * <p> +--------------------------------------------+
     * |                                            |
     * +--------------------------------------------+
     *
     * <pre>{@code
     *  w1         w2         w3       w4
     * }</pre>
     * <p> +-------+-------------+--------+-------+
     * |       |             |        |       |
     * +-------+-------------+--------+-------+
     *
     */
    public ImList<Rect> createColumns(ImList<Double> widths)
    {
        ImList<Double> startXs = widths.scanl(0.0, (z, i) -> z + i);
        return startXs.zipWith(widths, (start, width) -> new Rect(origin.plus(Point.xOffset(start)), pt(width, size.y)));
    }

    /**
     * <p> A rectangle representing this after it has been justified in
     * {@code container}
     * according to the x and y justifications
     *
     * <pre>{@code
     *     left  bottom                         centre bottom                       right bottom
     * }</pre>
     * <p> +--------------------------+        +--------------------------+    +--------------------------+
     * |                          |        |                          |    |                          |
     * |                          |        |                          |    |                          |
     * |                          |        | +----------------------+ |    |                          |
     * +----------------------+   |        | |                      | |    |   +----------------------+
     * |                      |   |        | |                      | |    |   |                      |
     * |                      |   |        | |                      | |    |   |                      |
     * |                      |   |        | |                      | |    |   |                      |
     * |                      |   |        | |                      | |    |   |                      |
     * |                      |   |        | |                      | |    |   |                      |
     * +----------------------+---+        +-+----------------------+-+    +---+----------------------+
     *
     * <pre>{@code
     *    left  top                         centre top                       right top
     *
     *
     *  +---------------------+----+      +-+----------------------+-+      +---+----------------------+
     *  |                     |    |      | |                      | |      |   |                      |
     *  |                     |    |      | |                      | |      |   |                      |
     *  |                     |    |      | |                      | |      |   |                      |
     *  +---------------------+    |      | |                      | |      |   |                      |
     *  |                          |      | |                      | |      |   |                      |
     *  |                          |      | |                      | |      |   +----------------------+
     *  +--------------------------+      | +----------------------+ |      |                          |
     *                                    |                          |      |                          |
     *                                    |                          |      |                          |
     *                                    +--------------------------+      +--------------------------+
     *
     *
     *
     *
     *    left centre                        centre centre                      right centre
     *
     *  +--------------------------+         +--------------------------+    +--------------------------+
     *  |                          |         |                          |    |                          |
     *  +---------------------+    |         |   +-------------------+  |    |   +----------------------+
     *  |                     |    |         |   |                   |  |    |   |                      |
     *  |                     |    |         |   |                   |  |    |   |                      |
     *  |                     |    |         |   |                   |  |    |   |                      |
     *  +---------------------+    |         |   |                   |  |    |   |                      |
     *  |                          |         |   |                   |  |    |   |                      |
     *  +--------------------------+         |   +-------------------+  |    |   +----------------------+
     *                                       |                          |    |                          |
     *                                       +--------------------------+    +--------------------------+
     * }</pre>
     *
     */
    //    public Rect justify(Rect container, XJustification xj, YJustification yj)
    //    {
    //        double x = xj == left
    //                   ? 0
    //                   : xj == XJustification.centre
    //                     ? (container.size.x - size.x) * 0.5
    //                     : container.size.x - size.x;
    //
    //        double y = yj == YJustification.top
    //                   ? 0.0
    //                   : yj == YJustification.centre
    //                     ? (container.size.y - size.y) * 0.5
    //                     : container.size.y - size.y;
    //
    //        return new Rect(Point.on ( x, y) box.x, box.y);
    //    }

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
     * <p> Get the rectangle that minimally contains
     * {@code rs}
     *
     */
    public static Rect extent(ImList<Rect> rs)
    {
        Point origin = pt(Util.min(rs.map(r -> r.origin.x)), Util.min(rs.map(r -> r.origin.y)));
        Point corner = pt(Util.max(rs.map(r -> r.corner.x)), Util.max(rs.map(r -> r.corner.y)));

        return Rect.originCorner(origin, corner);
    }

    public Shape toRectangle2D()
    {
        Rectangle2D r = new Rectangle2D.Double();
        r.setFrame(origin.x, origin.y, getWidth(), getHeight());
        return r;
    }

    public Rect moveBy(Point point)
    {
        return moveTo(origin.plus(point));
    }

    public Rect moveBy(double x, double y)
    {
        return moveBy(pt(x, y));
    }

    public Rect sizeRect()
    {
        return Rect.size(size);
    }
}