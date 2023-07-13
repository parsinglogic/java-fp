package dev.javafp.util;

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class CollectionUtilsTest
{

    @Test
    public void testAny() throws Exception
    {
        Collection<Integer> is = new HashSet<Integer>();

        assertEquals(null, CollectionUtils.any(is));

        is.add(1);

        assertEquals(Integer.valueOf(1), CollectionUtils.any(is));
        is.add(2);

        assertEquals(Integer.valueOf(1), CollectionUtils.any(is));
    }
}