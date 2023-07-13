package dev.javafp.lst;

import dev.javafp.eq.Eq;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImReverseListTest
{

    /**
     *
     * Let's try to test all the different lists.
     *
     * All the lists:
     *
     * ImList
     *   ImLazyList
     *     ImConsList
     *     ImJoinList
     *     ImTakeList
     *     ImEmptyList
     *     ImRepeatList
     *     ImTailsList
     *     ImAppendList
     *     ImCachingLazyList
     *       ImIpList
     *       ImZipWithList
     *       ImFIteratorList
     *       ImTripleList
     *       ImQuintetList
     *       ImQuartetList
     *       ImPairList
     *       ImFilteredList
     *       ImScanlList
     *       ImListOnReader
     *       ImTakeWhileList
     *       ImUnfoldList
     *       ImListOnReader2
     *       ImMappedList
     *       ImIteratorList
     *   ImEagerList
     *     ImListOnArray
     *     ImListOnList
     *     ImListOnPrimitiveArray
     *     ImListOnString
     *     ImReverseList
     *     ImListOnReader
     *
     *
     *
     * todo
     *
     * tails
     * LstOnZipper
     * zipwith
     * ImTripleList
     * ImQuintetList
     * ImQuartetList
     * ImPairList
     * ImScanlList
     *
     * ImListOnReader2
     *
     * ImListOnPrimitiveArray
     * ImListOnString
     */
    @Test
    public void testEqualsAndReverseOnManyDifferentListTypes()
    {
        /**
         * So this is not really testing ImReverseList on its own.
         */
        ImList<Integer> is = Range.oneTo(7);

        ImList<Integer> orig = ImList.on(1, 2, 3);
        ImList<Integer> rev = ImList.on(3, 2, 1);

        // @formatter:off
        ImList<ImList<? extends Object>> others = ImList.on(

                ImList.on(2, 3).push(1),                                 // cons
                ImList.join(ImList.on(ImList.on(1), ImList.on(2, 3))),            // join
                is.take(3),                                           // take
                orig.map(i -> i),                                     // map
                ImList.on(1).append(ImList.on(2, 3)),                       // append
                ImList.on(1, 3).intersperse(2),                          // Ip
                orig.filter(i -> i <= 3),                             // filter
                is.takeWhile(i -> i <= 3),                            // takeWhile
                ImList.unfold(1, i -> i + 1).take(3),                    // take on an unfold
                ImList.unfold(1, i -> i + 1).takeWhile(i -> i <= 3),     // takeWhile on an unfold
                orig.map(i -> i + i - i),                             // map
                ImList.onIterator(orig.iterator()),                      // iterator
                ImList.on(orig.toList()) ,                               // ImListOnList
                Range.inclusive(1, 3),                                // Range (just an array list )
                ImList.on(0,1,2,3).drop(1),                              // drop
                ImList.on(0,1,2,3).dropWhile(i -> i < 1)                 // dropWhile

            );
        // @formatter:on

        others.foreach(i -> {
            say(i.getClass());
            assertEquals(i, orig);
            assertEquals(orig, i);
            assertEquals(ImList.on(), i.drop(3));
            assertEquals(ImList.on(), i.reverse().drop(3));

        });

        assertTrue(ImList.and(others.map(i -> Eq.uals(i, orig))));
        assertTrue(ImList.and(others.map(i -> Eq.uals(i.reverse(), rev))));
        assertTrue(ImList.and(others.map(i -> Eq.uals(i.reverse().reverse(), orig))));
        assertTrue(ImList.and(others.map(i -> Eq.uals(i.reverse().reverse(), i))));
    }

    @Test
    public void testEquals()
    {
        ImList<Integer> orig = ImList.on(1, 2, 3);

        ImList<Integer> arrayLst = ImList.on(1, 2, 3);
        assertEquals(orig, arrayLst);
    }

    @Test
    public void reverseOnEmpty()
    {
        ImList<Integer> orig = ImList.on();

        assertEquals(orig, orig.reverse());
        assertEquals(orig, orig.reverse().reverse());
    }

}