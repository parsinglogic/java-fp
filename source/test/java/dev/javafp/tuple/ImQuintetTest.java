package dev.javafp.tuple;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImQuintetTest
{

    @Test
    public void simple()
    {
        ImQuintet<Integer, Integer, Integer, Integer, Integer> q = ImQuintet.on(1, 2, 3, 4, 5);

        assertEquals("ImQuintet: e1: 1\n"
                + "           e2: 2\n"
                + "           e3: 3\n"
                + "           e4: 4\n"
                + "           e5: 5\n", "" + q);
    }

    @Test
    public void testZip()
    {
        ImList<Integer> one = Range.inclusive(1, 3);
        ImList<Integer> two = Range.inclusive(4, 6);
        ImList<Integer> three = Range.inclusive(6, 8);
        ImList<Integer> four = Range.inclusive(9, 11);
        ImList<Integer> five = Range.inclusive(12, 14);

        String expected = "[\n" +
                "  1 ImQuintet: e1: 1\n" +
                "               e2: 4\n" +
                "               e3: 6\n" +
                "               e4: 9\n" +
                "               e5: 12\n" +
                "    ────────\n" +
                "  2 ImQuintet: e1: 2\n" +
                "               e2: 5\n" +
                "               e3: 7\n" +
                "               e4: 10\n" +
                "               e5: 13\n" +
                "    ────────\n" +
                "  3 ImQuintet: e1: 3\n" +
                "               e2: 6\n" +
                "               e3: 8\n" +
                "               e4: 11\n" +
                "               e5: 14\n" +
                "]\n";
        assertEquals(expected, ImQuintet.zip(one, two, three, four, five).toString());
    }
}