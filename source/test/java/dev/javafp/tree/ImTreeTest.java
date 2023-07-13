package dev.javafp.tree;

import dev.javafp.ex.ImIndexOutOfBoundsException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static dev.javafp.util.ImTestHelper.assertTreeIs;
import static dev.javafp.util.ImTestHelper.assertTreesAreEqual;
import static dev.javafp.util.ImTestHelper.flatten;
import static dev.javafp.util.TestUtils.failExpectedException;
import static junit.framework.TestCase.assertEquals;

public class ImTreeTest
{
    private ImTree<Character> nil = ImTree.Nil();

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testCreateLeft5() throws Exception
    {
        ImTree<Character> e = leaf('e');
        ImTree<Character> d = tree('d', e, nil);
        ImTree<Character> c = tree('c', d, nil);
        ImTree<Character> b = tree('b', c, nil);
        ImTree<Character> a = tree('a', b, nil);

        assertTreesAreEqual(a, t("a1 b2 c3 d4 e5"));
    }

    @Test
    public void testGetAtIndex0() throws Exception
    {
        try
        {
            ImTree<Character> e = leaf('e');
            e.getNodeAtIndex(0);
            failExpectedException(ImIndexOutOfBoundsException.class);
        } catch (ImIndexOutOfBoundsException e1)
        {

        }
    }

    @Test
    public void testGetAtIndexTooLarge() throws Exception
    {
        try
        {
            ImTree<Character> e = leaf('e');
            e.getNodeAtIndex(2);
            failExpectedException(ImIndexOutOfBoundsException.class);
        } catch (ImIndexOutOfBoundsException e1)
        {

        }
    }

    @Test
    public void testGetAtIndex() throws Exception
    {
        List<ImTree<Character>> shapes = new ImTreeShapes().allUpToSize(4, true, 'a');

        for (ImTree<Character> imTree : shapes)
        {
            ImTreeIterator<Character> it = new ImTreeIterator<Character>(imTree);

            System.err.println(imTree.toBoxString());
            int count = 0;
            while (it.hasNext())
            {
                count++;
                assertEquals("\n" + imTree.toBoxString() + "\nat index " + count, it.next(),
                        imTree.getNodeAtIndex(count).getElement());
            }
            System.err.println("\n");
        }
    }

    private ImTree<Character> tree(char c, ImTree<Character> left, ImTree<Character> right)
    {
        return new ImTree<Character>(Character.valueOf(c), left, right);
    }

    private ImTree<Character> leaf(char c)
    {
        return new ImTree<Character>(Character.valueOf(c), nil, nil);
    }

    @Test
    public void testCreateRight5() throws Exception
    {
        ImTree<Character> e = leaf('e');
        ImTree<Character> d = tree('d', nil, e);
        ImTree<Character> c = tree('c', nil, d);
        ImTree<Character> b = tree('b', nil, c);
        ImTree<Character> a = tree('a', nil, b);
        assertTreesAreEqual(a, t("a1 -2 b2 -3 c3 -4 d4 -5 e5"));
    }

    @Test
    public void testTree3() throws Exception
    {
        ImTree<Character> alone = leaf('a');
        ImTree<Character> leftOnly = tree('a', leaf('b'), nil);
        ImTree<Character> rightOnly = tree('a', nil, leaf('c'));
        ImTree<Character> both = tree('a', leaf('b'), leaf('c'));

        ImTree<Character> a1 = t("a1");
        assertTreesAreEqual(alone, a1);
        assertTreesAreEqual(leftOnly, t("a1 b2"));
        assertTreesAreEqual(rightOnly, t("a1 -2 c2"));
        assertTreesAreEqual(both, t("a1 b2 c2"));

        assertTreesAreEqual(tree('d', leftOnly, nil), t("d1 a2 b3"));
        assertTreesAreEqual(tree('d', rightOnly, nil), t("d1 a2 -3 c3"));
        assertTreesAreEqual(tree('d', both, nil), t("d1 a2 b3 c3"));

        assertTreesAreEqual(tree('d', nil, leftOnly), t("d1 -2 a2 b3"));
        assertTreesAreEqual(tree('d', nil, rightOnly), t("d1 -2 a2 -3 c3"));
        assertTreesAreEqual(tree('d', nil, both), t("d1 -2 a2 b3 c3"));
    }

    private ImTree<Character> t(String string)
    {
        return new ImTreeFactory(string).create();
    }

    @Test
    public void testToString() throws Exception
    {
        System.out.println(t("a1"));
        System.out.println(t("a1 b2"));
        System.out.println(t("a1 -2 b2"));
        System.out.println(t("a1 -2 b2 -3 c3"));
        System.out.println(t("a1 b2 c2"));
        System.out.println(t("a1 b2 d3 e3 f4 g4 c2"));
        System.out.println(t("e1 b2 d3 f3 a2 g3 c3"));
    }

    @Test
    public void testIsBalanced() throws Exception
    {
        for (int i = 0; i < 5; i++)
        {
            List<ImTree<Character>> shapes = new ImTreeShapes().withSize(i);
            int count = 0;
            for (ImTree<Character> tree : shapes)
            {
                if (tree.isBalanced())
                    count++;
            }

            if (i <= 1)
                assertEquals("" + i + " " + count, 1, count);
            if (i == 2)
                assertEquals("" + i + " " + count, 2, count);
            if (i == 3)
                assertEquals("" + i + " " + count, 1, count);
            if (i == 4)
                assertEquals("" + i + " " + count, 4, count);
            if (i == 5)
                assertEquals("" + i + " " + count, 12, count);
        }
    }

    @Test
    public void testConcat3WithManyTrees() throws Exception
    {

        // Create all balanced tree shapes up to size 6

        // Create these shapes again - but give them different names

        List<ImTree<Character>> lefts = new ImTreeShapes().allUpToSize(6, true, 'a');
        List<ImTree<Character>> rights = new ImTreeShapes().allUpToSize(6, true, 'm');

        // for each pair, combine them with concat
        // the resulting tree should be balanced and when flattened, it should be the
        // same as the concatenation of flattening the original trees
        for (ImTree<Character> left : lefts)
        {

            for (ImTree<Character> right : rights)
            {

                List<Character> expected = flatten(left);
                expected.add('x');
                expected.addAll(flatten(right));

                ImTree<Character> concat3 = ImTree.concat3('x', left, right);
                assertEquals(true, concat3.isBalanced());
                assertEquals(expected, flatten(concat3));
            }
        }

    }

    @Test
    public void testToString3() throws Exception
    {
        System.out.println(t("f1 e2 d3 c4 e5"));
    }

    @Test
    public void testLeftDeepSwizzle() throws Exception
    {
        ImTree<Character> expected = t("e1 b2 d3 f3 a2 g3 c3");

        ImTree<Character> after = ImTree.newTreeL('a', t("b1 d2 e2 f3 g3"), t("c1"));
        System.out.println(after);
        assertTreesAreEqual(expected, after);
    }

    @Test
    public void testConcat3Simple() throws Exception
    {
        assertTreeIs("b1 a2 c2", ImTree.concat3('b', t("a1"), t("c1")));
        assertTreeIs("d1 b2 a3 c3 f2 e3 g3", ImTree.concat3('f', t("b1 a2 d2 c3 e3"), t("g1")));
    }

    @Test
    public void testLeftShallowSwizzle() throws Exception
    {
        ImTree<Character> expected = t("b1 d2 x3 a2 e3 c3");

        // We have to give d a child to make it unbalanced enough
        ImTree<Character> after = ImTree.newTreeL('a', t("b1 d2 x3 e2"), t("c1"));
        System.out.println(after);
        assertTreesAreEqual(expected, after);
    }

    @Test
    public void testRightDeepSwizzle() throws Exception
    {
        ImTree<Character> expected = t("d1 a2 b3 f3 c2 g3 e3");

        ImTree<Character> after = ImTree.newTreeR('a', t("b1"), t("c1 d2 f3 g3 e2"));
        System.out.println(after);
        assertTreesAreEqual(expected, after);
    }

    @Test
    public void testRightShallowSwizzle() throws Exception
    {
        ImTree<Character> expected = t("c1 a2 b3 d3 e2 x3");

        // We have to give e a child to make it unbalanced enough
        ImTree<Character> after = ImTree.newTreeR('a', t("b1"), t("c1 d2 e2 x3"));
        System.out.println(after);
        assertTreesAreEqual(expected, after);
    }

    @Test
    public void testTreesForSwizzling() throws Exception
    {
        System.out.println(t("a1 b2 d3 e3 f4 g4 c2"));
        System.out.println(t("e1 b2 d3 f3 a2 g3 c3"));

        // left shallow
        System.out.println(t("a1 b2 d3 e3 c2"));
        System.out.println(t("b1 d2 a2 e3 c3"));

        // right deep
        System.out.println(t("a1 b2 c2 d3 f4 g4 e3"));
        System.out.println(t("d1 a2 b3 f3 c2 g3 e3"));

        // right shallow
        System.out.println(t("a1 b2 c2 d3 e3"));
        System.out.println(t("c1 a2 b3 d3 e2"));
    }

    @Test
    public void testToString2() throws Exception
    {
        ImTree<Character> c = tree('c', nil, nil);
        ImTree<Character> d = tree('d', nil, nil);
        ImTree<Character> b = tree('b', c, d);
        ImTree<Character> a = tree('a', b, nil);
        System.out.println(a);

    }

    @Test
    public void testMap() throws Exception
    {

        assertEquals("a (b (d e (f g)) c)".toUpperCase(), "" + t("a1 b2 d3 e3 f4 g4 c2").map(i -> i.toUpperCase(i)));

    }

}