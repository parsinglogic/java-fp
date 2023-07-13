package dev.javafp.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by aove215 on 14/07/2016.
 */
public class ImEitherTest
{

    @Test
    public void testOne()
    {
        ImEither<Integer, Boolean> e = ImEither.Left(3);
        assertEquals("Left 3", e.toString());

        ImEither<Integer, String> r = ImEither.Right("ok");
        assertEquals("Right ok", r.toString());

        ImEither<Integer, String> n1 = ImEither.Right(null);
        assertEquals("Right null", n1.toString());

        ImEither<Integer, String> n2 = ImEither.Left(null);
        assertEquals("Left null", n2.toString());
    }

    @Test
    public void testFlatMap()
    {
        ImEither<Integer, Boolean> e = ImEither.Left(3);

        assertEquals(e, e.flatMap(b -> ImEither.Right(!b)));
        assertEquals(ImEither.Right(false), ImEither.Right(true).flatMap(b -> ImEither.Right(!b)));
    }

    @Test
    public void testMatch()
    {
        ImEither<Integer, Boolean> e = ImEither.Left(3);

        assertEquals((int) 4, (int) e.match(i -> i + 1, b -> 3));
    }

    @Test
    public void testEquals()
    {
        ImEither<Integer, Integer> l = ImEither.Left(1);
        ImEither<Integer, Integer> r = ImEither.Right(1);
        ImEither<Integer, Integer> t = ImEither.Right(1);

        assertFalse(l.equals(r));
        assertTrue(r.equals(r));
        assertTrue(r.equals(t));
    }
}