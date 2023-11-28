package dev.javafp.box;

import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class LeafTextBoxTest
{
    @Test
    public void testFourLinesWithTabs()
    {
        LeafTextBox leafTextBox = LeafTextBox.with(2, 3, "ab\t\nc\nd\nxxxxx");

        assertEquals("ab", leafTextBox.getLine(1));
        assertEquals("c ", leafTextBox.getLine(2));
        assertEquals("d ", leafTextBox.getLine(3));
        assertEquals("  ", leafTextBox.getLine(4));
    }

    @Test
    public void testAddEmptyLeafBox()
    {
        LeafTextBox emptyString = LeafTextBox.with("");
        assertEquals(0, emptyString.width);
        assertEquals(1, emptyString.height);
        assertEquals(1, LeafTextBox.with(" ").before(emptyString).width);
        assertEquals(2, LeafTextBox.with(" ").above(emptyString).height);
        assertEquals(2, LeafTextBox.with("a\nb\n").height);
    }

    @Test
    public void testFourLinesWithTabsUsingWith()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("ab\t\t\nc\nd\nxxxxx");

        // We do this since the toString strips off trailing spaces
        AbstractTextBox tb = leafTextBox.before(LeafTextBox.with("+\n+\n+\n+"));

        assertEquals("ab      +\n"
                + "c       +\n"
                + "d       +\n"
                + "xxxxx   +\n", tb.toString());
    }

    @Test
    public void testFourLinesWithTabsUsingLefted()
    {
        LeafTextBox leafTextBox = LeafTextBox.lefted("ab\t\t\nc\nd\nxxxxx", 10);

        // We do this since the toString strips off trailing spaces
        AbstractTextBox tb = leafTextBox.before(LeafTextBox.with("+\n+\n+\n+"));

        assertEquals("ab        +\n"
                + "c         +\n"
                + "d         +\n"
                + "xxxxx     +\n", tb.toString());

        say(tb);
    }

    @Test
    public void testFourLinesUsingLefted()
    {
        String source = "ab\ncde\nfghij\nkl";
        LeafTextBox leafTextBox = LeafTextBox.lefted(source, 10);

        assertEquals(source + "\n", leafTextBox.toString());
        say(leafTextBox);

        say(LeafTextBox.with("abc"));
        say("----");
    }

    @Test
    public void testFourLinesUsingCentred()
    {
        String expected = ""
                + "    ab\n"
                + "   cde\n"
                + "  fghij\n"
                + "    kl\n";
        String source = "ab\ncde\nfghij\nkl\n";
        LeafTextBox leafTextBox = LeafTextBox.centred(source, 10);

        assertEquals(expected, leafTextBox.toString());
    }

    @Test
    public void testFourLinesUsingRighted()
    {
        String expected = ""
                + "        ab\n"
                + "       cde\n"
                + "     fghij\n"
                + "        kl\n";
        String source = "ab\ncde\nfghij\nkl\n";
        LeafTextBox leafTextBox = LeafTextBox.righted(source, 10);

        assertEquals(expected, leafTextBox.toString());
    }

    @Test
    public void testSplitsAtNewline()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("ab\ncde");

        assertEquals("ab ", leafTextBox.getLine(1));
        assertEquals("cde", leafTextBox.getLine(2));
    }

    @Test
    public void testEmptyString()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("");

        assertEquals("", leafTextBox.getLine(1));
        assertEquals(0, leafTextBox.width);
    }

    @Test
    public void testSplitsOnNewline()
    {
        String actual = LeafTextBox.with(3, 2, "abx\nc\n").toString();
        String expected = LeafTextBox.with(3, 2, "abx\nc").toString();
        assertEquals(expected, actual);
    }

    //    @Test
    //    public void testWithMargin()
    //    {
    //        LeafTextBox leafTextBox = LeafTextBox.withMargin("foo", 1);
    //
    //        assertEquals(" foo ", leafTextBox.getLine(1));
    //        assertEquals("     ", leafTextBox.getLine(2));
    //    }

    @Test
    public void testWithTabs()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("\tfoo\t");

        // We do this since the toString strips off trailing spaces
        AbstractTextBox tb = leafTextBox.before(LeafTextBox.with("x"));

        assertEquals("    foo x", tb.toString());
    }

    @Test
    public void testWithTabs3()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("\t");

        // We do this since the toString strips off trailing spaces
        AbstractTextBox tb = leafTextBox.before(LeafTextBox.with("x"));

        assertEquals("    x", tb.toString());
    }

    @Test
    public void testWithTabs2()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("a\tbc\t\n\td\ne");

        // We do this since the toString strips off trailing spaces
        AbstractTextBox tb = leafTextBox.before(LeafTextBox.with("x"));

        assertEquals("a   bc  x\n"
                + "    d\n"
                + "e\n", tb.toString());
    }

    @Test
    public void testWithTrailingSpace()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("foo ");

        assertEquals(4, leafTextBox.width);
        assertEquals("foo", leafTextBox.toString());
    }

    @Test
    public void testCentred()
    {
        LeafTextBox leafTextBox = LeafTextBox.centred("foo", 5);
        assertEquals(" foo ", leafTextBox.getLine(1));

        leafTextBox = LeafTextBox.centred("foo", 4);
        assertEquals("foo ", leafTextBox.getLine(1));

        leafTextBox = LeafTextBox.centred("foo", 2);
        assertEquals("fo", leafTextBox.getLine(1));
    }

    @Test
    public void testRighted()
    {
        assertEquals("  foo", LeafTextBox.righted("foo", 5).getLine(1));
    }

    @Test
    public void testLefted()
    {
        assertEquals("foo  ", LeafTextBox.lefted("foo", 5).getLine(1));
        assertEquals("foo", LeafTextBox.lefted("foo", 3).getLine(1));
    }

    @Test
    public void testWithMargins()
    {
        assertEquals("foo  ", LeafTextBox.lefted("foo", 5).getLine(1));
        assertEquals("foo", LeafTextBox.lefted("foo", 3).getLine(1));
    }

    @Test
    public void withShouldTrim()
    {
        assertEquals("a", LeafTextBox.with(1, 1, "ab\ncde").toString());
        assertEquals("a\nc\n", LeafTextBox.with(1, 2, "ab\ncde\nf").toString());
    }

    @Test
    public void withShouldPadAndTrim()
    {
        assertEquals("a\nc\n\n", LeafTextBox.with(1, 3, "ab\ncde").toString());
    }

    @Test
    public void testWrap()
    {
        LeafTextBox wrap = LeafTextBox.wrap(2, "abcdefg");

        assertEquals(2, wrap.width);
        assertEquals(4, wrap.height);

        LeafTextBox wrap2 = LeafTextBox.wrap(6, "Mind how you go");

        String expected = ""
                + "Mind h\n"
                + "ow you\n"
                + " go\n";

        assertEquals(expected, wrap2.toString());
    }

    @Test
    public void testTransformISOControlChars()
    {

        /**
         * test should really be:
         * If a string contains any control characters then each one is replaced by ¬
         * else it is unchanged
         */
        String s = "jhgjhgjhgjhgjghm,ghg";
        assertEquals(s, LeafTextBox.transformISOControlChars(s));

        s = "\n\ta";
        assertEquals("¬¬a", LeafTextBox.transformISOControlChars(s));
    }

    public static String getStackTrace(Exception e)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.getBuffer().toString();
    }

    @Test
    public void testLeftJustify()
    {
        LeafTextBox box = LeafTextBox.with("abcde\nfghi\njk");

        for (int w = 0; w <= 10; w++)
        {
            AbstractTextBox boxJ = box.leftJustifyIn(w);

            assertEquals(w, boxJ.width);

            for (int i = 1; i <= boxJ.height; i++)
                assertEquals(w, boxJ.getLine(i).length());
        }
    }
}