/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.FunctionNotAllowedOnEmptyList;

class ImTailsList<A> extends ImLazyList<ImList<A>>
{
    private final ImList<A> source;

    private ImTailsList(ImList<A> source)
    {
        super(Sz.addOne(source.getSz()));
        this.source = source;
    }

    public static <A> ImList<ImList<A>> on(ImList<A> source)
    {
        return source.isEmpty()
               ? ImList.on(ImList.on())
               : new ImTailsList(source);
    }

    @Override
    public ImList<A> head()
    {
        return source;
    }

    /**
     * `this` without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public ImList<ImList<A>> tail()
    {
        return source.isEmpty()
               ? ImList.on()
               : on(source.tail());
    }

    @Override
    protected int calculateSize()
    {
        return 1 + source.size();
    }

}