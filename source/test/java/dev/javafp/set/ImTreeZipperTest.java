package dev.javafp.set;

import dev.javafp.lst.ImList;
import dev.javafp.util.ImMaybe;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static dev.javafp.util.ImTestHelper.assertTreeIs;
import static dev.javafp.util.ImTestHelper.flatten;
import static dev.javafp.util.ImTestHelper.t;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ImTreeZipperTest
{
    @Test
    public void testZipperOnTrees()
    {
        assertEquals("-", "" + ImTreeZipper.onLeftmost(t("")));
        assertEquals("d", "" + ImTreeZipper.onLeftmost(t("d1")));
        assertEquals("b, (left, d, -)", "" + ImTreeZipper.onLeftmost(t("d1 b2")));
        assertEquals("a, (left, b, -), (left, d, -)", "" + ImTreeZipper.onLeftmost(t("d1 b2 a3")));
    }

    @Test
    public void testNextSimple()
    {
        /**
         *                   d
         *                  /  \
         *                 /    \
         *                b      f
         *               / \    / \
         *              a   c  e   h
         *                        /
         *                       g
         *
         */
        final ImTree<Character> node = t("d1 b2 a3 c3 f2 e3 h3 g4");

        say(node);

        ImTreeZipper<Character> path = ImTreeZipper.onLeftmost(node);

        assertEquals("a, (left, b, c), (left, d, f)", "" + path);

        path = checkNextZipperIs(path, "b, (left, d, f)");
        path = checkNextZipperIs(path, "c, (right, b, a), (left, d, f)");

        path = checkNextZipperIs(path, "d");
        path = checkNextZipperIs(path, "e, (left, f, h), (right, d, b)");
        path = checkNextZipperIs(path, "f, (right, d, b)");
        path = checkNextZipperIs(path, "g, (left, h, -), (right, f, e), (right, d, b)");
        path = checkNextZipperIs(path, "h, (right, f, e), (right, d, b)");

        // Now there is no next zipper
        assertEquals(false, path.next().isPresent());
    }

    private ImTreeZipper<Character> checkNextZipperIs(ImTreeZipper<Character> path, String string)
    {
        ImTreeZipper<Character> nextPath = path.next().get();
        assertEquals(string, "" + nextPath);
        return nextPath;
    }

    @Test
    public void testRemoveSimple()
    {
        /**
         *      a                      a  
         *     / \      Remove b        \
         *    b   c     -------->        c 
         */
        final ImTree<Character> node = t("a1 b2 c2");
        System.out.println(node);

        final ImTreeZipper<Character> path = ImTreeZipper.onLeftmost(node);

        System.out.println(path);

        ImTree<Character> pathWithoutB = path.removeNode().close();
        System.out.println(pathWithoutB);
        assertTreeIs("a1 -2 c2", pathWithoutB);
    }

    @Test
    public void testPopOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            for (int i = 1; i <= tree.size(); i++)
            {
                ImMaybe<ImTreeZipper<Character>> z = ImTreeZipper.onIndex(tree, i);
                List<Character> list = flatten(tree);
                list.remove(i - 1);
                assertEquals(list, flatten(z.get().removeNode()));
            }
        }
    }

    @Test
    public void testPopOnMany2()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allNonNilUpToSize(6, true, 'a'))
        {
            for (int i = 1; i <= tree.size(); i++)
            {
                // Get a tree zipper on index i
                ImTreeZipper<Character> z = ImTreeZipper.onIndex(tree, i).get();

                // Get the equivalent list
                List<Character> list = flatten(tree);

                // Remove the i-th element from the list
                list.remove(i - 1);

                // Remove the node at the zipper focus and the the list and the tree should look the same
                assertEquals(list, flatten(z.removeNode().close()));
            }
        }
    }

    @Test
    public void testGetRankOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree);

            for (int i = 1; i < tree.size(); i++)
            {
                assertEquals(i, z.getRank());
                z = z.next().get();
            }
        }
    }

    @Test
    public void testGetAfterSizeOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree).before();

            assertEquals(tree.size(), z.getAfterSize());

            for (int i = 1; i < tree.size(); i++)
            {
                z = z.next().get();
                assertEquals(tree.size() - i, z.getAfterSize());
            }
        }
    }

    @Test
    public void testAfterSizeMatchesRankOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree).before();

            assertEquals(tree.size(), z.getAfterSize());

            for (int i = 1; i < tree.size(); i++)
            {
                z = z.next().get();
                assertEquals(tree.size(), z.getAfterSize() + z.getRank());
            }
        }
    }

    @Test
    public void testGoToLocalRankOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            if (tree.size() <= 1)
                continue;

            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree);

            z = z.goToLocalRank(tree.size()).get();
            assertEquals(tree.getNodeAtIndex(tree.size()), z.getFocus());

            if (!z.isRoot())
            {
                System.err.println("tree\n" + tree.toBoxString());

                // To step back one we must get the root rank first
                int rank = z.getFocus().getRank() - 1;

                System.err.println("go to rank " + rank);
                System.err.println("z focus before " + z.getFocus());
                z = z.goToLocalRank(rank).get();
                System.err.println("z focus after " + z.getFocus());
                assertEquals(tree.getNodeAtIndex(tree.size() - 1), z.getFocus());
            }
        }
    }

    @Test
    public void testReplaceEmptyNodeWithEmpty()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(1, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree).after();
            assertSame(z, z.replaceNil(ImTree.Nil()));
        }
    }

    @Test
    public void testInsertEmptyNodeWithEmpty()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(1, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree);
            assertSame(z, z.insertAfter(ImTree.Nil()));
        }
    }

    @Test
    public void testCloseOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree);

            while (z.next().isPresent())
            {
                // If we haven't modified anything, the close returns the original tree
                assertSame(tree, z.close());
                z = z.next().get();
            }
        }
    }

    @Test
    public void testReplaceElementOnMany()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {
            for (int i = 1; i <= tree.size(); i++)
            {
                ImMaybe<ImTreeZipper<Character>> z = ImTreeZipper.onIndex(tree, i);
                List<Character> list = flatten(tree);
                list.set(i - 1, 'x');
                assertEquals(list, flatten(z.get().replaceElement('x').close()));
            }
        }
    }

    @Test
    public void testInsertAfterOnMany()
    {
        List<ImTree<Character>> trees = new ImTreeShapes().allUpToSize(6, true, 'a');
        List<ImTree<Character>> treesToInsert = new ImTreeShapes().allUpToSize(6, true, 'l');

        int testCount = 0;

        for (ImTree<Character> tree : trees)
        {
            // System.err.println(tree);
            ImList<Character> treeAsList = ImList.onList(flatten(tree));

            for (ImTree<Character> treeToInsert : treesToInsert)
            {
                ImList<Character> treeToInsertAsList = ImList.onList(flatten(treeToInsert));
                for (int i = 1; i <= tree.size(); i++)
                {
                    testCount++;
                    ImTreeZipper<Character> newZipper = insertAfterIndex(tree, i, treeToInsert);

                    // This is the expected flattened result
                    List<Character> newList = treeAsList.take(i).append(treeToInsertAsList).append(treeAsList.drop(i)).toList();

                    //                    // Insert after i should be the same as insert before i+1
                    //                    List<Character> newList = insertAtIndexUsingLists(tree, i + 1, treeToInsert);
                    //
                    //                    // The element at the zipper focus should be the element at i-1
                    //                    assertEquals(newZipper.getElement(), newList.get(i - 1));

                    String message = "" + testCount + "\nFailed on\n" + tree.toBoxString() + "\n" + "replace index "
                            + i + "\n" + treeToInsert.toBoxString();
                    assertEquals(message, newList, flatten(newZipper.close()));
                }
            }
        }
    }

    private ImTreeZipper<Character> insertAfterIndex(ImTree<Character> tree, int i, ImTree<Character> treeToInsert)
    {
        return ImTreeZipper.onIndex(tree, i).get().insertAfter(treeToInsert);
    }

    @Test
    public void testOnLeftmost()
    {
        assertEquals(ImTree.Nil(), ImTreeZipper.onLeftmost(t("")).getFocus());

        for (int i = 1; i <= 6; i++)
        {
            for (ImTree<Character> node : new ImTreeShapes().withSize(i, true, 'a'))
            {
                assertEquals(Character.valueOf('a'), ImTreeZipper.onLeftmost(node).getFocus().getElement());
            }
        }
    }

    @Test
    public void testOnRightmost()
    {
        assertEquals(ImTree.Nil(), ImTreeZipper.onRightmost(t("")).getFocus());

        for (int i = 1; i <= 6; i++)
        {
            for (ImTree<Character> node : new ImTreeShapes().withSize(i, true, 'a'))
            {
                assertEquals(Character.valueOf((char) ('a' + node.size() - 1)),
                        ImTreeZipper.onRightmost(node).getFocus().getElement());
            }
        }
    }

    @Test
    public void testNext()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allUpToSize(6, true, 'a'))
        {

            System.err.println(tree.toBoxString());
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(tree);

            List<Character> actual = new ArrayList<Character>();

            if (z.getFocus() != ImTree.<Character>Nil())
                actual.add(z.getElement());

            while (true)
            {
                ImMaybe<ImTreeZipper<Character>> m = z.next();

                if (m.isPresent())
                {
                    actual.add(m.get().getElement());
                    z = m.get();
                }
                else
                    break;
            }

            assertEquals(flatten(tree), actual);
        }
    }

    @Test
    public void testOnIndex()
    {
        for (ImTree<Character> tree : new ImTreeShapes().withSize(12, true, 'a'))
        {
            for (int i = 1; i <= tree.size(); i++)
            {
                ImMaybe<ImTreeZipper<Character>> m = ImTreeZipper.onIndex(tree, i);
                assertEquals(aPlus(i - 1), m.get().getElement());
            }
        }
    }

    @Test
    public void testPreviousOnEmpty()
    {
        ImTreeZipper<Character> z = ImTreeZipper.onRightmost(ImTree.<Character>Nil());

        assertEquals(true, z.previous().isPresent());
    }

    @Test
    public void testNextOnEmpty()
    {
        ImTreeZipper<Character> z = ImTreeZipper.onRightmost(ImTree.<Character>Nil());

        assertEquals(false, z.next().isPresent());
    }

    @Test
    public void testPrevious()
    {
        for (ImTree<Character> tree : new ImTreeShapes().allNonNilUpToSize(6, true, 'a'))
        {
            System.err.println(tree.toBoxString());

            ImTreeZipper<Character> z = ImTreeZipper.onRightmost(tree);

            List<Character> actual = new ArrayList<Character>();

            // Add the focus to `actual`
            actual.add(z.getElement());

            while (z.getRank() > 1)
            {
                ImMaybe<ImTreeZipper<Character>> m = z.previous();

                if (m.isPresent())
                {
                    // Add the previous to the front of the list `actual`
                    actual.add(0, m.get().getElement());
                    z = m.get();
                }
            }

            assertEquals(flatten(tree), actual);
        }
    }

    @Test
    public void testBefore()
    {
        for (ImTree<Character> node : new ImTreeShapes().withSize(6, true, 'a'))
        {

            System.err.println(node.toBoxString());
            ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(node);

            while (true)
            {

                System.err.println(z);
                ImTreeZipper<Character> before = z.before();

                assertEquals(null, before.getElement());

                assertEquals(z.getElement(), before.next().get().getFocus().getElement());
                ImMaybe<ImTreeZipper<Character>> m = z.next();

                if (m.isPresent())
                    z = m.get();
                else
                    break;
            }
        }
    }

    @Test
    public void testBefore1()
    {

        ImTree<Character> t = new ImTreeShapes().withSize(6, true, 'a').get(1);

        System.err.println(t.toBoxString());

        ImTreeZipper<Character> z = ImTreeZipper.onLeftmost(t);

        say(z.getFocus());
        say(z.next().get().getFocus());
        say(z.after().getFocus());

    }

    @Ignore
    public void testAfter()
    {
        for (ImTree<Character> node : new ImTreeShapes().withSize(6, true, 'a'))
        {
            System.err.println(node.toBoxString());
            ImTreeZipper<Character> z = ImTreeZipper.onRightmost(node);

            while (true)
            {
                System.err.println(z);
                ImTreeZipper<Character> after = z.after();

                assertEquals(null, after.getElement());

                assertEquals(z.getElement(), after.previous().get().getElement());
                ImMaybe<ImTreeZipper<Character>> m = z.previous();

                if (m.isPresent())
                    z = m.get();
                else
                    break;
            }
        }
    }

    @Test
    public void testFind()
    {
        List<ImTree<Character>> ones = new ImTreeShapes().allUpToSize(7, true, 'a');

        for (ImTree<Character> one : ones)
        {
            for (int j = 0; j < one.size(); j++)
            {
                ImTreeZipper<Character> z = ImTreeZipper.find(ImTreeZipper.onRoot(one), aPlus(j));
                ImMaybe<ImTreeZipper<Character>> next = z.next();

                if (j < one.size() - 1)
                {
                    assertEquals(aPlus(j + 1), next.get().getElement());
                }

                ImMaybe<ImTreeZipper<Character>> prev = z.previous();
                if (j > 0)
                {
                    assertEquals(aPlus(j - 1), prev.get().getElement());
                }
            }
        }
    }

    private Character aPlus(int j)
    {
        return Character.valueOf((char) ('a' + j));
    }

    @Test
    public void testCreateFromList()
    {
        //		/**
        //		 *      d  
        //		 *    .....  
        //		 *    b   f  
        //		 *   ... ...
        //		 *   a c e -
        //		 */
        //		final ImTree<Character> node = t("d1 b2 a3 c3 f2 e3");
        //		ImList<ImTree<Character>> nodes = ImList.on(getNode(node, 'd'), getNode(node, 'b'), getNode(node, 'a'));
        //
        //		ImTreeZipper<Character> path = ImTreeZipper.onNodes(nodes);
        //		System.out.println(path);

    }

    //	private ImTree<Character> getNode(final ImTree<Character> node, char charToFind)
    //	{
    //		return ImTreeZipper.find(ImTreeZipper.onRoot(node), charToFind).node;
    //	}

    @Test
    public void testFindOnEmpty()
    {
        //		final ImTree<Character> node = ImTree.Nil();
        //		assertSame(ImTreeZipper.getNil(), ImTreeZipper.find(ImTreeZipper.onRoot(node), 'z'));

    }
}