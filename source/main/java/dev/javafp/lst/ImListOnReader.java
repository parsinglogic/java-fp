/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.ex.UnexpectedChecked;

import java.io.BufferedReader;
import java.io.IOException;

class ImListOnReader extends ImCachingLazyList<String>
{

    private static final long serialVersionUID = 1L;

    private final BufferedReader reader;
    private final String line;

    private ImListOnReader(BufferedReader reader, String line)
    {
        super(UNKNOWN_UNKNOWN);
        this.reader = reader;

        this.line = line;
    }

    /**
     * <p> To preserve the empty list singleton I have to declare this function like this since if the string is empty I want
     * to return the empty list
     * <p> a single character -> [ "a" ]
     * Empty file         -> [ "" ] or [ ] ?
     * <p> If we write a list of strings to a file then I guess we add a nl to each line - so [ "" ] would get written and then read as
     * <p> [ "" ]
     *
     */
    static ImList<String> on(BufferedReader reader)
    {
        String line = getLine(reader);

        return line == null
               ? ImList.on()
               : new ImListOnReader(reader, line);
    }

    private static String getLine(BufferedReader reader)
    {
        try
        {
            return reader.readLine();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }

    }

    @Override
    protected String hd()
    {
        return line;
    }

    @Override
    protected ImList<String> tl()
    {
        return ImListOnReader.on(reader);
    }
}