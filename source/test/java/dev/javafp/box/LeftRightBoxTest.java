package dev.javafp.box;

import org.junit.Test;

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
        assertEquals(4, lr.getWidth());
        assertEquals(3, lr.getHeight());

        assertEquals(" X  ", lr.getLine(1));
        assertEquals("ABCD", lr.getLine(2));
        assertEquals(" Y  ", lr.getLine(3));

        System.out.println(lr);
    }

    @Test
    public void testIndented() throws Exception
    {
        assertEquals("  a\n  b\n", LeftRightBox.indent(2, LeafTextBox.with("a\nb")).toString());
    }

}