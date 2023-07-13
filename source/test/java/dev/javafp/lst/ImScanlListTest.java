package dev.javafp.lst;

import dev.javafp.util.Say;
import dev.javafp.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ImScanlListTest
{
    @Test
    public void testScanl()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);

        ImList<Integer> scanl = one.scanl(10, (z, i) -> step(z + 1));
        assertEquals("[10, 11, 12, 13]", scanl.toString());
        assertEquals(4, scanl.size());

        TestUtils.assertSameElements(scanl, scanl);
    }

    @Test
    public void testScanl0()
    {
        ImList<Integer> one = ImList.on();

        ImList<Integer> scanl = one.scanl(10, (z, i) -> step(z + 1));
        assertEquals("[10]", scanl.toString());
        assertEquals(1, scanl.size());

    }

    @Test
    public void testScanl1()
    {
        ImList<Integer> one = ImList.on(1);

        ImList<Integer> scanl = one.scanl(10, (z, i) -> step(z + 1));
        assertEquals("[10, 11]", scanl.toString());
        assertEquals(2, scanl.size());

    }

    @Test
    public void testScanlAdd()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);

        ImList<Integer> scanl = one.scanl(0, (z, i) -> z + i);
        assertEquals(ImList.on(0, 1, 3, 6), scanl);

    }

    @Test
    public void testRev()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);

        ImList<Integer> rev = one.foldl(ImList.on(), (z, i) -> z.push(i));
        Say.say(rev);

        ImList<ImList<Integer>> revs = one.scanl(ImList.on(), (z, i) -> z.push(i));
        Say.say(revs);

    }

    private Set<Integer> seenAlready = new HashSet<>();

    private Integer step(Integer i)
    {
        if (seenAlready.contains(i))
            Assert.fail("step called again on " + i);
        else
            seenAlready.add(i);

        return i;
    }

}