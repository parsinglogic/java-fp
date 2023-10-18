package dev.javafp.lst;

import dev.javafp.ex.SizeOnInfiniteList;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SzTest
{

    private final List<Integer> oneTwoThree = Arrays.asList(1, 2, 3);
    private final ImList<Integer> listKF = ImList.on(oneTwoThree);
    private final ImList<Integer> listKI = ImList.repeat(1);
    private final ImList<Integer> listUF = listKF.filter(i -> i > 0);
    private final ImList<Integer> listUU = ImList.unfold(1, i -> i + 1).takeWhile(i -> i < 4);

    @Test
    public void testAllSizes()
    {

        // Check sizes on these test lists
        checkKF(listKF, 3);
        checkKI(listKI);
        checkKI(listKI);
        checkUF(listUF, 3);
        checkUU(listUU, 3);

        // start - Free standing lists:

        // repeat -> KI
        checkKI(ImList.repeat(1));

        // unfold -> KI
        checkKI(ImList.unfold(1, i -> i + 1));

        // empty -> KF
        checkKF(ImList.on(), 0);

        // List -> KF
        checkKF(listKF, 3);

        // Array -> KF
        checkKF(ImList.on(1, 2, 3), 3);

        // Iterator -> UU
        checkUU(ImList.onIterator(oneTwoThree.iterator()), 3);

        // end - Free standing lists:

        // Lists on a list

        // filter 
        // -----------------------------------------

        // Filter KF -> UF
        checkUF(listKF.filter(i -> i > 0), 3);

        // Filter KI -> UU
        checkUU(listKI.filter(i -> i > 0));

        // Filter UF -> UF
        checkUF(listUF.filter(i -> i > 0), 3);

        // Filter UU -> UU
        checkUU(listUU.filter(i -> i > 0));

        // map 
        testNormal(l -> l.map(i -> i), 3);

        // ip
        testNormal(l -> new ImIpList(ImList.on(), 2, l), 4);

        // cons
        testNormal(l -> ImList.cons(2, l), 4);

        // tails
        testNormal(l -> l.tails(), 4);

        // scanl
        testNormal(l -> l.scanl(100, (i, j) -> i + j), 4);

        // drop
        testNormal(l -> l.drop(1), 2);

        // take
        testTake();

        // dropWhile
        testNormal(l -> l.dropWhile(i -> i < 0), 3);

        // takeWhile
        testTakeWhile(l -> l.takeWhile(i -> i < 3), 2);

        // Lists on two or more list

        // append
        testAppend((l, m) -> l.append(m), 6);

        // append
        testZipWith((l, m) -> l.zip(m), 3);
    }

    @Test
    public void testJoin()
    {
        ImList<ImList<Integer>> all = ImList.on(listKF, listUF, listUU, listKI);

        ImList<ImList<ImList<Integer>>> cross = ImList.cross(ImList.repeat(all, 4));

        int j = 0;
        for (ImList<ImList<Integer>> list : cross)
        {
            ImList<Integer> join = ImList.join(list);

            ImList<Integer> join12 = join.take(12);

            //TestUtils.show("join " + (++j), join12);

            assertEquals(ImLazyList.UNKNOWN_UNKNOWN, Sz.getSz(join));

            //            if (join.getSz() == LazyList.KNOWN_INFINITE)
            //            {
            //                // At least one must be KI
            //                assertTrue(list.filter(i -> i.getSz() == LazyList.KNOWN_INFINITE).size() >= 1);
            //            }
            //
            //            if (join.getSz() >= 0)
            //            {
            //                // All must be KI
            //                assertTrue(list.all(i -> i.getSz() >= 0));
            //            }
            //
            //            if (join.getSz() == LazyList.UNKNOWN_FINITE)
            //            {
            //                // None are KI
            //                assertFalse(list.any(i -> i.getSz() == LazyList.KNOWN_INFINITE));
            //
            //                // At least one is UF
            //                assertTrue(list.any(i -> (i.getSz() == LazyList.UNKNOWN_FINITE)));
            //
            //                if (j == 7)
            //                {
            //
            //                    TestUtils.show("types", list.map(i -> ImPair.on(i.getClass(), i.getSz())));
            //
            //                    System.out.println(list.any(i -> i.getSz() == LazyList.UNKNOWN_UNKNOWN));
            //                    System.out.println(list.filter(i -> i.getSz() == LazyList.UNKNOWN_UNKNOWN));
            //                }
            //
            //                // None are UU
            //                assertFalse(list.any(i -> i.getSz() == LazyList.UNKNOWN_UNKNOWN));
            //            }
            //
            //            if (join.getSz() == LazyList.UNKNOWN_UNKNOWN)
            //            {
            //                // None are KI
            //                assertFalse(list.any(i -> i.getSz() == LazyList.KNOWN_INFINITE));
            //
            //                // At least one is UU
            //                assertTrue(list.any(i -> i.getSz() == LazyList.UNKNOWN_UNKNOWN));
            //            }

        }

    }

    private void testAppend(Fn2<ImList<Integer>, ImList<Integer>, ImList<Integer>> fn, int count)
    {
        /**
         *        | KF | KI | UF | UU |
         *
         *     KF | KF | KI | UF | UU |
         *
         *     KI |    | KI | KI | KI |   
         *
         *     UF |    |    | UF | UU |   
         *
         *     UU |    |    |    | UU |   
         */

        // Row 1

        // KF * KF -> KF
        checkKF(fn.of(listKF, listKF), count);

        // KF * KI -> KI
        checkKI(fn.of(listKF, listKI));
        checkKI(fn.of(listKI, listKF));

        // KF * UF -> UF
        checkUF(fn.of(listKF, listUF), count);
        checkUF(fn.of(listUF, listKF), count);

        System.out.println(fn.of(listKF, listUU));

        // KF * UU -> UU
        checkUU(fn.of(listKF, listUU), count);
        checkUU(fn.of(listUU, listKF), count);

        // Row 2

        // KI * KI -> KI
        checkKI(fn.of(listKI, listKI));

        // KI * UF -> KI
        checkKI(fn.of(listKI, listUF));
        checkKI(fn.of(listUF, listKI));

        // KI * UU -> KI
        checkKI(fn.of(listKI, listUU));
        checkKI(fn.of(listUU, listKI));

        // Row 3

        // UF * UF -> UF
        checkUF(fn.of(listUF, listUF), count);

        // UF * UU -> UU
        checkUU(fn.of(listUF, listUU), count);
        checkUU(fn.of(listUU, listUF), count);

        // Row 4
        // UU * UU -> UU
        checkUU(fn.of(listUU, listUU));
    }

    private void testZipWith(Fn2<ImList<Integer>, ImList<Integer>, ImList<ImPair<Integer, Integer>>> fn, int count)
    {
        /**
         *        | KF | KI | UF | UU |
         *
         *     KF | KF | KF | UF | KF |
         *
         *     KI |    | KI | UF | UU |   
         *
         *     UF |    |    | UF | UF |   
         *
         *     UU |    |    |    | UU |   
         */

        // Row 1

        // KF * KF -> KF
        checkKF(fn.of(listKF, listKF), count);

        // KF * KI -> KF
        checkKF(fn.of(listKF, listKI), count);
        checkKF(fn.of(listKI, listKF), count);

        // KF * UF -> UF
        checkUF(fn.of(listKF, listUF), count);
        checkUF(fn.of(listUF, listKF), count);

        // KF * UU -> KF
        checkKF(fn.of(listKF, listUU), count);
        checkKF(fn.of(listUU, listKF), count);

        // Row 2

        // KI * KI -> KI
        checkKI(fn.of(listKI, listKI));

        // KI * UF -> UF
        checkUF(fn.of(listKI, listUF), count);
        checkUF(fn.of(listUF, listKI), count);

        // KI * UU -> UU
        checkUU(fn.of(listKI, listUU));
        checkUU(fn.of(listUU, listKI));

        // Row 3

        // UF * UF -> UF
        checkUF(fn.of(listUF, listUF), count);

        // UF * UU -> UF
        checkUF(fn.of(listUF, listUU), count);
        checkUF(fn.of(listUU, listUF), count);

        // Row 4
        // UU * UU -> UU
        checkUU(fn.of(listUU, listUU));
    }

    private <B> void testNormal(Fn<ImList<Integer>, ImList<B>> fn, int count)
    {
        System.out.println(fn.of(listKF));

        checkKF(fn.of(listKF), count);

        // map KI -> KI
        checkKI(fn.of(listKI));

        // map UF -> UF
        checkUF(fn.of(listUF), count);

        // map UU -> UU
        checkUU(fn.of(listUU));

    }

    private <B> void testTakeWhile(Fn<ImList<Integer>, ImList<B>> fn, int count)
    {
        System.out.println(fn.of(listKF));

        // KI -> UF
        checkUF(fn.of(listKF), count);

        // KI -> UU
        checkUU(fn.of(listKI));

        // map UF -> UF
        checkUF(fn.of(listUF), count);

        // map UU -> UU
        checkUU(fn.of(listUU));

    }

    private <B> void testTake()
    {
        checkKF(listKF.take(2), 2);

        // map KI -> KF
        checkKF(listKI.take(2), 2);

        // map UF -> UF
        checkUF(listUF.take(2), 2);

        // map UU -> UF
        checkUF(listUU.take(2), 2);

    }

    private <A> void checkKF(ImList<A> list, int expectedSize)
    {
        assertTrue(list.isEmpty()
                   ? list.size() == 0
                   : list.size() > 0);

        assertEquals(list.size(), Sz.getSz(list));
        assertEquals(expectedSize, list.size());
    }

    private <A> void checkUU(ImList<A> list)
    {

        assertEquals(ImLazyList.UNKNOWN_UNKNOWN, Sz.getSz(list));
    }

    private <A> void checkUU(ImList<A> list, int expectedSize)
    {
        assertEquals(ImLazyList.UNKNOWN_UNKNOWN, Sz.getSz(list));
        assertEquals(expectedSize, list.size());
    }

    private <A> void checkUF(ImList<A> list, int expectedSize)
    {
        assertEquals(ImLazyList.UNKNOWN_FINITE, Sz.getSz(list));
        assertEquals(expectedSize, list.size());
    }

    private <A> void checkKI(ImList<A> inf)
    {
        assertEquals(ImLazyList.KNOWN_INFINITE, Sz.getSz(inf));

        try
        {
            assertEquals(1, inf.size());
            TestUtils.failExpectedException(SizeOnInfiniteList.class);
        } catch (SizeOnInfiniteList e)
        {
        }
    }

    @Test
    public void testAppend()
    {
        checkKI(listKI);

        checkKI(listKI.append(listKI));

    }

}