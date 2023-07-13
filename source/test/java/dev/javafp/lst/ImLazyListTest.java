package dev.javafp.lst;

import dev.javafp.eq.Eq;
import dev.javafp.ex.SizeOnInfiniteList;
import dev.javafp.ex.ThreadInterrupted;
import dev.javafp.func.FnBlock;
import dev.javafp.time.Clock;
import dev.javafp.util.Say;
import dev.javafp.util.TestUtils;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Iterator;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class InfiniteIterator implements Iterator<Integer>
{

    @Override
    public boolean hasNext()
    {
        return true;
    }

    @Override
    public Integer next()
    {
        return 0;
    }
}

public class ImLazyListTest
{
    private final ImList<Integer> infiniteList = ImList.unfold(1, i -> i + 1);

    public void resolveSizeOnKnownInfinite()
    {

        say(infiniteList.resolveSize());
    }

    @After
    public void tearDown()
    {
        Say.setQuiet(false);
    }

    @Test
    public void testResolveSizeOnKnownInfinite()
    {
        TestUtils.assertThrows(this::resolveSizeOnKnownInfinite, SizeOnInfiniteList.class);
    }

    @Test
    public void testToSOnKnownInfinite()
    {
        String s = infiniteList.toS();

        assertEquals("[1, 2, 3] (showing the first 3 elements - list is infinite)", s);

    }

    @Test
    public void testGetTextBoxOnKnownInfinite()
    {

        String s = infiniteList.getTextBox().toString();

        assertEquals("[1, 2, 3] (showing the first 3 elements - list is infinite)", s);
    }

    public void iterateOnKnownInfinite()
    {

        int k = 0;
        for (int i : infiniteList)
        {
            k += i;
        }
    }

    @Test
    public void testIterateOnKnownInfinite()
    {
        TestUtils.assertThrows(this::iterateOnKnownInfinite, SizeOnInfiniteList.class);
    }

    class Runner implements Runnable
    {

        private final FnBlock blockToRun;

        Runner(FnBlock blockToRun)
        {
            this.blockToRun = blockToRun;
        }

        @Override
        public void run()
        {
            try
            {
                blockToRun.doit();
            } catch (ThreadInterrupted e)
            {
                System.out.println("Interrupted");
            }
        }
    }

    public void resolveSizeOnUnknownUnknown()
    {
        ImList<Integer> list1 = ImList.onIterator(new InfiniteIterator());
        say(list1.resolveSize());
    }

    @Ignore
    @Test(timeout = 60_000)
    public void testResolveSizeOnUnknownUnknown2() throws InterruptedException
    {
        Say.setQuiet(true);

        Thread t = new Thread(new Runner(this::resolveSizeOnUnknownUnknown));

        t.start();

        // Wait until there is something in the buffer
        Clock.sleepSeconds(2);

        t.interrupt();

        t.join();

        String buffer = Say.getBuffer().getString();

        assertTrue(buffer, buffer.contains("LazyList might be infinite"));

    }

    public void iterateOnUnknownUnknown()
    {
        ImList<Integer> list1 = ImList.onIterator(new InfiniteIterator());

        int k = 0;
        for (int i : list1)
        {
            k += i;
        }
    }

    public void equalsOnUnknownUnknown()
    {
        ImList<Integer> list1 = ImList.onIterator(new InfiniteIterator());
        ImList<Integer> list2 = ImList.onIterator(new InfiniteIterator());

        say(Eq.uals(list1, list2));
    }

    @Ignore
    @Test(timeout = 60_000)
    public void testEqualsOnUnknownUnknown() throws InterruptedException
    {
        Say.setQuiet(true);

        Thread t = new Thread(new Runner(this::equalsOnUnknownUnknown));

        t.start();

        // Wait until there is something in the buffer
        Clock.sleep(2, "");

        t.interrupt();

        t.join();

        String buffer = Say.getBuffer().getString();
        assertTrue(buffer, buffer.contains("LazyList might be infinite"));

    }

    //    @Test(timeout = 60_000)
    //    public void testEq() throws InterruptedException
    //    {
    //        ImList<Integer> list1 = ImList.onIterator(new InfiniteIterator());
    //        ImList<Integer> integers = list1.takeWhile(i -> i == 0);
    //
    //        assertEquals(ImList.on(1, 2), integers);
    //    }
    //
    //    public void testEqOnUU2()
    //    {
    //        ImList<Integer> list1 = ImList.onIterator(new InfiniteIterator());
    //        ImList<Integer> integers = list1.takeWhile(i -> i == 0);
    //
    //        assertEquals(ImList.on(1, 2), integers);
    //    }

    //    @Test(timeout = 9000)
    //    public void testEqOnUU() throws InterruptedException
    //    {
    //        Say.setQuiet(true);
    //
    //        Thread t = new Thread(new Runner(this::testEqOnUU2));
    //
    //        t.start();
    //
    //        Clock.sleepSeconds(6);
    //
    //        t.interrupt();
    //
    //        t.join();
    //
    //        String buffer = Say.getBuffer().getString();
    //        assertTrue(buffer, buffer.contains("LazyList might be infinite"));
    //    }

    @Test(timeout = 60_000)
    public void testIterateOnUnknownUnknown() throws InterruptedException
    {

        Say.setQuiet(true);

        Thread t = new Thread(new Runner(this::iterateOnUnknownUnknown));

        t.start();

        Clock.sleepSeconds(2);

        t.interrupt();

        t.join();

        String buffer = Say.getBuffer().getString();
        assertTrue(buffer, buffer.contains("List iterator might be infinite"));
    }

    @Test
    public void testToStringOnUnknownUnknown()
    {
        assertTrue(ImList.onIterator(new InfiniteIterator()).getTextBox().toString().contains("(showing the first "));
    }

    @Test
    public void testGetTextBoxOnUnknownUnknown()
    {
        assertTrue(ImList.onIterator(new InfiniteIterator()).toS().contains("(showing the first "));
    }

}