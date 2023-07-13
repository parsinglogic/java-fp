/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.file;

import dev.javafp.ex.UnexpectedChecked;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

public class LineReader implements Closeable
{

    private final BufferedReader reader;

    private String buffer = null;

    public LineReader(BufferedReader reader)
    {
        this.reader = reader;
    }

    public String readLine()
    {
        try
        {
            return buffer == null
                   ? readLineInternal()
                   : buffer;
        } finally
        {
            buffer = null;
        }
    }

    private String readLineInternal()
    {
        try
        {
            return reader.readLine();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public void close()
    {
        try
        {
            reader.close();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public static LineReader on(BufferedReader reader)
    {
        return new LineReader(reader);
    }

    public void pushLine(String aLine)
    {
        buffer = aLine;
    }
}