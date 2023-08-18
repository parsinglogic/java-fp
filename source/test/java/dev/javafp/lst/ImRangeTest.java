package dev.javafp.lst;

import dev.javafp.ex.InvalidArgument;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImRangeTest
{

    @Test
    public void testSomeCases()
    {
        assertEquals(ImList.on(), ImRange.inclusive(1, 0));
        assertEquals(ImList.on(1), ImRange.inclusive(1, 1));
        assertEquals(ImList.on(1, 2, 3), ImRange.inclusive(1, 3));
        assertEquals(ImList.on(1, 4), ImRange.inclusive(1, 4, 3));
        assertEquals(ImList.on(), ImRange.inclusive(4, 3));
    }

    @Test
    public void testErrorCases()
    {
        TestUtils.assertThrows(() -> ImRange.inclusive(1, 4, 4), InvalidArgument.class, "Argument step with value 4: value must divide max - min but step = 4, min = 1, max = 4");
    }
}