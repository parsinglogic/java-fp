package dev.javafp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TableTest
{
    @Test
    public void testTable()
    {
        String expected = "aab\n"
                + "c\n"
                + "d eeefffffff\n"
                + "";
        assertEquals(expected, Table.on("aa", "b").row("c").row("d", "eee", "fffffff").toString());
    }

}