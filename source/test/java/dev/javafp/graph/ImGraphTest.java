package dev.javafp.graph;

import dev.javafp.box.TopDownBox;
import dev.javafp.ex.KeyMissing;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.TestUtils;
import org.junit.Ignore;
import org.junit.Test;

import static dev.javafp.graph.ImGraph.Dir.In;
import static dev.javafp.graph.ImGraph.Dir.Out;
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

        assertEquals(ImList.on("1"), g4.getConnected(Out, "arc1", "0"));
        assertEquals(ImList.on("0"), g4.getConnected(In, "arc1", "1"));

        ImGraph<String, Integer, String> g5 = g4.addNodeToParent("arc2", "1", "2", 2);
        assertConnected(g5, "arc1", "0", "1", 1);

        assertConnected(g5, "arc2", "1", "2", 2);

        ImGraph<String, Integer, String> g6 = g5.addNodeToParent("arc3", "0", "3", 3);
        assertConnected(g6, "arc2", "1", "2", 2);
        ImGraph<String, Integer, String> g7 = g6.addArc("arc4", "3", "2");

        assertConnected(g7, "arc2", "1", "2", 2);
        assertConnected(g7, "arc4", "3", "2", 2);

        //
        // 0  -arc1->  1  -arc2->   2
        // |                        |
        // |  -arc3->  3  -arc4->   |

        TestUtils.assertSetsEqual("", ImList.on("1", "3"), g7.getConnected(Out, "0"));
        TestUtils.assertSetsEqual("", ImList.on("1", "3"), g7.getConnected(In, "2"));

        //        assertEquals(ImList.on(k1), g4.get(In,k2));
        //
        //        ImGraph<String, String> g5 = g4.mutate(k1, a -> mutate(a));
    }

    /**
     * Create a graph like this:
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
     *
     * a = art, m = mod
     */
    private ImGraph<String, String, String> makeTestGraph()
    {
        return ImGraph.<String, String, String>empty().addNode("A", "")
                .addNode("B", "")
                .addNodeToParent("mod", "A", "C", "")
                .addNodeToParent("mod", "B", "C", "")
                .addNodeToParent("art", "A", "x", "")
                .addNodeToParent("mod", "C", "D", "")
                .addNodeToParent("art", "C", "z", "")
                .addNodeToParent("art", "B", "z", "")
                .addNodeToParent("art", "D", "y", "");
    }

    /**
     * Create a graph like this:
     *
     *  <p> <img src="{@docRoot}/dev/doc-files/cycle.png" alt="rose-tree-a"  width="200" />
     *
     *              a
     *             / \
     *            c - b
     */
    private ImGraph<String, String, String> makeCycleGraph()
    {
        return ImGraph.<String, String, String>empty()
                .addNode("a", "")
                .addNodeToParent("-", "a", "b", "")
                .addNodeToParent("-", "b", "c", "")
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

        System.out.println(g1.getGraphVizGraph());
    }

    @Test
    public void testABCDGraphPaths()
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

        assertEquals("[[z, B], [z, C, B], [z, C, A]]", g1.getPaths(In, ImSet.on("mod", "art"), "z").toString());
        assertEquals("[[z, B], [z, C]]", g1.getPaths(In, ImSet.on("art"), "z").toString());
        assertEquals("[[D, C, B], [D, C, A]]", g1.getPaths(In, ImSet.on("mod"), "D").toString());
    }

    @Test
    public void testABCD()
    {

        /**
         * Upper case to upper case is a mod label
         * Anything to lower case is an art label
         * Arcs go downwards
         *
         *            A   B
         *           / \ /|
         *          x   C |
         *             / \|
         *            D   z
         *            |
         *            y
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
        TestUtils.assertSetsEqual("", ImList.on("x", "C"), g1.getConnected(Out, "A"));
        TestUtils.assertSetsEqual("", ImList.on("z", "C"), g1.getConnected(Out, "B"));
        TestUtils.assertSetsEqual("", ImList.on("z", "D"), g1.getConnected(Out, "C"));

        TestUtils.assertSetsEqual("", ImList.on("y"), g1.getConnected(Out, "D"));

        TestUtils.assertSetsEqual("", ImList.on(), g1.getConnected(Out, "x"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getConnected(Out, "y"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getConnected(Out, "z"));

        // Check the ins
        TestUtils.assertSetsEqual("", ImList.on(), g1.getConnected(In, "A"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getConnected(In, "B"));
        TestUtils.assertSetsEqual("", ImList.on("A", "B"), g1.getConnected(In, "C"));
        TestUtils.assertSetsEqual("", ImList.on("C"), g1.getConnected(In, "D"));

        TestUtils.assertSetsEqual("", ImList.on("A"), g1.getConnected(In, "x"));
        TestUtils.assertSetsEqual("", ImList.on("D"), g1.getConnected(In, "y"));
        TestUtils.assertSetsEqual("", ImList.on("B", "C"), g1.getConnected(In, "z"));

        // Check Closures - no labels
        TestUtils.assertSetsEqual("", ImList.on("C", "D", "x", "y", "z"), g1.getClosure(Out, "A"));
        TestUtils.assertSetsEqual("", ImList.on("C", "D", "y", "z"), g1.getClosure(Out, "B"));
        TestUtils.assertSetsEqual("", ImList.on("D", "y", "z"), g1.getClosure(Out, "C"));
        TestUtils.assertSetsEqual("", ImList.on("y"), g1.getClosure(Out, "D"));

        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, "x"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, "y"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, "z"));

        // Check get with single labels
        TestUtils.assertSetsEqual("", ImList.on("C"), g1.getConnected(Out, "mod", "A"));
        TestUtils.assertSetsEqual("", ImList.on("x"), g1.getConnected(Out, "art", "A"));

        // Check closures with single labels
        TestUtils.assertSetsEqual("", ImList.on("C", "D"), g1.getClosure(Out, "mod", "A"));
        TestUtils.assertSetsEqual("", ImList.on("C", "D"), g1.getClosure(Out, "mod", "B"));
        TestUtils.assertSetsEqual("", ImList.on("D"), g1.getClosure(Out, "mod", "C"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, "mod", "D"));

        // Check closures with multiple labels
        ImSet<String> modAndArt = ImSet.on("mod", "art");
        TestUtils.assertSetsEqual("", ImList.on("C", "D", "x", "y", "z"), g1.getClosure(Out, modAndArt, "A"));
        TestUtils.assertSetsEqual("", ImList.on("C", "D", "y", "z"), g1.getClosure(Out, modAndArt, "B"));
        TestUtils.assertSetsEqual("", ImList.on("D", "y", "z"), g1.getClosure(Out, modAndArt, "C"));
        TestUtils.assertSetsEqual("", ImList.on("y"), g1.getClosure(Out, modAndArt, "D"));

        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, modAndArt, "x"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, modAndArt, "y"));
        TestUtils.assertSetsEqual("", ImList.on(), g1.getClosure(Out, modAndArt, "z"));

        // Check closures going up
        assertEquals("", ImList.on("A", "B", "C", "D"), g1.getClosure(In, "y"));

        // Check in order
        assertEquals("", ImList.on("A", "C", "D", "y", "z", "x"), g1.getInOrderClosureOnSingleKey(Out, modAndArt, "A"));
        assertEquals("", ImList.on("y", "D", "C", "A", "B"), g1.getInOrderClosureOnSingleKey(In, modAndArt, "y"));
        assertEquals("", ImList.on("B", "A", "C", "D", "y", "z", "x"), g1.getInOrderClosure(Out, modAndArt, ImList.on("A", "B")));
    }

    @Test
    public void testCycles()
    {

        ImGraph<String, String, String> g1 = makeCycleGraph();

        // Check Closures - no labels
        TestUtils.assertSetsEqual("", ImList.on("b", "c", "a"), g1.getClosure(Out, "a"));

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
        ImGraph<String, String, String> g0 = ImGraph.empty();

        ImGraph<String, String, String> g1 =
                g0.addNode("A", "")
                        .addNode("B", "")
                        .addNodeToParent("mod", "A", "C", "")
                        .addNodeToParent("mod", "B", "C", "")
                        .addNodeToParent("art", "A", "x", "")
                        .addNodeToParent("mod", "C", "D", "")
                        .addNodeToParent("art", "C", "z", "")
                        .addNodeToParent("art", "B", "z", "")
                        .addNodeToParent("art", "x", "y", "")
                        .addNodeToParent("art", "D", "y", "");

        System.out.println(g1.show());

        // Check in order
        ImSet<String> modAndArt = ImSet.on("mod", "art");
        assertEquals("", ImList.on("A", "C", "D", "z", "x", "y"), g1.getInOrderClosureOnSingleKey(Out, modAndArt, "A"));
        assertEquals("", ImList.on("y", "x", "D", "C", "A", "B"), g1.getInOrderClosureOnSingleKey(In, modAndArt, "y"));
        assertEquals("", ImList.on("B", "A", "C", "D", "z", "x", "y"), g1.getInOrderClosure(Out, modAndArt, ImList.on("A", "B")));
        assertEquals("", ImList.on("A", "C", "D", "z", "x", "y"), g1.getInOrderClosure(Out, modAndArt, ImList.on("A", "C", "z")));
    }

    @Test
    public void testAddArcAfter()
    {

        ImGraph<Integer, String, String> g0 = ImGraph.empty();

        var g1 = addNodes(g0, 1, 2, 3, 4, 5)
                .addArc("child", 1, 5)
                .addArc("child", 1, 2)
                .addArcAfter("child", 1, 3, 2)
                .addArcAfter("child", 1, 4, 3);

        say(g1.show());

        assertEquals(ImList.on(2, 3, 4, 5), g1.getConnected(Out, "child", 1));
        assertEquals(ImList.on(2, 4, 5), g1.removeArc("child", 1, 3).getConnected(Out, "child", 1));
    }

    @Test
    public void testAddArcAfterWithErrors()
    {

        ImGraph<Integer, String, String> g0 = ImGraph.empty();

        var g1 = addNodes(g0, 1, 2, 3, 4, 5);

        TestUtils.assertThrows(() -> g1.addArcAfter("child", 1, 4, 3), KeyMissing.class,
                "The arcs out from key 1 with label child do not contain key 3");
    }

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
    public void testPairsOut()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> pairs = g.keys().map(k -> ImPair.on(k, g.getPairs(Out, k)));

        String expected =
                "(1, [])\n" +
                        "(2, [(15, 30), (14, 28), (13, 26), (12, 24), (11, 22), (10, 20), (9, 18), (8, 16), (7, 14), (6, 12), (5, 10), (4, 8), " +
                        "(3, 6), (2, 4)])\n" +
                        "(3, [(10, 30), (9, 27), (8, 24), (7, 21), (6, 18), (5, 15), (4, 12), (3, 9), (2, 6)])\n" +
                        "(4, [(7, 28), (6, 24), (5, 20), (4, 16), (3, 12), (2, 8)])\n" +
                        "(5, [(6, 30), (5, 25), (4, 20), (3, 15), (2, 10)])\n" +
                        "(6, [(5, 30), (4, 24), (3, 18), (2, 12)])\n" +
                        "(7, [(4, 28), (3, 21), (2, 14)])\n" +
                        "(8, [(3, 24), (2, 16)])\n" +
                        "(9, [(3, 27), (2, 18)])\n" +
                        "(10, [(3, 30), (2, 20)])\n" +
                        "(11, [(2, 22)])\n" +
                        "(12, [(2, 24)])\n" +
                        "(13, [(2, 26)])\n" +
                        "(14, [(2, 28)])\n" +
                        "(15, [(2, 30)])\n" +
                        "(16, [])\n" +
                        "(17, [])\n" +
                        "(18, [])\n" +
                        "(19, [])\n" +
                        "(20, [])\n" +
                        "(21, [])\n" +
                        "(22, [])\n" +
                        "(23, [])\n" +
                        "(24, [])\n" +
                        "(25, [])\n" +
                        "(26, [])\n" +
                        "(27, [])\n" +
                        "(28, [])\n" +
                        "(29, [])\n" +
                        "(30, [])\n";

        assertEquals(expected, TopDownBox.withAll(pairs).toString());
    }

    @Test
    public void testPairsIn()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> pairs = g.keys().map(k -> ImPair.on(k, g.getPairs(In, k)));

        String expected =
                "(1, [])\n" +
                        "(2, [])\n" +
                        "(3, [])\n" +
                        "(4, [(2, 2)])\n" +
                        "(5, [])\n" +
                        "(6, [(2, 3), (3, 2)])\n" +
                        "(7, [])\n" +
                        "(8, [(2, 4), (4, 2)])\n" +
                        "(9, [(3, 3)])\n" +
                        "(10, [(2, 5), (5, 2)])\n" +
                        "(11, [])\n" +
                        "(12, [(2, 6), (3, 4), (4, 3), (6, 2)])\n" +
                        "(13, [])\n" +
                        "(14, [(2, 7), (7, 2)])\n" +
                        "(15, [(3, 5), (5, 3)])\n" +
                        "(16, [(2, 8), (4, 4), (8, 2)])\n" +
                        "(17, [])\n" +
                        "(18, [(2, 9), (3, 6), (6, 3), (9, 2)])\n" +
                        "(19, [])\n" +
                        "(20, [(2, 10), (4, 5), (5, 4), (10, 2)])\n" +
                        "(21, [(3, 7), (7, 3)])\n" +
                        "(22, [(2, 11), (11, 2)])\n" +
                        "(23, [])\n" +
                        "(24, [(2, 12), (3, 8), (4, 6), (6, 4), (8, 3), (12, 2)])\n" +
                        "(25, [(5, 5)])\n" +
                        "(26, [(2, 13), (13, 2)])\n" +
                        "(27, [(3, 9), (9, 3)])\n" +
                        "(28, [(2, 14), (4, 7), (7, 4), (14, 2)])\n" +
                        "(29, [])\n" +
                        "(30, [(2, 15), (3, 10), (5, 6), (6, 5), (10, 3), (15, 2)])\n";

        assertEquals(expected, TopDownBox.withAll(pairs).toString());
    }

    @Test
    public void testGetConnectedOut()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(4);

        assertEquals(ImList.on(4), g.getConnected(Out, 2));
    }

    @Test
    public void testGetConnectedIn()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(4);

        assertEquals(ImList.on(2), g.getConnected(In, 4));
    }

    @Test
    public void testRemoveArc()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> pairs = g.keys().map(k -> ImPair.on(k, g.getPairs(Out, k)));

        ImGraph<Integer, String, String> newGraph = pairs.foldl(g, (gr, p) -> removeAllArcsOutFrom(gr, p));

        // assertEquals(expected, TopDownBox.withAll(pairs).toString());
        ImList<ImPair<Integer, ImList<ImPair<String, Integer>>>> newPairs = newGraph.keys().map(k -> ImPair.on(k, newGraph.getPairs(Out, k)));

        String expected =
                "(1, [])\n" +
                        "(2, [])\n" +
                        "(3, [])\n" +
                        "(4, [])\n" +
                        "(5, [])\n" +
                        "(6, [])\n" +
                        "(7, [])\n" +
                        "(8, [])\n" +
                        "(9, [])\n" +
                        "(10, [])\n" +
                        "(11, [])\n" +
                        "(12, [])\n" +
                        "(13, [])\n" +
                        "(14, [])\n" +
                        "(15, [])\n" +
                        "(16, [])\n" +
                        "(17, [])\n" +
                        "(18, [])\n" +
                        "(19, [])\n" +
                        "(20, [])\n" +
                        "(21, [])\n" +
                        "(22, [])\n" +
                        "(23, [])\n" +
                        "(24, [])\n" +
                        "(25, [])\n" +
                        "(26, [])\n" +
                        "(27, [])\n" +
                        "(28, [])\n" +
                        "(29, [])\n" +
                        "(30, [])\n";

        assertEquals(expected, TopDownBox.withAll(newPairs).toString());
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
            TestUtils.assertSameElements(multiples, g.getClosure(Out, n));
        });
    }

    @Test
    public void testShow()
    {
        ImGraph<Integer, String, String> g = makeMultiplesGraph(30);

        System.out.println(g.show());

        //System.out.println(g.getGraphVizGraph());
        //assertEquals(expected, s);
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

        assertEquals(ImList.on(6, 12, 18, 24, 30), g2.keys());
        assertEquals(ImList.on(6), g2.roots());
    }

    @Ignore
    @Test
    public void testShrink2()
    {
        ImGraph<String, String, String> g = makeV();

        say(g.show());

        assertEquals(g, makeV2());

        ImGraph<String, String, String> shrunk = g.shrinkToInclusiveClosureOf(ImSet.on("-"), ImList.on("a"));
        ImGraph<String, String, String> g2 = shrunk;

        say(shrunk.show());

        assertEquals(g, shrunk);

    }

    @Ignore
    @Test
    public void testEquals()
    {
        ImGraph<String, String, String> g = makeV();

        ImGraph<String, String, String> g2 = makeV2();

        say(g.show());
        say(g2.show());

        assertTrue(g.eq(g));
        assertTrue(g.eq(g2));

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

    private ImGraph<String, String, String> makeV()
    {

        ImGraph<String, String, String> e = ImGraph.empty();
        return e
                .addNode("a", "")
                .addNodeToParent("-", "a", "b", "")
                .addNodeToParent("-", "a", "c", "")
                .addNodeToParent("-", "b", "d", "")
                .addNodeToParent("-", "b", "e", "")
                .addNodeToParent("-", "c", "f", "")
                .addArc("-", "c", "e");

    }

    private ImGraph<String, String, String> makeV2()
    {

        ImGraph<String, String, String> e = ImGraph.empty();
        return e
                .addNode("a", "")
                .addNodeToParent("-", "a", "c", "")
                .addNodeToParent("-", "a", "b", "")
                .addNodeToParent("-", "b", "d", "")
                .addNodeToParent("-", "c", "f", "")
                .addNodeToParent("-", "c", "e", "")
                .addArc("-", "b", "e");

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

    private <KEY, VALUE, LABEL> void assertConnected(ImGraph<KEY, VALUE, LABEL> graph, LABEL arcLabel, KEY parentKey, KEY childKey,
            VALUE childValue)
    {
        assertTrue(graph.getConnected(Out, arcLabel, parentKey).contains(childKey));
        assertTrue(graph.getConnected(In, arcLabel, childKey).contains(parentKey));
        assertEquals(childValue, graph.getValue(childKey));
    }
}