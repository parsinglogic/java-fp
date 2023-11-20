package dev.javafp.util;

import dev.javafp.ex.InvalidState;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.javafp.util.Say.say;
import static dev.javafp.util.ServerTextUtils.toWord;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SayTest
{
    @Test
    public void testTable() throws Exception
    {
        String expected = "one                           : one\n"
                + "floccinaucinihilipilification : [99, 100, 101, 102, 103, 104, 105]\n"
                + "three                         : null\n"
                + "four                          : MISSING\n";
        ImList<Integer> things = ImRange.inclusive(99, 105);
        assertEquals(expected, Say.table("one", "one", "floccinaucinihilipilification", things, "three", null, "four").toString());
        Say.showTable("one", "one", "floccinaucinihilipilification", things, "three", null, "four");

    }

    @Test
    public void testSimpleDateFormat()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm:ss.SSS");

        System.out.println(sdf.format(new Date()));
    }

    @Test
    public void testFormatColumns()
    {

        ImList<String> col1 = ImRange.inclusive(0, 5).map(i -> toWord(i));
        ImList<String> col2 = ImRange.inclusive(6, 16).map(i -> toWord(i));
        ImList<String> col3 = ImRange.inclusive(10, 16).map(i -> toWord(i));

        String expected = ""
                + "zero  six      ten\n"
                + "one   seven    eleven\n"
                + "two   eight    twelve\n"
                + "three nine     thirteen\n"
                + "four  ten      fourteen\n"
                + "five  eleven   fifteen\n"
                + "      twelve   sixteen\n"
                + "      thirteen\n"
                + "      fourteen\n"
                + "      fifteen\n"
                + "      sixteen\n";

        assertEquals(expected, Say.formatColumns(col1, col2, col3).toString());
    }

    @Test
    public void testFormatColumns2()
    {

        ImList<String> col1 = ImRange.inclusive(10, 14).map(i -> toWord(i));
        ImList<String> col2 = ImRange.inclusive(1, 6).map(i -> toWord(i));
        ImList<String> col3 = ImRange.inclusive(7, 9).map(i -> toWord(i));

        say("col1", col1);
        say("col2", col2);
        say("col3", col3);

        String expected = "ten      one   seven\n"
                + "eleven   two   eight\n"
                + "twelve   three nine\n"
                + "thirteen four\n"
                + "fourteen five\n"
                + "         six\n";

        assertEquals(expected, Say.formatColumns(col1, col2, col3).toString());
    }

    @Test
    public void testFormatColumnsWithEmptyLists()
    {

        ImList<String> col1 = ImRange.inclusive(10, 14).map(i -> toWord(i));
        ImList<String> col2 = ImList.on();

        say("col1", col1);
        say("col2", col2);

        TestUtils.assertThrows(() -> Say.formatColumns(col1, col2).toString(), InvalidState.class);
    }

    @Test
    public void testLog()
    {
        // wibble main              -        2021-11-30 12:06:30.199       - SayTest::testLog                                   bing

        Say.setQuiet(true);
        String pre = "wibble ";
        Say.log(pre, "bing");

        String res = Say.getString();

        assertTrue(res.startsWith(pre));
    }

    @Test
    public void testFormat()
    {
        String s = String.format("%.8s %10d ", "0123456789", 1);
        assertEquals("01234567          1 ", s);
    }
}