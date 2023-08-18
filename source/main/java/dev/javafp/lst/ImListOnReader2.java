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

class ImListOnReader2 extends ImCachingLazyList<Character>
{

    private static final long serialVersionUID = 1L;

    private final BufferedReader reader;
    private final char character;

    private ImListOnReader2(BufferedReader reader, char c)
    {
        super(UNKNOWN_UNKNOWN);
        this.reader = reader;

        this.character = c;
    }

    static ImList<Character> on(BufferedReader reader)
    {
        try
        {
            int ch = reader.read();

            return ch == -1
                   ? ImList.on()
                   : new ImListOnReader2(reader, (char) ch);
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    @Override
    protected Character hd()
    {
        return character;
    }

    @Override
    protected ImList<Character> tl()
    {
        return ImListOnReader2.on(reader);
    }
}