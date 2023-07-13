package dev.javafp.lst;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DropWhileListTest
{
    @Test
    public void testTakeWhileAndDropWhileMatch()
    {

        ImList<Integer> one = Range.oneTo(5);

        for (int i = 0; i <= 5; i++)
        {
            final int j = i;
            ImList<Integer> takeWhile = one.takeWhile(n -> n <= j);
            assertEquals("" + j, one, takeWhile.append(one.dropWhile(n -> n <= j)));
            assertEquals("" + takeWhile, i, takeWhile.size());
        }

    }

    @Test
    public void testDropWhileHandlesInfinite()
    {
        ImList<Integer> one = ImList.unfold(1, i -> i + 1);

        ImList<Integer> list = one.dropWhile(n -> n <= 10);

        assertEquals(Range.inclusive(11, 110), list.take(100));
    }
}