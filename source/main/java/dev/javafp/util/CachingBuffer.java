/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.Throw;
import dev.javafp.queue.ImQueue;

/**
 * <p> A circular buffer that can be used to accumulate strings.
 */
public class CachingBuffer
{
    ImQueue<String> queue;

    public CachingBuffer(int size)
    {
        Throw.Exception.ifLessThan("size", size, 1);
        queue = ImQueue.ofSize(size);
    }

    /**
     * <p> Add string + "\n" to the buffer
     */
    public void println(String string)
    {
        add(string + "\n");
    }

    public void print(CharSequence cs)
    {
        add(cs.toString());
    }

    /**
     * <p> Add string to the buffer
     */
    private void add(String string)
    {
        queue = queue.addToEnd(string);
    }

    /**
     * <p> Get the buffer as a string
     */
    public String getString()
    {
        StringBuilder sb = new StringBuilder();

        for (String s : queue.toImList())
            sb.append(s);

        return sb.toString();
    }

    /**
     * <p> The number of elements in
     * {@code this}
     *
     */
    public int size()
    {
        return queue.size();
    }
}