package dev.javafp.util;

import junit.framework.TestCase;
import org.junit.Test;

import static dev.javafp.util.ImMaybe.just;

public class ImMaybeTest extends TestCase
{
    @Test
    public void testJoin()
    {
        assertEquals(ImMaybe.nothing(), ImMaybe.join(ImMaybe.nothing()));
        assertEquals(just(1), ImMaybe.join(just(just(1))));
    }
}