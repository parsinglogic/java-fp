package dev.javafp.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIteratorTest
{
    @Test
    public void testEmpty()
    {
        Iterator<Integer> e = ArrayIterator.on(new Integer[] {});
        Assert.assertFalse(e.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyThrowsOnNext()
    {
        Iterator<Integer> e = ArrayIterator.on(new Integer[] {});
        e.next();
    }

    @Test
    public void testSmall()
    {
        Iterator<String> e = ArrayIterator.on(new String[] { "a", "b" });
        Assert.assertTrue(e.hasNext());
        Assert.assertEquals("a", e.next());

        Assert.assertTrue(e.hasNext());
        Assert.assertEquals("b", e.next());

        Assert.assertFalse(e.hasNext());
    }
}