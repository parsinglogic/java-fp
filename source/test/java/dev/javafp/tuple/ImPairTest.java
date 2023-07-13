package dev.javafp.tuple;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImPairTest
{

    @Test
    public void testEqualsWithNullEtc()
    {
        ImPair<Integer, Integer> p1 = new ImPair(1, 2);
        ImPair<String, Integer> p2 = new ImPair("1", 2);

        assertEquals(false, p1.equals(null));
        assertEquals(false, p1.equals(p2));
        assertEquals(false, p2.equals(p1));
    }

    @Test
    public void testEquals()
    {
        List<Integer> list = Arrays.asList(null, 1, 2, 2);

        List<ImPair<Integer, Integer>> pairs = new ArrayList();
        for (Integer i : list)
        {
            for (Integer j : list)
            {
                pairs.add(new ImPair(i, j));
            }
        }

        for (ImPair<Integer, Integer> p1 : pairs)
        {
            for (ImPair<Integer, Integer> p2 : pairs)
            {
                assertTrue(p1.equals(p1));
                assertTrue(p2.equals(p2));

                boolean expected = eq(p1.fst, p2.fst) && eq(p1.snd, p2.snd);
                assertEquals(expected, p1.equals(p2));
                assertEquals(expected, p2.equals(p1));
                if (p1.equals(p2))
                {
                    assertEquals(p1.hashCode(), p2.hashCode());
                }
            }
        }
    }

    private boolean eq(Integer i1, Integer i2)
    {
        if (i1 == i2)
            return true;

        if (i1 == null || i2 == null)
            return false;

        return i1.equals(i2);
    }
}