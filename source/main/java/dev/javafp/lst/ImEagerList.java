/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

abstract class ImEagerList<A> implements ImList<A>
{

    final int size;

    ImEagerList(int size)
    {
        this.size = size;
    }

    /**
     * <p> The number of elements in
     * {@code this}
     *
     */
    @Override
    public int size()
    {
        return size;
    }

    public int getSz()
    {
        return size;
    }

    @Override
    public int resolveSize()
    {
        return size;
    }

    /**
     * <p> {@code true}
     *  iff
     * {@code this}
     * equals
     * {@code other}
     *
     * <p> Equality for lists means that both lists have the same size and the
     * {@code i}
     * th element of
     * {@code this}
     *  equals the
     * {@code i}
     * th element of
     * {@code other}
     *
     */
    @Override
    public boolean equals(Object other)
    {
        return other instanceof ImList
               ? equalsList((ImList<A>) other)
               : false;
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return toS();
    }

    /**
     * <p> When serialising, we turn every ImList into a ImListOnArray
     */
    protected Object writeReplace()
    {
        return ImList.on(toArray(Object.class));
    }

    /**
     * <p> Get the hash of the first few elements
     */
    public int hashCode()
    {
        return hashCode(10);
    }

}