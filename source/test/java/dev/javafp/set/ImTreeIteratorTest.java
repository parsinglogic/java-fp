package dev.javafp.set;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static dev.javafp.util.ImTestHelper.flatten;
import static dev.javafp.util.ImTestHelper.t;
import static org.junit.Assert.assertEquals;

public class ImTreeIteratorTest
{

    @Test
    public void testOnNil()
    {
        final ImTree<Character> node = t("");

        ImTreeIterator<Character> it = ImTreeIterator.on(node);
        assertEquals(false, it.hasNext());
    }

    @Test
    public void testOnTree()
    {
        // Test that iterator as a list returns the same as flatten for all tree shapes of size 0 to 7
        List<ImTree<Character>> shapes = new ImTreeShapes().allUpToSize(7, true, 'a');

        for (ImTree<Character> node : shapes)
        {
            //System.err.println("\n" + node.toBoxString() + "\n" + flatten(node) + "\n" + listFromIterator(node));
            assertEquals("failed on \n" + node.toBoxString() + "\n", flatten(node), listFromIterator(node));
        }
    }

    private <A> List<A> listFromIterator(ImTree<A> node)
    {
        List<A> result = new ArrayList<A>();

        ImTreeIterator<A> it = ImTreeIterator.on(node);

        while (it.hasNext())
        {
            result.add(it.next());
        }

        return result;

    }

}