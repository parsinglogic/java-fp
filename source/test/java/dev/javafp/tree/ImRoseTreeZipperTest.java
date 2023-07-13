package dev.javafp.tree;

import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImTestHelper;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static dev.javafp.tree.ImRoseTree.leaf;
import static dev.javafp.util.ImTestHelper.flatten;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class ImRoseTreeZipperTest
{
    @Test
    public void testSetElementOnMany() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                assertEquals(count, t.size());

                List<String> list = flatten(t);

                ImRoseTreeZipper<String> z = t.getZipper();
                for (int i = 0; i < t.size(); i++)
                {
                    list.set(i, list.get(i) + "bis");
                    z = z.setElement(z.getElement() + "bis");
                    if (z.hasNext())
                        z = z.next();
                }

                assertEquals(list, flatten(z.close()));
            }
        }
    }

    @Test
    public void testRemoveOnMany() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {

                ImRoseTreeZipper<String> z = t.getZipper();

                // We don't try to remove the root node
                for (int i = 2; i <= t.size(); i++)
                {
                    // Create a mutable tree from the tree
                    MutableTree<String> mt = MutableTree.from(t);

                    // In order to get to the i-th element it is actually easier to just search for an element
                    // with that string rather than write code to navigate to the i-th element in a mutable tree
                    // Remove the i-th element
                    mt.removeNodeWithElement("" + i);

                    ImRoseTree<String> newTree = removeNodeWithElement(z, "" + i);
                    assertEquals("Failed on " + i, mt.toString(), newTree.toString());
                }
            }
        }
    }

    @Test
    public void testCloseReturnsSameIfNoModification() throws Exception
    {
        for (int count = 6; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                ImRoseTreeZipper<String> z = t.getZipper();

                while (z.hasNext())
                {
                    assertEquals(t, z.close());
                    z = z.next();
                }
                assertEquals(t, z.close());
            }
        }
    }

    @Test
    public void testPushBeforeOnMany() throws Exception
    {
        ImRoseTree<String> treeToInsert = leaf("x");
        MutableTree<String> imTreeToInsert = MutableTree.from(treeToInsert);

        for (int count = 2; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {

                ImRoseTreeZipper<String> z = t.getZipper();
                for (int i = 2; i <= t.size(); i++)
                {
                    // Create a mutable tree from the tree
                    MutableTree<String> mt = MutableTree.from(t);

                    // System.err.print("insert before " + i + " on " + mt);

                    // In order to get to the i-th element it is actually easier to just search for an element
                    // with that string rather than write code to navigate to the i-th element in a mutable tree
                    // Remove the i-th element
                    mt.insertBeforeNodeWithElement("" + i, imTreeToInsert);
                    // System.err.println(" = " + mt);

                    ImRoseTree<String> newTree = insertBeforeNodeWithElement(z, "" + i, treeToInsert);
                    assertEquals("Failed on " + i, mt.toString(), newTree.toString());
                }
            }
        }
    }

    @Test
    public void testPushOnMany() throws Exception
    {
        ImRoseTree<String> treeToInsert = leaf("x");
        MutableTree<String> imTreeToInsert = MutableTree.from(treeToInsert);

        for (int count = 2; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                //                System.err.println(t.toBoxString());
                //                System.err.println(t);

                ImRoseTreeZipper<String> z = t.getZipper();

                for (int i = 2; i <= t.size(); i++)
                {
                    // Create a mutable tree from the tree
                    MutableTree<String> mt = MutableTree.from(t);

                    // In order to get to the i-th element it is actually easier to just search for an element
                    // with that string rather than write code to navigate to the i-th element in a mutable tree
                    // Remove the i-th element
                    mt.insertAfterNodeWithElement("" + i, imTreeToInsert);

                    ImRoseTree<String> newTree = insertAfterNodeWithElement(z, "" + i, treeToInsert);
                    assertEquals("Failed on " + i, mt.toString(), newTree.toString());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void exampleChangeNode4() throws Exception
    {
        ImRoseTree<Integer> tree = ImRoseTree.withNodes(1, leaf(2), ImRoseTree.withElements(3, 4, 5), leaf(6));

        //System.err.println(tree.getZipper().next().next().next().setElement(7).close().toBoxString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void exampleUp() throws Exception
    {
        ImRoseTree<Integer> tree = ImRoseTree.withNodes(1, leaf(2), ImRoseTree.withElements(3, 4, 5), leaf(6));

        ImRoseTreeZipper<Integer> z = tree.getZipper();
        //System.out.println(z);

        ImRoseTreeZipper<Integer> z3 = z.next().next();
        //System.out.println(z3);

        ImRoseTreeZipper<Integer> z4 = z3.next();
        //System.out.println(z4);

        ImTestHelper.checkExample(z4.up().equals(z3), "true");
        ImTestHelper.checkExample(z4.setElement(7).up() == z3, "false");
    }

    @Test
    public void testUpThrowsOnRoot() throws Exception
    {
        try
        {
            leaf(1).getZipper().up();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testNextThrowsOnRoot() throws Exception
    {
        try
        {
            leaf(1).getZipper().next();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testNextSiblingThrowsOnRoot() throws Exception
    {
        try
        {
            leaf(1).getZipper().nextSibling();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testPrevSiblingThrowsOnRoot() throws Exception
    {
        try
        {
            leaf(1).getZipper().prevSibling();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testNextSiblingThrowsWhenAtEnd() throws Exception
    {
        try
        {
            leaf(1).getZipper().down().nextSibling();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void testGetFocusThrows() throws Exception
    {
        try
        {
            leaf(1).getZipper().down().getFocus();
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void testGetElementThrows() throws Exception
    {
        try
        {
            leaf(1).getZipper().down().getElement();
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void testSetFocusThrows() throws Exception
    {
        try
        {
            leaf(1).getZipper().down().setFocus(leaf(1));
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void testSetElementThrows() throws Exception
    {
        try
        {
            leaf(1).getZipper().down().setElement(2);
            TestUtils.failExpectedException(ZipperHasNoFocusException.class);
        } catch (ZipperHasNoFocusException e)
        {
        }
    }

    @Test
    public void testPopThrowsOnRoot()
    {
        try
        {
            leaf(1).getZipper().pop();
            TestUtils.failExpectedException(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e)
        {
        }
    }

    @Test
    public void testPushThrowsOnRoot()
    {
        try
        {
            leaf(1).getZipper().push(leaf(1));
            TestUtils.failExpectedException(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e)
        {
        }
    }

    @Test
    public void testPushBeforeThrowsOnRoot()
    {
        try
        {
            leaf(1).getZipper().pushBefore(leaf(1));
            TestUtils.failExpectedException(UnsupportedOperationException.class);
        } catch (UnsupportedOperationException e)
        {
        }
    }

    @Test
    public void testPrevThrowsOnRoot()
    {
        try
        {
            leaf(1).getZipper().prev();
            TestUtils.failExpectedException(NoSuchElementException.class);
        } catch (NoSuchElementException e)
        {
        }
    }

    @Test
    public void exampleNextSibling() throws Exception
    {
        ImRoseTree<String> tree = ImRoseTree.withElements("a", "b", "c");

        ImRoseTreeZipper<String> z = tree.getZipper();

        z = z.down();
        ImTestHelper.checkExample(z, "{a (b c)} <- {}  b  c");
        ImTestHelper.checkExample(z.hasNextSibling(), "true");

        z = z.nextSibling();
        ImTestHelper.checkExample(z, "{a (b c)} <- {b}  c");
        ImTestHelper.checkExample(z.hasNextSibling(), "true");

        z = z.nextSibling();
        ImTestHelper.checkExample(z, "{a (b c)} <- b  {c}");
        ImTestHelper.checkExample(z.hasNextSibling(), "false");
        ImTestHelper.checkExample(z.hasPrevSibling(), "true");

        z = z.prevSibling();
        ImTestHelper.checkExample(z, "{a (b c)} <- {b}  c");
        ImTestHelper.checkExample(z.hasPrevSibling(), "false");

        z = z.down();
        ImTestHelper.checkExample(z, "{a (b c)} <- {b}  c <- {}");
        ImTestHelper.checkExample(z.hasNextSibling(), "false");
        ImTestHelper.checkExample(z.hasPrevSibling(), "false");

        z = z.up();

        z = z.prevSibling();
        ImTestHelper.checkExample(z, "{a (b c)} <- {}  b  c");
        ImTestHelper.checkExample(z.hasPrevSibling(), "false");

        z = z.up();
        ImTestHelper.checkExample(z, "{a (b c)}");
    }

    @Test
    public void testHasNextAndPrevOnMany() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                //                System.out.println("tree");
                //                System.out.println(t);

                ImRoseTreeZipper<String> z = t.getZipper();

                for (int i = 1; i < t.size(); i++)
                {
                    assertEquals(true, z.hasNext());

                    //System.out.println(z);
                    z = z.next();
                }

                //System.out.println(z);

                // The last zipper has no next element
                assertEquals(false, z.hasNext());

                //Now go back to the start
                for (int i = 1; i < t.size(); i++)
                {
                    //System.err.println(z);
                    assertEquals(true, z.hasPrev());
                    z = z.prev();
                }

                //System.err.println(z);
                // The first zipper has no prev element
                assertEquals(false, z.hasPrev());
            }
        }
    }

    private ImRoseTree<String> insertBeforeNodeWithElement(ImRoseTreeZipper<String> z, String stringToLookFor,
            ImRoseTree<String> treeToInsert)
    {
        while (true)
        {
            if (z.getElement().equals(stringToLookFor))
            {
                return z.pushBefore(treeToInsert).close();
            }
            z = z.next();
        }
    }

    private ImRoseTree<String> insertAfterNodeWithElement(ImRoseTreeZipper<String> z, String stringToLookFor,
            ImRoseTree<String> treeToInsert)
    {
        while (true)
        {
            if (z.getElement().equals(stringToLookFor))
            {
                //System.err.println("push on " + z.toString(fn));
                return z.push(treeToInsert).close();
            }
            z = z.next();
        }
    }

    /**
     * Go to the node which contains `stringToLookFor` and delete that node
     */
    private ImRoseTree<String> removeNodeWithElement(ImRoseTreeZipper<String> z, String stringToLookFor)
    {
        while (true)
        {
            if (z.getElement().equals(stringToLookFor))
            {
                return z.pop().close();
            }
            z = z.next();
        }
    }

    @Test
    public void testEquals() throws Exception
    {
        // Get a list of all tree shapes with size <= 5
        ImList<ImRoseTree<String>> ts = Range.inclusive(2, 5).flatMap(i -> ImRoseTreeShapes.allTreesWithSize(i));

        ts.foreach(t ->
        {
            ImRoseTreeZipper<String> z = t.getZipper();

            //            System.out.println("\nz "+  z);

            ImList<ImRoseTreeZipper<String>> zs = ImList.unfold(z, i -> i.next()).take(t.size());

            //            System.out.println("t size " + t.size());

            //            System.out.println("zs " + zs);

            // Compare zippers pair-wise
            ImList<ImPair<ImRoseTreeZipper<String>, ImRoseTreeZipper<String>>> pairs = zs.cartesianProduct();

            // Filter for the equal ones
            ImList<ImPair<ImRoseTreeZipper<String>, ImRoseTreeZipper<String>>> equal = pairs.filter(p -> p.fst.equals(p.snd));

            // There should the same numbser as the tree size
            //
            // For example, if t.size() == 2

            //   [z1, z2] x [z1, z2] = (z1, z1) (z2, z2) (z2, z1) (z2, z2)
            assertEquals(t.size(), equal.size());

            // Their toString()s should be the same
            ImPair<ImRoseTreeZipper<String>, ImRoseTreeZipper<String>> p = equal.head();

            assertEquals(p.fst.toString(), p.snd.toString());
        });

        //        // Form the cartesian product ts x ts
        //        ImList<ImPair<ImRoseTree<String>, ImRoseTree<String>>> pairs = ImList.cartesianProduct(ts);
        //
        //        // The only equal trees will be the ones that are the same
        //        ImList.foreachPair(pairs, (i, j) -> assertEquals(i.equals(j), i == j));
        //
        //        // Get another list of tree shapes
        //        ImList<ImRoseTree<String>> ts2 = Range.oneTo(6).flatMap(i -> shapes.allTreesWithSize(i));
        //
        //        // Form the cartesian product ts x ts2
        //        ImList<ImPair<ImRoseTree<String>, ImRoseTree<String>>> pairs2 = ImList.cartesianProduct(ts2);
        //
        //        // The equal trees will match the string equals
        //        ImList.foreachPair(pairs, (i, j) -> assertEquals(i.equals(j), i.toString().equals(j.toString())));
    }

    @Test
    public void testShowPath() throws Exception
    {
        // Get a list of all tree shapes with size <= 5
        ImList<ImRoseTree<String>> ts = Range.inclusive(5, 5).flatMap(i -> ImRoseTreeShapes.allTreesWithSize(i));

        say(ts.last());
        ImRoseTreeZipper<String> z = ImList.onIterator(ts.last().getZipperIterator()).last();

        assertEquals("1/2/3/4/5", z.showPath("/"));
    }
}