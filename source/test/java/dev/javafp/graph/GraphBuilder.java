package dev.javafp.graph;

import dev.javafp.lst.ImList;

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
     */

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
