package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest
{

    ImList<Double> ds = ImList.on(1.0, 2.0, 3.0, 4.0, 5.0);
    private double delta = 0.000000001;

    @Test
    public void testSplitInto2()
    {
        assertEquals(Integer.valueOf(1024), Util.until(i -> i > 1000, i -> i * 2, 1));
    }

    @Test
    public void testSum()
    {
        assertEquals(15.0, Util.sum(ds), delta);
    }

    @Test
    public void testSumOfStrings()
    {
        assertEquals(6.0, Util.sum(ImList.on("a", "ab", "", "abc"), s -> s.length() + 0.0), delta);
    }

    @Test
    public void testMin()
    {
        assertEquals(1.0, Util.min(ds), delta);
    }

    @Test
    public void testMax()
    {
        assertEquals(5.0, Util.max(ds), delta);
    }

    @Test
    public void testToPx()
    {
        assertEquals("3.1px", Util.toPx(3.1));
        assertEquals("3.5px", Util.toPx(3.54));
        assertEquals("3.5px", Util.toPx(3.549));
        assertEquals("3.6px", Util.toPx(3.55));
        assertEquals("12345678.1px", Util.toPx(12345678.12345));
    }

    //    public ImList<Character> getBadChars()
    //    {
    //        /**
    //         * <p> I would like to test with a larger set of 'bad' characters, but this causes problems
    //         * <p> I tried generating all the characters between 0 and 0xFFFF that are invisible.
    //         * <p> This included the surrogate characters when I first tried it. This turned out to be a bad idea
    //         * since when you try to encode a surrogate to UTF-8 it silently refuses to do it and gives you a
    //         * question mark (0x3F) instead. This confused me for a few hours.
    //         * <p> So we have to be careful when dealing with tests for 'bad' Unicode characters
    //         * <p> We want to allow all reasonable Unicode characters - but we want to remove the ones that will cause the
    //         * CSV parsing to fail in a strange way - before we give it to the CSV parser
    //         * <p> For unicode characters that are invisible, I think we should map them to a space
    //         * <p> Testing the unicode characters that are surrogates is hard because, using Java, it is very difficult to
    //         * write them into a file. They just get translated silently as ? (3F) characters - sigh -
    //         * <p> from https://en.wikipedia.org/wiki/Plane_(Unicode)#Basic_Multilingual_Plane
    //         * <p> Surrogates:
    //         * High Surrogates (D800–DBFF)
    //         * Low Surrogates (DC00–DFFF)
    //         * Private Use Area (E000–F8FF)
    //         * <p> We are using this Guava function
    //         * public static CharMatcher invisible()
    //         * <p> Determines whether a character is invisible; that is, if its Unicode category is any of
    //         * SPACE_SEPARATOR, LINE_SEPARATOR, PARAGRAPH_SEPARATOR, CONTROL, FORMAT, SURROGATE, and PRIVATE_USE according to ICU4J.
    //         * <p> Let's get the invisible chars that are not surrogates or private use
    //         * <p> I can't even print some of these without strange effects!
    //         * <p> Eg
    //         * 8234 202A <<‪>>, 8235 202B <<‫>>, 8236 202C <<‬>>, 8237 202D <<‭>>,
    //         * <p> I think one or more of the characters has kicked the display into printing right to left
    //         * <p> Let's restrict our tests to 0 - 0x200C to prevent strangeness
    //         *
    //         */
    //        ImList<Integer> bad = ImRange.inclusive(0x0, 0x200C).filter(c -> CsvRow.isBad((char) c.intValue()));
    //        //        ImList<Integer> bad2 = ImList.on(); //ImRange.inclusive(0xF8FF + 1, 0xFFFF).filter(c -> CsvRow.isBad((char) c.intValue()));
    //
    //        //        System.out.println("bad.size() " + bad.size());
    //        //        System.out.println(bad);
    //        //
    //        //        System.out.println("bad is " + bad.map(i -> String.format("%d %X <<%c>>", i, i, (char) i.intValue())));
    //
    //        return bad.map(i -> Character.valueOf((char) i.intValue()));
    //    }

}