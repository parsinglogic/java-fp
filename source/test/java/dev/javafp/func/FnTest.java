package dev.javafp.func;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FnTest
{
    @Test
    public void testOne()
    {
        Fn<Integer, Integer> plusOne = i -> i + 1;

        assertEquals((Integer) 2, plusOne.of(1));

        Fn<Integer, Integer> timesTwo = i -> i * 2;

        assertEquals((Integer) 6, timesTwo.of(3));
    }

    @Test
    public void testCompose()
    {
        Fn<Integer, Integer> plusOne = i -> i + 1;

        Fn<Integer, Integer> timesTwo = i -> i * 2;

        Fn<Integer, Integer> composed = plusOne.then(timesTwo);

        assertEquals((Integer) 8, composed.of(3));

        Fn<Integer, Integer> composed2 = composed.then(timesTwo);

        assertEquals((Integer) 20, composed2.of(4));

    }

    @Test
    public void testNot()
    {
        Fn<Integer, Boolean> isEven = i -> i % 2 == 0;
        Fn<Integer, Boolean> isOdd = Fn.not(isEven);

        assertNotEquals(isEven.of(1), isOdd.of(1));
        assertNotEquals(isEven.of(2), isOdd.of(2));
    }

}