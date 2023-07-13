package dev.javafp.tree;

import dev.javafp.lst.ImList;
import dev.javafp.util.TextUtils;
import org.junit.Test;

import static dev.javafp.util.ImTestHelper.flatten;
import static junit.framework.TestCase.assertEquals;

public class ImRoseTreeIteratorTest
{
    @Test
    public void testAllUpToSix() throws Exception
    {
        for (int count = 1; count <= 6; count++)
        {
            ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(count);

            for (ImRoseTree<String> t : trees)
            {
                //                System.out.println(t.toBoxString());

                assertEquals(count, t.size());

                assertEquals(TextUtils.join(flatten(t), " "), TextUtils.join(t.iterator(), " "));

            }
        }
    }
}