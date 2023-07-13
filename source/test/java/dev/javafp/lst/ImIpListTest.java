package dev.javafp.lst;

import dev.javafp.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImIpListTest
{

    @Test
    public void testPermutations()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);

        ImList<ImList<Integer>> permutations = list.permutations();
        assertEquals("[[1, 2, 3], [2, 1, 3], [2, 3, 1], [1, 3, 2], [3, 1, 2], [3, 2, 1]]", "" + permutations);

        TestUtils.assertSameElements(permutations, permutations);

    }

}