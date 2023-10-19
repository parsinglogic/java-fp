package dev.javafp.queue;

import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImMaybe;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImQueueTest
{

    @Test
    public void testAddToEnd()
    {
        ImQueue<Integer> q = ImQueue.on();

        ImQueue<Integer> q2 = q.addToEnd(1);

        assertEquals(ImMaybe.just(1), q2.first());

        ImQueue<Integer> q3 = q2.removeFirst().get();

        assertEquals(0, q3.size());

        assertTrue(q3.isEmpty());
    }

    @Test
    public void testSplit()
    {

        ImQueue<Integer> q = ImQueue.on(ImRange.oneTo(5));

        assertEquals(5, q.size());

        ImPair<Integer, ImQueue<Integer>> p = q.split().get();

        assertEquals((Integer) 1, p.fst);
        assertEquals(4, p.snd.size());
    }

    @Test
    public void testEquals()
    {
        ImQueue<Integer> q = ImQueue.on(ImRange.oneTo(3));

        assertEquals(q.toImList(), ImQueue.on(0, 1, 2).removeFirst().get().addToEnd(3).toImList());
    }

    @Test
    public void testEmpty()
    {
        assertEquals(ImQueue.on(), ImQueue.on(ImList.on()));

        assertTrue(ImQueue.on().isEmpty());

    }

    @Test
    public void testRemoveFirstOnSizeOneGivesEmpty()
    {
        ImQueue<Integer> q = ImQueue.on(1);

        assertEquals(ImQueue.on(), q.removeFirst().get());
    }

    @Test
    public void testSplitOnSizeOneGivesEmpty()
    {
        ImQueue<Integer> q = ImQueue.on(1);

        ImPair<Integer, ImQueue<Integer>> p = q.split().get();

        assertEquals(ImQueue.on(), p.snd);
    }

    @Test
    public void testFirstOnEmpty()
    {
        assertEquals(ImMaybe.nothing, ImQueue.on().first());
    }

    @Test
    public void testTailOnEmptyThrows()
    {
        assertEquals(ImMaybe.nothing, ImQueue.on().removeFirst());
    }

    @Test
    public void add()
    {

        ImQueue<Integer> q = ImQueue.onSize(3, 2, 3, 4);

        assertEquals(ImList.on(3, 4, 5), q.addToEnd(5).toImList());
    }

    @Test
    public void addAgain()
    {

        ImQueue<Integer> q = ImQueue.ofSize(1);

        assertEquals(ImList.on(4), q.addToEnd(1).addToEnd(2).addToEnd(3).addToEnd(4).toImList());
    }

}