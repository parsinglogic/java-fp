/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

/**
 * <p> the definition of a commandlline option
 */
public class OptionDefinition
{

    public enum Optional { yes, no};
    public enum HasValue { yes, no};
    public enum IsSingle { yes, no};

    // Name including the - at the start
    public final String name;

    // true if this option is ... er ... optional
    public final boolean isOptional;

    // true if the option has a value following it
    public final boolean hasAValue;

    // true if the option can only appear once
    public final boolean isSingle;

    public OptionDefinition(String name, boolean isOptional, boolean hasAValue, boolean isSingle)
    {
        this.name = name;
        this.isOptional = isOptional;
        this.hasAValue = hasAValue;
        this.isSingle = isSingle;
    }
}