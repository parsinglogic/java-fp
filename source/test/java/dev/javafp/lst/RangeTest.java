package dev.javafp.lst;

import dev.javafp.ex.InvalidArgument;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RangeTest
{

    @Test
    public void testSomeCases()
    {
        assertEquals(ImList.on(), Range.inclusive(1, 0));
        assertEquals(ImList.on(1), Range.inclusive(1, 1));
        assertEquals(ImList.on(1, 2, 3), Range.inclusive(1, 3));
        assertEquals(ImList.on(1, 4), Range.inclusive(1, 4, 3));
        assertEquals(ImList.on(), Range.inclusive(4, 3));
    }

    @Test
    public void testErrorCases()
    {
        TestUtils.assertThrows(() -> Range.inclusive(1, 4, 4), InvalidArgument.class, "step value must divide max - min but step = 4, min = 1, max = 4");
    }
}