/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.eq;

/**
 * A "shortcut" for
 * {@link Equals#isEqual(Object, Object)}
 */
public interface Eq
{

    /**
     * <p> Return
     * {@code Equals.isEqual()}
     *
     * <p> applied to
     * {@code one}
     *  and
     * {@code two}
     *
     */
    public static <T> boolean uals(T one, T two)
    {
        // Diagnostic for when I moved Characters to codepoints
        //        boolean x = (one instanceof Character && two instanceof ImCodePoint) || (two instanceof Character && one instanceof ImCodePoint);
        //
        //        Throw.Exception.ifTrue(x, "You are trying to compare a Character and a ImCodePoint");
        return Equals.isEqual(one, two);
    }
}