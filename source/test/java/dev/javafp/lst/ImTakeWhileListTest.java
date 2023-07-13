package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ImTakeWhileListTest
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
    public void testTakeWhileHandlesInfinite()
    {
        ImList<Integer> one = ImList.unfold(1, i -> i + 1);

        ImList<Integer> list = one.takeWhile(i -> i <= 10);

        assertEquals(Range.inclusive(1, 10), list);
    }

    @Test
    public void testTakeWhileCaches()
    {
        ImList<Integer> one = Range.oneTo(20);

        ImList<Integer> list1 = one.takeWhile(i -> foo(i));

        list1.head();
        list1.tail();
        list1.tail();

        TestUtils.assertSameElements(list1, list1);
    }

    private Set<Integer> seenAlready = new HashSet<>();

    private boolean foo(Integer i)
    {
        if (seenAlready.contains(i))
            Assert.fail("foo called again on " + i);
        else
            seenAlready.add(i);

        return i <= 5;
    }

}