/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;

/**
 * <p> Command line args parser
 * <p> We assume some stuff
 * <p> An option can't have a value that starts with a -
 * <p> prog -a -b foo bar -c wibble bing
 * <p> commands are bar bing (if -b has a value)
 * or foo bar bing if -b does not have a value
 *
 */
public class CommandLineArgsParser
{

    private final ImList<OptionDefinition> optionDefinitions;

    public CommandLineArgsParser(ImList<OptionDefinition> optionDefinitions)
    {
        this.optionDefinitions = optionDefinitions;
    }

    public Chat<CommandLineArgsResult> parse0(ImList<String> args)
    {
        Chat<CommandLineArgs> res = parse(CommandLineArgs.empty(), args);

        if (res.isOk)
        {
            return Chat.Right(CommandLineArgsResult.on(res.rightOrThrow(), this));
        }
        else
            return Chat.Left(res.getChatString());
    }

    private Chat<CommandLineArgs> parse(CommandLineArgs z, ImList<String> args)
    {
        if (args.isEmpty())
        {
            return Chat.Right(z);
        }
        else
        {
            String firstArg = args.head();
            ImList<String> remainingArgs = args.tail();

            if (firstArg.startsWith("-"))
            {
                // It's an option. Can we find this option?
                ImMaybe<OptionDefinition> optionDefinitionMaybe = optionDefinitions.find(i -> Eq.uals(i.name, firstArg));

                if (!optionDefinitionMaybe.isPresent())
                {
                    // option not found
                    return Chat.Left(String.format("Option %s is not recognised", firstArg));
                }
                else
                {
                    OptionDefinition optionDef = optionDefinitionMaybe.get();

                    if (remainingArgs.isEmpty())
                    {
                        // There are no more args
                        return optionDef.hasAValue
                               ? Chat.Left(String.format("Option %s needs a value", firstArg))
                               : z.hasOptionSet(optionDef.name)
                                 ? Chat.Left(String.format("Option %s has already been set", firstArg))
                                 : Chat.Right(z.addOption(optionDef.name));
                    }
                    else
                    {
                        // There are more args
                        String nextArg = remainingArgs.head();
                        ImList<String> nextRemainingArgs = remainingArgs.tail();

                        // If the value starts with - this is an error
                        return nextArg.startsWith("-")
                               ? Chat.Left(String.format("Option %s can't have a value that starts with -", firstArg))
                               : z.hasOptionSet(optionDef.name) && optionDef.isSingle
                                 ? Chat.Left(String.format("Option %s can only have one value", firstArg))
                                 : parse(z.addOption(optionDef.name, nextArg), nextRemainingArgs);
                    }
                }
            }
            else
            {
                // Must be a command
                return parse(z.addCommand(firstArg), remainingArgs);
            }
        }

    }

}