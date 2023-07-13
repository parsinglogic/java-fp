package dev.javafp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassUtilsTest
{

    @Test
    public void testSimpleName()
    {
        assertEquals("String", ClassUtils.simpleName(String.class));
    }

}