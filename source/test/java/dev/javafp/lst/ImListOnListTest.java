package dev.javafp.lst;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ImListOnListTest
{

    @Test
    public void testToList()
    {

        List<Integer> list = Arrays.asList(1, 2, 3);

        ImList<Integer> l = ImList.onList(list);

        assertSame(list, l.toList());

    }

    @Test
    public void testAppendElement()
    {
        ImList<Integer> list = ImList.onList(Arrays.asList(1, 2, 3));

        assertEquals(ImListOnList.class, list.getClass());

        ImList<Integer> list2 = list.appendElement(4).appendElement(5);

        assertEquals(ImListOnArray.class, list2.getClass());

        assertEquals("[1, 2, 3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElementAfterDrop()
    {
        ImList<Integer> list = ImList.onList(Arrays.asList(1, 2, 3));

        assertEquals(ImListOnList.class, list.getClass());

        ImList<Integer> list2 = list.drop(2).appendElement(4).appendElement(5);

        assertEquals(ImListOnArray.class, list2.getClass());

        assertEquals("[3, 4, 5]", list2.toString());
    }
}