/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import dev.javafp.file.FileUtil;
import dev.javafp.lst.ImList;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.nio.file.Path;

/**
 * <p> Exceptions relating to problems with files
 */
public class FileProblem extends RuntimeException
{
    public FileProblem(String message)
    {
        super(message);
    }

    public static FileProblem create(Path path, String message)
    {
        String first = "Expected " + TextUtils.quote(path.toString()) + " " + message;

        return new FileProblem(TextUtils.join(getInfo(path).push(first), "\n", "\n", ""));
    }

    public static ImList<String> getInfo(Path path)
    {
        ImMaybe<Path> real = FileUtil.getRealPath(path);

        String lineThree = real.isPresent()
                           ? "Real path     = " + TextUtils.quote(real.get())
                           : "Real path     = (path does not exist)";

        return ImList.on(
                "Path          = " + TextUtils.quote(path.toString()),
                "Absolute path = " + TextUtils.quote(path.toAbsolutePath().normalize()),
                lineThree
        );

    }
}