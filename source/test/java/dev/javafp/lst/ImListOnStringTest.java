package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Test;

import static dev.javafp.util.TestUtils.assertEq;

public class ImListOnStringTest
{

    @Test
    public void testFilter()
    {
        ImList<Character> ls = ImListOnString.on("a=b=c");

        assertEq(2, ls.filter(c -> c == '=').size());
    }

    @Test
    public void testEmpty()
    {
        ImList<Character> ls = ImListOnString.on("");

        assertEq(0, ls.filter(c -> c == '=').size());
    }

    @Test
    public void testToString()
    {
        ImList<Character> ls = ImListOnString.on("abc");

        TestUtils.assertEq("abc", ls.toString(""));
    }

}