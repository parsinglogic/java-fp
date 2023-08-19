package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.set.ImTree;
import dev.javafp.set.ImTreeFactory;
import dev.javafp.set.ImTreeZipper;
import dev.javafp.shelf.ImShelf;
import dev.javafp.tree.ImRoseTree;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImTestHelper
{
    public static void assertTreesAreEqual(final ImShelf<Character> expected, final ImShelf<Character> actual)
    {
        if (!(expected.equals(actual)))
        {
            assertEquals("" + expected, "" + actual);
        }
    }

    public static void assertTreesAreEqual(final ImTree<Character> expected, final ImTree<Character> actual)
    {
        assertEquals(expected, actual);
    }

    public static <A> void assertListEqualsTree(final List<A> expected, final ImShelf<A> actual)
    {
        assertEquals(expected.toString(), actual.toString());
    }

    public static void assertTreeIs(final String expectedTokens, final ImTree<Character> actual)
    {
        assertTreesAreEqual(t(expectedTokens), actual);
    }

    public static void assertStringEquals(final Object expected, final Object actual)
    {
        assertEquals("" + expected, "" + actual);
    }

    public static ImTree<Character> t(final String string)
    {
        return new ImTreeFactory(string).create();
    }

    public static <A> List<A> flatten(ImTree<A> tree)
    {
        List<A> result = new ArrayList<A>(tree.size());
        flatten(tree, result);
        return result;
    }

    public static <A> List<A> flatten(ImTreeZipper<A> zipper)
    {
        return flatten(zipper.close());
    }

    public static <A> void flatten(ImTree<A> tree, List<A> acc)
    {
        if (tree != ImTree.Nil())
        {
            flatten(tree.getLeft(), acc);
            acc.add(tree.getElement());
            flatten(tree.getRight(), acc);
        }
    }

    public static <A> List<A> flatten(ImRoseTree<A> tree)
    {
        List<A> result = new ArrayList<A>(tree.size());
        flatten(tree, result);
        return result;
    }

    public static <A> void flatten(ImRoseTree<A> tree, List<A> acc)
    {
        acc.add(tree.getElement());
        for (ImRoseTree<A> a : tree.getSubTrees())
        {
            flatten(a, acc);
        }
    }

    public static ImList<ImList<Integer>> sums(int n, int m)
    {
        if (n == 1)
        {
            ImList<Integer> one = ImList.on(m);
            return ImList.<ImList<Integer>>on(one);
        }
        else
        {
            ImList<ImList<Integer>> result = ImList.empty();
            for (int i = 0; i <= m; i++)
            {
                for (ImList<Integer> s : sums(n - 1, m - i))
                {
                    result = ImList.cons(ImList.cons(i, s), result);
                }
            }
            return result;
        }

    }

    //    public static void failExpectedException()
    //    {
    //        fail("Expected exception");
    //    }
    //
    //    public static void failExpectedException(Class<?> clazz)
    //    {
    //        fail("Expected exception " + clazz.getName());
    //    }
    //
    //    public static void failExpectedException(String message, Class<?> clazz)
    //    {
    //        fail(message + " - expected exception " + clazz.getName());
    //    }

    /**
     * This reverses the expected and actual and strips spaces from the front of the actual so that it is easier
     * to cut and paste the code into an example section of a javadoc.
     */
    public static void checkExample(Object actual, Object expected)
    {
        assertEquals(("" + expected).replaceFirst("^ +", ""), "" + actual);
    }

    public static List<Integer> makeList(int size, int start)
    {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < size; i++)
        {
            result.add(i + start);
        }

        return result;
    }

}