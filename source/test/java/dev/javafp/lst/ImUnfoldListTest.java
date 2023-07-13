package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ImUnfoldListTest
{

    @Test
    public void testUnfoldListCaches()
    {
        ImList<Integer> list1 = ImList.unfold(1, i -> step(i)).take(5);

        list1.head();
        list1.tail();
        list1.tail();

        TestUtils.assertSameElements(list1, list1);
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