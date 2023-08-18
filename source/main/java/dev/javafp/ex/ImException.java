/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import dev.javafp.lst.ImList;

/**
 * <p> A {@link RuntimeException} with some extra features.
 */
public class ImException extends RuntimeException
{

    /**
     * A vanilla exception. Not intended to be used directly.
     */
    public ImException()
    {
        super();
    }

    /**
     * An exception with message
     * {@code message}
     */
    public ImException(String message)
    {
        super(message);
    }

    /**
     * <p> An exception with message created by:
     *
     * <pre>{@code
     * ImList.on(ss).toString(' ')
     * }</pre>
     *
     */
    public ImException(Object... ss)
    {
        super(ImList.on(ss).toString(' '));
    }

    /**
     * An exception with nested exception
     * {@code e}
     */
    public ImException(Exception e)
    {
        super(e);
    }

    /**
     * An exception with message
     * {@code message}
     * and nested exception
     * {@code e}
     */
    public ImException(String message, Exception e)
    {
        super(message, e);
    }
}