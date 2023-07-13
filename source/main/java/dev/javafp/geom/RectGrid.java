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
 * <p> A rectangular grid of rectangles
 */
public class RectGrid extends ImValuesImpl
{

    public double xSpace;
    public ImList<Double> widths;
    public double ySpace;
    public ImList<Double> heights;

    private final ImList<Double> xs;
    private final ImList<Double> ys;
    public final Point size;

    /**
     * <p> A class to make RectGrid's
     * <p> Used in expressions like this:
     * <p> RectGrid g = RectGrid.x(1, 2.0, 3.0).y(4, 5.0, 6.0);
     *
     */
    public static class RectGridMaker
    {
        private final double xSpace;
        private final ImList<Double> widths;

        public RectGridMaker(double xSpace, ImList<Double> widths)
        {
            this.xSpace = xSpace;
            this.widths = widths;
        }

        public RectGrid y(double ySpace, Double... heights)
        {
            return RectGrid.on(xSpace, widths, ySpace, ImList.on(heights));
        }
    }

    public static RectGrid on(double xSpace, ImList<Double> widths, double ySpace, ImList<Double> heights)
    {

        return new RectGrid(xSpace, widths, ySpace, heights);
    }

    private RectGrid(double xSpace, ImList<Double> widths, double ySpace, ImList<Double> heights)
    {
        this.xSpace = xSpace;
        this.widths = widths;
        this.ySpace = ySpace;
        this.heights = heights;

        xs = widths.scanl(0.0, (z, x) -> z + x);
        ys = heights.scanl(0.0, (z, y) -> z + y);

        size = Point.on(xs.last() + (widths.size() - 1) * xSpace, ys.last() + (heights.size() - 1) * ySpace);

    }

    public static RectGridMaker x(double xSpace, Double... widths)
    {
        return new RectGridMaker(xSpace, ImList.on(widths));
    }

    /**
     * <p> Get the rectangle at grid coordinates
     * {@code xStartingAtOne}
     * ,
     * {@code yStartingAtOne}
     *
     */
    public Rect at(int xStartingAtOne, int yStartingAtOne)
    {
        return Rect.on(
                xs.at(xStartingAtOne) + (xStartingAtOne - 1) * xSpace,
                ys.at(yStartingAtOne) + (yStartingAtOne - 1) * ySpace,
                widths.at(xStartingAtOne),
                heights.at(yStartingAtOne));
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(xSpace, widths, ySpace, heights);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("xSpace", "widths", "ySpace", "heights");
    }
}