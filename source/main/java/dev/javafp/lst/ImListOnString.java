/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.eq.Eq;
import dev.javafp.ex.FunctionNotAllowedOnEmptyList;
import dev.javafp.ex.Throw;

class ImListOnString extends ImEagerList<Character> implements Eq
{
    private final String source;
    private final int skipCount;

    private ImListOnString(String source, int skipCount, int size)
    {
        super(size);
        this.source = source;
        this.skipCount = skipCount;
    }

    static ImList<Character> on(String source)
    {
        return on(source, 0, source.length());
    }

    static ImList<Character> on(String source, int skipCount, int size)
    {
        return size == 0
               ? ImList.empty()
               : new ImListOnString(source, skipCount, size);
    }

    @Override
    public Character head()
    {
        return source.charAt(skipCount);
    }

    /**
     * `this` without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public ImList<Character> tail()
    {
        return on(source, skipCount + 1, size() - 1);
    }

    /**
     * <p> So this is a fast way (I hope) to drop elements
     */
    @Override
    public ImList<Character> drop(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);
        return count == 0
               ? this
               : count >= size()
                 ? ImList.on()
                 : on(source, skipCount + count, size - count);
    }

    /**
     * <p> So this is a fast way (I hope) to drop elements
     */
    @Override
    public ImList<Character> take(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);
        return count == 0
               ? ImList.on()
               : count >= size()
                 ? this
                 : on(source, skipCount, count);
    }
}