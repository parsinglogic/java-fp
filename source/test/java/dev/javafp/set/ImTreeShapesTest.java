package dev.javafp.set;

import org.junit.Test;

import java.util.List;

import static dev.javafp.util.ImTestHelper.assertTreeIs;
import static org.junit.Assert.assertEquals;

public class ImTreeShapesTest
{
    @Test
    public void testZero()
    {
        List<ImTree<Character>> zeros = new ImTreeShapes().withSize(0);

        assertEquals(1, zeros.size());
        assertEquals(ImTree.nil, zeros.get(0));
    }

    @Test
    public void testOne()
    {
        List<ImTree<Character>> ones = new ImTreeShapes().withSize(1);

        assertEquals(1, ones.size());
        assertTreeIs("a1", ones.get(0));
    }

    @Test
    public void testTwo()
    {
        List<ImTree<Character>> twos = new ImTreeShapes().withSize(2);

        assertEquals(2, twos.size());
        assertTreeIs("b1 a2 -2", twos.get(0));
        assertTreeIs("a1 -2 b2", twos.get(1));
    }
}