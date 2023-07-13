/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.set.ImMap;
import dev.javafp.shelf.ImShelf;

/**
 * <p> We keep a map String ->[String] for the options
 * <p> For option sthat don't have a value we put a dummy value in the list
 *
 */
public class CommandLineArgs
{
    public final ImMap<String, ImShelf<String>> options;
    public final ImShelf<String> commands;

    private static final CommandLineArgs empty = new CommandLineArgs(ImMap.empty(), ImShelf.empty());

    public static CommandLineArgs empty()
    {
        return empty;
    }

    public CommandLineArgs(ImMap<String, ImShelf<String>> options, ImShelf<String> commands)
    {
        this.options = options;
        this.commands = commands;
    }

    public boolean hasOptionSet(String name)
    {
        return options.get(name) != null;
    }

    public CommandLineArgs addOption(String name)
    {
        return new CommandLineArgs(options.put(name, ImShelf.on("SET")), commands);
    }

    public CommandLineArgs addOption(String name, String value)
    {
        return new CommandLineArgs(options.updateValue(name, ImShelf.empty(), v -> v.adding(value)), commands);
    }

    public CommandLineArgs addCommand(String commandWord)
    {
        return new CommandLineArgs(options, commands.adding(commandWord));
    }
}