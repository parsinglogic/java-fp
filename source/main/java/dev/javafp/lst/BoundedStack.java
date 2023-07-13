/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.IllegalState;

/**
 * <p> A stack that has a maximum capacity.
 * <p> If we push an object onto the stack that would make its size > the capacity then we drop the oldest one
 * <p> We store two lists. If the capacity is 2:
 * <p> empty         []     []
 * push 1        [1]    []
 * push 2        [2, 1] []
 * push 3        [3]    [2, 1]
 * push 4        [4, 3] [2, 1]
 * push 5        [5]    [4, 3]
 * <p> We rely on the fact that taking elements from a list is not expensive
 *
 */
public class BoundedStack<A>
{

    public final int capacity;
    public final int size;
    public final ImList<A> one;
    public final ImList<A> two;

    private BoundedStack(int capacity, int size, ImList<A> one, ImList<A> two)
    {
        this.capacity = capacity;
        this.size = size;

        this.one = one;
        this.two = two;
    }

    public BoundedStack(int capacity)
    {
        this(capacity, 0, ImList.on(), ImList.on());
    }

    public BoundedStack<A> push(A a)
    {
        return one.size() < capacity
               ? new BoundedStack<>(capacity, size + 1, one.withHead(a), two)
               : new BoundedStack<>(capacity, size + 1, ImList.on(a), one);
    }

    public BoundedStack<A> pop(A a)
    {
        if (size == 0)
            throw new IllegalState("The stack is empty");
        else
            return one.isEmpty()
                   ? new BoundedStack<>(capacity, size - 1, two.tail(), ImList.on())
                   : new BoundedStack<>(capacity, size - 1, one.tail(), two);
    }

    public ImList<A> peek(int count)
    {
        return count > one.size()
               ? one.append(two.take(count - one.size()))
               : one.take(count);
    }
}