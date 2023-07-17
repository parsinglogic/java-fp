package dev.javafp.rand;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.set.ImMap;
import dev.javafp.util.Util;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertTrue;

public class RandoTest
{

    @Test
    public void testRandomStringIsFair()
    {
        RandomString rs = new RandomString("abcdef");

        // count the frequencies
        ImMap<Character, Integer> map = ImList.onString(rs.next(10000)).foldl(ImMap.empty(), (z, c) -> z.updateValue(c, 1, i -> i + 1));

        say(map);

        // do a very crude check
        int minInt = Util.minInt(map.values());
        int maxInt = Util.maxInt(map.values());

        double v = maxInt * 1.0 / minInt;
        say(v);

        assertTrue(v < 1.2);
    }

    @Test
    public void testOne()
    {
        ImList<Integer> rs = Range.oneTo(100).map(i -> Rando.nextInt(1, 10));

        assertTrue(ImList.and(rs.map(r -> r >= 1 && r <= 10)));
    }

}