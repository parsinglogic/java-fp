package dev.javafp.tree;

import dev.javafp.eq.Eq;
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
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SuppressWarnings("unchecked")
public class ImRoseTreeTest
{

    @Test
    public void testAllUpToSix()
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {

                say(t.toBoxString());

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
    public void testAllOfSizeSix()
    {

        ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(6);

        say("# of trees", trees.size());

        ImRoseTree<String> t = trees.at(6);
        say(t.toBoxString());
        say(t.toString());
        say(t.toImList().toString("\n"));

        //        int i = 0;
        //        for (ImRoseTree<String> tt : trees)
        //        {
        //            say(i++, tt.toBoxString());
        //
        //        }

        //        1 (2 3 (4) 5 6)

    }

    @Test
    public void testEquals()
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
    public void testEqualsWithTreesOfDifferentTypes()
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);
        ImRoseTree<Boolean> tree2 = withElements(true, true, true);
        assertEquals(false, tree.equals(tree2));
    }

    @Test
    public void testEqualsWithNull()
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);

        assertEquals(false, tree.equals(null));
    }

    @Test
    public void exampleMakeTree()
    {
        ImRoseTree<Integer> tree = makeTree(3, 3);

        System.err.println(tree.toBoxString());
    }

    @Test
    public void testReplace()
    {
        ImRoseTree<Integer> tree = withElements(1, 2, 3);
        assertEquals("" + withElements(4, 2, 3), "" + tree.replaceElement(4));
    }

    @Test
    public void testReplaceWithSameElementReturnsSameTree()
    {
        Integer rootElement = Integer.valueOf(1);
        ImRoseTree<Integer> tree = withElements(rootElement, 2, 3);
        assertSame(tree, tree.replaceElement(rootElement));
    }

    @Test
    public void exampleToBoxString()
    {
        System.err.println(withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toBoxString());
    }

    @Test
    public void exampleToString()
    {
        System.err.println(withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toString());
    }

    @Test
    public void exampleIterator()
    {
        System.err.println(withNodes("a", leaf("b"), withElements("c", "e", "f", "g"), withElements("d", "h")).toBoxString());
    }

    @Test
    public void exampleGetNodeAtIndex()
    {
        ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c", "e", "f", "g"), withElements("d", "h"));

        System.err.println(t.toBoxString());

        checkExample(t.getNodeAtIndex(5).getElement(), "f");
    }

    @Test
    public void exampleContains()
    {
        ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c"), withElements("d", "h"));
        System.err.println(t.toBoxString());
        checkExample(t.contains("d"), "true");
    }

    @Test
    public void exampleGetChildren()
    {
        ImRoseTree<String> t = withElements("c", "e", "f", "g");
        System.err.println(t.toBoxString());

        checkExample(t.getSubTrees().size(), "3");
        checkExample(t.getSubTrees().at(1).getClass().getName(), "dev.javafp.tree.ImRoseTree");
    }

    @Test
    public void exampleMap()
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

            return withNodes(Integer.valueOf(count), ImList.onList(kids));
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

    /**
     * <p> Get the next token in the list, skipping over spaces.
     * <p> Return a pair with the token as the first element and the remainder of the list as
     * the second element.
     * <p> If there are no tokens in the list
     * return the empty string in the first position of the pair
     * <p> A token is
     * (
     * )
     * [^() ]* - ie a sequences of chars that are not (, ) or space
     *
     */
    public static ImPair<String, ImList<Character>> getToken(ImList<Character> cs)
    {
        if (cs.isEmpty())
            return ImPair.on("", cs);
        else if (Eq.uals('(', cs.head()))
            return ImPair.on("(", cs.tail());
        else if (Eq.uals(')', cs.head()))
            return ImPair.on(")", cs.tail());
        else if (Eq.uals(' ', cs.head()))
            return getToken(cs.tail());
        else
        {
            return cs.splitWhile(c -> "() ".indexOf(c) == -1).map(Object::toString, j -> j);
        }
    }
    //
    //    ImPair<ImRoseTree<Integer>,ImList<Character>>  getRoot(ImList<Character> cs)
    //    {
    //
    //        ImPair<String, ImList<Character>> p1 = getToken(cs);
    //
    //        ImPair<String, ImList<Character>> p2 = getToken(p1.snd);
    //
    //        ImPair<ImList<ImRoseTree<Integer>>,ImList<Character>>  kidsPair = Equals.isEqual(p2.fst, "(")
    //                                           ? getKids(p2.snd)
    //                                           : ImPair.on(ImList.on().upCast(), p2.snd);
    //
    //        return ImPair.on(ImRoseTree.withNodes(Integer.valueOf(p1.fst), kidsPair.fst), kidsPair.snd);
    //    }
    //
    //
    //
    //
    //    }
}