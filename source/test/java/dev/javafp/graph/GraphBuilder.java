package dev.javafp.graph;

import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.func.Fn3;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;

import static dev.javafp.util.Say.say;

public class GraphBuilder
{

    public final ImGraph<String, String, String> graph;

    public GraphBuilder(ImGraph<String, String, String> graph)
    {
        this.graph = graph;
    }

    public static GraphBuilder with(ImGraph<String, String, String> graph)
    {
        return new GraphBuilder(graph);
    }

    /**
     * Add a new node with key `newNodeKey` to the graph and return a list of graphs with all possible arcs
     * connecting the new node to existing nodes
     *
     * There are n nodes so the number of out arcs from the new node to them is n.
     * and the new node can have an arc to itself or not so
     * we are adding 4 ^ n + 2 new graphs
     */
    public ImList<ImGraph<String, String, String>> addNewNodeAndConnect(ImList<ImGraph<String, String, String>> oldGraphs, String newNodeKey)
    {
        // TODO comment - numbers

        /**
         * If we consider each node
         *
         *     1, 2, ..., n
         *
         * How many different ways can we connect the new node to these nodes?
         *
         * Let's start by considering just connecting the new node out to some of the existing nodes.
         *
         * From the new node there can be one connection out to one of them
         * or 2 connections to 2 of them etc up to n connections to all n of them
         *
         * But we have to consider in connections as well. We could have an in and an out connection to any of the existing nodes.
         *
         * So, if we write nodes we could connect out to as oₙ and nodes we could connect in to as iₙ then we have
         *
         *     o₁ o₂ ... oₙ, i₁ i₂ ... iₙ
         *
         * The new node could also connect to itself - we will indicate that by k
         *
         *     o₁ o₂ ... oₙ, i₁ i₂ ... iₙ, k
         *
         *
         * The new node can "connect to" 1 of these or 2 of them or 3 etc up to 2n + 1 of them.
         *
         * Connecting to 2n+1 of them would mean that the new node is connected in and out to all of the n nodes and connected to itself
         *
         * Sooooooo
         *
         * We need to choose all single nodes and then all pairs of nodes and then all triples etc up to tuples of size 2n from this list.
         *
         * Then we connect the new node to each element of these tuples (order is not significant in each tuple)
         *
         * We write the n nodes, followed by n newNodeKey's
         *
         * Then we write n newNodeKey's followed by the n nodes.
         *
         * Then we zip them together
         *
         * Let newNodeKey = 5 and let n = 4
         *
         *     1, 2, 3, 4, 5, 5, 5, 5
         *     5, 5, 5, 5, 1, 2, 3, 4
         *
         * Add the connection from 5 to 5
         *
         *     1, 2, 3, 4, 5, 5, 5, 5, 5
         *     5, 5, 5, 5, 1, 2, 3, 4, 5
         *
         *     (1,5), (2,5), (3,5), (4.5), (5,1), (5,2), (5,3), (5, 4), (5,5)
         *
         * These pairs represent
         *
         *     o₁ o₂ ... oₙ, i₁ i₂ ... iₙ, k
         *
         *
         */

        ImList<String> nodes = graph.values();
        int n = nodes.size();

        ImList<String> newNodeKeyRepeated = ImList.repeat(newNodeKey, nodes.size());
        ImList<String> one = nodes.append(newNodeKeyRepeated);
        ImList<String> two = newNodeKeyRepeated.append(nodes);

        // Form the set of pairs and add the last one connecting newNodeKey to itself
        ImList<ImPair<String, String>> pairs = one.zip(two).appendElement(ImPair.on(newNodeKey, newNodeKey));

        ImList<ImList<ImPair<String, String>>> sets = ImList.oneTo(n).flatMap(i -> pairs.allCombinationsOfSize(i));

        say(sets.toString("\n"));

        // Now we have the connections that we are going to add.
        //
        // We need to add them to each graph in the list of graphs that we already have

        return oldGraphs.flatMap(g -> addNewNodeAndArcs(g, newNodeKey, sets));

    }

    private ImList<ImGraph<String, String, String>> addNewNodeAndArcs(ImGraph<String, String, String> otherGraph, String newNodeKey, ImList<ImList<ImPair<String, String>>> sets)
    {

        return Throw.Exception.ifYouGetHere();

        //        // Add the new node
        //        ImGraph<String, String, String> g = otherGraph.addNode(newNodeKey, newNodeKey);
        //
        //        // For each set of nodes, connect the new node to that set
        //        ImList<ImGraph<String, String, String>> graphs = sets.map(s -> addArcsBetween(g, s));
        //
        //        return graphs;
    }

    public static ImList<ImGraph<String, String, String>> getAllGraphsOfSizeThree()
    {
        ImGraph<String, String, String> g = ImGraph.empty();

        //        ImList<ImGraph<String, String, String>> gs = addNewNodeAndConnect(g, "a");

        ImGraph<String, String, String> g1 = g.addNode("a", "a");

        ImList<ImGraph<String, String, String>> gs = GraphBuilder.with(g1).addNewNodeAndConnect(ImList.on(g1), "b");

        //        say(gs.map(i -> i.showAsSets()));

        ImList<ImGraph<String, String, String>> gs2 = GraphBuilder.with(gs.head()).addNewNodeAndConnect(gs, "c");

        //        say(gs2.map(i -> i.showAsSets()));

        return gs2;
    }

    /**
     */
    public ImList<ImGraph> addNewNodeAndConnect2(String newNodeKey)
    {

        Fn3<String, String, ImGraph, ImGraph> addArc = (String n1, String n2, ImGraph g) -> g.addArc("", n1, n2);
        Fn2<String, ImGraph, ImGraph> addNode = (String n, ImGraph g) -> g.addNode(n, "");

        Fn2<String, ImGraph, Fn<ImGraph, ImGraph>> makeF0 = (String n, ImGraph g) -> addNode.ofFirst(n).then(Fn.id());
        Fn3<String, String, ImGraph, Fn<ImGraph, ImGraph>> makeF1 = (String e, String n, ImGraph g) -> addNode.ofFirst(n).then(addArc.ofFirst(e).ofFirst(n));
        Fn3<String, String, ImGraph, Fn<ImGraph, ImGraph>> makeF2 = (String e, String n, ImGraph g) -> addNode.ofFirst(n).then(addArc.ofFirst(n).ofFirst(e));
        Fn3<String, String, ImGraph, Fn<ImGraph, ImGraph>> makeF3 = (String e, String n, ImGraph g) -> makeF1.of(e, n, g).then(addArc.ofFirst(n).ofFirst(e));

        return graph.nodeKeys().flatMap(e -> apply(makeF1.of(e, newNodeKey, graph), makeF2.of(e, newNodeKey, graph), makeF3.of(e, newNodeKey, graph))).push(makeF0.of(newNodeKey, graph).of(graph));
    }

    /**
     * Apply `functions` to `graph` to give a list of graphs
     */
    private ImList<ImGraph> apply(Fn<ImGraph, ImGraph>... fns)
    {
        return ImList.on(fns).foldl(ImList.on(), (z, i) -> z.appendElement(i.of(graph)));
    }

    public ImList<ImGraph> addNewNodeAndAllArcs(String newNode)
    {
        var gs0 = ImList.on(graph.addNode(newNode, ""));
        var gs1 = graph.nodeKeys().map(n -> f1((String) n, newNode));
        var gs2 = graph.nodeKeys().map(n -> f2((String) n, newNode));
        var gs3 = graph.nodeKeys().map(n -> f3((String) n, newNode));

        ImList<ImGraph> one = ImList.join(gs0, gs1, gs2, gs3);

        return ImList.join(one, one.map(i -> i.addArc("", newNode, newNode)));

    }

    private ImGraph f1(String e, String newNode)
    {
        return graph.addNode(newNode, "").addArc("", newNode, e);
    }

    private ImGraph f2(String e, String newNode)
    {
        return graph.addNode(newNode, "").addArc("", e, newNode);
    }

    private ImGraph f3(String e, String newNode)
    {
        return graph.addNode(newNode, "").addArc("", e, newNode).addArc("", newNode, e);
    }

    public static ImList<ImGraph> allGraphsOfSize(int size)
    {
        // Make the starting graph
        ImGraph<String, String, String> start = ImList.oneTo(size).foldl(ImGraph.<String, String, String>empty(), (z, i) -> z.addNode(i.toString(), ""));

        // Make the starting builder
        GraphBuilder builder = GraphBuilder.with(start);

        // Get the list of all possible arcs
        ImList<String> nodes = start.nodeKeys();

        ImList<ImDuo<String>> ps = nodes.allPairs().map(n -> ImDuo.from(n));

        ImList<ImDuo<String>> allPairs = ImList.join(ps, ps.map(p -> p.swap()), nodes.map(n -> ImDuo.byTwo(n)));

        // for each subset of this list, create a graph with those connections

        return allPairs.powerSet().map(ss -> builder.addArcsBetween(ss));
    }

    private ImGraph<String, String, String> addArcsBetween(ImList<ImDuo<String>> connectSet)
    {
        return connectSet.foldl(graph, (z, i) -> z.addArc("", i.fst, i.snd));
    }
}
