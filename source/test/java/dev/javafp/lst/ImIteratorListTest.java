package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImIteratorListTest
{

    @Test
    public void testIteratorListCaches()
    {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

        ImList<Integer> list1 = ImList.onAll(list);

        assertEquals(Range.oneTo(5), list1);
        assertEquals(Range.oneTo(5), list1);

        TestUtils.assertSameElements(list1, list1);

    }

}