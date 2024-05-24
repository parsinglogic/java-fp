package dev.javafp.graph;

import dev.javafp.eq.Eq;
import dev.javafp.ex.ImException;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.rand.Rando;
import dev.javafp.set.ImMap;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import static dev.javafp.graph.ImGraph.Dir.In;
import static dev.javafp.graph.ImGraph.Dir.Out;
import static dev.javafp.tuple.ImPair.on;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by aove215 on 10/06/16.
 */
public class ImGraphTest
{
    @Test
    public void testOne()
    {
        ImGraph<String, Integer, String> g1 = ImGraph.empty();

        assertFalse(g1.containsNodeWithKey("0"));

        ImGraph<String, Integer, String> g2 = g1.addNode("0", 0);

        assertTrue(g2.containsNodeWithKey("0"));

        ImGraph<String, Integer, String> g3 = g2.addNode("1", 1);

        assertTrue(g3.containsNodeWithKey("0"));
        assertTrue(g3.containsNodeWithKey("1"));

        ImGraph<String, Integer, String> g4 = g3.addArc("arc1", "0", "1");
        assertConnected(g4, "arc1", "0", "1", 1);

        assertEquals(ImSet.on("1"), g4.getAdjacents(Out, "arc1", "0"));
        assertEquals(ImSet.on("0"), g4.getAdjacents(In, "arc1", "1"));

        ImGraph<String, Integer, String> g5 = g4.addNodeToParentIfMissing("arc2", "1", "2", 2);
        assertConnected(g5, "arc1", "0", "1", 1);

        assertConnected(g5, "arc2", "1", "2", 2);

        ImGraph<String, Integer, String> g6 = g5.addNodeToParentIfMissing("arc3", "0", "3", 3);
        assertConnected(g6, "arc2", "1", "2", 2);
        ImGraph<String, Integer, String> g7 = g6.addArc("arc4", "3", "2");

        assertConnected(g7, "arc2", "1", "2", 2);
        assertConnected(g7, "arc4", "3", "2", 2);

        //
        // 0  -arc1->  1  -arc2->   2
        // |                        |
        // |  -arc3->  3  -arc4->   |

        assertEquals("", ImSet.on("1", "3"), g7.getAdjacents(Out, "0"));
        assertEquals("", ImSet.on("1", "3"), g7.getAdjacents(In, "2"));

        //        assertEquals(ImList.on(k1), g4.get(In,k2));
        //
        //        ImGraph<String, String> g5 = g4.mutate(k1, a -> mutate(a));
    }

    /**
     * Create a graph like this:
     *
     *
     *        |--> A
     *        |    |- art -> x
     *        |    |- mod -> C
     *        |    |         |- art -> z
     *        |    |         |- mod -> D
     *        |    |         |         |- art -> y
     *        |--> B
     *        |    |- art -> (z)
     *        |    |- mod -> (C)
     */
    private ImGraph<String, String, String> makeTestGraph()
    {
        return ImGraph.<String, String, String>empty().addNode("A", "")
                .addNode("B", "")
                .addNodeToParentIfMissing("mod", "A", "C", "")
                .addNodeToParentIfMissing("mod", "B", "C", "")
                .addNodeToParentIfMissing("art", "A", "x", "")
                .addNodeToParentIfMissing("mod", "C", "D", "")
                .addNodeToParentIfMissing("art", "C", "z", "")
                .addNodeToParentIfMissing("art", "B", "z", "")
                .addNodeToParentIfMissing("art", "D", "y", "");
    }

    /**
     * <p> Create a graph like this:
     *
     * <pre>{@code
     *          a
     *         / \
     *        c - b
     * }</pre>
     *
     */
    private ImGraph<String, String, String> makeCycleGraph()
    {
        return ImGraph.<String, String, String>empty()
                .addNode("a", "")
                .addNodeToParentIfMissing("-", "a", "b", "")
                .addNodeToParentIfMissing("-", "b", "c", "")
                .addArc("-", "c", "a");
    }

    @Test
    public void testABCDGraphViz()
    {
        /**
         *
         *            A    B
         *           / \ m/|
         *         a/  m\/ |
         *         x    C a|
         *             / \ |
         *           m/  a\|
         *           D     z
         *          a|
         *           y
         */
        ImGraph<String, String, String> g1 = makeTestGraph();

        say("testGraph", g1);

        say("visGraph", g1.getGraphVizGraph());
    }

    @Test
    public void testABCDGraphPaths()
    {

        /**
         * Create a graph like this:
         *
         *
         *        |--> A
         *        |    |- art -> x
         *        |    |- mod -> C
         *        |    |         |- art -> z
         *        |    |         |- mod -> D
         *        |    |         |         |- art -> y
         *        |--> B
         *        |    |- art -> (z)
         *        |    |- mod -> (C)
         */
        ImGraph<String, String, String> g1 = makeTestGraph();

        assertEquals("[[z, B], [z, C, A], [z, C, B]]", g1.getPaths(In, ImSet.on("mod", "art"), "z").toString());
        assertEquals("[[z, B], [z, C]]", g1.getPaths(In, ImSet.on("art"), "z").toString());
        assertEquals("[[D, C, A], [D, C, B]]", g1.getPaths(In, ImSet.on("mod"), "D").toString());
    }

    @Test
    public void testABCD()
    {

        /**
         * Create a graph like this:
         *
         *
         *        |--> A
         *        |    |- art -> x
         *        |    |- mod -> C
         *        |    |         |- art -> z
         *        |    |         |- mod -> D
         *        |    |         |         |- art -> y
         *        |--> B
         *        |    |- art -> (z)
         *        |    |- mod -> (C)
         */

        ImGraph<String, String, String> g1 = makeTestGraph();

        assertConnected(g1, "art", "A", "x", "");
        assertConnected(g1, "mod", "A", "C", "");
        assertConnected(g1, "mod", "B", "C", "");
        assertConnected(g1, "art", "B", "z", "");

        assertConnected(g1, "mod", "C", "D", "");
        assertConnected(g1, "art", "C", "z", "");

        assertConnected(g1, "art", "D", "y", "");

        // Check the outs
        assertEquals("", ImSet.on("x", "C"), g1.getAdjacents(Out, "A"));
        assertEquals("", ImSet.on("z", "C"), g1.getAdjacents(Out, "B"));
        assertEquals("", ImSet.on("z", "D"), g1.getAdjacents(Out, "C"));

        assertEquals("", ImSet.on("y"), g1.getAdjacents(Out, "D"));

        assertEquals("", ImSet.on(), g1.getAdjacents(Out, "x"));
        assertEquals("", ImSet.on(), g1.getAdjacents(Out, "y"));
        assertEquals("", ImSet.on(), g1.getAdjacents(Out, "z"));

        // Check the ins
        assertEquals("", ImSet.on(), g1.getAdjacents(In, "A"));
        assertEquals("", ImSet.on(), g1.getAdjacents(In, "B"));
        assertEquals("", ImSet.on("A", "B"), g1.getAdjacents(In, "C"));
        assertEquals("", ImSet.on("C"), g1.getAdjacents(In, "D"));

        assertEquals("", ImSet.on("A"), g1.getAdjacents(In, "x"));
        assertEquals("", ImSet.on("D"), g1.getAdjacents(In, "y"));
        assertEquals("", ImSet.on("B", "C"), g1.getAdjacents(In, "z"));

        // Check Closures - no labels
        assertEquals("", ImSet.on("C", "D", "x", "y", "z"), g1.getClosure(Out, "A"));
        assertEquals("", ImSet.on("C", "D", "y", "z"), g1.getClosure(Out, "B"));
        assertEquals("", ImSet.on("D", "y", "z"), g1.getClosure(Out, "C"));
        assertEquals("", ImSet.on("y"), g1.getClosure(Out, "D"));

        assertEquals("", ImSet.on(), g1.getClosure(Out, "x"));
        assertEquals("", ImSet.on(), g1.getClosure(Out, "y"));
        assertEquals("", ImSet.on(), g1.getClosure(Out, "z"));

        // Check get with single labels
        assertEquals("", ImSet.on("C"), g1.getAdjacents(Out, "mod", "A"));
        assertEquals("", ImSet.on("x"), g1.getAdjacents(Out, "art", "A"));

        // Check closures with single labels
        assertEquals("", ImSet.on("C", "D"), g1.getClosure(Out, "mod", "A"));
        assertEquals("", ImSet.on("C", "D"), g1.getClosure(Out, "mod", "B"));
        assertEquals("", ImSet.on("D"), g1.getClosure(Out, "mod", "C"));
        assertEquals("", ImSet.on(), g1.getClosure(Out, "mod", "D"));

        // Check closures with multiple labels
        ImSet<String> modAndArt = ImSet.on("mod", "art");
        assertEquals("", ImSet.on("C", "D", "x", "y", "z"), g1.getClosure(Out, modAndArt, "A"));
        assertEquals("", ImSet.on("C", "D", "y", "z"), g1.getClosure(Out, modAndArt, "B"));
        assertEquals("", ImSet.on("D", "y", "z"), g1.getClosure(Out, modAndArt, "C"));
        assertEquals("", ImSet.on("y"), g1.getClosure(Out, modAndArt, "D"));

        assertEquals("", ImSet.on(), g1.getClosure(Out, modAndArt, "x"));
        assertEquals("", ImSet.on(), g1.getClosure(Out, modAndArt, "y"));
        assertEquals("", ImSet.on(), g1.getClosure(Out, modAndArt, "z"));

        // Check closures going up
        assertEquals("", ImSet.on("A", "B", "C", "D"), g1.getClosure(In, "y"));

        // Check in order
        assertEquals("", ImList.on("A", "C", "x", "D", "z", "y"), g1.topologicalOrder(i -> g1.getAdjacents(Out, modAndArt, i), "A"));
        assertEquals("", ImList.on("y", "D", "C", "A", "B"), g1.topologicalOrder(i -> g1.getAdjacents(In, modAndArt, i), "y"));
        assertEquals("", ImList.on("A", "B", "x", "C", "D", "z", "y"), g1.topologicalOrder(i -> g1.getAdjacents(Out, modAndArt, i), ImList.on("A", "B")));
    }

    private <KEY, VALUE, LABEL> void assertConnected(ImGraph<KEY, VALUE, LABEL> graph, LABEL arcLabel, KEY parentKey, KEY childKey,
            VALUE childValue)
    {
        assertTrue(graph.getAdjacents(Out, arcLabel, parentKey).contains(childKey));
        assertTrue(graph.getAdjacents(In, arcLabel, childKey).contains(parentKey));
        assertEquals(childValue, graph.getValue(childKey));
    }

    @Test
    public void testInOrderClosure()
    {

        /**
         * Upper case to upper case is a mod label
         * Anything to lower case is an art label
         * Arcs go downwards
         *
         *            Graph                    Graph prints like this
         *
         *                      B              |--> A
         *                     /|              |    |- art -> x
         *            A       / |              |    |         |- art -> y
         *           / \     /  |              |    |- mod -> C
         *         art mod mod art             |    |         |- art -> z
         *         /     \ /    |              |    |         |- mod -> D
         *        x       C     |              |    |         |         |- art -> (y)
         *        |      / \    |              |--> B
         *        |    mod art  |              |    |- art -> (z)
         *       art   /     \  |              |    |- mod -> (C)
         *        |   D       \ |
         *        |  /         \|
         *        | art         z
         *        |/
         *        y
         *
         */
        ImGraph<String, String, String> g0 = ImGraph.on();

        ImGraph<String, String, String> g1 =
                g0.addNode("A", "")
                        .addNode("B", "")
                        .addNodeToParentIfMissing("mod", "A", "C", "")
                        .addNodeToParentIfMissing("mod", "B", "C", "")
                        .addNodeToParentIfMissing("art", "A", "x", "")
                        .addNodeToParentIfMissing("mod", "C", "D", "")
                        .addNodeToParentIfMissing("art", "C", "z", "")
                        .addNodeToParentIfMissing("art", "B", "z", "")
                        .addNodeToParentIfMissing("art", "x", "y", "")
                        .addNodeToParentIfMissing("art", "D", "y", "");

        System.out.println(g1.show());

        // Check in order
        ImSet<String> modAndArt = ImSet.on("mod", "art");
        assertEquals("", ImList.on("A", "C", "x", "D", "z", "y"), g1.topologicalOrder(i -> g1.getAdjacents(Out, modAndArt, i), "A"));
        assertEquals("", ImList.on("y", "D", "x", "C", "A", "B"), g1.topologicalOrder(i -> g1.getAdjacents(In, modAndArt, i), "y"));
        assertEquals("", ImList.on("A", "B", "x", "C", "D", "z", "y"), g1.topologicalOrder(i -> g1.getAdjacents(Out, modAndArt, i), ImList.on("A", "B")));
        assertEquals("", ImList.on("A", "C", "x", "D", "z", "y"), g1.topologicalOrder(i -> g1.getAdjacents(Out, modAndArt, i), ImList.on("A", "C", "z")));
    }

    //    @Test
    //    public void testAddArcAfter()
    //    {
    //
    //        ImGraph<Integer, String, String> g0 = ImGraph.empty();
    //
    //        var g1 = addNodes(g0, 1, 2, 3, 4, 5)
    //                .addArc("child", 1, 5)
    //                .addArc("child", 1, 2)
    //                .addArcAfter("child", 1, 3, 2)
    //                .addArcAfter("child", 1, 4, 3);
    //
    //        say(g1.show());
    //
    //        assertEquals(ImList.on(2, 3, 4, 5), g1.getAdjacents(Out, "child", 1));
    //        assertEquals(ImList.on(2, 4, 5), g1.removeArc("child", 1, 3).getAdjacents(Out, "child", 1));
    //    }
    //
    //    @Test
    //    public void testAddArcAfterWithErrors()
    //    {
    //
    //        ImGraph<Integer, String, String> g0 = ImGraph.empty();
    //
    //        var g1 = addNodes(g0, 1, 2, 3, 4, 5);
    //
    //        TestUtils.assertThrows(() -> g1.addArcAfter("child", 1, 4, 3), KeyMissing.class,
    //                "The arcs out from key 1 with label child do not contain key 3");
    //    }

    private ImGraph<Integer, String, String> addNodes(ImGraph<Integer, String, String> g, Integer... is)
    {
        return ImList.on(is).foldl(g, (z, i) -> z.addNode(i, ""));
    }

    @Test
    public void testMap()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        assertEquals(g, g.map(n -> n));
    }

    @Test
    public void testGetConnectedOut()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(4);

        assertEquals(ImSet.on(4), g.getAdjacents(Out, 2));
    }

    @Test
    public void testGetConnectedIn()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(4);

        assertEquals(ImSet.on(2), g.getAdjacents(In, 4));
    }

    //    @Test
    //    public void testRemoveArc()
    //    {
    //        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);
    //
    //        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> pairs = g.keys().map(k -> ImPair.on(k, g.getPairs(Out, k)));
    //
    //        ImGraph<Integer, String, String> newGraph = pairs.foldl(g, (gr, p) -> removeAllArcsOutFrom(gr, p));
    //
    //        // assertEquals(expected, TopDownBox.withAll(pairs).toString());
    //        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> newPairs = newGraph.keys()
    //                .map(k -> ImPair.on(k, newGraph.getPairs(Out, k)));
    //
    //        String expected =
    //                "(1, [])\n" +
    //                        "(2, [])\n" +
    //                        "(3, [])\n" +
    //                        "(4, [])\n" +
    //                        "(5, [])\n" +
    //                        "(6, [])\n" +
    //                        "(7, [])\n" +
    //                        "(8, [])\n" +
    //                        "(9, [])\n" +
    //                        "(10, [])\n" +
    //                        "(11, [])\n" +
    //                        "(12, [])\n" +
    //                        "(13, [])\n" +
    //                        "(14, [])\n" +
    //                        "(15, [])\n" +
    //                        "(16, [])\n" +
    //                        "(17, [])\n" +
    //                        "(18, [])\n" +
    //                        "(19, [])\n" +
    //                        "(20, [])\n" +
    //                        "(21, [])\n" +
    //                        "(22, [])\n" +
    //                        "(23, [])\n" +
    //                        "(24, [])\n" +
    //                        "(25, [])\n" +
    //                        "(26, [])\n" +
    //                        "(27, [])\n" +
    //                        "(28, [])\n" +
    //                        "(29, [])\n" +
    //                        "(30, [])\n";
    //
    //        assertEquals(expected, TopDownBox.withAll(newPairs).toString());
    //    }

    @Test
    public void testRemoveArcRemovesArcsInEntriesWithEmptyValues()
    {
        TestGraph g = TestGraph.on().makePath(1, 2);
        TestGraph e = TestGraph.on().makePath(1).makePath(2);

        assertEquals(e, new TestGraph(g.removeArc("-", 1, 2)));

    }

    private ImGraph<Integer, String, String> removeAllArcsOutFrom(ImGraph<Integer, String, String> g,
            ImPair<Integer, ImList<ImPair<String, Integer>>> p)
    {
        return p.snd.foldl(g, (gr, pr) -> gr.removeArc(pr.fst, p.fst, pr.snd));
    }

    @Test
    public void testGetClosure()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        ImRange.inclusive(2, 30).foreach(n ->
        {
            ImList<Integer> multiples = ImRange.step(2 * n, n).takeWhile(i -> i <= 30);
            assertEquals(multiples.toImSet(), g.getClosure(Out, n));
        });
    }

    @Test
    public void testShow()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(20);

        System.out.println(g.show());

        //System.out.println(g.getGraphVizGraph());
        //assertEquals(expected, s);
    }

    @Test
    public void testPartition()
    {
        ImGraph<Integer, String, String> empty = ImGraph.empty();

        final ImGraph<Integer, String, String> g = ImList.on(1, 2, 3).foldl(empty, (z, i) -> z.addNode(i, "" + i))
                .addArc("a", 1, 2)
                .addArc("b", 2, 3)
                .addArc("c", 3, 1);

        final ImList<ImSet<Integer>> parts = g.partition();
        assertEquals(g.nodeKeysSet(), parts.head());

        assertEquals(g.nodeKeysSet(), parts.foldl(ImSet.empty(), (z, i) -> z.union(i)));

        parts.foreach(i -> checkPart(g, i));

    }

    @Test
    public void testPartition2()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        final ImList<ImSet<Integer>> parts = g.partition();

        assertEquals(g.nodeKeysSet(), parts.foldl(ImSet.empty(), (z, i) -> z.union(i)));

        parts.foreach(i -> checkPart(g, i));
    }

    <KEY, DATA, LABEL> void checkPart(ImGraph<KEY, DATA, LABEL> graph, ImSet<KEY> keysInPart)
    {
        ImSet<KEY> expected = graph.getClosure(i -> graph.getAdjacents(Out, i), keysInPart).union(keysInPart);

        assertEquals(expected, keysInPart);
    }

    @Test
    public void testShowOnLoopyGraph()
    {
        //        ImGraph<Integer, String, String> g = makeMultiplesGraph(6);

        ImGraph<Integer, String, String> g = ImList.on(1, 2, 3, 4).foldl(ImGraph.empty(), (z, i) -> z.addNode(i, "" + i));

        g = g.addArc("a", 1, 2)
                .addArc("b", 2, 3)
                .addArc("d", 2, 3)
                .addArc("c", 4, 3);

        System.out.println(g.show());
    }

    @Test
    public void testShowOnLoopyGraph2()
    {
        //        ImGraph<Integer, String, String> g = makeMultiplesGraph(6);

        ImGraph<Integer, String, String> g = ImList.on(0, 1).foldl(ImGraph.empty(), (z, i) -> z.addNode(i, "" + i));

        g = g.addArc("-", 1, 0)
                .addArc("b", 1, 1);

        System.out.println(g.show());

    }

    @Test
    public void testShowOnRandomGraph()
    {
        ImGraph<Integer, String, String> g = makeRandomGraph(6);

        System.out.println(g);
        System.out.println(g.showAsSets());
        System.out.println(g.show());
    }

    @Test
    public void testRemoveArcOnRandomGraph()
    {
        ImGraph<Integer, String, String> g = makeRandomGraph(6);

        ImList<ImArc<Integer, String>> arcs = g.arcs().toList().shuffle();

        ImGraph<Integer, String, String> empty = g.arcs().foldl(g, (z, i) -> removeArcAndCheck(z, i));

        assertEquals(ImSet.on(), empty.arcs());

        //        System.out.println(g.showAsSets());
    }

    @Test
    public void testRemoveNodeOnRandomGraph()
    {
        ImGraph<Integer, String, String> g = makeRandomGraph(6);

        ImList<ImArc<Integer, String>> arcs = g.arcs().toList().shuffle();

        ImGraph<Integer, String, String> empty = g.arcs().foldl(g, (z, i) -> removeArcAndCheck(z, i));

        assertEquals(ImSet.on(), empty.arcs());

        //        System.out.println(g.showAsSets());
    }

    private <K, D, L> ImGraph<K, D, L> removeArcAndCheck(ImGraph<K, D, L> g0, ImArc<K, L> arcToRemove)
    {

        ImGraph<K, D, L> g = g0.removeArc(arcToRemove);

        assertTrue(g0.arcs().contains(arcToRemove));
        assertFalse(g.arcs().contains(arcToRemove));

        checkIntegrity(g);

        return g;
    }

    @Test
    public void testShrink()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        System.out.println(g.show());
        System.out.println("roots" + g.roots());

        /**
         * Get g2:
         *
         *    g2
         *    |--> 6
         *    |    |- 5 -> 30
         *    |    |- 4 -> 24
         *    |    |- 3 -> 18
         *    |    |- 2 -> 12
         *    |    |       |- 2 -> (24)
         */
        ImGraph<Integer, String, String> shrunk = g.shrinkToInclusiveClosureOf(ImSet.on("2", "3", "4", "5"), ImList.on(6));
        ImGraph<Integer, String, String> g2 = shrunk;

        say(shrunk.show());

        assertEquals(ImList.on(6, 12, 18, 24, 30), g2.nodeKeys());
        assertEquals(ImList.on(6), g2.roots());
    }

    @Test
    public void testShrink2()
    {

        TestGraph g = TestGraph.on().makePath(1, 2, 4).makePath(2, 5).makePath(1, 3, 6).makePath(3, 5, 6);
        say(g.show());

        TestGraph g1 = new TestGraph(g.shrinkToInclusiveClosureOf(ImSet.on("-"), ImList.on(2)));

        TestGraph e1 = TestGraph.on().makePath(2, 4).makePath(2, 5, 6);

        say(e1.show());
        say(g1.show());

        TestGraph g2 = new TestGraph(g.shrinkToInclusiveClosureOf(ImSet.on("-"), ImList.on(5)));
        assertEquals(TestGraph.on().makePath(5, 6), g2);

    }

    @Test
    public void testEquals()
    {
        ImGraph<String, String, String> g = makeV();

        ImGraph<String, String, String> g2 = makeV2();

        say(g.show());
        say(g2.show());

        assertTrue(g.equals(g));
        assertTrue(g.equals(g2));

    }

    @Test
    public void testSelfLoops()
    {
        ImGraph<String, String, String> g = ImGraph.<String, String, String>empty()
                .addNode("a", "")
                .addNode("b", "")
                .addArc("-", "a", "a")
                .addArc("-", "a", "a");

        say(g);

    }

    private ImGraph<Integer, String, String> makeMultiplesGraph(int n)
    {
        ImGraph<Integer, String, String> g = ImGraph.empty();

        for (int i = 1; i <= n; i++)
            g = g.addNode(i, "");

        for (int i = 2; i < n; i++)
        {
            g = addMultiples(g, i, n);
        }

        return g;
    }

    private ImGraph<Integer, String, String> addMultiples(ImGraph<Integer, String, String> g, int i, int size)
    {
        return addMultiples(g, i, 2, size);
    }

    private ImGraph<Integer, String, String> addMultiples(ImGraph<Integer, String, String> g, int i, int factor, int size)
    {
        return i * factor > size
               ? g
               : addMultiples(g.addArc("" + factor, i, i * factor), i, factor + 1, size);
    }

    private ImGraph<String, String, String> makeV()
    {

        ImGraph<String, String, String> e = ImGraph.empty();
        return e
                .addNode("a", "")
                .addNodeToParentIfMissing("-", "a", "b", "")
                .addNodeToParentIfMissing("-", "a", "c", "")
                .addNodeToParentIfMissing("-", "b", "d", "")
                .addNodeToParentIfMissing("-", "b", "e", "")
                .addNodeToParentIfMissing("-", "c", "f", "")
                .addArc("-", "c", "e");

    }

    private ImGraph<String, String, String> makeV2()
    {

        ImGraph<String, String, String> e = ImGraph.empty();
        return e
                .addNode("a", "")
                .addNodeToParentIfMissing("-", "a", "c", "")
                .addNodeToParentIfMissing("-", "a", "b", "")
                .addNodeToParentIfMissing("-", "b", "d", "")
                .addNodeToParentIfMissing("-", "c", "f", "")
                .addNodeToParentIfMissing("-", "c", "e", "")
                .addArc("-", "b", "e");

    }

    //    @Test
    //    public void testAdjacents()
    //    {
    //        ImGraph<String, String, String> g = makeGraph();
    //
    //        say(g.show());
    //
    //        say(g.tSort(i -> g.getAdjacents(Out, i), ImSet.on("a"), ImSet.on()));
    //
    //    }

    @Test
    public void testTopologicalSortOnRandomDags()
    {

        ImRange.nTimesDo(20, () -> assertTopologicalSortIsOk());
    }

    public void assertTopologicalSortIsOk()
    {
        int nodeCount = 10;
        int edgeCount = 20;

        ImGraph<Integer, String, String> g = makeDag(nodeCount, edgeCount);
        //        say(g.show());
        //
        //        say(g.arcs());

        ImList<Integer> keys = g.topologicalOrder(i -> g.getAdjacents(Out, i), g.nodeKeys().head());

        //        say("start", g.keys().head(), keys);

        // For each edge e = (u, v), u should appear before v in the result

        // Get pairs of each key in the list and their successor
        ImList<ImPair<Integer, Integer>> ps = keys.zip(ImRange.oneTo(nodeCount));

        // Create a map taking key to its rank in the list
        ImMap<Integer, Integer> keyRanks = ImMap.fromPairs(ps);

        for (ImArc<Integer, String> a : g.arcs())
        {
            assertTrue("" + a, keyRanks.get(a.start) < keyRanks.get(a.end));
        }
    }

    @Test
    public void testTopologicalSortOnExample1()
    {

        ImGraph<Integer, String, String> g = makeDagExample1();

        say(g.show());

        say(g.topologicalOrder(i -> g.getAdjacents(Out, i), 1));

    }

    @Test
    public void testTopologicalSortOnExample9()
    {

        ImGraph<Integer, String, String> g = makeDagExample9();

        say(g.show());

        say(g.topologicalOrder(i -> g.getAdjacents(Out, i), 1));

    }

    @Test
    public void testTopologicalSortOnExample2()
    {

        ImGraph<Integer, String, String> g = makeDagExample2();

        say(g.show());

        say(g.topologicalOrder(i -> g.getAdjacents(Out, i), 1));

    }

    public static <K, V, L> void checkIntegrity(ImGraph<K, V, L> graph)
    {

        ImSet<ImArc<K, L>> inSet = graph.arcsIn.values().foldl(ImSet.on(), (z, i) -> z.union(i));
        ImSet<ImArc<K, L>> outSet = graph.arcsOut.values().foldl(ImSet.on(), (z, i) -> z.union(i));

        if (!Eq.uals(inSet, outSet))
        {
            say("outArcs != inArcs", inSet.minus((outSet)), outSet.minus((inSet)));

            throw new ImException();
        }

        if (outSet.map(i -> i.start).minus(graph.nodeKeysSet()).isNotEmpty())
        {
            say("invalid arcs:", outSet);
        }

        if (outSet.map(i -> i.end).minus(graph.nodeKeysSet()).isNotEmpty())
        {
            say("invalid arcs:", outSet);
        }
    }

    @Test
    public void testRemoveArc()
    {

        /**
         * A test graph:
         *
         *              4
         *          ┌─◁─▢─◁─┐
         *    1    2│   │   │
         *    ▢──▷──▢   △   ▢ 5
         *          │   │   │
         *          └─▷─▢─▷─┘
         *              3
         *
         */
        ImList<Integer> nodes = ImRange.oneTo(5);
        ImGraph<Integer, String, String> g0 = nodes.foldl(ImGraph.on(), (z, i) -> z.addNode(i, ""));

        ImList<ImPair<Integer, Integer>> pairs = ImList.on(on(1, 2), on(1, 2), on(2, 3), on(3, 4), on(4, 2), on(3, 5), on(5, 4));

        say("pairs", pairs);

        var g = pairs.foldl(g0, (z, i) -> z.addArc("", i.fst, i.snd));

        ImSet<ImArc<Integer, String>> a = g.getArcs(Out, 1);

        ImArc<Integer, String> arc = a.toList().head();
        say(arc);

        ImGraph<Integer, String, String> g2 = g.removeArc(arc.label, arc.start, arc.end);

        say("g", g);
        say("g2", g2);

        checkIntegrity(g2);
    }

    /**
     * Create a graph like this:
     *
     */
    private ImGraph<String, String, String> makeGraph()
    {
        return ImGraph.<String, String, String>empty()
                .addNode("a", "")
                .addNodeToParentIfMissing("-", "a", "b", "")
                .addNodeToParentIfMissing("-", "b", "c", "")
                .addArc("-", "a", "c");
    }

    /**
     * Create a graph like this:
     *
     */
    private ImGraph<Integer, String, String> makeDagExample7()
    {
        return ImGraph.<Integer, String, String>empty()
                .addNode(1, "")
                .addNodeToParentIfMissing("-", 1, 4, "")
                .addNodeToParentIfMissing("-", 1, 2, "")
                .addNodeToParentIfMissing("-", 2, 5, "")
                .addNodeToParentIfMissing("-", 5, 4, "")
                .addNodeToParentIfMissing("-", 1, 3, "")
                .addNodeToParentIfMissing("-", 3, 6, "")
                .addNodeToParentIfMissing("-", 6, 7, "")
                .addArc("-", 2, 3)
                .addArc("-", 5, 6);
    }

    /**
     * Create a DAG using 1 .. max
     *
     * with count edges
     *
     */
    private ImGraph<Integer, String, String> makeDag(int nodeCount, int edgeCount)
    {

        ImGraph<Integer, String, String> e = ImGraph.empty();

        //        ImGraph<Integer, String, String> g = ImRange.oneTo(nodeCount).foldl(e, (z, i) -> z.addNode(i, ""));

        ImGraph<Integer, String, String> g = e.addNode(1, "");
        while (true)
        {
            if (g.arcs().size() == edgeCount)
                break;

            int n1 = Rando.nextInt(1, nodeCount);
            int n2 = Rando.nextInt(n1 + 1, nodeCount + 1);

            // Only add if the start node is present
            if (g.containsNodeWithKey(n1))
                g = g.addNodeIfMissing(n1, "").addNodeToParentIfMissing("-", n1, n2, "");
        }

        return g;
    }

    private ImGraph<Integer, String, String> makeRandomGraph(int n)
    {
        return ImRange.oneTo(n).foldl(ImGraph.empty(), (z, i) -> addToGraph(z, 1));
    }

    private ImGraph<Integer, String, String> addToGraph(ImGraph<Integer, String, String> g, int n)
    {
        // Add n nodes
        int count = g.nodeKeysSet().size();
        ImGraph<Integer, String, String> g2 = ImRange.inclusive(count + 1, count + n).foldl(g, (z, i) -> z.addNode(i, ""));

        int newSize = g2.nodeKeysSet().size();

        if (newSize < 2)
            return g2;
        else
        {
            int connectCount = Rando.nextIntInclusive(newSize / 2, newSize - 1);

            // Chose a random node
            ImList<Integer> keys = g2.nodeKeys();
            Integer node = keys.at(Rando.nextInt(1, newSize));

            // Choose connectCount nodes
            ImList<Integer> ns = keys.shuffle().take(connectCount);

            // connect node to them

            ImGraph<Integer, String, String> g3 = ns.foldl(g2, (z, i) -> z.addArc("", node, i));

            return g3;
        }
    }

    static class TestGraph extends ImGraph<Integer, String, String>
    {

        public static TestGraph on()
        {
            ImGraph<Integer, String, String> ge = ImGraph.empty();
            return new TestGraph(ge);
        }

        public TestGraph(ImGraph<Integer, String, String> other)
        {
            super(other.valueMap, other.arcsOut, other.arcsIn);
        }

        public TestGraph makePath(Integer... nsArray)
        {
            ImList<Integer> ns = ImList.on(nsArray);

            // Create the nodes
            TestGraph g2 = ns.foldl(this, (z, i) -> new TestGraph(z.addNodeIfMissing(i, "")));

            // Connect them with arcs
            ImList<ImPair<Integer, Integer>> zip = ns.zip(ns.tail());

            //            say(zip);
            return zip.foldl(g2, (z, p) -> new TestGraph(z.addArc("-", p.fst, p.snd)));
        }
    }

    private ImGraph<Integer, String, String> makeDagExample1()
    {

        TestGraph g = TestGraph.on();

        return g.makePath(1, 2, 5, 4)
                .makePath(1, 3, 6, 7)
                .makePath(2, 3)
                .makePath(5, 6);
    }

    private ImGraph<Integer, String, String> makeDagExample9()
    {
        ImGraph<Integer, String, String> ge = ImGraph.empty();
        TestGraph g = new TestGraph(ge);

        return g.makePath(1, 2, 4, 7, 8)
                .makePath(7, 10)
                .makePath(4, 9)
                .makePath(7, 10)
                .makePath(2, 8);
    }

    private ImGraph<Integer, String, String> makeDagExample2()
    {
        ImGraph<Integer, String, String> ge = ImGraph.empty();
        TestGraph g = new TestGraph(ge);

        return g.makePath(1, 2, 4)
                .makePath(1, 3, 4);
    }

    @Test
    public void testCycles()
    {
        ImGraph<String, String, String> g1 = makeCycleGraph();

        // Check Closures - no labels
        assertEquals("", ImSet.on("b", "c", "a"), g1.getClosure(Out, "a"));

    }

}