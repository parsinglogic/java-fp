package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandLineArgsParserTest
{

    private final ImList<OptionDefinition> optionsDefinitions = ImList.on(
            new OptionDefinition("-repo", true, true, false),
            new OptionDefinition("-name", true, true, true),
            new OptionDefinition("-token", true, true, true)
    );

    private final ImList<String> commands = ParseUtils.split(' ', "bish bash bosh");

    @Test
    public void testErrors()
    {
        CommandLineArgsParser parser = new CommandLineArgsParser(ImList.on(new OptionDefinition("-foo", false, true, true)));

        assertEquals("Option -bar is not recognised", parser.parse0(ImList.on("-bar")).getChatString());
        assertEquals("Option -foo needs a value", parser.parse0(ImList.on("-foo")).getChatString());
        assertEquals("Option -foo can't have a value that starts with -", parser.parse0(ImList.on("-foo", "-a")).getChatString());
        assertEquals("Option -foo can only have one value", parser.parse0(ImList.on("-foo", "a", "-foo", "a")).getChatString());
        assertEquals(ImList.on("a", "b", "c"), parser.parse0(ImList.on("a", "-foo", "a", "b", "c")).rightOrThrow().commands);
    }

    @Test
    public void testJadleArgsWork()
    {
        ImList<String> options = ParseUtils.split(',', "-token 123,-repo 1,-repo 2,-name foo");

        ImList<ImList<String>> words = ImList.interleave(commands, options);

        ImList<ImList<String>> ws = words.map(i -> ParseUtils.split(' ', i.toString(" ")));

        ws.foreach(i -> tryArgs(i));
    }

    private void tryArgs(ImList<String> words)
    {
        CommandLineArgsParser parser = new CommandLineArgsParser(optionsDefinitions);

        Chat<CommandLineArgsResult> argsChat = parser.parse0(words);

        CommandLineArgsResult args = argsChat.rightOrThrow();

        assertEquals(commands, args.commands);

        assertEquals(true, args.hasOptionSet("-token"));
        assertEquals(ImList.on("123"), args.getOption("-token"));

        assertEquals(true, args.hasOptionSet("-name"));
        assertEquals(ImList.on("foo"), args.getOption("-name"));

        assertEquals(true, args.hasOptionSet("-repo"));
        assertEquals(ImList.on("1", "2"), args.getOption("-repo"));
    }

}