/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.FunctionNotAllowedOnEmptyList;

class ImTakeList<A> extends ImLazyList<A>
{

    private final ImList<A> source;
    private final int count;

    private ImTakeList(ImList<A> sourceNonEmpty, int countStartingAtOne)
    {
        super(Sz.take(sourceNonEmpty.getSz(), countStartingAtOne));
        this.source = sourceNonEmpty;
        this.count = countStartingAtOne;
    }

    public static <A> ImList<A> on(ImList<A> source, int count)
    {
        return count <= 0 || source.isEmpty()
               ? ImList.on()
               : new ImTakeList<>(source, count);
    }

    /**
     * The first element in
     * {@code this}
     * .
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public A head()
    {
        return source.head();
    }

    /**
     * `this` without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public ImList<A> tail()
    {
        // We must avoid calling tail() on source if it is empty
        return count <= 1 || source.isEmpty()
               ? ImList.on()
               : on(source.tail(), count - 1);
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> The only reason this function will be run is if we are UU or UF.
         * <p> This means that source is UU or UF
         * <p> In either case, resolving this will generate a finite list
         *
         */
        return source.getSz() == UNKNOWN_UNKNOWN
               ? resolveSize()
               : Math.min(count, source.size());
    }
}