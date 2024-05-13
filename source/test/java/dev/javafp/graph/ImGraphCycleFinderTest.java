package dev.javafp.graph;

import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import static dev.javafp.tuple.ImPair.on;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertTrue;

public class ImGraphCycleFinderTest
{
    @Test
    public void one()
    {
        /**
         * A test graph:
         *
         *              4
         *          ┌─◁─▢─◁─┐
         *    1    2│   │   │5    6
         *    ▢──▷──▢   △   ▢──▷──▢
         *          │   │   │
         *          └─▷─▢─▷─┘
         *              3
         *
         */
        ImList<Integer> nodes = ImRange.oneTo(6);
        ImGraph<Integer, String, String> g0 = nodes.foldl(ImGraph.on(), (z, i) -> z.addNode(i, ""));

        ImList<ImPair<Integer, Integer>> pairs = ImList.on(on(1, 2), on(2, 3), on(3, 4), on(4, 2), on(3, 5), on(5, 4), on(5, 6));

        say("pairs", pairs);

        var g = pairs.foldl(g0, (z, i) -> z.addArc("", i.fst, i.snd));

        say("g", g.showAsSets());

        assertTrue(g.hasCycle());

        ImGraphCycleFinder finder = ImGraphCycleFinder.with(g);

        ImList<ImList<ImArc<Integer, String>>> allCycles = finder.getAllCycles();

        say("allCycles", allCycles.toString("\n"));

        ImGraph<Integer, String, String> small = finder.removeNonCycles();

        say("small", small.showAsSets());
        //        say("small", small.show());

        ImGraphTest.checkIntegrity(small);

        //        var g2 = g.addArc("", 4, 1);
        //
        //        var a2 = PathFinder.with(g2).getAllPaths();
        //
        //        say("a2", a2.toString("\n"));
    }
}