/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import dev.javafp.tuple.ImTriple;

/**
 * <p> Created by aove215 on 14/12/15.
 */
public class LineEquation
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

    public double applyTo(Point p)
    {
        return a * p.x + b * p.y + c;
    }

    public ImTriple<Double, Double, Double> intersection(LineEquation other)
    {
        // get the intersection point

        /**
         * <p> | 1        1        1       |
         * | this.a   this.b   this.c  |
         * | other.a  other.b  other.c |
         *
         */

        double x = this.b * other.c - other.b * this.c;
        double y = other.a * this.c - this.a * other.c;
        double h = this.a * other.b - other.a * this.b;

        return ImTriple.on(x, y, h);
    }
}