package dev.javafp.lst;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImMappedListTest
{

    @Test
    public void testMappedListCaches()
    {
        ImList<Integer> one = Range.oneTo(5);

        ImList<Integer> list = one.map(i -> i + 1);
        assertEquals(Range.inclusive(2, 6), list);
    }

    @Test
    public void testTwo()
    {

        List<Integer> a = ImList.on(1, 10, 3, 4, 9).toList();

        ImList<Integer> one = ImList.onAll(a);

        ImList<Integer> list = one.map(i -> i + 1);
        ImList<Integer> list2 = one.map(i -> i + 2);

        ImList<Integer> f = list2.filter(i -> list.contains(i));

        f.flush();
    }

    @Test
    public void testNulls()
    {
        ImList<Integer> l = ImList.on((Integer) null);

        // This won't throw an exception because of laziness!
        l.map(i -> i.getClass());

    }
}