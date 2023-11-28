package dev.javafp.box;

import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class LeftRightBoxTest
{
    @Test
    public void testOne() throws Exception
    {
        /**
         *
         *    +---+ +---+ +---+---+
         *    |   | | X | |       |
         *    +---+ +---+ +---+---+
         *    | A | | B | | C   D |
         *    +---+ +---+ +---+---+ 
         *          | Y |
         *          +---+ 
         *
         *
         *
         *
         *
         */
        LeafTextBox b1 = LeafTextBox.with(1, 2, " \nA");
        LeafTextBox b2 = LeafTextBox.with(1, 3, "X\nB\nY");
        LeafTextBox b3 = LeafTextBox.with(2, 2, "  \nCD");

        LeftRightBox lr = LeftRightBox.with(b1, b2, b3);
        assertEquals(4, lr.width);
        assertEquals(3, lr.height);

        assertEquals(" X  ", lr.getLine(1));
        assertEquals("ABCD", lr.getLine(2));
        assertEquals(" Y  ", lr.getLine(3));

        System.out.println(lr);
        say(lr);
    }

    @Test
    public void testIndented()
    {
        assertEquals("  a\n  b\n", LeftRightBox.indent(2, LeafTextBox.with("a\nb")).toString());
    }

    @Test
    public void testLeftJustify()
    {
        LeafTextBox box = LeafTextBox.with("abcde\nfghi\njk");

        LeftRightBox lr = LeftRightBox.with(box, LeafTextBox.with("xxx"));

        for (int w = 0; w <= 10; w++)
        {
            AbstractTextBox lrj = lr.leftJustifyIn(w);

            assertEquals(w, lrj.width);

            for (int i = 1; i <= lrj.height; i++)
                assertEquals(w, lrj.getLine(i).length());
        }
    }

}