package dev.javafp.graph;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;

import static dev.javafp.graph.ImGraph.Dir.Out;

/**
 * <p> A class to find cycles in graphs.
 * <p> A cycle is a path where you can reach the start node by following arcs
 * <p> If we consider an example of a graph that contains 6 nodes, connected as shown:
 *
 * <pre>{@code
 *           4
 *       ┌─◁─▢─◁─┐
 * 1    2│   │   │5    6
 * ▢──▷──▢   △   ▢──▷──▢
 *       │   │   │
 *       └─▷─▢─▷─┘
 *           3
 * }</pre>
 * <p> Then we can see that there are "two" cycles:
 *
 * <pre>{@code
 * 2, 3, 4, 2
 * 2, 3, 5, 4, 2
 * }</pre>
 * <p> We could think of each of these paths as representing a number of paths that are cycles
 *
 * <pre>{@code
 * 2, 3, 4, 2
 * }</pre>
 * <p> also represents
 *
 * <pre>{@code
 * 3, 4, 2, 3
 * 4, 2, 3, 4
 * }</pre>
 * <p> This class implements two algorithms
 * <p> One (getAllCycles) searches for cycles and returns any that it finds
 * <p> The other (removeNonCycles) removes nodes and arcs from the graph to create a new graph until
 * <p> we either have an empty graph - which means
 * that the original graph did not contain any cycles
 * <p> or we have a graph that contains only cycles - in the sense that if youstart on a node and follow an out arc to
 * another node, you will never encounter a node that has no out arcs.
 * <p> In the example, the remaining graph is this:
 *
 * <pre>{@code
 *           4
 *       ┌─◁─▢─◁─┐
 *       │   │   │
 *     2 ▢   △   ▢ 5
 *       │   │   │
 *       └─▷─▢─▷─┘
 *           3
 * }</pre>
 *  */
public class ImGraphCycleFinder<L, K, V>
{
    private final ImGraph<K, V, L> graph;

    private ImGraphCycleFinder(ImGraph<K, V, L> graph)
    {
        this.graph = graph;
    }

    /**
     * <p> Create a cycle finder with
     * {@code graph}
     *  as the graph to test
     *
     */
    public static <K, V, L> ImGraphCycleFinder with(ImGraph<K, V, L> graph)
    {
        return new ImGraphCycleFinder(graph);
    }

    /**
     * <p> Return a list of arcs that represent the cycles. Each list of arcs of length
     * {@code n}
     *  will represent
     * {@code n}
     *  possible cycles.
     *
     */
    public ImList<ImList<ImArc<K, L>>> getAllCycles()
    {
        var lists = graph.nodeKeys().flatMap(i -> getCyclesStartingAt(i));

        return lists.nub((i, j) -> Eq.uals(i.toImSet(), j.toImSet()));
    }

    private ImList<ImList<ImArc<K, L>>> getCyclesStartingAt(K node)
    {
        return getCyclesFromNode(ImList.on(), node);
    }

    private ImList<ImList<ImArc<K, L>>> getCyclesFromNode(ImList<ImArc<K, L>> pathSoFar, K node)
    {

        ImList<ImArc<K, L>> outArcsOnThisNode = graph.getArcs(Out, node).toList();

        // If the node is empty, we have found a non-loop path so we are not interested in this

        if (outArcsOnThisNode.isEmpty())
            return ImList.on();
        else
            // for each arc,  get the paths
            return ImList.join(outArcsOnThisNode.map(a -> getCyclesFromArc(pathSoFar, a)));
    }

    private ImList<ImList<ImArc<K, L>>> getCyclesFromArc(ImList<ImArc<K, L>> pathSoFar, ImArc<K, L> arc)
    {
        // If arc is the head of pathSoFar then we have a loop - return pathSoFar
        if (pathSoFar.isNotEmpty() && Eq.uals(pathSoFar.head(), arc))
            return ImList.on(pathSoFar);
        else if (pathSoFar.contains(arc))
            // If the pathSoFar contains arc but it is not the head then we have stumbled into a loop but we are not
            // interested now since it will be picked up from another starting node
            return ImList.on();
        else
            // add arc to the path and continue with the next node
            return getCyclesFromNode(pathSoFar.appendElement(arc), arc.end);
    }

    /**
     * <p> Return the graph with all the non cycle arcs removed so that the only arcs left form cycles.
     * <p> We can't tell how many cycles there are or how they are connected.
     *
     */
    public ImGraph<K, V, L> removeNonCycles()
    {
        ImList<K> roots = graph.roots();
        ImList<K> leaves = graph.leaves();

        if (roots.isEmpty() && leaves.isEmpty())
            return graph;
        else
        {

            ImList<K> nub = roots.append(leaves).nub();

            var g = graph.removeNodes(nub);

            return ImGraphCycleFinder.with(g).removeNonCycles();
        }
    }

    private ImGraph<K, V, L> removeArcs(ImSet<ImArc<K, L>> arcs)
    {
        return arcs.foldl(graph, (z, i) -> graph.removeArc(i.label, i.start, i.end));
    }

}
