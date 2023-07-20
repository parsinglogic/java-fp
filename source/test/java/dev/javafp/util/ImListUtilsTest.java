package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import static dev.javafp.lst.ImList.unfold;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImListUtilsTest
{
    @Test
    public void testRandomSubSeq()
    {
        ImList<Integer> range = Range.oneTo(10);

        Range.inclusive(0, range.size()).foreach(i ->
        {

            ImPair<ImList<Integer>, ImList<Integer>> pair = ImListUtils.randomSubSeq(range, i);

            //            say("pair", pair);

            // The first list is the correct size
            assertEquals((int) i, pair.fst.size());

            // If we put the lists together ans sort we get the original
            assertEquals(range, pair.fst.append(pair.snd).sort());

            // They are both subsequences
            assertTrue(pair.fst.isSubSequence(range));
            assertTrue(pair.snd.isSubSequence(range));
        });

    }

    //    @Test
    //    public void testRandomSubSeqIsVaguelyRandom()
    //    {
    //        ImList<Integer> range = Range.oneTo(10);
    //
    //        ImList<ImList<Integer>> subs = ImList.repeat(range, 30).map(r -> ImListUtils.randomSubSeq(r, 5).fst);
    //
    //        //        say(subs.toString("\n"));
    //
    //        ImSet<ImList<Integer>> set = subs.foldl(ImSet.empty(), (z, i) -> z.add(i));
    //
    //        say(set.size());
    //
    //    }

    @Test
    public void testRandomSubSeqIsVaguelyRandom()
    {

        /**
         * A very crude test to assert that if we partition a list of 12 integers into a random sub-sequence of size 7 24 times, then most of the first
         * sub-sequences will not have any repeats in them
         */
        ImList<Integer> range = Range.oneTo(12);

        int runCount = 40;
        ImList<Integer> all = unfold(getUniqueRandomSubSeqSize(range), i -> getUniqueRandomSubSeqSize(range)).take(runCount);

        double average = Util.sumInt(all) / (double) runCount;
        say("average", average);
        assertTrue(average > 23);

    }

    private static int getUniqueRandomSubSeqSize(ImList<Integer> range)
    {
        ImList<ImList<Integer>> subs = ImList.repeat(range, 24).map(r -> ImListUtils.randomSubSeq(r, 7).fst);

        return subs.foldl(ImSet.empty(), (z, i) -> z.add(i)).size();
    }

}