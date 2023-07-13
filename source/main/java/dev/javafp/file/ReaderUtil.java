/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.file;

import dev.javafp.ex.UnexpectedChecked;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <p> A utility class to read a String from an InputStream
 */

public class ReaderUtil
{
    private static final int BUFFER_SIZE = 16_000;

    public static String read(InputStream in)
    {
        char[] buffer = new char[BUFFER_SIZE];

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
        {
            StringBuilder sb = new StringBuilder();

            while (true)
            {
                int readCount = br.read(buffer, 0, buffer.length);

                if (readCount == -1)
                    return sb.toString();
                else
                    sb.append(buffer, 0, readCount);
            }

        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }
}