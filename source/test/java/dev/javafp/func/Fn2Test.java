package dev.javafp.func;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Fn2Test
{

    @Test
    public void testOne()
    {
        Fn<Integer, Integer> f = i -> i + 1;

        assertEquals((Integer) 2, f.of(1));

        Fn2<Integer, Integer, Integer> f2 = (i, j) -> i - j;

        assertEquals((Integer) 2, f2.of(4, 2));

        assertEquals((Integer) 4, f2.ofSecond(1).of(5));
        assertEquals((Integer) 5, f2.ofFirst(10).of(5));
    }

}