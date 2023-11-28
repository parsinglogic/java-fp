package dev.javafp.tree;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MutableTreeTest
{
    @Test
    public void testOne()
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                MutableTree<String> mt = MutableTree.from(t);

                assertEquals(t.toString(), mt.toString());
            }
        }
    }

    @Test
    public void testRemove()
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {

                for (int i = 2; i <= t.size(); i++)
                {
                    MutableTree<String> mt = MutableTree.from(t);

                    boolean removed = mt.removeNodeWithElement("" + i);

                    assertEquals(true, mt.toString().indexOf("" + i) == -1);

                    assertEquals("failed on " + i, true, removed);

                }

            }
        }
    }

}