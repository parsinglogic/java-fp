/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.geom.Point;

import java.io.Serializable;

/**
 * <p> transform="matrix(a,b,c,d,e,f)"
 * <p> The matrix is this - using homogeneous coordinates:
 *
 * <pre>{@code
 * a c e
 * b d f
 * 0 0 1
 * }</pre>
 * <p> <a href="http://www.mat.ucsb.edu/594cm/2010/Week1/homog-coords.pdf"  ></a>
 * <a href="www.cs.iastate.edu/~cs577/handouts/homogeneous-transform.pdf"  ></a>
 * <p> Applying a matrix to a point
 *
 * <pre>{@code
 *            x
 *            y
 *            1
 *
 * a c e      ax + cy + e
 * b d f      bx + dy + f
 * 0 0 1      1
 * }</pre>
 * <p> translation by (e,f)
 *
 * <pre>{@code
 * 1 0 e      x + e
 * 0 1 f      y + f
 * 0 0 1      1
 * }</pre>
 * <p> scaling by a in x and d in y
 *
 * <pre>{@code
 * a 0 0       ax
 * 0 d 0       by
 * 0 0 1       1
 * }</pre>
 * <p> Rotation about the origin by alpha anticlockwise:
 *
 * <pre>{@code
 *              x
 *              y
 *              1
 *
 * cos -sin 0   x*cos - x*sin
 * sin  cos 0   x*sin + x*cos
 * 0    0   1   1
 * }</pre>
 * <p> where cos = cos(alpha), sin = sin(alpha)
 *
 */
public class SvgTransform implements Serializable
{

    private static final long serialVersionUID = 1L;

    public final double a;
    public final double b;
    public final double c;
    public final double d;
    public final double e;
    public final double f;

    public SvgTransform(double a, double b, double c, double d, double e, double f)
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public static SvgTransform move(double x, double y)
    {
        return new SvgTransform(1, 0, 0, 1, x, y);
    }

    public static SvgTransform move(Point p)
    {
        return move(p.x, p.y);
    }

    public static SvgTransform identity()
    {
        return new SvgTransform(1, 0, 0, 1, 0, 0);
    }

    /**
     * <p> Pre-multiply this transform by t:
     *
     * <pre>{@code
     *                 a                  c                  e
     *                 b                  d                  f
     *                 0                  0                  1
     *
     * t.a  t.c  t.e   t.a * a + t.c * b  t.a * c + t.c * d  t.a * e + t.c * f + t.e
     * t.b  t.d  t.f   t.b * a + t.d * b  t.b * c + t.d * d  t.b * e + t.d * f + t.f
     * 0    0    1     0                  0                  1
     * }</pre>
     *
     */
    public SvgTransform preMultiplyBy(SvgTransform t)
    {
        return new SvgTransform(//
                t.a * a + t.c * b, //
                t.b * a + t.d * b, //
                t.a * c + t.c * d, //
                t.b * c + t.d * d, //
                t.a * e + t.c * f + t.e, //
                t.b * e + t.d * f + t.f);
    }

    /**
     * <p> Apply the transform to a point
     * {@code p}
     *  where
     * {@code p = (x, y, z)}
     *
     * <pre>{@code
     *               x
     *               y
     *               1
     *
     * a    c    e   ax + cy + e
     * b    d    f   bx + dy + f
     * 0    0    1   1
     * }</pre>
     *
     */
    public Point applyTo(Point p)
    {
        return Point.on(a * p.x + c * p.y + e, b * p.x + d * p.y + f);
    }

    public SvgTransform postMultiplyBy(SvgTransform t)
    {
        return t.preMultiplyBy(this);
    }

    @Override
    public boolean equals(Object other)
    {
        return this == other
               ? true
               : other instanceof SvgTransform
                 ? eq((SvgTransform) other)
                 : false;
    }

    private boolean eq(SvgTransform t)
    {
        return a == t.a && b == t.b && c == t.c && d == t.d && e == t.e && f == t.f;
    }

    @Override
    public int hashCode()
    {
        return Hash.hash(a, b, c, d, e, f);
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return "" + pp(a) + "," + pp(b) + "," + pp(c) + "," + pp(d) + "," + pp(e) + "," + pp(f);
    }

    private static String pp(double d)
    {
        return TextUtils.prettyPrint(d);
    }

    public String getSvgString()
    {
        return "matrix(" + toString() + ")";
    }

    /**
     * <p> Take the
     * {@code SVG}
     *  string
     * {@code svgString}
     *  in the form of:
     * {@code matrix(a,b,c,d,e,f)}
     *  and make an
     * {@code SVG}
     *  transform like this:
     *
     * <pre>{@code
     * a c e
     * b d f
     * 0 0 1
     * }</pre>
     *
     */
    public static SvgTransform fromSvgString(String svgString)
    {
        String[] split = svgString.split(",");

        double a = Double.valueOf(split[0].substring(7));

        double b = Double.valueOf(split[1]);
        double c = Double.valueOf(split[2]);
        double d = Double.valueOf(split[3]);
        double e = Double.valueOf(split[4]);
        double f = Double.valueOf(split[5].substring(0, split[5].length() - 1));
        return new SvgTransform(a, b, c, d, e, f);
    }

    public static SvgTransform rotate(double degrees)
    {
        return null;
    }

    public SvgTransform multiply(SvgTransform s)
    {
        return this.postMultiplyBy(s);
    }

    /**
     * <p> If the transform is only a scale and shift then the inverse is easy-peasy-lemon-squeazy
     */
    public SvgTransform inverse()
    {
        return new SvgTransform(1 / a, b, c, 1 / d, -e / a, -f / d);
    }

    public static SvgTransform scale(double xScale, double yScale)
    {
        return new SvgTransform(xScale, 0, 0, yScale, 0, 0);
    }

    public static SvgTransform scale(double scaleValue)
    {
        return scale(scaleValue, scaleValue);
    }

    public Point getMove()
    {
        return new Point(e, f);
    }
}