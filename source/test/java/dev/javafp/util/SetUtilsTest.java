package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.util.Set;

import static dev.javafp.util.SetUtils.diff;
import static dev.javafp.util.SetUtils.intersect;
import static dev.javafp.util.SetUtils.newSet;
import static dev.javafp.util.SetUtils.remove;
import static dev.javafp.util.SetUtils.union;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetUtilsTest
{

    @Test
    public void testUnionMatchesContainsAll()
    {
        Set<Integer> s1 = newSet(1, 2, 3);
        Set<Integer> s2 = newSet(3, 4);
        Set<Integer> s3 = newSet(1, 5, 6);

        Set<Integer> union = union(s1, s2, s3);

        assertTrue(union.containsAll(s1));
        assertTrue(union.containsAll(s2));
        assertTrue(union.containsAll(s3));

    }

    @Test
    public void testIntersectMatchesContainsAll()
    {
        Set<Integer> allSet = newSet(1, 2, 3, 4);
        ImList<Integer> allList = ImList.onAll(allSet);

        ImList<ImList<Integer>> powerSet = allList.powerSet();

        for (ImPair<ImList<Integer>, ImList<Integer>> p : powerSet.cartesianProduct())
        {
            Set<Integer> s1 = p.fst.toSet();
            Set<Integer> s2 = p.snd.toSet();
            Set<Integer> inBoth = intersect(s1, s2);

            assertTrue(s1.containsAll(inBoth));
            assertTrue(s2.containsAll(inBoth));

            assertEquals(s1, union(diff(s1, s2), inBoth));
            assertEquals(s2, union(diff(s2, s1), inBoth));

        }
    }

    @Test
    public void testRemoveMatchesDiff()
    {
        Set<Integer> one = newSet(1);
        Set<Integer> allSet = newSet(1, 2, 3, 4);
        ImList<Integer> allList = ImList.onAll(allSet);

        ImList<ImList<Integer>> powerSet = allList.powerSet();

        for (ImList<Integer> l : powerSet)
        {
            Set<Integer> set = l.toSet();

            Set<Integer> diff = diff(set, one);

            Set<Integer> removed = remove(set, 1);

            assertEquals(diff, removed);

            // If the element ws not in the set, then remove should return the original set
            assertEquals(diff.equals(set), removed == set);
        }
    }
}