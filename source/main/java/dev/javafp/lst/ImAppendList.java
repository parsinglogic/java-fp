/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

/**
 * <p> A list formed of two lists, concatenated
 */
class ImAppendList<A> extends ImLazyList<A>
{

    private final ImList<A> one;
    private final ImList<A> two;

    private ImAppendList(ImList<A> one, ImList<A> two)
    {
        super(Sz.append(one.getSz(), two.getSz()));
        this.one = one;
        this.two = two;

    }

    public static <A> ImList<A> on(ImList<A> one, ImList<A> two)
    {
        return one.isEmpty()
               ? two
               : two.isEmpty()
                 ? one
                 : new ImAppendList(one, two);
    }

    @Override
    public A head()
    {
        return one.head();
    }

    @Override
    public ImList<A> tail()
    {
        return on(one.tail(), two);
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> If one or two are infinite then we want to blow up so we just call size on them
         */
        return one.size() + two.size();
    }

}