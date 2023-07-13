package dev.javafp.util;

import dev.javafp.ex.IllegalState;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParseUtilsTest
{
    @Test
    public void testSkipOverAny()
    {
        assertEquals(ImPair.on("", ""), ParseUtils.skipOverAny(' ', ""));
        assertEquals(ImPair.on(" ", ""), ParseUtils.skipOverAny(' ', " "));
        assertEquals(ImPair.on("  ", ""), ParseUtils.skipOverAny(' ', "  "));
        assertEquals(ImPair.on(" ", "x"), ParseUtils.skipOverAny(' ', " x"));
        assertEquals(ImPair.on("", "x"), ParseUtils.skipOverAny(' ', "x"));
        assertEquals(ImPair.on("", "xy"), ParseUtils.skipOverAny(' ', "xy"));
    }

    @Test
    public void testSplitAt()
    {
        assertEquals(ImPair.on("a", "b"), ParseUtils.splitAt('/', "a/b"));
        assertEquals(ImPair.on("aa", "b"), ParseUtils.splitAt('/', "aa/b"));
        assertEquals(ImPair.on("", "b"), ParseUtils.splitAt('/', "/b"));
        assertEquals(ImPair.on("aa", ""), ParseUtils.splitAt('/', "aa/"));
        assertEquals(ImPair.on("", null), ParseUtils.splitAt('/', ""));
        assertEquals(ImPair.on("abc", null), ParseUtils.splitAt('/', "abc"));
        assertEquals(ImPair.on("", ""), ParseUtils.splitAt('/', "/"));

    }

    @Test
    public void testSplitAt2()
    {
        assertEquals(ImPair.on("a", "b"), ParseUtils.splitAt("//", "a//b"));
        assertEquals(ImPair.on("a", "b//"), ParseUtils.splitAt("//", "a//b//"));
        assertEquals(ImPair.on("aa", "b"), ParseUtils.splitAt("//", "aa//b"));
        assertEquals(ImPair.on("", "b"), ParseUtils.splitAt("//", "//b"));
        assertEquals(ImPair.on("aa", ""), ParseUtils.splitAt("//", "aa//"));
        assertEquals(ImPair.on(null, ""), ParseUtils.splitAt("//", ""));
        assertEquals(ImPair.on(null, "abc"), ParseUtils.splitAt("//", "abc"));
        assertEquals(ImPair.on("", ""), ParseUtils.splitAt("//", "//"));
        assertEquals(ImPair.on("", "////"), ParseUtils.splitAt("//", "//////"));

    }

    @Test
    public void testGetStringFromVnf()
    {
        assertEquals(ImPair.on("foo", ""), ParseUtils.getStringFromVnf("3 foo"));
        assertEquals(ImPair.on("foo", "wibble"), ParseUtils.getStringFromVnf("3 foowibble"));
        assertEquals(ImPair.on("foo", ""), ParseUtils.getStringFromVnf("3 foo"));
    }

    @Test
    public void testGetStringMatching()
    {
        String pathComponent = "(([A-Za-z][A-Za-z0-9]*)|[0-9]+)";

        String path = pathComponent + "(/" + pathComponent + ")*";

        Pattern pattern = Pattern.compile(path);

        checkMatches(pattern, "abc", " def");
        checkMatches(pattern, "abc", "");
        checkMatches(pattern, "a", " ");
        checkMatches(pattern, "x/ab/c", "");
        checkMatches(pattern, "123", "");
        checkMatches(pattern, "aa", "/");

        checkNoMatch(pattern, "/", "");
        checkNoMatch(pattern, "", "");
    }

    private void checkMatches(Pattern pattern, String prefix, String suffix)
    {
        assertEquals(prefix, ParseUtils.getStringMatching(pattern, prefix + suffix));
    }

    private void checkNoMatch(Pattern pattern, String prefix, String suffix)
    {
        assertEquals("", ParseUtils.getStringMatching(pattern, prefix + suffix));
    }

    @Test
    public void testGetRest()
    {
        assertEquals(" def", ParseUtils.getRest("abc", "abc def"));
        assertEquals("", ParseUtils.getRest("abc", ""));
        assertEquals("", ParseUtils.getRest("ab", "a"));
        assertEquals("abc", ParseUtils.getRest("", "abc"));
        assertEquals("", ParseUtils.getRest("", ""));
    }

    @Test
    public void testSplit()
    {
        assertEquals(ImList.on("a", "b", "c"), ParseUtils.split('/', "a/b/c"));
        assertEquals(ImList.on("a", "", "c"), ParseUtils.split('.', "a..c"));

        assertEquals(ImList.on("", "a", "", ""), ParseUtils.split('/', "/a//"));

        assertEquals(ImList.on("", ""), ParseUtils.split('/', "/"));
        assertEquals(ImList.on(""), ParseUtils.split('/', ""));
    }

    @Test
    public void testGetFieldAtIndex()
    {
        assertEquals("a", ParseUtils.getFieldAtIndex("a/b/c", '/', 1));
        assertEquals("b", ParseUtils.getFieldAtIndex("a/b/c", '/', 2));
        assertEquals("c", ParseUtils.getFieldAtIndex("a/b/c", '/', 3));
        assertEquals("", ParseUtils.getFieldAtIndex("a/b/c", '/', 4));
        assertEquals("c", ParseUtils.getFieldAtIndex("a/b/c", '/', -1));
        assertEquals("b", ParseUtils.getFieldAtIndex("a.b.c", '.', -2));
        assertEquals("a", ParseUtils.getFieldAtIndex("a/b/c", '/', -3));
        assertEquals("", ParseUtils.getFieldAtIndex("a/b/c", '/', -4));
        assertEquals("", ParseUtils.getFieldAtIndex("a..c", '.', 2));

        try
        {
            ParseUtils.getFieldAtIndex("a/b/c", '/', 0);
            fail();
        } catch (IllegalState e)
        {

            assertEquals("indexStartingAtOneOrMinusOne must not be zero", e.getMessage());
        }
    }

    @Test
    public void testRemoveFieldAtIndex()
    {
        assertEquals("b.c", ParseUtils.removeFieldAtIndex("a.b.c", '.', 1));
        assertEquals("a/c", ParseUtils.removeFieldAtIndex("a/b/c", '/', 2));
        assertEquals("a/b", ParseUtils.removeFieldAtIndex("a/b/c", '/', 3));
        assertEquals("a/b/c", ParseUtils.removeFieldAtIndex("a/b/c", '/', 4));
        assertEquals("a/b", ParseUtils.removeFieldAtIndex("a/b/c", '/', -1));
        assertEquals("a/c", ParseUtils.removeFieldAtIndex("a/b/c", '/', -2));
        assertEquals("b/c", ParseUtils.removeFieldAtIndex("a/b/c", '/', -3));
        assertEquals("a/b/c", ParseUtils.removeFieldAtIndex("a/b/c", '/', -4));
        assertEquals("a/c", ParseUtils.removeFieldAtIndex("a//c", '/', 2));

        try
        {
            ParseUtils.removeFieldAtIndex("a/b/c", '/', 0);
            fail();
        } catch (IllegalState e)
        {

            assertEquals("indexStartingAtOne must not be zero", e.getMessage());
        }
    }

    @Test
    public void testIsDigits()
    {
        assertEquals(true, ParseUtils.isDigits("123123"));
        assertEquals(true, ParseUtils.isDigits("1"));
        assertEquals(true, ParseUtils.isDigits("0123456789"));
        assertEquals(false, ParseUtils.isDigits("wibble"));
    }

}