package dev.javafp.ex;

import dev.javafp.lst.ImList;
import org.junit.Test;

import java.nio.file.Paths;

import static dev.javafp.util.Say.say;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by aove215 on 13/07/2016.
 */
public class FileProblemTest
{
    @Test
    public void testOne()
    {
        ImList<String> expected = ImList.on(
                "Path          = \"/abc\"",
                "Absolute path = \"/abc\"",
                "Real path     = (path does not exist)");

        assertEquals(expected, FileProblem.getInfo(Paths.get("/abc")));

        say(expected.toString("\n"));
    }
}