/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.func.Fn2;

class ImZipWithList<A, B, C> extends ImCachingLazyList<C>
{

    private final ImList<A> listOne;
    private final ImList<B> listTwo;
    private final Fn2<A, B, C> fn;

    private ImZipWithList(ImList<A> listOne, ImList<B> listTwo, Fn2<A, B, C> fn)
    {
        super(Sz.zipWith(listOne.getSz(), listTwo.getSz()));
        this.listOne = listOne;
        this.listTwo = listTwo;
        this.fn = fn;
    }

    public static <A, B, C> ImList<C> on(ImList<? extends A> listOne, ImList<? extends B> listTwo, Fn2<A, B, C> fn)
    {
        return listOne.isEmpty() || listTwo.isEmpty()
               ? ImList.on()
               : new ImZipWithList(listOne, listTwo, fn);
    }

    @Override
    protected C hd()
    {
        return fn.of(listOne.head(), listTwo.head());
    }

    @Override
    protected ImList<C> tl()
    {
        return on(listOne.tail(), listTwo.tail(), fn);
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> The only reason this function will be run is if we are UU or UF.
         * <p> This means that source is UU or UF
         * <p> If both are UF we can take a short cut otherwise we resolve
         *
         */
        return listOne.getSz() == UNKNOWN_FINITE && listTwo.getSz() == UNKNOWN_FINITE
               ? Math.min(listOne.size(), listTwo.size())
               : resolveSize();
    }

}