/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.NullValue;
import dev.javafp.ex.UnexpectedChecked;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * <p>Some utility functions
 */
public class ImUtils
{

    /**
     * <p> Checks that the argument
     * <code>
     * value
     * </code>
     *  is not
     * <code>
     * null
     * </code>
     * , throwing a
     * <code>
     * NullArgumentException
     * </code>
     *  using
     * <code>
     * name
     * </code>
     *  as name of the argument
     * that was null like this:
     * <code>
     * org.apache.commons.lang.NullArgumentException: fred must not be null.
     * </code>
     * @param name the name of the argument (will show up in the exception stack trace)
     * @param value the value to check
     *
     */
    public static void checkArgumentNotNull(String name, Object value)
    {
        if (value == null)
        {
            throw new NullValue(name);
        }
    }

    public static String unescapeXml(String s)
    {
        String result = s.replaceAll("&quot;", "\"");
        result = result.replaceAll("&apos;", "'");
        result = result.replaceAll("&lt;", "<");
        result = result.replaceAll("&gt;", ">");
        result = result.replaceAll("&amp;", "&");
        return result;
    }

    /**
     * The XML escaped form of
     * {@code s}
     *
     */
    public static String escapeXml(String s)
    {
        String result = s.replaceAll("&", "&amp;");
        result = result.replaceAll("'", "&apos;");
        result = result.replaceAll("\"", "&quot;");
        result = result.replaceAll("<", "&lt;");
        result = result.replaceAll(">", "&gt;");
        return result;
    }

    public static void flush(Writer out)
    {
        try
        {
            out.flush();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public static String getStackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.getBuffer().toString();
    }

}