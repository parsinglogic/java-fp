package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by aove215 on 14/07/2016.
 */
public class CanAccessClassesWhenCompilingTest
{

    @Test
    public void testCanCreatePrimitiveArray()
    {
        int[] ints = { 2, 4, 6 };

        ImList<Integer> is = ImList.onPrimitiveArray(ints);

        assertEquals(ImList.on(2, 4, 6, 8), is.appendElement(8));
        assertEquals(ImList.on(6, 8), is.appendElement(8).drop(2));
        assertEquals(ImList.on(2, 4), is.appendElement(8).take(2));
        assertEquals(ImList.on(2, 4), is.take(2));
    }

}