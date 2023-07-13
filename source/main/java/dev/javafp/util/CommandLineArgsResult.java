/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.lst.ImList;

public class CommandLineArgsResult
{

    private final CommandLineArgs args;
    private final CommandLineArgsParser parser;
    public final ImList<String> commands;

    public CommandLineArgsResult(CommandLineArgs args, CommandLineArgsParser parser)
    {
        this.args = args;
        this.commands = args.commands.toImList();
        this.parser = parser;
    }

    public static CommandLineArgsResult on(CommandLineArgs args, CommandLineArgsParser parser)
    {
        return new CommandLineArgsResult(args, parser);
    }

    public boolean hasOptionSet(String optionNameIncludingDash)
    {
        return args.hasOptionSet(optionNameIncludingDash);
    }

    public ImList<String> getOption(String optionNameIncludingDash)
    {
        try
        {
            return args.options.get(optionNameIncludingDash).toImList();
        } catch (Exception e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public String getSingleOption(String optionNameIncludingDash)
    {
        try
        {
            return args.options.get(optionNameIncludingDash).toImList().head();
        } catch (Exception e)
        {
            throw new UnexpectedChecked(e);
        }
    }
}