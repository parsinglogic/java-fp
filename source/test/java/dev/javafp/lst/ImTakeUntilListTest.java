package dev.javafp.lst;

import dev.javafp.eq.Equals;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImTakeUntilListTest
{

    @Test
    public void simple()
    {
        ImList<String> ss = ImList.on("a", "b", "c", "d");

        assertEquals(ImList.on("a", "b", "c"), ImTakeUntilList.on(ss, s -> Equals.isEqual(s, "c")));
        assertEquals(ss, ImTakeUntilList.on(ss, s -> Equals.isEqual(s, "x")));
        assertEquals(ImList.on(), ImTakeUntilList.on(ImList.on(), s -> Equals.isEqual(s, "x")));
    }

    @Test
    public void simple2()
    {
        ImList<String> ss = ImList.on("a", "b", "c", "d");

        assertEquals(ImList.on("a", "b", "c"), ss.takeUntil(s -> Equals.isEqual(s, "c")));
        assertEquals(ss, ss.takeUntil(s -> Equals.isEqual(s, "x")));
        assertEquals(ImList.on(), ImList.on().takeUntil(s -> Equals.isEqual(s, "x")));
    }

    @Test
    public void onInfiniteListIsLazy()
    {
        ImList<String> ss = ImList.repeat("");

        assertEquals("", ss.takeUntil(s -> s == null).head());
    }
}