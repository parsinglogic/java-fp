/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

public class ClassUtils
{

    public static String simpleName(Class<?> clazz)
    {
        String[] names = clazz.getName().split("\\.");

        return names[names.length - 1];
    }

    public static String shortClassName(Object object)
    {
        return object == null ? "null" : simpleName(object.getClass());
    }

    public static String simpleNameOf(Object object)
    {
        return simpleName(object.getClass());
    }

    //    public static ImMaybe<ImList<String>> getResource(String name)
    //    {
    //
    //        InputStream is = AppVersion.class.getResourceAsStream(name);
    //
    //        if (is == null)
    //            return ImMaybe.nothing;
    //        else
    //        {
    //            try (Reader reader = new InputStreamReader(is))
    //            {
    //                // We need to flush so that when the reader is closed, we don't have a problem
    //                return ImMaybe.just(ImList.on(reader).flush());
    //            } catch (IOException e)
    //            {
    //                throw new UnexpectedChecked(e);
    //            }
    //        }
    //
    //    }
}