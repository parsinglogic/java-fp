package dev.javafp.box;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractTextBoxTest
{

    @Test
    public void testBoxed()
    {
        LeafTextBox leafTextBox = LeafTextBox.with("ab\ncde");

        String expected = ""
                + "┌─────┐\n"
                + "│ ab  │\n"
                + "│ cde │\n"
                + "└─────┘\n";

        assertEquals(expected, leafTextBox.boxed().toString());

    }

}