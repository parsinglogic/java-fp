/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.lst.ImList;

import static dev.javafp.util.Say.say;

public class QuadraticSolver
{

    /**
     * <p> A class to find solutions for x where:
     * <p> a
     * <em>x</em>
     * x + b*x + c = 0
     * <p> If |a| is too small it ignores it
     *
     */
    public static ImList<Double> solve(double a, double b, double c)
    {
        double SMALL = 0.000000000000001;

        if (Math.abs(a) < SMALL)
        {
            // If a is small we have an overflow problem. If a is small we assume that it is zero and thus we have
            //
            //    b * x + c = 0
            //
            //    s = -c/b
            say("a is small");
            return ImList.on(-c / b);
        }
        else
        {

            double bSquared = b * b;

            double fourAc = 4 * a * c;

            if (bSquared < fourAc)
            {
                return ImList.on();
            }
            else
            {
                double squareRootOfbSquaredMinus4ac = Math.sqrt(bSquared - fourAc);

                double root1 = (-b + squareRootOfbSquaredMinus4ac) / (2 * a);
                double root2 = (-b - squareRootOfbSquaredMinus4ac) / (2 * a);

                if (Math.abs(root1 - root2) < SMALL)
                    return ImList.on(root1);
                else
                    return ImList.on(Math.min(root1, root2), Math.max(root1, root2));

            }

        }

    }
}