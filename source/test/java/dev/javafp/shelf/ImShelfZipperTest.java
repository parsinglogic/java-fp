package dev.javafp.shelf;

import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.util.ImTestHelper;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ImShelfZipperTest
{

    @Test
    public void testNextThrowsAtEnd() throws Exception
    {
        try
        {
            ImShelf.on(1).getZipper().next().next();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testSetFocusThrows() throws Exception
    {
        try
        {
            ImShelf.on(1).getZipper().setFocus(2);
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void getFocusThrows() throws Exception
    {
        try
        {
            ImShelf.on(1).getZipper().getFocus();
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void popThrows() throws Exception
    {
        try
        {
            ImShelf.on(1).getZipper().pop();
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    //    @Test
    //    public void testEquals() throws Exception
    //    {
    //        ImShelf<Integer> list = ImShelf.onArray(1, 2, 3, 4);
    //
    //        ImList<ImShelfZipper<Integer>> zs1 = ImList.unfold(list.getZipper(), i -> i.next()).take(4);
    //        ImList<ImShelfZipper<Integer>> zs2 = ImList.unfold(list.getZipper(), i -> i.next()).take(4);
    //
    //        ImList<ImPair<ImShelfZipper<Integer>, ImShelfZipper<Integer>>> cart = ImList.cartesianProduct(zs1, zs2);
    //
    //        ImList.foreachPair(cart, (i, j) -> assertEquals("" + i + " == " + j, i.equals(j), i.toString().equals(j.toString())));
    //    }
    //
    //    @Test
    //    public void testEqualsOnNull() throws Exception
    //    {
    //        ImList<Integer> list = ImRange.oneTo(4);
    //
    //        assertEquals(list.getZipper().equals( null), false);
    //    }

    @Test
    public void testInsertBeforeAOnMany() throws Exception
    {
        Random r = new Random(12345678);

        List<Integer> one = new ArrayList<Integer>();
        one.add(-1);

        ImShelf<Integer> oneShelf = ImShelf.on(-1);

        ImShelfZipper<Integer> zipper = oneShelf.getZipper();

        for (int count = 0; count <= 100; count++)
        {
            List<Integer> other = ImTestHelper.makeList(r.nextInt(10) + 1, count * 10);

            int randomIndex = r.nextInt(one.size());

            //            System.err.println("--------------------------------");

            //System.err.println("before" + one);
            //            System.err.println("randomIndex " + randomIndex);

            zipper = move(zipper, randomIndex + 1);

            ImShelfZipper<Integer> oldZipper = zipper;

            //System.err.println("zipper index " + zipper.getIndex());

            one.addAll(randomIndex, other);

            //            System.err.println("\ntree before\n" + zipper.treeZipper.getRoot().toBoxString());
            //            System.err.println("zipper before " + zipper.treeZipper);
            //            System.err.println("zipper focus before " + zipper.treeZipper.getFocus().toBoxString());

            zipper = zipper.pushAll(ImShelf.onAll(other));

            //            System.err.println("zipper  after " + zipper.treeZipper);
            //            System.err.println("\ntree after\n" + zipper.treeZipper.getRoot().toBoxString());
            //            System.err.println("zipper focus after\n" + zipper.treeZipper.getFocus().toBoxString());
            //
            //            System.err.println("zipper rank after " + zipper.treeZipper.getRank());
            //            System.err.println("zipper index after " + zipper.getIndex());
            //
            //            System.err.println("tree after close\n" + zipper.close().getTree().toBoxString());

            //            assertEquals("" + one, "" + zipper.close());

            // The index of the zipper should increase by the number of elements added
            assertEquals(oldZipper.getIndex() + other.size(), zipper.getIndex());

            // System.err.println("after " + one);
        }
    }

    private ImShelfZipper<Integer> move(ImShelfZipper<Integer> zipper, int indexToMoveTo)
    {
        if (zipper.getIndex() < indexToMoveTo)
            return move(zipper.next(), indexToMoveTo);
        else if (zipper.getIndex() > indexToMoveTo)
            return move(zipper.prev(), indexToMoveTo);
        else
            return zipper;
    }

    @Test
    public void testPushOnMany() throws Exception
    {
        List<Integer> startList = new ArrayList<Integer>();

        for (int count = 0; count <= 31; count++)
        {
            int j = 100;
            startList.add(count);

            // Insert an element at each position of the list
            for (int i = 1; i <= count; i++)
            {
                j++;
                List<Integer> endList = new ArrayList<Integer>(startList);

                ImShelf<Integer> shelf = ImShelf.onAll(startList);
                ImShelfZipper<Integer> mz = shelf.getZipperOnIndex(i);

                endList.add(i, j);

                assertEquals("" + endList, "" + mz.push(j).close());
            }
        }
    }

    //    @Test
    //    public void testInsertElementOnEmpty() throws Exception
    //    {
    //        ImShelfZipper<Integer> z = ImShelf.<Integer> empty().getZipper();
    //
    //        assertEquals(null, z.getFocus());
    //
    //        assertFalse(z.next().hasValue());
    //        assertFalse(z.prev().hasValue());
    //
    //        assertEquals(Arrays.asList(1), new ArrayList<Integer>(z.insertBefore(1).close()));
    //    }

    @Test
    public void testPopElementOnSingle() throws Exception
    {
        ImShelf<Integer> shelf = ImShelf.onAll(Arrays.asList(1));

        assertEquals("{}", "" + shelf.getZipper().next().pop());
    }

    @Test
    public void testPopOnMany() throws Exception
    {
        List<Integer> startList = new ArrayList<Integer>();

        for (int count = 1; count <= 10; count++)
        {
            startList.add(count);

            // Remove an element at each position of the list
            for (int i = 1; i <= count; i++)
            {
                List<Integer> endList = new ArrayList<Integer>(startList);

                ImShelf<Integer> shelf = ImShelf.onAll(startList);
                ImShelfZipper<Integer> z = shelf.getZipperOnIndex(i);

                endList.remove(i - 1);

                assertEquals("" + endList, "" + z.pop().close());
            }
        }
    }

    @Test
    public void testCloseWithNoChangesReturnsSameShelf() throws Exception
    {
        List<Integer> startList = new ArrayList<Integer>();

        for (int count = 1; count <= 10; count++)
        {
            startList.add(count);

            // Remove an element at each position of the list
            for (int i = 1; i <= count; i++)
            {
                ImShelf<Integer> shelf = ImShelf.onAll(startList);
                ImShelfZipper<Integer> z = shelf.getZipperOnIndex(i);

                assertSame(shelf, z.close());
            }
        }
    }

    //    @Test
    //    public void testInsertBeforeSimple() throws Exception
    //    {
    //        // Set up a list  [1 .. 10]
    //        List<Integer> checkList = ImTestHelper.makeList(10, 1);
    //
    //        // Get the zipper on it
    //        ImShelfZipper<Integer> z = ImShelf.onAll(checkList).getZipper();
    //
    //        int j = 100;
    //
    //        for (int i = 1; i <= 10; i++)
    //        {
    //            // Do the equivalent insert
    //            insertOnListBefore(j, z, checkList);
    //
    //            // Insert an element before the current focus
    //            z = z.insertBefore(j);
    //
    //            // The list and the shelf should be equal
    //            assertEquals("" + checkList, "" + z.close());
    //
    //            j++;
    //        }
    //
    //    }

    @Test
    public void testPushSimple() throws Exception
    {
        // Set up a list  [1 .. 10]
        List<Integer> checkList = ImTestHelper.makeList(10, 1);

        // Get the zipper on it
        ImShelfZipper<Integer> z = ImShelf.onAll(checkList).getZipper();

        int j = 100;

        for (int i = 1; i <= 10; i++)
        {
            // Do the equivalent insert
            insertOnListAfter(j, z, checkList);

            // Insert an element before the current focus
            z = z.push(j);

            // The list and the shelf should be equal
            assertEquals("" + checkList, "" + z.close());

            j++;
        }
    }

    private void insertOnListAfter(int element, ImShelfZipper<Integer> zipper, List<Integer> list)
    {
        list.add(zipper.getIndex(), element);
    }

    @Test
    public void exampleZipper() throws Exception
    {
        ImShelf<Integer> shelf = ImShelf.on(2, 3);
        ImShelf<Integer> other = ImShelf.on(5, 13);

        ImShelfZipper<Integer> start = shelf.getZipper();

        ImTestHelper.checkExample(start, "{}  2  3");

        ImShelfZipper<Integer> z = start.next();
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

        ImTestHelper.checkExample(start.close() == shelf, "true");
        ImTestHelper.checkExample(start.next().close() == shelf, "true");
    }

    @Test
    public void testPushAllOnMany() throws Exception
    {
        List<Integer> startList = new ArrayList<Integer>();

        for (int count = 1; count <= 10; count++)
        {
            startList.add(count);

            // Insert elements at each position of the list
            for (int i = 1; i <= count; i++)
            {

                for (int k = 0; k < count; k++)
                {
                    List<Integer> listToInsert = getList(k);

                    List<Integer> endList = new ArrayList<Integer>(startList);

                    ImShelf<Integer> shelf = ImShelf.onAll(startList);
                    ImShelfZipper<Integer> z = shelf.getZipperOnIndex(i);

                    //  0  1  2   3      0  1  2  3    4     5
                    // [1, 2, 3] [4] => [1, 2, 3, 100, 101] [4]

                    endList.addAll(i, listToInsert);

                    // Add the list to the zipper
                    ImShelfZipper<Integer> newShelf = z.pushAll(ImShelf.<Integer>onAll(listToInsert));

                    assertEquals(endList, new ArrayList<Integer>(newShelf.close().toImList().toList()));
                }
            }
        }
    }

    private List<Integer> getList(int k)
    {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < k; i++)
        {
            result.add(i + 100);
        }

        return result;
    }

    @Test
    public void testHasNextOnEmpty() throws Exception
    {
        assertEquals(false, ImShelf.empty().getZipper().hasNext());
    }

    @Test
    public void testHasNextOnSingletonList() throws Exception
    {
        assertEquals(false, ImShelf.on(1).getZipper().next().hasNext());
    }

    @Test
    public void testHasNextOnSmallList() throws Exception
    {
        ImShelfZipper<Integer> z = ImShelf.on(1).getZipper().next();

        z = z.push(1);
        assertEquals(false, z.hasNext());

        assertEquals(true, z.prev().hasNext());
    }

    @Test
    public void testEqualsOnSmallList() throws Exception
    {
        ImShelfZipper<Integer> z = ImShelf.on(1, 2, 3, 4).getZipper().next().next();

        assertEquals("1  {2}  3  4", z.toString());
    }

    @Test
    public void testHasPrevOnEmpty() throws Exception
    {
        assertEquals(false, ImShelf.empty().getZipper().hasPrev());
    }

    @Test
    public void testHasPrevOnSingletonList() throws Exception
    {
        ImShelfZipper<Integer> z = ImShelf.on(1).getZipper();
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
    public void testHasPrevOnSmallList() throws Exception
    {
        ImShelfZipper<Integer> z = ImShelf.on(1).getZipper().next();

        assertEquals(false, z.hasPrev());

        z = z.push(2);
        assertEquals(true, z.hasPrev());

        assertEquals(false, z.prev().hasPrev());
    }

    @Test
    public void testPrevOnEmpty() throws Exception
    {
        ImShelfZipper<Object> ze = ImShelf.empty().getZipper();
        assertEquals(false, ze.hasPrev());

        ze = ze.prev();
        assertEquals(false, ze.hasNext());
        assertEquals(false, ze.hasPrev());

        ze = ze.prev();
        assertEquals(false, ze.hasNext());
        assertEquals(false, ze.hasPrev());
    }

    @Test
    public void exampleToString() throws Exception
    {
        ImTestHelper.checkExample(ImShelf.empty().getZipper().toString(), "{}");

        ImShelfZipper<Integer> z = ImShelf.on(1, 2, 3).getZipper();
        ImTestHelper.checkExample(z.toString(), "                     {}  1  2  3");
        ImTestHelper.checkExample(z.next().toString(), "              {1}  2  3");
        ImTestHelper.checkExample(z.next().next().toString(), "       1  {2}  3");
        ImTestHelper.checkExample(z.next().next().next().toString(), "1  2  {3}");
    }

    @Test
    public void testPopAndPushAreInverses() throws Exception
    {
        for (int i = 1; i <= 4; i++)
        {
            // Create a list of i elements
            List<Integer> list = ImTestHelper.makeList(i, 1);

            ImShelf<Integer> imShelf = ImShelf.onAll(list);

            ImShelfZipper<Integer> z0 = imShelf.getZipper();

            Integer e = Integer.valueOf(5);

            System.err.println("list " + imShelf);

            assertEquals(0, z0.getIndex());
            assertEquals(imShelf, z0.close());

            for (ImShelfZipper<Integer> z : imShelf.getZipper())
            {
                ImShelfZipper<Integer> z1 = z.push(e).pop();

                assertEquals("" + z, "" + z1);

                ImShelfZipper<Integer> z2 = z.pop().push(z.getFocus());

                assertEquals("" + z, "" + z2);
            }
        }
    }
}