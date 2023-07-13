package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilTest
{

    ImList<Double> ds = ImList.on(1.0, 2.0, 3.0, 4.0, 5.0);
    private double delta = 0.000000001;

    @Test
    public void testSplitInto2()
    {
        assertEquals(Integer.valueOf(1024), Util.until(i -> i > 1000, i -> i * 2, 1));
    }

    @Test
    public void testSum()
    {
        assertEquals(15.0, Util.sum(ds), delta);
    }

    @Test
    public void testSumOfStrings()
    {
        assertEquals(6.0, Util.sum(ImList.on("a", "ab", "", "abc"), s -> s.length() + 0.0), delta);
    }

    @Test
    public void testMin()
    {
        assertEquals(1.0, Util.min(ds), delta);
    }

    @Test
    public void testMax()
    {
        assertEquals(5.0, Util.max(ds), delta);
    }

    @Test
    public void testToPx()
    {
        assertEquals("3.1px", Util.toPx(3.1));
        assertEquals("3.5px", Util.toPx(3.54));
        assertEquals("3.5px", Util.toPx(3.549));
        assertEquals("3.6px", Util.toPx(3.55));
        assertEquals("12345678.1px", Util.toPx(12345678.12345));
    }
}