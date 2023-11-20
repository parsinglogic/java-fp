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
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImValuesImpl;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * A 2D point
 * {@code (x, y)}
 * .
 *
 * <p> The x-axis goes to the right and the y-axis goes down.
 *
 * <p> We use the terms east, west etc in some of the functions. These are indicated below.
 *
 * <p> <img src="{@docRoot}/dev/doc-files/point.png"  width=300/>
 *
 * @see Rect
 */
public class Point extends ImValuesImpl
{

    /**
     * <p> The point
     * {@code (0, 0)}
     *
     */
    public static final Point zero = Point.on(0, 0);

    /**
     * <p> The
     * {@code x}
     *  coordinate
     *
     */
    public final double x;

    /**
     * <p> The
     * {@code y}
     *  coordinate
     *
     */
    public final double y;

    /**
     * <p> The point
     * {@code (x, y)}
     *
     */
    private Point(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * <p> The point
     * {@code (x, 0)}
     *
     */
    public static Point xOffset(double x)
    {
        return Point.on(x, 0);
    }

    /**
     * <p> The point
     * {@code (0, y)}
     *
     */
    public static Point yOffset(double y)
    {
        return Point.on(0, y);
    }

    /**
     * <p> The point
     * {@code (x, y)}
     *
     */
    public static Point on(double x, double y)
    {
        return new Point(x, y);
    }

    /**
     * <p> The point
     * {@code (x, y)}
     *
     */
    public static Point pt(double x, double y)
    {
        return Point.on(x, y);
    }

    /**
     * <p> The point
     * {@code (side, side)}
     *
     */
    public static Point square(double side)
    {
        return Point.on(side, side);
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
        return LeafTextBox.with("(" + TextUtils.prettyPrint(x) + ", " + TextUtils.prettyPrint(y) + ")");
    }

    /**
     * <p> The point
     * {@code (x + other.x, y + other.y)}
     *
     */
    public Point plus(Point other)
    {
        return Point.on(x + other.x, y + other.y);
    }

    /**
     * <p> The point
     * {@code (x - other.x, y - other.y)}
     *
     */
    public Point minus(Point other)
    {
        return Point.on(x - other.x, y - other.y);
    }

    /**
     * <p> The point
     * {@code (x * factor, y * factor)}
     *
     */
    public Point times(double factor)
    {
        return Point.on(x * factor, y * factor);
    }

    /**
     * <p> The point
     * {@code (x * xFactor, y * yFactor)}
     *
     */
    public Point times(double xFactor, double yFactor)
    {
        return Point.on(x * xFactor, y * yFactor);
    }

    /**
     * The "length" of this point, ie its distance from teh origin
     * {@code Math.sqrt(x * x + y * y)}
     *
     */
    public double length()
    {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * <p> The mid-point between
     * {@code this}
     *  and
     * {@code other}
     * .
     *
     * <pre>{@code
     * this.plus(other.minus(this).times(0.5))
     * }</pre>
     *
     */
    public Point midPoint(Point other)
    {
        return this.plus(other.minus(this).times(0.5));
    }

    /**
     * <p> The mid-point between
     * {@code p1}
     *  and
     * {@code p2}
     * .
     *
     * <pre>{@code
     * p1.midPoint(p2)
     * }</pre>
     *
     */
    public static Point mid(Point p1, Point p2)
    {
        return p1.midPoint(p2);
    }

    /**
     * <p> The mid-point between
     * {@code (0, 0)}
     *  and
     * {@code this}
     *
     * <pre>{@code
     * times(0.5)
     * }</pre>
     *
     */
    public Point centre()
    {
        return times(0.5);
    }

    /**
     * <p> The point
     * {@code (x, yValue)}
     *
     */
    public Point y(double yValue)
    {
        return Point.on(x, yValue);
    }

    /**
     * <p> The point
     * {@code (xValue, y)}
     *
     */
    public Point x(double xValue)
    {
        return Point.on(xValue, y);
    }

    /**
     * <p> {@code true}
     *  if
     * {@code x >= p.x && y >= p.y}
     *
     *
     */
    public boolean ge(Point p)
    {
        return x >= p.x && y >= p.y;
    }

    /**
     * {@code true}
     * if
     * {@code p.ge(this)}
     *
     */
    public boolean le(Point p)
    {
        return p.ge(this);
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  is to the left of and above
     * {@code p}
     * .
     *
     *
     * <p> ie
     * {@code x < p.x && y < p.y}
     *
     */
    public boolean lt(Point p)
    {
        return x < p.x && y < p.y;
    }

    /**
     * <p> A
     * {@link Rect}
     *  with corners
     * {@code this}
     *  and
     * {@code p}
     *
     */
    public Rect corner(Point p)
    {
        return Rect.on(min(x, p.x), min(y, p.y), abs(p.x - x), abs(p.y - y));
    }

    /**
     * <p> A
     *  {@link Rect}
     *  with
     * {@code NW}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     *
     * <pre>{@code
     * NW/this      NE
     * +------------+
     * |            |
     * |            |
     * |            |
     * +------------+
     * SW           SE
     * }</pre>
     *
     *
     */
    public Rect NW(Point size)
    {
        return Rect.originSize(this, size);
    }

    /**
     * <p> A
     * {@link Rect}
     * with
     * {@code NE}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     * <pre>{@code
     * NW           NE/this
     * +------------+
     * |            |
     * |            |
     * |            |
     * +------------+
     * SW           SE
     * }</pre>
     *
     */
    public Rect NE(Point size)
    {
        return Rect.on(x - size.x, y, size.x, size.y);
    }

    /**
     * <p> A
     * {@link Rect}
     * with
     * {@code SE}
     *  =
     * {@code this}
     *  and size
     * {@code size}
     * <pre>{@code
     * NW           NE
     * +------------+
     * |            |
     * |            |
     * |            |
     * +------------+
     * SW           SE/this
     * }</pre>
     *
     */
    public Rect SE(Point size)
    {
        return Rect.on(x - size.x, y - size.y, size.x, size.y);
    }

    /**
     * <p> A
     * {@link Rect}
     * with
     * {@code SE}
     *  =
     * {@code this}
     * and size
     * {@code size}
     * <pre>{@code
     * NW           NE
     * +------------+
     * |            |
     * |            |
     * |            |
     * |            |
     * |            |
     * +------------+
     * SW/this      SE
     * }</pre>
     *
     */
    public Rect SW(Point size)
    {
        return Rect.on(x, y - size.y, size.x, size.y);
    }

    /**
     * The point whose x is the same as
     * {@code this}
     * but is a distance
     * {@code delta}
     * "to the north" of this.
     *
     * <p>ie
     * {@code (x, y - delta)}
     */
    public Point north(double delta)
    {
        return Point.on(x, y - delta);
    }

    /**
     * The point whose x is the same as
     * {@code this}
     * but is a distance
     * {@code delta}
     * "to the south" of this.
     *
     * <p>ie
     * {@code (x, y - delta)}
     */
    public Point south(double delta)
    {
        return Point.on(x, y + delta);
    }

    /**
     * The point whose y is the same as
     * {@code this}
     * but is a distance
     * {@code delta}
     * "to the east" of this.
     *
     * <p>ie
     * {@code (x + delta, y)}
     */
    public Point east(double delta)
    {
        return Point.on(x + delta, y);
    }

    /**
     * The point whose y is the same as
     * {@code this}
     * but is a distance
     * {@code delta}
     * "to the west" of this.
     *
     * <p>ie
     * {@code (x - delta, y)}
     */
    public Point west(double delta)
    {
        return Point.on(x - delta, y);
    }

    /**
     * The point that is this scaled to have length 1
     * <p>ie
     * {@code this.times(1 / this.length())}
     */
    public Point normalise()
    {
        return this.times(1 / this.length());
    }

    /**
     * <p> The
     * <strong>dot product</strong>
     *  of
     * {@code this}
     *  and
     * {@code other}
     * <p>ie
     * {@code this.x * other.x + this.y * other.y)}
     */
    public double dot(Point other)
    {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * The
     *  {@link Rect}
     * that has NW corner
     * {@code (0, 0)}
     * and size
     * {@code this}
     *
     * <p>ie:
     * {@code Rect.size(this)}
     */
    public Rect toRect()
    {
        return Rect.size(this);
    }

    /**
     * <p> The pair
     * {@code (x, y)}
     */
    public ImPair<Double, Double> toPair()
    {
        return ImPair.on(x, y);
    }

    /**
     * The point
     *
     * {@code (x + xOff, y)}
     */
    public Point plusX(double xOff)
    {
        return Point.on(x + xOff, y);
    }

    /**
     * The point
     *
     * {@code (x, y + yOff)}
     */
    public Point plusY(double yOff)
    {
        return Point.on(x, y + yOff);
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
        return ImList.on(x, y);
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
        return ImList.on("x", "y");
    }

}