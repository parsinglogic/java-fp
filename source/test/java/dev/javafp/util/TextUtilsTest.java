package dev.javafp.util;

import dev.javafp.eq.Equals;
import dev.javafp.ex.ArgumentShouldNotBeLessThan;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TextUtilsTest
{

    final Character tab = Character.valueOf('\t');
    final Character space = Character.valueOf(' ');

    @Test
    public void testArrays()
    {

        say(TextUtils.getBoxFrom(new byte[] { 0, 1, 2 }));

        String ex = ""
                + "1:   0\n"
                + "2:   1\n"
                + "3:   2";

        String ex2 = ""
                + "1:   true \n"
                + "2:   false\n"
                + "3:   true ";

        assertEquals(ex, TextUtils.getBoxFrom(new byte[] { 0, 1, 2 }).toString());

        assertEquals(ex, (TextUtils.getBoxFrom(new int[] { 0, 1, 2 })).toString());

        assertEquals(ex2, (TextUtils.getBoxFrom((new boolean[] { true, false, true }))).toString());

    }

    @Test
    public void testPadToWidth()
    {
        assertEquals("a   ", TextUtils.padOrTrimToWidth("a", 4));
        assertEquals("ab", TextUtils.padOrTrimToWidth("abc", 2));
    }

    @Test
    public void testPadToWidthWillTruncate()
    {
        assertEquals("abc", TextUtils.padOrTrimToWidth("abcde", 3));
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

    @Test
    public void testDetabWithMany()
    {
        ImList<Character> chars = ImList.onString("\t\t\tabcd");

        ImList<ImList<Character>> ps = chars.powerSet().flatMap(is -> is.permutations());

        ImList<ImList<Character>> psSet = ImSet.onAll(ps).toList();

        say(psSet.take(10).toString("\n"));

        say(psSet.size());

        ImList<String> bs = psSet.map(is -> is.toString(""));

        System.out.println(bs.take(10).toString("\n"));

        bs.foreach(i -> checkTabExpansion(i, TextUtils.detab(4, i)));

    }

    @Test
    public void testCheckTabsWhenWrong()
    {
        testCheckTabsReportsError(" ", " ", "before contains a space");
        testCheckTabsReportsError("a", "\t", "after contains a tab");
        testCheckTabsReportsError("abcde", "\t", "after contains a tab");

        testCheckTabsReportsError("a", "", "not equal count = 4, before = ['a'], after = []");
        testCheckTabsReportsError("", "a", "not equal count = 4, before = [], after = ['a']");

        testCheckTabsReportsError("a\t", "b   ", "first chars not equal count = 4, before = ['a', '\\t'], after = ['b', ' ', ' ', ' ']");
        testCheckTabsReportsError("a\t", "a ZZ", "expecting 3 spaces count = 3, before = ['\\t'], after = [' ', 'Z', 'Z']");
        testCheckTabsReportsError("u\t", "u ", "expecting 3 spaces count = 3, before = ['\\t'], after = [' ']");
        testCheckTabsReportsError("\t", "", "not equal count = 4, before = ['\\t'], after = []");
        testCheckTabsReportsError("v\t", "v  ", "expecting 3 spaces count = 3, before = ['\\t'], after = [' ', ' ']");
        testCheckTabsReportsError("w\t", "wxxx", "expecting 3 spaces count = 3, before = ['\\t'], after = ['x', 'x', 'x']");
        testCheckTabsReportsError("m\t", "mxx ", "expecting 3 spaces count = 3, before = ['\\t'], after = ['x', 'x', ' ']");
        testCheckTabsReportsError("a\t", "a   y", "not equal count = 4, before = [], after = ['y']");

    }

    @Test
    public void testCheckTabsWhenRight()
    {
        checkTabExpansion("", "");
        checkTabExpansion("abcde", "abcde");
        checkTabExpansion("\t", "    ");
        checkTabExpansion("\ta", "    a");
        checkTabExpansion("\t\t\t", "            ");
        checkTabExpansion("\t\tx", "        x");
        checkTabExpansion("a\t\tx", "a       x");
        checkTabExpansion("ab\t\tx", "ab      x");
        checkTabExpansion("abcd\t\tx", "abcd        x");

    }

    public void testCheckTabsReportsError(String before, String after, String errorMessage)
    {
        try
        {
            checkTabExpansion(before, after);
            TestUtils.failExpectedException(java.lang.AssertionError.class);
        } catch (java.lang.AssertionError e)
        {
            say(e.getMessage());

            assertEquals(errorMessage, e.getMessage());
        }
    }

    /**
     * Check that `after` is `before` with tabs expanded
     *
     * We assume that there are no spaces in `before`. We will not have any in the tests.
     *
     * Let's consider an example:
     *
     *     before                             after
     *       0   1   2   3   4   5   6         0   1   2   3   4   5   6   7   8   9  10  11  12
     *     ┌───┬───┬───┬───┬───┬───┬───┐     ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
     *     │ a │\t │\t │ b │ c │\t │ d │     │ a │   │   │   │   │   │   │   │ b │ c │   │   │ d │
     *     └───┴───┴───┴───┴───┴───┴───┘     └───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘
     *
     */
    private void checkTabExpansion(String before, String after)
    {
        assertFalse("before contains a space", before.contains(" "));
        assertFalse("after contains a tab", after.contains("\t"));

        checkTabExpansion(4, ImList.onString(before), ImList.onString(after));
    }

    /**
     * We look at the first character of each list.
     * We keep track of how many spaces to expect if we get a tab in `before`
     * It goes 4,3,2,1,4,3,2,1,4 ...
     * We tick it before we do the recursive call.
     *
     * If the char in `before` is not a tab then the char in `after` must be the same and we recurse with the tails of each list
     *
     * Otherwise we check that the first `spaceCount` chars of `after` are spaces
     */
    private void checkTabExpansion(int spaceCount, ImList<Character> before, ImList<Character> after)
    {

        // Get a description of where we are to use in error messages
        String id = String.format(" count = %d, before = %s, after = %s", spaceCount, formatForDisplay(before), formatForDisplay(after));

        //        say("checkTabExpansion", id);

        if (before.isEmpty() || after.isEmpty())
        {
            assertTrue("not equal" + id, Equals.isEqual(before, after));
            return;
        }

        // Neither list is empty - get the first character
        Character b = before.head();
        Character a = after.head();

        if (b.equals(tab))
        {
            // We should find 'count' spaces at the start of after
            ImList<Character> ss = after.take(spaceCount);

            // All should be spaces
            assertTrue("expecting " + spaceCount + " spaces" + id, ss.size() == spaceCount && ss.all(i -> i.equals(space)));

            checkTabExpansion(4, before.tail(), after.drop(spaceCount));
        }
        else
        {
            assertTrue("first chars not equal" + id, b.equals(a));
            checkTabExpansion(tick(spaceCount), before.tail(), after.tail());
        }

    }

    ImList<String> formatForDisplay(ImList<Character> cs)
    {
        return cs.map(i -> escapeTabChar(i));
    }

    private String escapeTabChar(Character c)
    {
        return c.equals(tab)
               ? "'\\t'"
               : TextUtils.quote(String.valueOf(c), "'");

    }

    int tick(int n)
    {
        return n - 1 == 0
               ? 4
               : n - 1;
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