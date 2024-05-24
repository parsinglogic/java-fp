package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HashTest
{

    @Test
    public void testOne()
    {
        int k;

        Object[] x = { 1, 2 };

        Object y = x;

        ImList<Integer> l = ImList.on(1, 2);

        assertEquals(l.hashCode(), Hash.hashCodeOf(y));

    }
}