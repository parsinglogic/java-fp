package dev.javafp.graph;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;

import static dev.javafp.graph.ImGraph.Dir.Out;
import static dev.javafp.util.Say.say;

public class ImGraphCycleFinder<L, K, V>
{
    private final ImGraph<K, V, L> graph;

    public ImGraphCycleFinder(ImGraph<K, V, L> graph)
    {
        this.graph = graph;
    }

    public static <K, V, L> ImGraphCycleFinder with(ImGraph<K, V, L> graph)
    {
        return new ImGraphCycleFinder(graph);
    }

    public ImList<ImList<ImArc<K, L>>> getAllCycles()
    {
        var lists = graph.nodeKeys().flatMap(i -> getCyclesStartingAt(i));

        say("lists", lists.toString('\n'));

        //        say("1, 4", Eq.uals(lists.at(2).toImSet(), lists.at(4).toImSet()));
        //
        //        say("nub", lists.take(4).nub((i, j) -> Eq.uals(i.toImSet(), j.toImSet())));

        return lists.nub((i, j) -> Eq.uals(i.toImSet(), j.toImSet()));
    }

    public ImList<ImList<ImArc<K, L>>> getCyclesStartingAt(K node)
    {
        return getCyclesFromNode(ImList.on(), node);
    }

    public ImList<ImList<ImArc<K, L>>> getCyclesFromNode(ImList<ImArc<K, L>> pathSoFar, K node)
    {
        //        say("node", node);

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
     * Return the graph with all the non cycle arcs removed so that the only arcs left form cycles
     * We can't tell how many cycles or how they are connected
     */
    public ImGraph<K, V, L> removeNonCycles()
    {
        ImList<K> roots = graph.roots();
        ImList<K> leaves = graph.leaves();

        if (roots.isEmpty() && leaves.isEmpty())
            return graph;
        else
        {
            //            say("graph before", graph.showAsSets());

            ImList<K> nub = roots.append(leaves).nub();
            //            say("nub", nub);

            var g = graph.removeNodes(nub);

            //            say("graph after", g.showAsSets());

            return ImGraphCycleFinder.with(g).removeNonCycles();
        }

    }

    private ImGraph<K, V, L> removeArcs(ImSet<ImArc<K, L>> arcs)
    {
        return arcs.foldl(graph, (z, i) -> graph.removeArc(i.label, i.start, i.end));
    }

}
