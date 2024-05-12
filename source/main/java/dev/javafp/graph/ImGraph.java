/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.graph;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.eq.Equals;
import dev.javafp.ex.CantRemoveNodes;
import dev.javafp.ex.KeyExists;
import dev.javafp.ex.KeyMissing;
import dev.javafp.ex.NodeHasNeighbours;
import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.Caster;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImValuesImpl;

import static dev.javafp.graph.ImGraph.Dir.In;
import static dev.javafp.graph.ImGraph.Dir.Out;

/**
 * <p> A graph (in the "standard" Computer Science Graph Theory sense).
 * <p> We would have liked to just rattle-off the properties of the types of graph that this implementation supports
 * and move on with our lives - but, on reflection
 * we feel that the terminology is not completely standard
 * so let's spend some time defining exactly what a graph is and some related terms.
 * <h3>Directed graph</h3>
 * <p> Many authors define a
 * <strong>graph</strong>
 *  by starting with a
 * <strong>directed graph</strong>
 *  like this:
 * <blockquote>
 * <p> A directed graph or (digraph) is a pair G = (V, A) where
 * <p> •
 * {@code v}
 *  is a set of vertices (or nodes), and
 * <p> • A ⊆
 * {@code v}
 *  ×
 * {@code v}
 *  is a set of directed edges (or arcs).
 * </blockquote>
 * <p> from
 * <a href="https://www.cs.cmu.edu/afs/cs/academic/class/15210-s15/www/lectures/graph-intro.pdf"  >"Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Graphs: Definition, Applications, Representation"</a>
 * <p> So these authors use node/vertex (and edge/arc) interchangeably. Other authors reserve node and arc for directed graphs and
 * vertex and edge for undirected graphs.
 * <p> For now, we will use vertex and edge.
 * <p> Note that this definition considers the vertices and the edges to be in a
 * <strong>set</strong>
 * .
 * <p> A practical implementation of a graph in Java will have to decide if a graph can contain
 * <ul>
 * <li>
 * <p> any user-defined object as a vertex
 * or
 * </li>
 * <li>
 * <p> it will define the type of the vertex in some way.
 * </li>
 * </ul>
 * <p> For example
 * {@code Set}
 * s and
 * {@code ImSet}
 * s can contain objects that have
 * reasonable implementations of hashCode() and equals(). Should graph implementations impose the same restriction?
 * <p> {@code ImGraph}
 *  takes the second approach. To create a vertex in an
 * {@code ImGraph}
 *  you have to supply a key - which must be a "well behaved object" with respect to belonging
 * to a set. However, you can associate any object you want with
 * each key. It does not need to be well-behaved.
 * <p> Each edge,
 * {@code a}
 * , can be written like this:
 *
 * <pre>{@code
 * a = (u, v)
 * }</pre>
 * <p> where
 * {@code u}
 *  and
 * {@code v}
 *  are vertices and
 * {@code a}
 *  is a pair - which implies an order to each component.
 * <p> {@code a}
 *  is said to be
 * <strong>incident on</strong>
 * {@code u}
 *  and
 * {@code v}
 *  - also
 * {@code a}
 * <strong>connects</strong>
 * {@code a}
 *  and
 * {@code v}
 *  and, furthermore, to be
 * <strong>incident from</strong>
 * {@code u}
 *  and
 * <strong>incident to</strong>
 * {@code v}
 * .
 * <p> Equivalently we say
 * {@code a}
 * <strong>leaves</strong>
 * {@code u}
 *  and
 * <strong>enters</strong>
 * {@code v}
 * .
 * <p> {@code u}
 *  is called the
 * <strong>in vertex</strong>
 *  for
 * {@code a}
 *  and
 * {@code v}
 *  is the
 * <strong>out vertex</strong>
 * .
 * <h3>Undirected graph</h3>
 * <p> Authors then describe an
 * <strong>undirected graph</strong>
 *  by stating that, in an undirected graph, the edges are
 * <strong>unordered</strong>
 *  pairs.
 * <p> To indicate this fact we can write an unordered edge
 * {@code e}
 *  like this
 *
 * <pre>{@code
 * e = {u, v}
 * }</pre>
 * <p> where
 *
 * <pre>{@code
 * {u, v} == {v, u}
 * }</pre>
 * <p> {@code e}
 *  is said to be
 * <strong>incident on</strong>
 * {@code u}
 *  and
 * {@code v}
 * .
 * <p> <strong>Mathematical</strong>
 *  graph definitions (as opposed to Computer Science definitions) tend to specify that a graph is a
 * <strong>non empty</strong>
 *  set of vertices.
 * <p> After this, authors tend to disagree about what the
 * <strong>fundamental</strong>
 *  properties are and what they are called but here are some commonly mentioned properties:
 * <h3>Labeled graph</h3>
 * <p> A graph where each vertex has a
 * <strong>label</strong>
 *  - that is simply some data. Note that each vertex in an
 * {@code ImGraph}
 *  is uniquely specified by its key. Adding some data
 * to the vertex will not affect its key.
 * <h3>Edge-labeled graph</h3>
 * <p> A graph where each edge has a label - Again this is simply some data. The idea here is that the label is
 * an
 * <strong>enumeration</strong>
 *  - to indicate a category/set/type that can be used to identify different types of edges.
 * <h3>Edge-weighted graph</h3>
 * <p> A graph where each edge(AKA edge) has some data associated with it. This data tends to be different in purpose from a label - usually
 * a
 * <strong>value</strong>
 *  (rather than an enumeration) that can be used in graph algorithms.
 * <h3>Self-loop</h3>
 * <p> A graph that can have an edge that connects a vertex to itself. This property is considered to only be available for directed graphs.
 * <h3>Adjacency</h3>
 * <p> A vertex
 * {@code u}
 *  is
 * <strong>adjacent</strong>
 *  to vertex
 * {@code v}
 *  if:
 * <ol>
 * <li>
 * <p> there exists an edge
 * {@code (u, v)}
 *  (in a directed graph)
 * </li>
 * <li>
 * <p> there exists an edge
 * {@code {u, v}}
 *  (in an undirected graph - recall that {u, v} == {v, u})
 * </li>
 * </ol>
 * <p> The adjacent vertices of a vertex are called its
 * <strong>neighbours</strong>
 * .
 * <h3>Paths</h3>
 * <p> A
 * <strong>path</strong>
 *  is a non empty list of edges/edges:
 *
 * <pre>{@code
 * [ (a, b), (b, c), (c, d) ... (x, y) ]
 * }</pre>
 * <p> Where the out vertex of each edge is the in vertex of the next edge in the list.
 * <p> If the edges are not directed then a path is a list of edges where each edge can be written as an ordered pair
 * such that the second vertex of each pair is the first vertex of the next pair in the list.
 * <p> For a directed graph that allows self loops we can have a path that just has one edge in it - and therefore one vertex.
 * <p> The
 * <strong>length</strong>
 *  of a path is the number of edges in it.
 * <p> A path's edges will define a list of vertices - by taking the first vertex in each pair and finally adding the second vertex of the last pair. We say that
 * the path
 * <strong>contains</strong>
 *  those vertices and each vertex is
 * <strong>in the path</strong>
 * . We may have vertices repeated in this list.
 * <p> A path is
 * <strong>simple</strong>
 *  if all its vertices are distinct.
 * <p> For an undirected graph, a path must contain at least two vertices (because self-loops are not allowed).
 * <h3>Reachability</h3>
 * <p> If there exists a path whose first vertex is
 * {@code u}
 *  and whose last vertex is
 * {@code v}
 *  then we say that
 * {@code v}
 *  is
 * <strong>reachable</strong>
 *  from
 * {@code u}
 * .
 * <h3>Cyclic/acyclic graph</h3>
 * <p> A
 * <strong>cycle</strong>
 *  is a path where its first and last vertex are equal.
 * <p> A
 * <strong>cyclic graph</strong>
 *  is one that contains one or more cycles. An
 * <strong>acyclic graph</strong>
 *  is one that has no cycles.
 * <h3>Connected</h3>
 * <p> For an undirected graph, if all of the vertices are reachable from each other, the graph is
 * <strong>connected</strong>
 * , otherwise it is
 * <strong>disconnected</strong>
 * .
 * <p> For a directed graph we use the term
 * <strong>strongly connected</strong>
 *  for this property.
 * <h2>The ImGraph properties</h2>
 * <p> Ok - finally - we can describe this
 * {@code ImGraph}
 *  implementation.
 * <p> An ImGraph represents a graph that:
 * <ol>
 * <li>
 * <p> is directed
 * </li>
 * <li>
 * <p> is labeled
 * </li>
 * <li>
 * <p> is edge-labeled
 * </li>
 * <li>
 * <p> can have cycles
 * </li>
 * <li>
 * <p> can be disconnected
 * </li>
 * <li>
 * <p> can be empty - ie have no nodes
 * </li>
 * </ol>
 * <p> This means that you can't represent an undirected graph ... er .. directly.
 * <p> All the functions have names that use node/arc rather than vertex/edge.
 * <p> Graphs are immutable - each time you add a node or an arc between two nodes, a new graph is created.
 * The
 * {@code show()}
 *  method returns a text representation of the graph in the form of an ascii art diagram.
 * <p> This is an example of a graph with edges labelled
 * {@code art}
 *  or
 * {@code mod}
 *  (and its ascii-art diagram):
 * <p> <img src="{@docRoot}/dev/doc-files/graph-diagrams.png"  width=700/>
 * <h2>References</h2>
 * <p> <a href="https://www.cs.cmu.edu/afs/cs/academic/class/15210-s15/www/lectures/preliminaries-notes.pdf"  >"Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Mathematical Preliminaries"</a>
 * <p> <a href="https://www.cs.cmu.edu/afs/cs/academic/class/15210-s15/www/lectures/graph-intro.pdf"  >"Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Graphs: Definition, Applications, Representation"</a>
 *
 * Each node has a unique key that identifies it.
 * Each arc stores the key of its source node and the key of its target node.
 * Each arc has a label
 */
public class ImGraph<KEY, DATA, LABEL> extends ImValuesImpl
{

    protected ImMap<KEY, DATA> valueMap;
    protected ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsOut;
    protected ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsIn;

    private static ImGraph empty = new ImGraph(ImMap.empty(), ImMap.empty(), ImMap.empty());

    /**
     * The direction of an arc in a graph with respect to a node.
     */
    public enum Dir
    {
        /**
         * An arc in a graph has direction <em>in</em> to a node.
         */
        In,

        /**
         * An arc in a graph has direction <em>out</em> from a node.
         */
        Out
    }

    protected ImGraph(ImMap<KEY, DATA> valueMap, ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsOut, ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsIn)
    {
        this.valueMap = valueMap;
        this.arcsIn = arcsIn;
        this.arcsOut = arcsOut;
    }

    protected static <KEY, DATA, LABEL> ImGraph<KEY, DATA, LABEL> with(ImMap<KEY, DATA> valueMap, ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsOut, ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsIn)
    {
        return valueMap.isEmpty()
               ? empty()
               : new ImGraph(valueMap, arcsOut, arcsIn);
    }

    /**
     * <p> If
     * {@code iks}
     *  = the union of the closure of
     * {@code ks}
     *  and
     * {@code ks}
     * then this function returns the graph that has nodes that are
     * {@code iks}
     *  and any arcs that are incident on nodes in
     * {@code iks}
     *
     */
    public ImGraph<KEY, DATA, LABEL> shrinkToInclusiveClosureOf(ImSet<LABEL> labels, ImList<KEY> ks)
    {
        // Get the inclusive closure of ks
        ImSet<KEY> closure = getClosure(i -> this.getAdjacents(Out, labels, i), ks.toImSet()).union(ks);

        ImSet<KEY> otherKeysSet = ImSet.onAll(nodeKeys()).minus(closure);

        ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> entries = this.arcsIn.removeAll(otherKeysSet);

        // Remove any in arcs that start outside the closure set
        ImList<ImPair<KEY, ImSet<ImArc<KEY, LABEL>>>> pairs = entries.pairs().map(p -> ImPair.on(p.fst, p.snd.filter(a -> !otherKeysSet.contains(a.start))));

        // This may have left pairs where the second element is the empty set.
        // Remove these and create a new arcsIn map

        ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsIn3 = ImMap.fromPairs(pairs.filter(p -> p.snd.isNotEmpty()));

        return ImGraph.with(valueMap.removeAll(otherKeysSet), arcsOut.removeAll(otherKeysSet), arcsIn3);
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(valueMap, arcsOut, arcsIn);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("valueMap", "arcsOut", "arcsIn");
    }

    /**
     * <p> Add a node with key
     * {@code key}
     *  with data
     * {@code value}
     * .
     * <p> If
     * {@code key}
     * is null then throw {@link dev.javafp.ex.NullValue}.
     *
     * If a node with key
     * {@code key}
     * already exists then throw {@link KeyExists}
     */
    public ImGraph<KEY, DATA, LABEL> addNode(KEY key, DATA value)
    {
        Throw.Exception.ifNull("key", key);
        mustNotContain(key);

        return ImGraph.with(valueMap.put(key, value), arcsOut, arcsIn);
    }

    /**
     * <p> Add a node with key
     * {@code childKey}
     *  and data
     * {@code childValue}
     * and add an arc with label
     * {@code arcLabel}
     * from the node with key
     * {@code parentKey}
     * to the new node
     * If a node with key
     * {@code key}
     * already exists then return the original graph.
     */
    public ImGraph<KEY, DATA, LABEL> addNodeToParentIfMissing(LABEL arcLabel, KEY parentKey, KEY childKey, DATA childValue)
    {
        Throw.Exception.ifNull("parentKey", parentKey);
        Throw.Exception.ifNull("childKey", childKey);
        mustContain(parentKey);

        return addNodeIfMissing(childKey, childValue).addArc(arcLabel, parentKey, childKey);
    }

    /**
     * <p> Add a node with key
     * {@code key}
     *  with data
     * {@code value}
     * If a node with key
     * {@code key}
     * already exists then return the original graph.
     */
    public ImGraph<KEY, DATA, LABEL> addNodeIfMissing(KEY key, DATA value)
    {
        Throw.Exception.ifNull("key", key);

        return containsNodeWithKey(key)
               ? this
               : addNode(key, value);
    }

    /**
     * The singleton empty graph.
     */
    public static <KEY, DATA, LABEL> ImGraph<KEY, DATA, LABEL> empty()
    {
        return Caster.cast(empty);
    }

    /**
     * The singleton empty graph.
     */
    public static <KEY, DATA, LABEL> ImGraph<KEY, DATA, LABEL> on()
    {
        return empty();
    }

    /**
     * <p> {@code true}
     *  if the graph contains a node with key
     * {@code key}
     *
     */
    public boolean containsNodeWithKey(KEY key)
    {
        Throw.Exception.ifNull("key", key);

        return valueMap.get(key) != null;
    }

    /**
     * <p> Add an arc with label
     * {@code label}
     *  from
     * {@code start}
     *  to
     * {@code end}
     *
     */
    public ImGraph<KEY, DATA, LABEL> addArc(LABEL label, KEY start, KEY end)
    {
        Throw.Exception.ifNull("start", start);
        Throw.Exception.ifNull("end", end);
        mustContain(start);
        mustContain(end);

        ImArc<KEY, LABEL> arc = ImArc.on(label, start, end);

        ImSet<ImArc<KEY, LABEL>> out = arcsOut.getOrDefault(start, ImSet.empty()).add(arc);
        ImSet<ImArc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImSet.empty()).add(arc);

        return ImGraph.with(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    /**
     * <p> Remove the arc
     * {@code arc}
     *
     */
    public ImGraph<KEY, DATA, LABEL> removeArc(LABEL label, KEY start, KEY end)
    {
        Throw.Exception.ifNull("start", start);
        Throw.Exception.ifNull("end", end);
        mustContain(start);
        mustContain(end);

        ImArc<KEY, LABEL> arcToRemove = ImArc.on(label, start, end);
        return removeArc(arcToRemove);
    }

    /**
     * <p> Remove the arc with label
     * {@code label}
     *  from
     * {@code start}
     *  to
     * {@code end}
     *
     * <p> If
     * {@code start}
     * or
     * {@code end}
     * is null then throw {@link dev.javafp.ex.NullValue}.
     *
     *
     * If one or more of the nodes do not exist then throw {@link KeyMissing}.
     *
     * If no such arc exists then return the original graph,
     */
    public ImGraph<KEY, DATA, LABEL> removeArc(ImArc<KEY, LABEL> arcToRemove)
    {

        var newOut = removeArc(arcsOut, arcToRemove.start, arcToRemove);
        var newIn = removeArc(arcsIn, arcToRemove.end, arcToRemove);

        return ImGraph.with(valueMap, newOut, newIn);
    }

    private ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> removeArc(ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> arcsMap, KEY key, ImArc<KEY, LABEL> arcToRemove)
    {

        ImSet<ImArc<KEY, LABEL>> newSet = arcsMap.get(key).remove(arcToRemove);

        // We have to make sure we remove the entry for `key`` if it is now empty - otherwise just replace the value at `key`
        return newSet.isEmpty()
               ? arcsMap.remove(key)
               : arcsMap.put(key, newSet);
    }

    /**
     * <p> Remove the node with key
     * {@code key}
     *
     * If the node is connected to another node then throw {@link NodeHasNeighbours}.
     *
     */
    public ImGraph<KEY, DATA, LABEL> removeNode(KEY key)
    {
        Throw.Exception.ifNull("key", key);
        mustContain(key);

        var connected = getAdjacents(In, key).union(getAdjacents(Out, key));

        if (connected.isNotEmpty())
        {
            throw new NodeHasNeighbours(key, connected);
        }

        return ImGraph.with(valueMap.remove(key), arcsOut.remove(key), arcsIn.remove(key));
    }

    private void mustContain(KEY key)
    {
        if (!containsNodeWithKey(key))
            throw new KeyMissing(key);
    }

    private void mustNotContain(KEY key)
    {
        if (containsNodeWithKey(key))
            throw new KeyExists(key);
    }

    /**
     * <p> Remove all the nodes in
     * {@code keys}
     * .
     * <p> Remove any arcs incident on nodes.
     * <p> if any node is connected to a node that does not belong to keys then throw {@link CantRemoveNodes}
     */
    public ImGraph<KEY, DATA, LABEL> removeNodes(Iterable<KEY> keys)
    {
        //        ImList<KEY> keysList = ImList.onAll(keys);

        ImSet<KEY> keysSet = ImSet.onAll(keys);

        // Get the keys in keysList that are not in the graph
        ImSet<KEY> baddies = keysSet.filter(k -> valueMap.get(k) == null);

        if (baddies.isNotEmpty())
        {
            throw new KeyMissing(baddies.anyElement()); // TODO improve this
        }
        else
        {
            var outArcs = keysSet.map(k -> getArcs(Out, k));
            var inArcs = keysSet.map(k -> getArcs(In, k));

            var g = ImSet.join(outArcs.union(inArcs)).foldl(this, (z, i) -> z.removeArc(i));

            return ImGraph.with(g.valueMap.removeAll(keys), g.arcsOut, g.arcsIn);
        }
    }

    private ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> getMap(Dir dir)
    {
        return dir == In
               ? arcsIn
               : arcsOut;
    }

    /**
     * The data value associated with the node with key
     * {@code key}
     */
    public DATA getValue(KEY key)
    {
        Throw.Exception.ifNull("key", key);

        return valueMap.get(key);
    }

    /**
     * The nodes that have no incoming arcs.
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     */
    public ImList<KEY> roots()
    {
        return nodeKeys().filter(k -> getAdjacents(In, k).isEmpty());
    }

    /**
     * The nodes that have no outgoing arcs.
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     */
    public ImList<KEY> leaves()
    {
        return nodeKeys().filter(k -> getAdjacents(Out, k).isEmpty());
    }

    /**
     * {@code true}
     * if any of the nodes in the graph have cycles.
     *
     * A cycle is a path from a node to itself that can be traced by following any arc on a node in the
     * direction
     * {@code Out}
     */
    public boolean hasCycle()
    {
        if (arcsOut.entrySet.containsElementWhere(e -> xxx(e)))
            return true;
        else
            return roots().any(this::hasCycle);
    }

    private <K, L> boolean xxx(ImMap.Entry<K, ImSet<ImArc<K, L>>> e)
    {
        return e.value.containsElementWhere(a -> Equals.isEqual(a.end, e.key));
    }

    private boolean hasCycle(KEY root)
    {
        return hasCycle(ImSet.empty(), root);
    }

    private boolean hasCycle(ImSet<KEY> set, KEY key)
    {
        Throw.Exception.ifNull("key", key);

        //System.out.println(ImList.onAll(set) + " --- " + key);
        return set.contains(key)
               ? true
               : getAdjacents(Out, key).any(a -> hasCycle(set.add(key), a));
    }

    /**
     * The text-box representation of the graph in an "ascii-art" form
     *
     * <p> <img src="{@docRoot}/dev/doc-files/graph-diagrams.png"  width=1000/>
     *
     */
    public AbstractTextBox show()
    {

        // Split the graph into parts
        ImList<ImSet<KEY>> parts = this.partition();

        // For each part, try to find the root. If one does not exist, take a random node from the set that points to at least one other node
        ImList<KEY> roots = parts.map(i -> getRootOrANodeInALoop(i));

        return getBoxForPairs(ImPair.on(ImSet.empty(), ImList.on()), roots.map(r -> ImPair.on("", r))).snd;
    }

    private KEY getRootOrANodeInALoop(ImSet<KEY> keys)
    {
        // There will be 0 or 1 roots
        ImSet<KEY> roots = keys.filter(i -> this.getArcs(In, i).isEmpty());

        // If there are no roots, find a node that points to another node
        return (roots.isEmpty() ? keys.filter(i -> this.getArcs(Out, i).isNotEmpty()) : roots).anyElement().get();
    }

    public AbstractTextBox showAsSets()
    {
        if (this.isEmpty())
            return LeafTextBox.with("empty()");
        else
        {
            ImList<ImSet<ImArc<KEY, LABEL>>> values = arcsOut.values();

            return LeafTextBox.with(nodeKeys().toString(" ")).above(LeafTextBox.with(values.flatMap(i -> i.toList()).toString("\n")));
        }
    }

    public boolean isEmpty()
    {
        return this == empty();
    }

    public ImList<ImSet<KEY>> partition()
    {

        // Get the nodes that point to another node or are freestanding
        ImSet<KEY> ks = nodeKeysSet().filter(i -> getArcs(Out, i).isNotEmpty() || (getArcs(Out, i).isEmpty() && getArcs(In, i).isEmpty()));

        // Partition the graph
        return partition(ks);
    }

    /**
     * The list of sets of keys representing nodes that are not connected by following "out arcs"
     */
    private ImList<ImSet<KEY>> partition(ImSet<KEY> remaining)
    {
        if (remaining.isEmpty())
            return ImList.on();
        else
        {
            KEY k = remaining.anyElement().get();

            ImSet<KEY> reachable = this.getClosure(Out, k).add(k);

            return partition(remaining.minus(reachable)).push(reachable);
        }

    }

    /**
     * <p> Showing the text boxes:
     * <p> This is an example of a graph with arcs labeled "-", its ascii-art diagram and an illustration of how each box
     * relates to the graph content.
     *
     * <p> <img src="{@docRoot}/dev/doc-files/graph-getboxfor.png"  width=400/>
     *
     * <p> Note that, where a node has been displayed already (assuming we are processing top-down), it is not displayed in full.
     * Instead, just its label is shown in brackets to indicate that it has been shown already - somewhere above the current line.

     */
    ImPair<ImSet<KEY>, AbstractTextBox> getBoxFor(ImSet<KEY> seen, String label, KEY key) //ImPair<String, KEY> labelAndKey)
    {

        if (seen.contains(key))
            return ImPair.on(seen, LeafTextBox.with("|- " + label + " -> (" + key + ")"));
        else
        {
            AbstractTextBox firstBox = LeafTextBox.with("|-" + getLabelToDisplay(label) + "-> " + key);

            ImSet<KEY> seen2 = seen.add(key);

            // Get the pairs of arc labels and keys and convert the label to a string
            ImList<ImPair<String, KEY>> pairs = getPairs(Out, key).map(p -> ImPair.on(p.fst.toString(), p.snd));

            if (pairs.isEmpty())
            {
                //ImPair<ImSet<KEY>, ImList<AbstractTextBox>> setAndBox = getSetAndBox(start, labelAndKey);
                return ImPair.on(seen2, firstBox);
            }
            else
            {
                // Get the boxes for the children
                // I need to accumulate two things - the seen set and the list of boxes - so I need a ImPair
                ImPair<ImSet<KEY>, ImList<AbstractTextBox>> start = ImPair.on(seen2, ImList.on());

                //                say("getBoxFor - calling boxForPairs, start =", start);
                ImPair<ImSet<KEY>, AbstractTextBox> boxForPairs = getBoxForPairs(start, pairs);
                //                say("getBoxFor - finished calling boxForPairs, boxForPairs.fst =", boxForPairs.fst);

                // Create the left hand box with the vertical line
                String s = "|" + " ".repeat(getLabelToDisplay(label).length() + 4);

                LeafTextBox left = LeafTextBox.with(TextUtils.join(ImList.repeat(s, boxForPairs.snd.height), "\n"));

                // Put them together left to right and put the first line on top
                return ImPair.on(boxForPairs.fst, firstBox.above(left.before(boxForPairs.snd)));
            }
        }
    }

    private String getLabelToDisplay(String label)
    {
        return label.isEmpty()
               ? ""
               : " " + label + " ";
    }

    /**
     *
     * @param start
     * @param pairs
     * @return
     */
    private ImPair<ImSet<KEY>, AbstractTextBox> getBoxForPairs(ImPair<ImSet<KEY>, ImList<AbstractTextBox>> start,
            ImList<ImPair<String, KEY>> pairs)
    {
        // For each pair, get the new set and the text box and add the text box to the accumulator

        ImPair<ImSet<KEY>, ImList<AbstractTextBox>> setAndBoxes = pairs.foldl(start, (z, sk) -> getBoxFor2(z, sk));

        // Stack the boxes vertically
        AbstractTextBox b = TopDownBox.withAllBoxes(setAndBoxes.snd.reverse());

        return ImPair.on(setAndBoxes.fst, b);
    }

    /**
     * <p> This just wraps this::getBoxFor so that it can be used in foldl
     */
    private ImPair<ImSet<KEY>, ImList<AbstractTextBox>> getBoxFor2(ImPair<ImSet<KEY>, ImList<AbstractTextBox>> z, ImPair<String, KEY> labelAndKey)
    {

        ImPair<ImSet<KEY>, AbstractTextBox> setAndBox = getBoxFor(z.fst, labelAndKey.fst, labelAndKey.snd);

        return ImPair.on(setAndBox.fst, z.snd.push(setAndBox.snd));
    }

    /**
     * The graph that has the same nodes and arcs as the original but with each
     * data value for each node transformed by
     * {@code fn}
     */
    public <NEWDATA> ImGraph<KEY, NEWDATA, LABEL> map(Fn<DATA, NEWDATA> fn)
    {
        return ImGraph.with(valueMap.map(fn), arcsOut, arcsIn);
    }

    /**
     * A representation of the graph in <a href="https://graphviz.org/">GraphViz</a> format.
     *
     *
     */
    public String getGraphVizGraph()
    {

        ImList<String> head = ImList.on(
                "digraph d {",
                "rankdir=TD;",
                "size=\"10,10\";",
                "node [shape = box];"
        );
        ImList<String> tail = ImList.on("}");

        ImList<String> nodes = nodeKeys().flatMap(this::getGraphVizChunk);

        return TextUtils.join(ImList.join(head, nodes, tail), "\n");
    }

    /**
     *
     * A list of all the keys in the graph
     */
    public ImList<KEY> nodeKeys()
    {
        return valueMap.keys();
    }

    /**
     *
     * A set of all the keys in the graph
     */
    public ImSet<KEY> nodeKeysSet()
    {
        return valueMap.keysSet();
    }

    /**
     *
     * A list of all the arcs in the graph
     */
    public ImSet<ImArc<KEY, LABEL>> arcs()
    {
        return ImSet.join(arcsOut.values());
    }

    /**
     *
     * A list of all the data values in the graph
     */
    public ImList<DATA> values()
    {
        return valueMap.values();
    }

    private ImList<String> getGraphVizChunk(KEY key)
    {
        Throw.Exception.ifNull("key", key);

        ImList<ImPair<LABEL, KEY>> out = getPairs(Out, key);

        return out.isEmpty()
               ? ImList.on(TextUtils.quote(key) + ";")
               : out.map(p -> TextUtils.quote(key) + " -> " + TextUtils.quote(p.snd) +
                "[ label = " + TextUtils.quote(p.fst) + "];");
    }

    /**
     * <p> Get the arcs in the direction
     * {@code dir}
     *  from
     * {@code key}
     *  in the form of pairs containing the arc label and the key
     *
     */
    public ImList<ImPair<LABEL, KEY>> getPairs(Dir dir, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getArcs(dir, key).toList().map(arc -> ImPair.on(arc.label, arc.getSlot(dir)));
    }

    private ImMap<KEY, ImSet<ImArc<KEY, LABEL>>> getArcsMap(Dir dir)
    {
        Throw.Exception.ifNull("dir", dir);

        return dir == Out
               ? arcsOut
               : arcsIn;
    }

    /**
     * <p> Get the closure of node with key
     * {@code key}
     * in the direction
     * {@code dir}
     * following arcs with label
     * {@code label}
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     *
     * <p> The list will *not* include the key
     * {@code key}
     * unless that node is in a cycle - in which case it *will* contain it
     *
     */
    public ImSet<KEY> getClosure(Dir dir, LABEL label, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getClosure(k -> getAdjacents(dir, label, k), ImSet.on(key), ImSet.on());
    }

    /**
     * <p> The closure of
     * {@code candidates}
     *  with respect to the function
     * {@code adjacentFn}
     *
     * <p> Note that the closure does not intersect with
     * {@code candidates}
     *
     *
     */
    public ImSet<KEY> getClosure(Fn<KEY, ImSet<KEY>> adjacentFn, ImSet<KEY> candidates)
    {
        return getClosure(adjacentFn, candidates, ImSet.on());
    }

    private ImSet<KEY> getClosure(Fn<KEY, ImSet<KEY>> adjacentFn, ImSet<KEY> candidates, ImSet<KEY> visited)
    {
        // Go through all the nodes in the start set to generate a new set.
        // Note that we remove any already in visited since we are not going to chase them
        ImSet<KEY> newCandidates = candidates.flatMap(adjacentFn).minus(visited);

        // If there are any new candidates then continue
        // otherwise stop
        return newCandidates.isNotEmpty()
               ? getClosure(adjacentFn, newCandidates, visited.union(newCandidates))
               : visited;
    }

    /**
     * <p> Get the keys that are adjacent to
     * {@code key}
     *  by arcs in the direction
     * {@code dir}
     *  that have the label
     * {@code label}
     *
     */
    public ImSet<KEY> getAdjacents(Dir dir, LABEL label, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getArcs(dir, key).filter(arc -> arc.label.equals(label)).map(arc -> arc.getSlot(dir));
    }

    /**
     * <p> Get the keys that are connected to
     * {@code key}
     *  by arcs in the direction
     * {@code dir}
     *  that have a label contained in
     * {@code labels}
     *
     *
     */
    public ImSet<KEY> getAdjacents(Dir dir, ImSet<LABEL> labels, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getArcs(dir, key).filter(arc -> labels.contains(arc.label)).map(arc -> arc.getSlot(dir));
    }
    //------------------------------------------------------------------------------------

    /**
     * <p> Get the closure of node with key
     * {@code key}
     * in the direction
     * {@code dir}
     * following the arcs with labels in the set
     * {@code labels}
     *
     * <p> Returns a <em>set</em> of keys.
     *
     * <p> The list will not include the key
     * {@code key}
     * unless that node is in a cycle - in which case it will contain it
     *
     */
    public ImSet<KEY> getClosure(Dir dir, ImSet<LABEL> labels, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getClosure(k -> getAdjacents(dir, labels, k), ImSet.on(key), ImSet.on());
    }

    /**
     * <p> The arcs in the direction
     * {@code dir}
     *  on the node
     * {@code key}
     *
     */
    public ImSet<ImArc<KEY, LABEL>> getArcs(Dir dir, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getMap(dir).getOrDefault(key, ImSet.empty());
    }

    /**
     * <p> Get the paths of the keys that are connected to
     * {@code key}
     *  by arcs in the direction
     * {@code dir}
     *  that have a label contained in
     * {@code labels}
     * The first entry in each path is key
     *
     */
    public ImList<ImList<KEY>> getPaths(Dir dir, ImSet<LABEL> labels, KEY key)
    {

        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        ImList<KEY> neighbours = getAdjacents(dir, labels, key).toList();

        ImList<ImList<KEY>> paths = neighbours.isEmpty()
                                    ? ImList.on(ImList.on())
                                    : neighbours.flatMap(n -> getPaths(dir, labels, n));

        return paths.map(p -> p.push(key));
    }

    //------------------------------------------------------------------------------------

    /**
     *
     * <p> Get the closure of node with key
     * {@code key}
     * in the direction
     * {@code dir}
     *  following all arcs
     *
     * <p> Returns a <em>set</em> of keys.
     *
     * <p> The list will not include the key
     * {@code key}
     * unless that node is in a cycle - in which case it will contain it
     *
     * <pre>{@code
     * getClosure(Out, "a")
     * }</pre>
     * on the graph:
     * <p> <img src="{@docRoot}/dev/doc-files/no-cycle.png"  width="200" />
     * <p> will return the set:
     * <pre>{@code
     * {b, c}
     * }</pre>
     *
     * <pre>{@code
     * getClosure(Out, "a")
     * }</pre>
     * on the graph:
     * <p> <img src="{@docRoot}/dev/doc-files/cycle.png"  width="200" />
     * <p> will return the set:
     * <pre>{@code
     * {a, b, c}
     * }</pre>
     *
     */
    public ImSet<KEY> getClosure(Dir dir, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getClosure(k -> getAdjacents(dir, k), ImSet.on(key));
    }

    /**
     * <p> Get the set of nodes that are connected to the node with key
     * {@code key}
     *  by any arcs in the direction
     * {@code dir}
     *
     */
    public ImSet<KEY> getAdjacents(Dir dir, KEY key)
    {
        Throw.Exception.ifNull("dir", dir);
        Throw.Exception.ifNull("key", key);

        return getMap(dir).getOrDefault(key, ImSet.empty()).map(arc -> arc.getSlot(dir));
    }

    /**
     * The list of data values associated with the nodes whose keys are in
     *
     * {@code key}
     *
     * There might be repeated values in the list.
     */
    public ImList<DATA> getValuesFromKeys(ImList<KEY> keys)
    {
        return keys.map(this::getValue);
    }

    /**
     * <p> A list of keys in
     * <strong>topological order</strong>
     *  with respect to
     * {@code adjacentFn}
     *  starting from
     * {@code startNodes}
     * .
     * <p> This means:
     * <p> If
     * {@code ks}
     *  is the returned list of keys then:
     *
     * <pre>{@code
     * for all arcs, (u,v) in this
     * u appears before v in ks
     * }</pre>
     *
     */
    public ImList<KEY> topologicalOrder(Fn<KEY, ImSet<KEY>> adjacentFn, ImList<KEY> startNodes)
    {
        return ts(adjacentFn, startNodes);
    }

    /**
     * <p> A list of keys in
     * <strong>topological order</strong>
     *  with respect to
     * {@code adjacentFn}
     *  starting from
     * {@code startNode}
     * .
     * <p> This means:
     * <p> If
     * {@code ks}
     *  is the returned list of keys then:
     *
     * <pre>{@code
     * for all arcs, (u,v) in this
     * u appears before v in ks
     * }</pre>
     *
     */
    public ImList<KEY> topologicalOrder(Fn<KEY, ImSet<KEY>> adjacentFn, KEY startNode)
    {
        Throw.Exception.ifNull("startNode", startNode);

        return ts(adjacentFn, ImList.on(startNode));
    }

    private ImList<KEY> ts(Fn<KEY, ImSet<KEY>> adjacentFn, ImList<KEY> candidates)
    {
        if (candidates.isEmpty())
            return ImList.on();

        KEY c = candidates.head();
        ImList<KEY> cs = candidates.tail();

        ImList<KEY> ks = ts(adjacentFn, cs.append(adjacentFn.of(c).toList()));

        return ks.contains(c)
               ? ks
               : ks.push(c);
    }

}