package dev.javafp.lst;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImRepeatListTest
{

    @Test
    public void testTake()
    {
        assertEquals(ImList.on(1, 1), ImList.repeat(1).take(2));
    }

}