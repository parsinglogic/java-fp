package dev.javafp.util;

import dev.javafp.ex.ArgumentShouldNotBeLessThan;
import dev.javafp.lst.ImList;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextUtilsTest
{

    @Test
    public void testArrays()
    {

        say(TextUtils.getBoxFrom(new byte[] { 0, 1, 2 }));

        String ex = ""
                + "1:   0\n"
                + "2:   1\n"
                + "3:   2\n";

        String ex2 = ""
                + "1:   true\n"
                + "2:   false\n"
                + "3:   true\n";

        assertEquals(ex, TextUtils.getBoxFrom(new byte[] { 0, 1, 2 }).toString());

        assertEquals(ex, (TextUtils.getBoxFrom(new int[] { 0, 1, 2 })).toString());

        assertEquals(ex2, (TextUtils.getBoxFrom((new boolean[] { true, false, true }))).toString());

    }

    @Test
    public void testPadToWidth()
    {
        assertEquals("a   ", TextUtils.padToWidth("a", 4));
        assertEquals("ab", TextUtils.padToWidth("abc", 2));
    }

    @Test
    public void testPadToWidthWillTruncate()
    {
        assertEquals("abc", TextUtils.padToWidth("abcde", 3));
    }

    @Test
    public void testDetab()
    {
        assertTabEquals("    ", TextUtils.detab(4, "\t"));
        assertTabEquals("y   ", TextUtils.detab(4, "y\t"));
        assertTabEquals("y   z", TextUtils.detab(4, "y\tz"));
        assertTabEquals("yy  ", TextUtils.detab(4, "yy\t"));
        assertTabEquals("yyy ", TextUtils.detab(4, "yyy\t"));
        assertTabEquals("yyyy    ", TextUtils.detab(4, "yyyy\t"));
        assertTabEquals("            ", TextUtils.detab(4, "\t\t\t"));
        assertTabEquals("    foo ", TextUtils.detab(4, "\tfoo\t"));

    }

    private void assertTabEquals(String expected, String actual)
    {
        assertEquals(expected.replace(' ', 'x'), actual.replace(' ', 'x'));
    }

    @Test
    public void testAbbreviate()
    {
        assertEquals("", TextUtils.abbreviate("", 4));
        assertEquals("abcd", TextUtils.abbreviate("abcd", 4));
        assertEquals("abc...", TextUtils.abbreviate("abcdefg", 6));

        TestUtils.assertThrows(() -> TextUtils.abbreviate("abcdefg", 2), ArgumentShouldNotBeLessThan.class);
    }

    @Test
    public void testTruncate()
    {
        assertEquals("", TextUtils.truncate("", 4));
        assertEquals("abcd", TextUtils.truncate("abcd", 4));
        assertEquals("abcdef", TextUtils.truncate("abcdefg", 6));

        TestUtils.assertThrows(() -> TextUtils.truncate("abcdefg", -1), ArgumentShouldNotBeLessThan.class);
    }

    @Test
    public void testChunk()
    {
        assertArrayEquals(new String[] {}, TextUtils.splitIntoChunks(0, ""));
        assertArrayEquals(new String[] { "a" }, TextUtils.splitIntoChunks(3, "a"));
        assertArrayEquals(new String[] { "ab" }, TextUtils.splitIntoChunks(3, "ab"));
        assertArrayEquals(new String[] { "abc" }, TextUtils.splitIntoChunks(3, "abc"));
        assertArrayEquals(new String[] { "abc", "d" }, TextUtils.splitIntoChunks(3, "abcd"));
        assertArrayEquals(new String[] { "abc", "def" }, TextUtils.splitIntoChunks(3, "abcdef"));
        assertArrayEquals(new String[] { "a", "b" }, TextUtils.splitIntoChunks(1, "ab"));
    }

    @Test
    public void testCharAtFromEnd()
    {
        assertEquals('c', TextUtils.charAtFromEnd("abc", 0));
        assertEquals('b', TextUtils.charAtFromEnd("abc", 1));
        assertEquals('a', TextUtils.charAtFromEnd("abc", 2));
    }

    @Test
    public void testJoinMinEmptyList()
    {
        assertEquals("", TextUtils.joinMin(ImList.on(), "start", "sep", "stop"));
        assertEquals("", TextUtils.joinMin(ImList.on().iterator(), "start", "sep", "stop"));

    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testCharAtFromEndThrowsWhenIndexIsTooLarge()
    {
        TextUtils.charAtFromEnd("abc", 3);
    }

    @Test
    public void testShowCollection()
    {
        List<String> list = Arrays.asList("one", "two");

        assertEquals("[one, two]", TextUtils.showCollection(list));
    }

    @Test
    public void testShowCollection2()
    {
        System.out.println(TextUtils.showCollection(ImList.on("apples", "blackberries", "cherries")));
    }

    @Test
    public void testJoin()
    {
        assertEquals("apples,blackberries,cherries", TextUtils.join(ImList.on("apples", "blackberries", "cherries"), ","));
    }

    @Test
    public void testJoin2()
    {
        assertEquals("(apples, blackberries, cherries)", TextUtils.join(ImList.on("apples", "blackberries", "cherries"), "(", ", ", ")"));
    }

    @Test
    public void testTrimRight()
    {
        assertEquals("", TextUtils.trimRight(""));
        assertEquals("abc", TextUtils.trimRight("abc      "));
        assertEquals("abc", TextUtils.trimRight("abc"));
    }

    @Test
    public void testIndentBy()
    {
        assertEquals("   abc", TextUtils.indentBy(3, "abc"));
        assertEquals("abc", TextUtils.indentBy(0, "abc"));
        assertEquals("abc", TextUtils.indentBy(-321321, "abc"));
    }

    @Test
    public void testRightJustifyIn()
    {
        assertEquals("    abc", TextUtils.rightJustifyIn(7, "abc"));
        assertEquals("bc", TextUtils.rightJustifyIn(2, "abc"));
        assertEquals("abc", TextUtils.rightJustifyIn(-1, "abc"));
        assertEquals("      ", TextUtils.rightJustifyIn(6, ""));
    }

    @Test
    public void testLeftJustifyIn()
    {
        assertEquals("abc    ", TextUtils.leftJustifyIn(7, "abc"));
        assertEquals("ab", TextUtils.leftJustifyIn(2, "abc"));
        assertEquals("abc", TextUtils.leftJustifyIn(-1, "abc"));
        assertEquals("    ", TextUtils.leftJustifyIn(4, ""));
    }

    @Test
    public void testCentreIn()
    {
        assertEquals("  abc  ", TextUtils.centreIn(7, "abc"));
        assertEquals(" abc  ", TextUtils.centreIn(6, "abc"));
        assertEquals("ab", TextUtils.centreIn(2, "abc"));
        assertEquals("", TextUtils.centreIn(0, "abc"));
        assertEquals("     ", TextUtils.centreIn(5, ""));
    }

    @Test
    public void testIsNlAnISOControl()
    {
        assertTrue(Character.isISOControl(Character.valueOf('\n')));
    }

}