package dev.javafp.eq;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.time.StopWatch;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test shows the difference in speed between different collections
 */
public class EqTest
{

    private final static ImList<Integer> tenMeeelionIntegers = Range.oneTo(10_000_000).flush();
    private final static Integer[] tenMeeelionIntegers2 = tenMeeelionIntegers.toArray(Integer.class);
    private final static ImList<Integer> tenMeeelionIntegers3 = ImList.on(tenMeeelionIntegers2);

    @Test
    public void testLargeEqWithArrayLsts()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers;
        ImList<Integer> range2 = tenMeeelionIntegers3;

        StopWatch s = new StopWatch();
        assertTrue(Eq.uals(range, range2));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeEqualsWithArrayLsts()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers;
        ImList<Integer> range2 = tenMeeelionIntegers3;

        StopWatch s = new StopWatch();
        assertTrue(range.equalsList(range2));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeAppendWithLstsAndEquals()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1);
        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(1);

        StopWatch s = new StopWatch();
        assertTrue(range.equalsList(range2));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeAppendWithLstsAndEq()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1);
        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(1);

        StopWatch s = new StopWatch();
        assertTrue(Eq.uals(range, (range2)));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeAppendWithLstsAndEqAndFlush()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1).flush();
        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(1).flush();

        StopWatch s = new StopWatch();
        assertTrue(Eq.uals(range, range2));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeAppendWithLstsAndEqAndFlush2()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1).flush();
        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(1).flush();

        StopWatch s = new StopWatch();
        assertTrue(Eq.uals(range, range2));
        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void testLargeAppendWithLstsAndEqAndFlush3()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1);
        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(1);

        StopWatch s = new StopWatch();

        Object[] a1 = range.toArray(Object.class);
        Object[] a2 = range2.toArray(Object.class);
        assertTrue(Objects.deepEquals(a1, a2));
        say("elapsed", s.getElapsedMs());

    }

    @Test
    public void timeFlush()
    {
        ImList<Integer> range = tenMeeelionIntegers3.filter(i -> i < Integer.MAX_VALUE);

        say("range", range.getClass());

        StopWatch s = new StopWatch();
        ImList<Integer> r = range.flush();

        say("elapsed flush", s.getElapsedMs());
    }

    @Test
    public void timeFlush2()
    {
        ImList<Integer> range = tenMeeelionIntegers3.filter(i -> i < Integer.MAX_VALUE);

        say("range", range.getClass());

        StopWatch s = new StopWatch();
        ImList<Integer> r = range.flush();

        say("elapsed flush2", s.getElapsedMs());
    }

    @Test
    public void testFlushTime()
    {
        StopWatch s = new StopWatch();

        ImList<Integer> range = tenMeeelionIntegers3.appendElement(1).flush();
        //        ImList<Integer> range2 = tenMeeelionIntegers3.appendElement(2).flush();

        say("elapsed", s.getElapsedMs());
    }

    @Test
    public void equalsStandardJavaLists()
    {
        // These are Java lists
        List<Integer> range = tenMeeelionIntegers.append(ImList.on(1)).toList();
        List<Integer> range2 = tenMeeelionIntegers.append(ImList.on(0)).toList();

        StopWatch s = new StopWatch();
        assertFalse(range.equals(range2));
        say("elapsed", s.getElapsedMs());

        s.reset();
        assertFalse(Objects.deepEquals(range, range2));
        say("elapsed", s.getElapsedMs());

    }

    @Test
    public void testLargeWithConversion()
    {
        // These are the slowest lists
        ImList<Integer> range = tenMeeelionIntegers.append(ImList.on(1));
        ImList<Integer> range2 = tenMeeelionIntegers.append(ImList.on(0));

        StopWatch s = new StopWatch();

        List<Integer> list1 = range.toList();
        List<Integer> list2 = range2.toList();

        assertFalse(range.equalsList(range2));
        say("elapsed", s.getElapsedMs());

    }

    @Test
    public void testLargeWithListsOnJavaArrays()
    {
        // These are Lsts - but on Java arrays
        final ImList<Integer> ints2 = ImList.on(tenMeeelionIntegers.toList());
        ImList<Integer> range = ints2.append(ImList.on(1));
        ImList<Integer> range2 = ints2.append(ImList.on(0));

        StopWatch s = new StopWatch();
        assertFalse(range.equalsList(range2));
        say("elapsed", s.getElapsedMs());

    }

    @Test
    public void testSmallWithLsts()
    {

        ImList<Integer> one = ImList.on(1, 2, 3);
        ImList<Integer> two = one.filter(i -> i < 4);

        say("one", one.getClass());
        say("two", two.getClass());

        say("one.equals(two)", one.equalsList(two));
    }

    @Test
    public void testSmallWithTwoArrays()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);
        ImList<Integer> two = ImList.on(1, 2, 3);

        say("one", one.getClass());
        say("two", two.getClass());

        say("one.equals(two)", one.equalsList(two));

        assertTrue(one.equalsList(two));
    }

    @Test
    public void testSmallWithTwoArraysOfDifferentTypes()
    {
        ImList<Integer> one = ImList.on(1, 2, 3);
        ImList<String> two = ImList.on("1", "2", "3");

        say("one", one.getClass());
        say("two", two.getClass());

        say("one.equals(two)", one.equals(two));

        assertFalse(one.equals(two));
    }

    @Test
    public void testLargeWithArrays()
    {

        // These are the slowest lists
        say("one", tenMeeelionIntegers.getClass());
        say("two", tenMeeelionIntegers3.getClass());

        StopWatch s = new StopWatch();
        assertTrue(tenMeeelionIntegers.equalsList(tenMeeelionIntegers3));
        say("elapsed", s.getElapsedMs());
    }
}