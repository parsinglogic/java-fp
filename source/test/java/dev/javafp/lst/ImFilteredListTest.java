package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ImFilteredListTest
{

    @Test
    public void testFilteredListCaches()
    {
        ImList<Integer> one = Range.oneTo(10);
        ImList<Integer> list1 = one.filter(i -> pred(i));

        list1.head();
        list1.tail();
        list1.tail();

        TestUtils.assertSameElements(list1, list1);

    }

    private Set<Integer> seenAlready = new HashSet<>();

    private boolean pred(Integer i)
    {
        if (seenAlready.contains(i))
            Assert.fail("step called again on " + i);
        else
            seenAlready.add(i);

        return i <= 5;
    }

}