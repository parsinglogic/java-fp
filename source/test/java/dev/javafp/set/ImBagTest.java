package dev.javafp.set;

import dev.javafp.eq.Equals;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.rand.Rando;
import dev.javafp.util.Util;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class ImBagTest
{

    public ImList<Integer> randomList(int min, int maxExclusive, int count)
    {
        return ImList.unfold(0, i -> Rando.nextInt(min, maxExclusive)).take(count);
    }

    @Test
    public void testOne()
    {
        ImList<Integer> ints = randomList(0, 10, 10);

        ImList<Integer> ints2 = ints.shuffle();

        say(ints);
        say(ints2);

        ImBag<Integer> b1 = ImBag.onAll(ints);
        say(b1);

        ImBag<Integer> b2 = ImBag.onAll(ints2);
        say(b2);

        assertEquals(b1, b2);

    }

    @Test
    public void testSize()
    {
        assertEquals(0, ImBag.empty().size());
        
        ImList<Integer> ints = randomList(0, 10, 9);

        int expectedSize = Util.sumInt(ImRange.zeroTo(10).map(i -> ints.filter(j -> Equals.isEqual(i, j)).size()));

        ImBag<Integer> b1 = ImBag.onAll(ints);
        say(b1);

        assertEquals(expectedSize, b1.size());

    }

}