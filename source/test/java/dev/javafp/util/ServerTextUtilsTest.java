package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.set.ImMap;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ServerTextUtilsTest
{

    @Test
    public void testToWord()
    {
        assertEquals("zero one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen",
                Range.inclusive(0, 19).map(i -> ServerTextUtils.toWord(i)).toString(" "));

        assertEquals("twenty", ServerTextUtils.toWord(20));
        assertEquals("twenty one", ServerTextUtils.toWord(21));
        assertEquals("twenty two", ServerTextUtils.toWord(22));
        assertEquals("thirty three", ServerTextUtils.toWord(33));
        assertEquals("forty four", ServerTextUtils.toWord(44));
        assertEquals("fifty five", ServerTextUtils.toWord(55));
        assertEquals("sixty six", ServerTextUtils.toWord(66));
        assertEquals("seventy seven", ServerTextUtils.toWord(77));
        assertEquals("eighty eight", ServerTextUtils.toWord(88));
        assertEquals("ninety nine", ServerTextUtils.toWord(99));

        assertEquals("100", ServerTextUtils.toWord(100));
        assertEquals("-1", ServerTextUtils.toWord(-1));
    }

    @Test
    public void testExample()
    {
        ImList<Integer> is = Range.step(11, 11).take(9);

        say(is);
        // [11, 22, 33, 44, 55, 66, 77, 88, 99]

        ImList<String> words = is.map(i -> ServerTextUtils.toWord(i));

        say(words);
        // [eleven, twenty two, thirty three, forty four, fifty five, sixty six, seventy seven, eighty eight, ninety nine]

        ImMap<String, Integer> wordsToIntegers = ImMap.fromPairs(words.zip(is));

        say(wordsToIntegers.get("ninety nine"));

    }

    @Test
    public void testCssIdCheck()
    {
        for (String s : ImList.on("a", "a-", "-", "-a", "A1", "abcdefghiJKLMNOP_-12349"))
        {
            assertEquals("fail on " + s, null, ServerTextUtils.checkCssIdentifier(s));
        }
    }

    @Test
    public void testCssIdCheckWhenBad()
    {
        for (String s : ImList.on("", "-1", "0", "-123", "()", "a{!", "1a"))
        {
            assertNotEquals("fail on " + s, null, ServerTextUtils.checkCssIdentifier(s));
        }
    }

    @Test
    public void testCssIdCheckWhenEmpty()
    {
        assertEquals("CSS identifier can't be the empty string", ServerTextUtils.checkCssIdentifier(""));
    }

    @Test
    public void testCssIdCheckWhenBadChar()
    {
        assertEquals("CSS identifier \"uywteuryt^\" contains invalid characters - each char must match [-_a-zA-Z0-9]", ServerTextUtils.checkCssIdentifier("uywteuryt^"));
    }

    @Test
    public void testCssIdCheckWhenStartsWithDigit()
    {
        assertEquals("CSS identifier \"12345\" starts with a digit - which is not allowed", ServerTextUtils.checkCssIdentifier("12345"));
    }

    @Test
    public void testCssIdCheckWhenStartsWithAHyphenAndThenADigit()
    {
        assertEquals("CSS identifier \"-1Tgq1-_\" starts with a hyphen followed by a digit - which is not allowed", ServerTextUtils.checkCssIdentifier("-1Tgq1-_"));
    }

}