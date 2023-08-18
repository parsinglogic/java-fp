/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.FunctionNotAllowedOnEmptyList;

/**
 * <p> For lists like ImMappedList that calculate their values by running functions we have a problem
 * <p> Consider this:
 *
 * <pre>{@code
 * ImList<Wibble> wibbles = ImList.on(1,2,3).map(i -> new Wibble(i))
 * }</pre>
 * <p> Because the function is run each time head() is called we have:
 *
 * <pre>{@code
 * assertSame(wibbles.head(), wibbles.head())
 * }</pre>
 * <p> throwing an exception
 * <p> ie
 * wibbles.head() != wibbles.head()
 * <p> Each time we call head() it creates a new Wibble()
 * <p> This leads to problems.
 * <p> Also - you might expect this code:
 *
 * <pre>{@code
 * x = foo.permutations()
 *
 * x.size();
 * x.size();
 * }</pre>
 * <p> to do less work than this code:
 *
 * <pre>{@code
 * foo.permutations().size();
 * foo.permutations().size();
 * }</pre>
 * <p> But if we don't cache, the permutations code is called twice in both cases.
 * <p> Also - some lists are built on iterators. These lists will give the wrong results unless we cache.
 * <p> For these reasons, we have this class so that these classes can cache their values
 *
 */
abstract class ImCachingLazyList<B> extends ImLazyList<B>
{

    private B headCache;
    private ImList<B> tailCache;

    private boolean isHeadCached = false;
    private boolean isTailCached = false;

    protected abstract B hd();

    protected abstract ImList<B> tl();

    protected ImCachingLazyList(int size)
    {
        super(size);
    }

    @Override
    public final B head()
    {
        if (isHeadCached)
            return headCache;
        else
        {
            headCache = hd();
            isHeadCached = true;
            return headCache;
        }

    }

    /**
     * `this` without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    @Override
    public final ImList<B> tail()
    {
        if (isTailCached)
            return tailCache;
        else
        {
            tailCache = tl();
            isTailCached = true;
            return tailCache;
        }
    }

}