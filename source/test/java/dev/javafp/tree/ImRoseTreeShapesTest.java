package dev.javafp.tree;

import dev.javafp.lst.ImList;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static dev.javafp.util.ImTestHelper.assertStringEquals;

public class ImRoseTreeShapesTest
{

    @Test
    public void testOne()
    {
        assertStringEquals("[1]", ImRoseTreeShapes.allTreesWithSize(1));
    }

    @Test
    public void testTwo()
    {
        assertStringEquals("[1 (2)]", ImRoseTreeShapes.allTreesWithSize(2));
    }

    @Test
    public void testThree()
    {
        ImList<ImRoseTree<String>> trees = ImRoseTreeShapes.allTreesWithSize(3);

        ImList<String> expected = ImList.on("1 (2 (3))", "1 (2 3)");
        assertTreesEqual(expected, trees);
    }

    @Test
    public void testFour()
    {
        assertTreesEqual(ImList.on("1 (2 (3 4))", "1 (2 (3 (4)))", "1 (2 (3) 4)", "1 (2 3 (4))", "1 (2 3 4)"),
                ImRoseTreeShapes.allTreesWithSize(4));
    }

    private void assertTreesEqual(ImList<String> ss, ImList<ImRoseTree<String>> trees)
    {
        TestUtils.assertSetsEqual("", ss.toSet(), trees.map(Object::toString).toSet());
    }

    @Test
    public void testFive()
    {
        ImList<String> expected = ImList.on("1 (2 (3 4 5))",
                "1 (2 (3 4 (5)))",
                "1 (2 (3 (4) 5))",
                "1 (2 (3 (4 (5))))",
                "1 (2 (3 (4 5)))",
                "1 (2 (3 4) 5)",
                "1 (2 (3 (4)) 5)",
                "1 (2 (3) 4 (5))",
                "1 (2 (3) 4 5)",
                "1 (2 3 (4 5))",
                "1 (2 3 (4 (5)))",
                "1 (2 3 (4) 5)",
                "1 (2 3 4 (5))",
                "1 (2 3 4 5)");
        assertTreesEqual(expected, ImRoseTreeShapes.allTreesWithSize(5));
    }
}