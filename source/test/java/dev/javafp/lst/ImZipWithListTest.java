package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ImZipWithListTest
{
    @Test
    public void testZip()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);
        ImList<Integer> two = ImList.on(1, 2, 3);

        ImList<Integer> zipped = one.zipWith(two, (i, j) -> zip(i, j));
        assertEquals(ImList.on(2, 4, 6), zipped);
        assertEquals(ImList.on(2, 4, 6), zipped);

        TestUtils.assertSameElements(zipped, zipped);
    }

    private Set<Integer> seenAlready = new HashSet<>();

    private Integer zip(Integer i, Integer j)
    {
        if (seenAlready.contains(i))
            Assert.fail("step called again on " + i);
        else
            seenAlready.add(i);

        return i + j;
    }

}