/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

class ImJoinList<A> extends ImLazyList<A>
{

    private final ImList<A> hd;
    private final ImList<ImList<A>> tl;

    private ImJoinList(ImList<ImList<A>> nonEmpty)
    {
        super(Sz.join(nonEmpty));
        this.hd = nonEmpty.head();
        this.tl = nonEmpty.tail();
    }

    public static <A> ImList<A> on(ImList<ImList<A>> l)
    {
        return l.isEmpty()
               ? ImList.on()
               : l.head().isEmpty()
                 ? on((l.tail()))
                 : new ImJoinList<>(l);
    }

    @Override
    public A head()
    {
        return hd.head();
    }

    @Override
    public ImList<A> tail()
    {
        return on(tl.withHead(hd.tail()));
    }

    @Override
    protected int calculateSize()
    {
        return hd.size() + tl.foldl(0, (z, l) -> z + l.size());
    }

}