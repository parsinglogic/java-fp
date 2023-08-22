package dev.javafp.lst;

import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.shelf.ImShelf;
import dev.javafp.shelf.ImShelfZipper;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ImTestHelper;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;

public class ImListZipperTest
{

    @Test
    public void testGetNextThrows()
    {
        TestUtils.assertThrows(() -> ImList.on().getZipper().next(), NoSuchElementException.class);
    }

    @Test
    public void testSetFocusThrows()
    {
        TestUtils.assertThrows(() -> ImList.on().getZipper().setFocus(1), ZipperHasNoFocusException.class);
    }

    @Test
    public void testGetFocusThrows()
    {
        TestUtils.assertThrows(() -> ImList.on().getZipper().getFocus(), ZipperHasNoFocusException.class);
    }

    @Test
    public void testPopThrows()
    {
        TestUtils.assertThrows(() -> ImList.on().getZipper().pop(), ZipperHasNoFocusException.class);
    }

    @Test
    public void testHasNextOnEmpty()
    {
        assertEquals(false, ImList.on().getZipper().hasNext());
    }

    @Test
    public void testHasNextOnSingletonList()
    {
        assertEquals(false, ImList.on(1).getZipper().next().hasNext());
    }

    @Test
    public void testHasNextOnSmallList()
    {
        ImListZipper<Integer> z = ImList.on(1).getZipper().next();

        z = z.push(1);
        assertEquals(false, z.hasNext());

        assertEquals(true, z.prev().hasNext());
    }

    @Test
    public void exampleZipper()
    {
        ImList<Integer> list = ImList.on(2, 3);
        ImList<Integer> other = ImList.on(5, 13);

        ImListZipper<Integer> start = list.getZipper();

        ImTestHelper.checkExample(start, "{}  2  3");

        ImListZipper<Integer> z = start.next();
        ImTestHelper.checkExample(z, "{2}  3");

        z = z.setFocus(8);
        ImTestHelper.checkExample(z, "{8}  3");

        z = z.prev();
        ImTestHelper.checkExample(z, "{}  8  3");

        z = z.push(0);
        ImTestHelper.checkExample(z, "{0}  8  3");

        z = z.pushAll(other);
        ImTestHelper.checkExample(z, "0  5  {13}  8  3");

        z = z.prev();
        ImTestHelper.checkExample(z, "0  {5}  13  8  3");

        z = z.pop();
        ImTestHelper.checkExample(z, "{0}  13  8  3");

        z = z.next();
        ImTestHelper.checkExample(z, "0  {13}  8  3");

        ImTestHelper.checkExample(z.close(), "[0, 13, 8, 3]");

        ImTestHelper.checkExample(start.close() == list, "true");
    }

    @Test
    public void testEquals()
    {
        ImList<Integer> list = ImRange.oneTo(4);

        ImList<ImListZipper<Integer>> zs1 = ImList.unfold(list.getZipper(), i -> i.next()).take(4);
        ImList<ImListZipper<Integer>> zs2 = ImList.unfold(list.getZipper(), i -> i.next()).take(4);

        ImList<ImPair<ImListZipper<Integer>, ImListZipper<Integer>>> cart = ImList.cartesianProduct(zs1, zs2);

        ImList.foreachPair(cart, (i, j) -> assertEquals("" + i + " == " + j, i.equals(j), i.toString().equals(j.toString())));
    }

    @Test
    public void testEqualsOnNull()
    {
        ImList<Integer> list = ImRange.oneTo(4);

        assertEquals(list.getZipper().equals(null), false);
    }

    @Test
    public void testHasPrevOnEmpty()
    {
        assertEquals(false, ImList.on().getZipper().hasPrev());
    }

    @Test
    public void testHasPrevOnSingletonList()
    {
        ImListZipper<Integer> z = ImList.on(1).getZipper();
        assertEquals(false, z.hasPrev());

        // We can go off the left of the list
        z = z.prev();
        assertEquals(false, z.hasPrev());

        // We can go off the left of the list again
        z = z.prev();
        assertEquals(false, z.hasPrev());

        assertEquals(true, z.hasNext());
        assertEquals(Integer.valueOf(1), z.next().getFocus());
    }

    @Test
    public void testHasPrevOnSmallList()
    {
        ImListZipper<Integer> z = ImList.on(1).getZipper().next();

        assertEquals(false, z.hasPrev());

        z = z.push(2);
        assertEquals(true, z.hasPrev());

        assertEquals(false, z.prev().hasPrev());
    }

    @Test
    public void testPrevOnEmpty()
    {
        ImListZipper<Object> ze = ImList.on().getZipper();
        assertEquals(false, ze.hasPrev());

        ze = ze.prev();
        assertEquals(false, ze.hasNext());
        assertEquals(false, ze.hasPrev());

        ze = ze.prev();
        assertEquals(false, ze.hasNext());
        assertEquals(false, ze.hasPrev());
    }

    @Test
    public void exampleToString()
    {
        ImTestHelper.checkExample(ImList.on().getZipper().toString(), "{}");

        ImListZipper<Integer> z = ImList.on(1, 2, 3).getZipper();
        ImTestHelper.checkExample(z.toString(), "                       {}  1  2  3");
        ImTestHelper.checkExample(z.next().toString(), "                {1}  2  3");
        ImTestHelper.checkExample(z.next().next().toString(), "          1  {2}  3");
        ImTestHelper.checkExample(z.next().next().next().toString(), "   1  2  {3}");
    }

    public static String star(Object o)
    {
        return "*";
    }

    @Test
    public void testNextOnEmpty()
    {
        try
        {
            ImList.on().getZipper().next();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testPermutations()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);

        System.err.println(perms4(list));
        System.err.println(perms(list));
        //        System.err.println(perms2(list));
        //        System.err.println(perms3(list));

    }

    public <T> ImList<ImList<T>> perms4(ImList<T> list)
    {
        if (list.isEmpty())
            return ImList.on(ImList.on());

        ImList<ImList<T>> p = ImList.on();

        for (ImList<T> l : perms4(list.tail()))
        {
            p = p.push(l.push(list.head()));

            for (ImListZipper<T> z : l.getZipper())
                p = p.push(z.push(list.head()).close());
        }

        return p;
    }

    public <T> ImList<ImList<T>> perms(ImList<T> list)
    {
        if (list.isEmpty())
            return ImList.on(ImList.on());

        ImList<ImList<T>> p = ImList.on();

        for (ImListZipper<T> z : list.getZipper())
        {
            for (ImList<T> t : perms(z.pop().close()))
            {
                p = p.push(t.push(z.getFocus()));
            }
        }

        return p;
    }

    @Test
    public void exampleWhileLoop()
    {
        ImListZipper<Integer> z = ImList.on(1, 2, 3).getZipper();

        while (z.hasNext())
        {
            z = z.next();
            System.err.println(z.pop().close());
        }
    }

    @Test
    public void testFind()
    {
        ImListZipper<Integer> z = ImList.on(1, 2, 3).getZipper();

        ImMaybe<ImList<Integer>> ll = z.find(2).map(zz -> zz.push(4).close());

        assertEquals(ImList.on(1, 2, 4, 3), ll.get());
    }

    @Test
    public void exampleWhileLoop2()
    {
        ImListZipper<Integer> z = ImList.on(1, 2, 3, 5).getZipper();

        while (z.hasNext())
        {
            z = z.next();
            if (z.hasPrev() && z.hasNext())
                z = z.pop();
        }

        ImTestHelper.checkExample(z.close(), "[1, 5]");
    }

    @Test
    public void examplePush()
    {
        ImListZipper<Integer> z = ImList.on(1, 2, 3).getZipper();

        ImTestHelper.checkExample(z.next().push(5), "1  {5}  2  3");
    }

    @Test
    public void exampleRemove()
    {
        ImListZipper<Integer> z = ImList.on(1, 2, 3).getZipper();
        z = z.next().next();
        ImTestHelper.checkExample(z.pop().getFocus(), "1");
    }

    @Test
    public void testPopAndPushAreInverses()
    {
        for (int i = 1; i <= 4; i++)
        {
            // Create a list of i elements
            List<Integer> list = ImTestHelper.makeList(i, 1);

            ImList<Integer> lst = ImList.on(list);

            ImListZipper<Integer> z0 = lst.getZipper();

            Integer e = Integer.valueOf(5);

            System.err.println("list " + lst);

            assertEquals(0, z0.getIndex());
            assertEquals(lst, z0.close());

            for (ImListZipper<Integer> z : lst.getZipper())
            {
                ImListZipper<Integer> z1 = z.push(e).pop();

                assertEquals("" + z, "" + z1);

                ImListZipper<Integer> z2 = z.pop().push(z.getFocus());

                assertEquals("" + z, "" + z2);
            }
        }
    }

    @Test
    public void exampleForEachLoop()
    {
        for (ImListZipper<Integer> z : ImList.on(1, 2, 3).getZipper())
        {
            System.err.println(z.pop().close());
        }
    }

    @Test
    public void testNextAndPrevMatchesListIterator()
    {
        for (int i = 1; i <= 4; i++)
        {
            // Create a list of i elements
            List<Integer> list = ImTestHelper.makeList(i, 1);
            ListIterator<Integer> li = list.listIterator();

            // Create a zipper on the list
            ImListZipper<Integer> z = ImList.onAll(list).getZipper();

            for (int j = 1; j <= i; j++)
            {
                Integer next = li.next();
                z = z.next();
                assertEquals(next, z.getFocus());
                assertEquals(li.hasNext(), z.hasNext());
            }

            // -sigh-
            li.previous();

            for (int j = 1; j < i; j++)
            {
                z = z.prev();
                Integer previous = li.previous();
                assertEquals(previous, z.getFocus());
                assertEquals(li.hasPrevious(), z.hasPrev());
            }
        }
    }

    @Test
    public void testSetMatchesListIterator()
    {
        for (int i = 1; i <= 4; i++)
        {
            // Create a list of i elements
            List<Integer> list = ImTestHelper.makeList(i, 1);
            ListIterator<Integer> li = list.listIterator();

            // Create a zipper on the list
            ImListZipper<Integer> z = ImList.onAll(list).getZipper();

            for (@SuppressWarnings("unused")
                 int j = 1; i < i; i++)
            {
                z = z.next();
                assertEquals(li.next(), z.getFocus());
                assertEquals(li.hasNext(), z.hasNext());
            }
        }
    }

    @Test
    public void testMatchesShelfZipper()
    {
        // Set up a list  [1 .. 10]
        List<Integer> checkList = ImTestHelper.makeList(10, 1);

        // Set up a list  [21, 22, 23]
        List<Integer> otherList = ImTestHelper.makeList(3, 20);

        // Get the shelf zipper on it
        ImShelfZipper<Integer> zShelf = ImShelf.onAll(checkList).getZipper();

        // Get the list zipper on it
        ImListZipper<Integer> zList = ImList.onAll(checkList).getZipper();

        zShelf = zShelf.next();
        zList = zList.next();
        check(zList, zShelf);

        zShelf = zShelf.next();
        zList = zList.next();
        check(zList, zShelf);

        zShelf = zShelf.prev();
        zList = zList.prev();
        check(zList, zShelf);

        zShelf = zShelf.push(100);
        zList = zList.push(100);
        check(zList, zShelf);

        zShelf = zShelf.pushAll(ImShelf.onAll(otherList));
        zList = zList.pushAll(ImList.onAll(otherList));
        check(zList, zShelf);

        zShelf = zShelf.pop();
        zList = zList.pop();
        check(zList, zShelf);

        // Step along to the last element
        while (zShelf.hasNext())
        {
            zShelf = zShelf.next();
            zList = zList.next();
            check(zList, zShelf);
        }

        // Remove all the elements
        while (zShelf.getIndex() > 0)
        {
            System.err.println(zShelf);

            zShelf = zShelf.pop();
            System.err.println(zList);
            zList = zList.pop();
            check(zList, zShelf);
        }
    }

    private void check(ImListZipper<Integer> zList, ImShelfZipper<Integer> zShelf)
    {
        assertEquals("" + zShelf, "" + zList);
        //        assertEquals("shelf " + zShelf + "\nlist ", zShelf.getFocus(), zList.getFocus());
        //        assertEquals("index", zShelf.getIndex(), zList.getIndex());
    }
}