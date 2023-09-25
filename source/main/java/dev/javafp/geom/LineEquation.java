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
 * <p> A representation of an infinite straight line in 2 dimensions,
 * {@code ax + by + c = 0}
 *
 */
public class LineEquation extends ImValuesImpl
{
    public final double a;
    public final double b;
    public final double c;

    public LineEquation(double a, double b, double c)
    {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    /**
     * A line equation with values `a`, `b`, `c`
     */
    public static LineEquation on(double a, double b, double c)
    {
        return new LineEquation(a, b, c);
    }

    /**
     *
     * <p> Plug the
     * {@code x}
     *  and
     * {@code y}
     *  values of
     * {@code p}
     *  into the line equation to get a value that is the
     * {@code d * sqrt(a*a + b*b)}
     *  where
     * {@code d}
     *  is the distance from the point to the line.
     *
     *
     * @see <a href="https://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html">mathworld.wolfram.com</a>
     */
    public double applyTo(Point p)
    {
        return Math.abs(a * p.x + b * p.y + c);
    }

    /**
     *
     * <p> Get the intersection point of
     * {@code this}
     *  and
     * {@code other}
     *
     * <p> The point returned in a triple,
     * {@code (x, y, h)}
     *  is in homogeneous coordinates.
     * <p> If
     * {@code h == 0}
     *  then the lines do not intersect.
     *
     *
     */
    public ImTriple<Double, Double, Double> intersection(LineEquation other)
    {
        double x = this.b * other.c - other.b * this.c;
        double y = other.a * this.c - this.a * other.c;
        double h = this.a * other.b - other.a * this.b;

        return ImTriple.on(x, y, h);
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
        return ImList.on(a, b, c);
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
        return ImList.on("a", "b", "c");
    }
}