package dev.javafp.graph;

import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class GraphBuilderTest
{

    @Test
    public void testAllGraphsOfSizeThree()
    {

        ImList<ImGraph> gs = GraphBuilder.allGraphsOfSize(3);

        ImSet<ImGraph> setOfGraphs = gs.toImSet();

        assertEquals(512, setOfGraphs.size());

    }

    @Test
    public void testAllGraphsOfSizeFour()
    {

        ImList<ImGraph> gs = GraphBuilder.allGraphsOfSize(4).flush();

        ImSet<ImGraph> setOfGraphs = gs.toImSet();

        say(setOfGraphs.getStats());

        assertEquals(64 * 1024, gs.size());
    }

    @Test
    public void testAllGraphsOfSizeTwo()
    {

        ImList<ImGraph> gs = GraphBuilder.allGraphsOfSize(2);

        ImSet<ImGraph> setOfGraphs = gs.toImSet();

        assertEquals(16, setOfGraphs.size());

    }

}