package dev.javafp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassUtilsTest
{

    @Test
    public void testSimpleName()
    {
        assertEquals("String", String.class.getSimpleName());

        assertEquals("String", String.class.getSimpleName());
    }

}