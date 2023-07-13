package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class ImListUtilsTest
{
    @Test
    public void testPart()
    {
        ImList<Character> set = Range.inclusive('A', 'K').map(i -> getChar(i));

        Range.inclusive(0, set.size()).foreach(i ->
        {
            say("i", i);

            ImPair<ImList<Character>, ImList<Character>> pair = ImListUtils.randomSubSeq(set, i);

            say("pair", pair);

            assertEquals((int) i, pair.fst.size());

            assertEquals(pair.fst.toImSet().union(pair.snd), set.toImSet());
        });

    }

    private Character getChar(int i)
    {
        return Character.valueOf((char) i);
    }

}