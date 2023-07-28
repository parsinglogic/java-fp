/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.queue;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImMaybe;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A queue
 */
public class ImQueue<A> extends ImValuesImpl
{
    // The list that provides the head of the queue
    private final ImList<A> one;

    // The list that is used to add elements to the queue
    private final ImList<A> two;

    public final int maxSize;

    private static ImQueue<?> empty = new ImQueue<>(0, ImList.on(), ImList.on());

    protected ImQueue(int maxSize, ImList<A> one, ImList<A> two)
    {
        this.one = one;
        this.two = two;
        this.maxSize = maxSize;
    }

    public static <A> ImQueue<A> ofSize(int maxSize)
    {
        return new ImQueue<>(maxSize, ImList.on(), ImList.on());
    }

    private static <A> ImQueue<A> onTwoLists(int maxSize, ImList<A> one, ImList<A> two)
    {
        return new ImQueue<>(maxSize, one, two);
    }

    /**
     * Add `thing` to the end of the queue
     */

    public ImQueue<A> addToEnd(A thing)
    {
        // If we are full, remove the head and add `thing`
        return size() == maxSize
               ? removeFirst().get().addToEnd(thing)
               : ImQueue.onTwoLists(maxSize, one, two.push(thing));
    }

    /**
     * The last element in the queue in a `ImMaybe` or `Nothing` if no such element exists
     */
    public ImMaybe<A> last()
    {
        return isEmpty()
               ? ImMaybe.nothing()
               : ImMaybe.just(two.isEmpty()
                              ? one.last()
                              : two.head());
    }

    /**
     * The first element in the queue  in a `ImMaybe` or `Nothing` if no such element exists
     */
    public ImMaybe<A> first()
    {
        return isEmpty()
               ? ImMaybe.nothing()
               : ImMaybe.just(one.isEmpty()
                              ? two.last()
                              : one.head());
    }

    /**
     * <p> The number of elements in
     * {@code this}
     *
     */
    public int size()
    {
        return one.size() + two.size();
    }

    /**
     * A pair of the first element and the queue with the first element removed in a `ImMaybe` or `Nothing` if the queue is empty
     */
    public ImMaybe<ImPair<A, ImQueue<A>>> split()
    {
        return isEmpty()
               ? ImMaybe.nothing()
               : ImMaybe.just(one.isEmpty()
                              ? ImPair.on(two.last(), onTwoLists(maxSize, two.reverse().tail(), ImList.on()))
                              : ImPair.on(one.head(), onTwoLists(maxSize, one.tail(), two)));
    }

    public static <A> ImQueue<A> on(ImList<A> as)
    {
        return onTwoLists(Integer.MAX_VALUE, as, ImList.on());
    }

    @SafeVarargs
    public static <A> ImQueue<A> on(A... as)
    {
        return on(ImList.on(as));
    }

    @SafeVarargs
    public static <A> ImQueue<A> onSize(int maxSize, A... as)
    {
        return onTwoLists(maxSize, ImList.on(as).take(maxSize), ImList.on());
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public ImList<A> toImList()
    {
        return one.append(two.reverse());
    }

    public ImMaybe<ImQueue<A>> removeFirst()
    {
        return split().flatMap(i -> ImMaybe.just(i.snd));
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(maxSize, one, two);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("maxSize", "one", "two");
    }

}