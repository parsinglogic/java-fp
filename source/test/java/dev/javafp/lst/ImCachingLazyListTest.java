package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ImCachingLazyListTest
{

    @Test
    public void testMappedListCaches()
    {
        ImList<Integer> one = Range.oneTo(5);

        ImList<Integer> list = one.map(i -> step(i));
        TestUtils.assertSameElements(list, list);

        list.head();
        list.head();
        list.tail();
        list.tail();
        list.at(2);
        list.at(2);
    }

    private Set<Integer> seenAlready = new HashSet<>();

    private Integer step(Integer i)
    {

        if (seenAlready.contains(i))
            Assert.fail("step called again on " + i);
        else
            seenAlready.add(i);

        return i + 1;
    }

}