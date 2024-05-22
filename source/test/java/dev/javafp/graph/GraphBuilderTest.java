package dev.javafp.graph;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.*;

public class GraphBuilderTest
{

    @Test
    public void testCycleFinder()
    {

        ImList<ImGraph<String, String, String>> gs = GraphBuilder.getAllGraphsOfSizeThree();

        say(gs.map(i -> i.showAsSets()));

    }
}