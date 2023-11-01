package dev.javafp.box;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TopDownBoxTest
{
    @Test
    public void testOne() throws Exception
    {
        /**
         *
         *    +---+---+---+---+---+ 
         *    | A |   |   | B |   | 
         *    +---+---+---+---+---+ 
         *    +---+      
         *    | C |
         *    +---+
         *    +---+---+---+ 
         *    | D | E | F | 
         *    +---+---+---+ 
         *    +---+---+
         *    | G | H |
         *    +---+---+ 
         */
        LeafTextBox b1 = LeafTextBox.with(5, 1, "A  B");
        LeafTextBox b2 = LeafTextBox.with(1, 1, "C");
        LeafTextBox b3 = LeafTextBox.with(3, 1, "DEFX");
        LeafTextBox b4 = LeafTextBox.with(2, 1, "GH");

        TopDownBox td = TopDownBox.with(b1, b2, b3, b4);

        assertEquals(5, td.width);
        assertEquals(4, td.height);

        assertEquals("A  B ", td.getLine(1));
        assertEquals("C    ", td.getLine(2));
        assertEquals("DEF  ", td.getLine(3));
        assertEquals("GH   ", td.getLine(4));
        assertEquals("     ", td.getLine(5));

        System.out.println(td);
    }

}