package dev.javafp.lst;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static junit.framework.TestCase.assertEquals;

/**
 * Read lines from a Reader
 * TODO
 * Set up a string "abcd\r\r\n\n"
 * for each permutation, assert that our lines are the same as Reader:readLine
 *
 * Hmm I can't do this. The semantics are different. I consider CR without NL as part of the line
 * I guess I could use readLine myself
 */
public class ImListOnReaderTest
{

    @Test
    public void testSimple()
    {
        ImList<Integer> lines = ImRange.oneTo(5);

        StringReader reader = new StringReader(lines.toString("\n"));

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("" + lines, "" + list);
    }

    @Test
    public void testWithCRLF()
    {
        ImList<Integer> lines = ImRange.oneTo(5);

        StringReader reader = new StringReader(lines.toString("\r\n"));

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("" + lines, "" + list);
    }

    @Test
    public void oneLineWithNoLineSepearators()
    {
        StringReader reader = new StringReader("abc");

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("abc", list.toString("|"));
    }

    @Test
    public void oneLineWithACRInThemiddle()
    {
        StringReader reader = new StringReader("ab\rcd");

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("ab|cd", list.toString("|"));
    }

    @Test
    public void testWithCROnOneLine()
    {
        StringReader reader = new StringReader("a\r\r");

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("a|", list.toString("|"));
    }

    @Test
    public void twoLinesWithCRInTheFirstLine()
    {
        StringReader reader = new StringReader("a\r\n\r");

        ImList<String> list = ImList.on(new BufferedReader(reader));

        assertEquals("a|", list.toString("|"));
    }

}