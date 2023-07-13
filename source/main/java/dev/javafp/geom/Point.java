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
import dev.javafp.tuple.ImPair;
import dev.javafp.util.SvgTransform;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImValuesImpl;

import static java.lang.Math.abs;
import static java.lang.Math.min;

@SuppressWarnings("serial")
public class Point extends ImValuesImpl
{
    public static final Point zero = Point.on(0, 0);

    public final double x;
    public final double y;

    public Point(double x, double y)
    {
        this.x = x;
        this.y = y;

    }

    public static Point xOffset(double x)
    {
        return new Point(x, 0);
    }

    public static Point yOffset(double y)
    {
        return new Point(0, y);
    }

    public static Point on(double x, double y)
    {
        return new Point(x, y);
    }

    public static Point pt(double x, double y)
    {
        return new Point(x, y);
    }

    public static Point Point(double x, double y)
    {
        return new Point(x, y);
    }

    public static Point square(double side)
    {
        return Point.on(side, side);
    }

    public static Point fromAwt(java.awt.Point point)
    {
        return Point.on(point.getX(), point.getY());
    }

    @Override
    public AbstractTextBox getTextBox()
    {
        return LeafTextBox.with("(" + TextUtils.prettyPrint(x) + ", " + TextUtils.prettyPrint(y) + ")");
    }

    public Point plus(Point other)
    {
        return new Point(x + other.x, y + other.y);
    }

    public Point minus(Point other)
    {
        return new Point(x - other.x, y - other.y);
    }

    /**
     *
     * <pre>{@code
     *             x
     *             y
     *             1
     *
     *  a c e      ax + cy + e
     *  b d f      bx + dy + f
     *  0 0 1      1
     * }</pre>
     *
     */
    public Point preMultiply(SvgTransform t)
    {
        return new Point(t.a * x + t.c * y + t.e, t.b * x + t.d * y + t.f);
    }

    public Point times(double factor)
    {
        return Point.on(x * factor, y * factor);
    }

    public Point times(double xFactor, double yFactor)
    {
        return Point.on(x * xFactor, y * yFactor);
    }

    public double length()
    {
        return Math.sqrt(x * x + y * y);
    }

    public Point midPoint(Point other)
    {
        return this.plus(other.minus(this).times(0.5));
    }

    public static Point mid(Point p1, Point p2)
    {
        return p1.midPoint(p2);
    }

    public Point centre()
    {
        return times(0.5);
    }

    public Point y(double yValue)
    {
        return Point.on(x, yValue);
    }

    public Point x(double xValue)
    {
        return Point.on(xValue, y);
    }

    /**
     *
     * <pre>{@code
     * true iff x >= p.x && y >= p.y
     * }</pre>
     *
     */
    public boolean ge(Point p)
    {
        return x >= p.x && y >= p.y;
    }

    /**
     *
     * <pre>{@code
     * true iff p.ge(this)
     * }</pre>
     *
     */
    public boolean le(Point p)
    {
        return p.ge(this);
    }

    /**
     *
     * <pre>{@code
     * true iff this is to the left of and above p
     * }</pre>
     *
     */
    public boolean lt(Point p)
    {
        return x < p.x && y < p.y;
    }

    /**
     * <p> A
     * {@code Rect}
     *  with corners
     * {@code this}
     *  and
     * {@code p}
     *
     */
    public Rect corner(Point p)
    {
        return new Rect(min(x, p.x), min(y, p.y), abs(p.x - x), abs(p.y - y));
    }

    /**
     * <p> A
     * {@code Rect}
     *  with
     * {@code NW}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     * <p> NW                NE
     * +----------------+
     * |                |
     * |                |
     * |                |
     * |                |
     * |                |
     * +----------------+
     * SW                SE
     *
     */
    public Rect NW(Point size)
    {
        return new Rect(this, size);
    }

    /**
     * <p> A
     * {@code Rect}
     *  with
     * {@code NE}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     * <p> NW                NE
     * +----------------+
     * |                |
     * |                |
     * |                |
     * |                |
     * |                |
     * +----------------+
     * SW                SE
     *
     */
    public Rect NE(Point size)
    {
        return new Rect(x - size.x, y, size.x, size.y);
    }

    /**
     * <p> A Rect with
     * {@code SE}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     * <p> NW                NE
     * +----------------+
     * |                |
     * |                |
     * |                |
     * |                |
     * |                |
     * +----------------+
     * SW                SE
     *
     */
    public Rect SE(Point size)
    {
        return new Rect(x - size.x, y - size.y, size.x, size.y);
    }

    /**
     * <p> A Rect with SW = this and size
     * {@code size}
     * <p> NW                NE
     * +----------------+
     * |                |
     * |                |
     * |                |
     * |                |
     * |                |
     * +----------------+
     * SW                SE
     *
     */
    public Rect SW(Point size)
    {
        return new Rect(x, y - size.y, size.x, size.y);
    }

    public Point north(double delta)
    {
        return Point.on(x, y - delta);
    }

    public Point south(double delta)
    {
        return Point.on(x, y + delta);
    }

    public Point east(double delta)
    {
        return Point.on(x + delta, y);
    }

    public Point west(double delta)
    {
        return Point.on(x - delta, y);
    }

    public Point normalise()
    {
        return this.times(1 / this.length());
    }

    public double dot(Point other)
    {
        return this.x * other.x + this.y * other.y;
    }

    public Rect toRect()
    {
        return Rect.size(this);
    }

    public ImPair<Double, Double> toPair()
    {
        return ImPair.on(x, y);
    }

    public Point plusX(double xOff)
    {
        return Point.on(x + xOff, y);
    }

    public Point plusY(double yOff)
    {
        return Point.on(x, y + yOff);
    }

    /**
     * <p> Justify
     * {@code this}
     *  in
     * {@code rectangle}
     *  based on orientation
     * {@code orient}
     *
     */
    public Rect justifyIn(Rect rectangle, Orient2 orient)
    {
        //        double width = (orient.ox != Fill ? x : rectangle.getWidth());
        //        double height = (orient.oy != Fill ? y : rectangle.getHeight());

        double xOff = xOff(rectangle.getWidth(), x, orient.ox);
        double yOff = yOff(rectangle.getHeight(), y, orient.oy);

        return rectangle.setWidth(x).setHeight(y).moveBy(xOff, yOff);
    }

    private double xOff(double containerWidth, double myWidth, Orient1 ox)
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

    private double yOff(double containerHeight, double myHeight, Orient1 oy)
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

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(x, y);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("x", "y");
    }
}