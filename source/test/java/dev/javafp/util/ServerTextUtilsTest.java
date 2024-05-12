package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.set.ImMap;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class ServerTextUtilsTest
{

    @Test
    public void testToWords()
    {
        //        ServerTextUtils.toWords(Long.MAX_VALUE);
        ServerTextUtils.toWords(Long.MAX_VALUE);
        ServerTextUtils.toWords(1001);

    }

    @Test
    public void testSomeValues()
    {
        say(Long.MAX_VALUE);
        assertOk("zero", 0);
        assertOk("seven hundred and fifty five", 755);
        assertOk("one thousand, seven hundred and fifty five", 1755);
        assertOk("one thousand and five", 1005);

        assertOk("one hundred", 100);
        assertOk("one thousand", 1_000);
        assertOk("one million", 1_000_000);
        assertOk("nine sextillion and one", 9_000_000_000_000_000_001L);
        assertOk("nine sextillion and ninety nine", 9_000_000_000_000_000_099L);
        assertOk("nine sextillion, one hundred", 9_000_000_000_000_000_100L);
        assertOk("nine sextillion, two hundred and twenty three quintillion, three hundred and seventy two quadrillion, thirty six billion, "
                + "eight hundred and fifty four million, seven hundred and seventy five thousand, eight hundred and seven", 9_223_372_036_854_775_807L);
        // 9,223,372,036,854,775,807
        // 9_223_372_036_854_775_807

    }

    private void assertOk(String expected, long i)
    {
        String actual = ServerTextUtils.toWords(i);
        say(i, "->", actual);

        assertEquals("error on " + i, expected, actual);

    }

    @Test
    public void testToWord()
    {
        assertEquals("zero one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen",
                ImRange.inclusive(0, 19).map(i -> ServerTextUtils.toWords(i)).toString(" "));

        assertEquals("twenty", ServerTextUtils.toWords(20));
        assertEquals("twenty one", ServerTextUtils.toWords(21));
        assertEquals("twenty two", ServerTextUtils.toWords(22));
        assertEquals("thirty three", ServerTextUtils.toWords(33));
        assertEquals("forty four", ServerTextUtils.toWords(44));
        assertEquals("fifty five", ServerTextUtils.toWords(55));
        assertEquals("sixty six", ServerTextUtils.toWords(66));
        assertEquals("seventy seven", ServerTextUtils.toWords(77));
        assertEquals("eighty eight", ServerTextUtils.toWords(88));
        assertEquals("ninety nine", ServerTextUtils.toWords(99));

        assertEquals("100", ServerTextUtils.toWords(100));
        assertEquals("-1", ServerTextUtils.toWords(-1));
    }

    @Test
    public void testExample()
    {
        ImList<Integer> is = ImRange.step(11, 11).take(9);

        say(is);
        // [11, 22, 33, 44, 55, 66, 77, 88, 99]

        ImList<String> words = is.map(i -> ServerTextUtils.toWords(i));

        say(words);
        // [eleven, twenty two, thirty three, forty four, fifty five, sixty six, seventy seven, eighty eight, ninety nine]

        ImMap<String, Integer> wordsToIntegers = ImMap.fromPairs(words.zip(is));

        say(wordsToIntegers.get("ninety nine"));

    }

}