package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MixerTest
{
    @Test
    public void testOne() throws Exception
    {
        assertEquals(ImList.on(ImList.on()), Mixer.mix(ImList.on(), ImList.on()));
    }

    @Test
    public void testTwo() throws Exception
    {
        assertEquals("[[1, 2, 3, 4], [1, 3, 2, 4], [1, 3, 4, 2], [3, 1, 2, 4], [3, 1, 4, 2], [3, 4, 1, 2]]", //
                "" + Mixer.mix(ImList.on(1, 2), ImList.on(3, 4)));
    }

    @Test
    public void testMixIsOk() throws Exception
    {
        for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++)
                testMixIsOk(Range.inclusive(0, i), Range.inclusive(0 + 10, j + 10));
    }

    public <T> void testMixIsOk(ImList<T> one, ImList<T> two)
    {

        // Let's just check that one and two are distinct
        assertEquals(one.prependAll(two).toSet().size(), one.size() + two.size());

        ImList<ImList<T>> mixed = Mixer.mix(one, two);

        // Take the permutations of one ++ two and then observe that out of one.size()! lists, the elements of
        // one will be in the wrong order
        int s = Sums.factorial(one.size() + two.size()) / (Sums.factorial(one.size()) * Sums.factorial(two.size()));
        assertEquals(s, mixed.size());

        // Each list in mixed is distinct
        assertEquals(mixed.toSet().size(), mixed.size());

        for (ImList<T> m : mixed)
        {
            assertEquals(true, one.isSubSequence(m));
            assertEquals(true, two.isSubSequence(m));
            assertEquals(one.size() + two.size(), m.size());
        }
    }
}