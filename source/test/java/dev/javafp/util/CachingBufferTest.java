package dev.javafp.util;

import dev.javafp.ex.ArgumentShouldNotBeLessThan;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CachingBufferTest
{

    @Test
    public void testThrowsIfInvalidSize()
    {
        TestUtils.assertThrows(() -> new CachingBuffer(0), ArgumentShouldNotBeLessThan.class, "Argument size with value 0 is out of range. It should be >= 1");
    }

    @Test
    public void getString()
    {
        CachingBuffer b = new CachingBuffer(2);

        assertEquals("", b.getString());

        b.print("a");
        b.print("b");
        b.println("c");

        assertEquals("bc\n", b.getString());
    }
}