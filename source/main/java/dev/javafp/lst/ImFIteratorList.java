/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

/**
 * <p> Can't make this non public yet.
 */
class ImFIteratorList<A> extends ImCachingLazyList<A>
{

    private transient final FIterator<A> source;

    private ImFIteratorList(FIterator<A> source)
    {
        super(UNKNOWN_UNKNOWN);
        this.source = source;
    }

    public static <A> ImList<A> on(FIterator<A> source)
    {
        return new ImFIteratorList<>(source);
    }

    @Override
    protected A hd()
    {
        return source.get();
    }

    @Override
    protected ImList<A> tl()
    {
        return source.hasNext()
               ? new ImFIteratorList<>(source.next())
               : ImList.on();
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> If source is an infinite list and filterFn never returns true then I will loop
         */
        return resolveSize();
    }

}