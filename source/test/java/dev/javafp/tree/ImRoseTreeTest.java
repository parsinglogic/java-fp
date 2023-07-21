package dev.javafp.tree;

import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static dev.javafp.tree.ImRoseTree.leaf;
import static dev.javafp.tree.ImRoseTree.withElements;
import static dev.javafp.tree.ImRoseTree.withNodes;
import static dev.javafp.util.ImTestHelper.checkExample;
import static dev.javafp.util.ImTestHelper.flatten;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SuppressWarnings("unchecked")
public class ImRoseTreeTest
{

    @Test
    public void testAllUpToSix() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {

                int i = 1;
                for (String string : flatten(t))
                {
                    assertEquals(string, t.getNodeAtIndex(i).getElement());
                    i++;

                    assertEquals(true, true);
                }
            }
        }
    }

    @Test
    public void testEquals() throws Exception
    {

        // Get a list of all tree shapes with size <= 5
        ImList<ImRoseTree<String>> ts = ImRange.oneTo(6).flatMap(i -> ImRoseTreeShapes.allTreesWithSize(i));

        // Form the cartesian product ts x ts
        ImList<ImPair<ImRoseTree<String>, ImRoseTree<String>>> pairs = ts.cartesianProduct();

        // The only equal trees will be the ones that are the same
        ImList.foreachPair(pairs, (i, j) -> assertEquals(i.equals(j), i == j));

        // Get another list of tree shapes
        ImList<ImRoseTree<String>> ts2 = ImRange.oneTo(6).flatMap(i -> ImRoseTreeShapes.allTreesWithSize(i));

        // Form the cartesian product ts x ts2
        ImList<ImPair<ImRoseTree<String>, ImRoseTree<String>>> pairs2 = ts2.cartesianProduct();

        // The equal trees will match the string equals
        ImList.foreachPair(pairs, (i, j) -> assertEquals(i.equals(j), i.toString().equals(j.toString())));
    }

    @Test
    public void testEqualsWithTreesOfDifferentTypes() throws Exception
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);
        ImRoseTree<Boolean> tree2 = withElements(true, true, true);
        assertEquals(false, tree.equals(tree2));
    }

    @Test
    public void testEqualsWithNull() throws Exception
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);

        assertEquals(false, tree.equals(null));
    }

    @Test
    public void exampleMakeTree() throws Exception
    {
        ImRoseTree<Integer> tree = makeTree(3, 3);

        System.err.println(tree.toBoxString());
    }

    @Test
    public void testReplace() throws Exception
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);
        assertEquals("" + withElements(4, 2, 3), "" + tree.replaceElement(4));
    }

    @Test
    public void testReplaceWithSameElementReturnsSameTree() throws Exception
    {
        Integer rootElement = Integer.valueOf(1);
        ImRoseTree<Integer> tree = withElements(rootElement, 2, 3);
        assertSame(tree, tree.replaceElement(rootElement));
    }

    @Test
    public void exampleToBoxString() throws Exception
    {
        System.err.println(withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toBoxString());
    }

    @Test
    public void exampleToString() throws Exception
    {
        System.err.println(withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toString());
    }

    @Test
    public void exampleIterator() throws Exception
    {
        System.err.println(withNodes("a", leaf("b"), withElements("c", "e", "f", "g"), withElements("d", "h")).toBoxString());
    }

    @Test
    public void exampleGetNodeAtIndex() throws Exception
    {
        ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c", "e", "f", "g"), withElements("d", "h"));

        System.err.println(t.toBoxString());

        checkExample(t.getNodeAtIndex(5).getElement(), "f");
    }

    @Test
    public void exampleContains() throws Exception
    {
        ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c"), withElements("d", "h"));
        System.err.println(t.toBoxString());
        checkExample(t.contains("d"), "true");
    }

    @Test
    public void exampleGetChildren() throws Exception
    {
        ImRoseTree<String> t = withElements("c", "e", "f", "g");
        System.err.println(t.toBoxString());

        checkExample(t.getSubTrees().size(), "3");
        checkExample(t.getSubTrees().at(1).getClass().getName(), "dev.javafp.tree.ImRoseTree");
    }

    @Test
    public void exampleMap() throws Exception
    {
        ImRoseTree<String> t = withElements("c", "e", "f", "g");

        checkExample(t.map(String::toUpperCase), "C (E F G)");
    }

    private ImRoseTree<Integer> makeTree(int width, int height)
    {
        return makeTree(width, height, 1);
    }

    /**
     * Create a rose tree where the elements are Integers and each node has `width` child nodes
     *
     *
     */
    private ImRoseTree<Integer> makeTree(int width, int height, int count)
    {
        int c = count + 1;

        if (height == 2)
        {
            return withElements(count, ImList.<Integer>onAll(range(c, width)));
        }
        else
        {
            // Hmm - the fact that I am passing in the count here is defeating me in writing a more fp approach
            List<ImRoseTree<Integer>> kids = new ArrayList<>();
            for (int i = 0; i < width; i++)
            {
                ImRoseTree<Integer> t = makeTree(width, height - 1, c);
                kids.add(t);
                c += t.size();
            }

            return withNodes(Integer.valueOf(count), ImList.on(kids));
        }
    }

    private List<Integer> range(int first, int count)
    {
        List<Integer> range = new ArrayList<Integer>();
        for (int i = first; i < first + count; i++)
        {
            range.add(i);
        }
        return range;
    }
}