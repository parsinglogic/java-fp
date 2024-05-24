/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

/**
 * <p> Hmm - how do I explain this puppy?
 * <p> It is a helper for generateListsByInjecting. It generates a list of lists.
 * <p> It is a lazy list of lists.
 *
 * <pre>{@code
 * [1, 2, 3].generateListsByInjecting(0) =>  [[0, 1, 2, 3], [1, 0, 2, 3], [1, 2, 0, 3], [1, 2, 3, 0]]
 * }</pre>
 * <p> The implementation is a little fiddly.
 *
 * <pre>{@code
 * list.generateListsByInjecting(thing)
 * }</pre>
 *
 *  is implemented as
 *
 * <pre>{@code
 * ip([], thing, list)
 * }</pre>
 *
 * <p> Here is the constructor:
 *
 * <pre>{@code
 * ImIpList(ImList<A> one, A thing, ImList<A> two)
 * }</pre>
 * <p> It has two lists - one and two. The list one starts off as an empty list. At each recursive step, the head is
 * list one with thing appended and two appended to that.
 * <p> the tail is a new ImIpList with
 *
 * <pre>{@code
 * one = one ++ [head(two)]
 * two = tail two
 * }</pre>
 *
 * <pre>{@code
 * one       thing   two
 * []        *       [1, 2, 3]
 * [1]       *       [2, 3]
 * [1, 2]    *       [3]
 * [1, 2, 3] *       []
 * }</pre>
 * <p> The original, eager algorithm:
 *
 * <pre>{@code
 *    return two.isEmpty()
 *            ? on(one.append(thing))
 *            : cons(one.append(thing.append(two)), ip(one.append(on(two.head())), thing, two.tail()));
 * }</pre>
 *
 */
class ImIpList<A> extends ImCachingLazyList<ImList<A>>
{
    private final ImList<A> one;
    private final A thing;
    private final ImList<A> two;

    protected ImIpList(ImList<A> one, A thing, ImList<A> two)
    {
        super(Sz.addOne(Sz.getSz(two)));
        this.one = one;
        this.thing = thing;
        this.two = two;
    }

    @Override
    protected ImList<A> hd()
    {
        //return one.append(thingList.append(two));
        return one.append(two.withHead(thing));
    }

    @Override
    protected ImList<ImList<A>> tl()
    {
        return two.isEmpty()
               ? ImList.on()
               : new ImIpList<A>(one.append(ImList.on(two.head())), thing, two.tail());
    }

    @Override
    protected int calculateSize()
    {
        /**
         * <p> If two is an infinite list and filterFn never returns true then I will loop
         */
        return resolveSize();
    }

}