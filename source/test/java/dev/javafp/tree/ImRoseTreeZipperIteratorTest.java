package dev.javafp.tree;

import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static dev.javafp.util.ImTestHelper.flatten;
import static org.junit.Assert.assertEquals;

public class ImRoseTreeZipperIteratorTest
{
    @Test
    public void testAllUpToSix() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                // System.out.println(t.toBoxString());

                assertEquals(count, t.size());

                Iterator<ImRoseTreeZipper<String>> zi = t.getZipperIterator();

                List<String> things = new ArrayList<String>();

                while (zi.hasNext())
                {
                    things.add(zi.next().getElement());
                }

                assertEquals(TextUtils.join(flatten(t), " "), TextUtils.join(things, " "));
            }
        }
    }
}