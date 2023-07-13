/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.geom;

import static dev.javafp.geom.Orient1.Bottom;
import static dev.javafp.geom.Orient1.Centre;
import static dev.javafp.geom.Orient1.Left;
import static dev.javafp.geom.Orient1.Right;
import static dev.javafp.geom.Orient1.Top;

public enum Orient2
{
    LeftTop(Left, Top),
    LeftCentre(Left, Centre),
    LeftBottom(Left, Bottom),
    LeftFill(Left, Bottom),

    CentreTop(Centre, Top),
    CentreCentre(Centre, Centre),
    CentreBottom(Centre, Bottom),

    RightTop(Right, Top),
    RightCentre(Right, Centre),
    RightBottom(Right, Bottom);

    public final Orient1 ox;
    public final Orient1 oy;

    Orient2(Orient1 ox, Orient1 oy)
    {
        this.ox = ox;
        this.oy = oy;
    }

}