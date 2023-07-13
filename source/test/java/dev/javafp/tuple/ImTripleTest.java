package dev.javafp.tuple;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImTripleTest
{
    @Test
    public void testZip()
    {

        ImList<ImTriple<String, Integer, Double>> expected = ImList.on(ImTriple.on("a", 1, 1.0), ImTriple.on("b", 2, 2.0), ImTriple.on("c", 3, 3.0));

        ImList<String> strings = ImList.on("a", "b", "c");
        ImList<Integer> ints = ImList.on(1, 2, 3);
        ImList<Double> floats = ImList.on(1.0, 2.0, 3.0);

        ImList<ImTriple<String, Integer, Double>> actual = ImTriple.zip(strings, ints, floats);

        assertEquals(expected, actual);
    }

    @Test
    public void testZipWith()
    {

        ImList<ImTriple<String, Integer, Double>> expected = ImList.on(ImTriple.on("a", 1, 1.0), ImTriple.on("b", 2, 2.0), ImTriple.on("c", 3, 3.0));

        ImList<String> actual = ImTriple.map(expected, (s, i, f) -> s + "-" + i + "-" + f);

        assertEquals(ImList.on("a-1-1.0", "b-2-2.0", "c-3-3.0"), actual);
    }

    @Test
    public void testInfinite()
    {
        ImList<String> as = ImList.repeat("a");
        ImList<String> bs = ImList.repeat("b");
        ImList<String> cs = ImList.repeat("c");

        ImList<ImTriple<String, String, String>> actual = ImTriple.zip(as, bs, cs);

        ImList<ImTriple<String, String, String>> expected = ImList.repeat(ImTriple.on("a", "b", "c"));

        assertEquals(expected.take(3), actual.take(3));
    }

    @Test
    public void testSizes()
    {
        ImList<String> as = ImList.repeat("a");
        ImList<String> bs = ImList.repeat("b");
        ImList<String> cs = ImList.repeat("c", 2);

        ImList<ImTriple<String, String, String>> actual = ImTriple.zip(as, bs, cs);

        assertEquals(2, actual.size());

    }

    @Test
    public void testEmpty()
    {
        ImList<String> as = ImList.on();
        ImList<String> bs = ImList.repeat("b");
        ImList<String> cs = ImList.repeat("c", 2);

        ImList<ImTriple<String, String, String>> actual = ImTriple.zip(as, bs, cs);

        assertEquals(ImList.on(), actual);
    }

}