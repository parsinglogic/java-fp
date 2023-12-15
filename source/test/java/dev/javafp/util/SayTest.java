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

public class SayTest
{
    /**
     *  The width of the line "header"
     */
    int headerWidth = 83;
    // 2023-11-29 15:45:24.299       - SayTest::testTable                                 one

    @Test
    public void testSay()
    {
        Say.setQuiet(true);
        say("one", "two", "three");

        assertEquals("one two three\n", Say.getBufferString().substring(headerWidth));
    }

    @Test
    public void testSayArray()
    {

        int TEST_COUNT = 2;
        ImList<Integer> lst = ImRange.inclusive(-2, TEST_COUNT);

        Integer[] array = lst.toArray(Integer.class);

        say("array", array);

        Say.setQuiet(true);

        say("lst", lst);

        //  2023-11-21 12:10:44.337       - SayTest::testSayArray                              lst [-2, -1, 0, 1, 2]
        //  2023-11-21 12:14:01.093       9 SayTest::testTable                                 hello

        assertEquals("lst [-2, -1, 0, 1, 2]\n", Say.getBufferString().substring(headerWidth));

        Say.clearBuffer();

        say("array", array);

        String bufferString = Say.getBufferString();

        ImList<String> longLines = ParseUtils.split('\n', bufferString).filter(i -> i.length() >= headerWidth);

        String ls = longLines.map(i -> i.substring(headerWidth)).toString("\n");

        // When we use say, it does remove the trailing space
        String expected = ""
                + "array 1:   -2\n"
                + "      2:   -1\n"
                + "      3:   0\n"
                + "      4:   1\n"
                + "      5:   2";

        assertEquals(expected, ls);
    }

    @Test
    public void testGetMethodName()
    {
        assertEquals("SayTest::testGetMethodName", Say.getMethodName(0));
    }

    @Test
    public void testTable()
    {
        String expected = ""
                + "one                           : one                               \n"
                + "floccinaucinihilipilification : [99, 100, 101, 102, 103, 104, 105]\n"
                + "three                         : null                              \n"
                + "four                          : MISSING                           ";

        ImList<Integer> things = ImRange.inclusive(99, 105);
        assertEquals(expected, Say.table("one", "one", "floccinaucinihilipilification", things, "three", null, "four").toString());
        Say.showTable("one", "one", "floccinaucinihilipilification", things, "three", null, "four");

        Say.say("hello");

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
                + "zero  six      ten      \n"
                + "one   seven    eleven   \n"
                + "two   eight    twelve   \n"
                + "three nine     thirteen \n"
                + "four  ten      fourteen \n"
                + "five  eleven   fifteen  \n"
                + "      twelve   sixteen  \n"
                + "      thirteen          \n"
                + "      fourteen          \n"
                + "      fifteen           \n"
                + "      sixteen           ";

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

        String expected = ""
                + "ten      one   seven \n"
                + "eleven   two   eight \n"
                + "twelve   three nine  \n"
                + "thirteen four        \n"
                + "fourteen five        \n"
                + "         six         ";

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
    public void testFormat()
    {
        String s = String.format("%.8s %10d ", "0123456789", 1);
        assertEquals("01234567          1 ", s);
    }

    @Test
    public void testNewLines()
    {
        say(TextUtils.getBoxFrom("\n\n\n"));
        Say.printNewLines(5);
    }
}