package dev.javafp.ex;

import dev.javafp.lst.ImList;
import org.junit.Ignore;

import java.nio.file.Paths;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by aove215 on 13/07/2016.
 */
public class FileProblemTest
{
    @Ignore
    public void testOne()
    {
        ImList<String> expected = ImList.on(
                "Path          = \"abc\"",
                "Absolute path = \"/Users/aove215/projects/drum/drum/jadle.ij/abc\"",
                "Real path     = (path does not exist)");

        assertEquals(expected, FileProblem.getInfo(Paths.get("abc")));
    }
}