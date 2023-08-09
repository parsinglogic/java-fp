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
import dev.javafp.ex.KeyExists;
import dev.javafp.ex.KeyIsNull;
import dev.javafp.ex.KeyMissing;
import dev.javafp.ex.NodeHasArcs;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImListZipper;
import dev.javafp.set.ImMap;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.Caster;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImValuesImpl;

import static dev.javafp.graph.ImGraph.Dir.In;
import static dev.javafp.graph.ImGraph.Dir.Out;

/**
 * A graph (in the "standard" Computer Science Graph Theory sense) with some extra features.
 *
 * We would have liked to just rattle off the properties of the types of graph that this implementation supports - but, on reviewing the literature
 * we see that:
 *
 * 1. there does not seem to be enough standardisation in the Graph theory terms
 * 2. Not many programmers are aware of any of the formal definitions
 *
 * Soooo - I feel I need to chatter for a while to define some terms...
 *
 * All disciplines have problems with standardising terminology and Computer Science is no exception.
 *
 * In the area of trees and graphs this is certainly true.
 *
 * ### directed graph
 *
 * Most authors define a graph by starting with a **directed graph** like this:
 *
 * > A directed graph or (digraph) is a pair G = (V, A) where
 * > • V is a set of vertices (or nodes), and
 * > • A ⊆ V × V is a set of directed edges (or arcs).
 *
 * from ["Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Graphs: Definition, Applications, Representation"][cmugraph]
 *
 *
 * ### undirected graph
 *
 * And then they describe an **undirected graph** by stating that the edges are not **ordered pairs** as implied by the cartesian product notation and are
 * **unordered**
 * so that
 *
 *     edge (a, b) = edge (b, a)
 *
 * After this they tend to disagree about what the fundamental properties are but here are some common properties:
 *
 * ### labeled graph
 *
 * A graph where each node has a label - that is simply some data
 *
 * ### edge-labeled graph
 *
 * A graph where each edge(AKA arc) has a label - that is simply some data
 *
 * ### multi-way graph
 *
 * A graph that can have more than one arc connecting two nodes
 *
 * ### self-loop capable graph
 *
 * A graph that can have an arc that connects a node to itself. Often, this property is considered to only be available for directed graphs
 *
 * The above properties, we would consider, define categories or types of graphs - in the sense that we might consider implementing them with different data structures.
 *
 * A node b is **adjacent** to node b iff:
 *
 * there exists an edge (a,b) (in a directed graph)
 * there exists an edge {a, b} (in an undirected graph - where {a, b} == {b, a})
 *
 * The **connected set** of nodes for a node n is defined by a calculation:
 *
 * Start with the empty set.
 *
 * There are two stages:
 *
 * 1. add the adjacent nodes of n to the set
 *
 * 2. for each node in the set, add its adjacent nodes to the set
 *
 * repeat 2. until the set does not grow any larger.
 *
 *
 * A node b is **reachable** from a iff:
 *
 * the connected set for a includes b.
 *
 * There are other properties that seem to be properties that a particular instance of a graph can have rather than a category or a type in the sense described above.
 *
 *
 * A path is a list of arcs:
 *
 *     [ (a, b), (b, c), (c, d) ... (x, y) ]
 *
 * where
 * each arc is unique
 * the second element of each pair is the first element of the next pair in the list.
 *
 * The nodes in path are the list formed by taking the
 *
 * second element of each arc.
 *
 * A path in an undirected graph is ...
 *
 *
 * ### Cyclic/acyclic graph
 *
 * A cycle is a node that is reachable from itself
 *
 *
 * A cyclic graph is one that contains one or more cycles. An acyclic graph is one that has no cycles.
 *
 * ### Connected/unconnected graph
 *
 * If the connected set for at least one node is the same set as the nodes for the graph, then the graph is connected - otherwise it is unconnected (AKA a forest)
 *
 * For directed graphs we can also define **strongly connected** - which means that all nodes are reachable by all other nodes
 *
 * A tree is often defined as an undirected graph that is connected, acyclic and has a node identified (somehow) as the root node.
 *
 * This definition now imposes a direction on each arc. You start at the root and then each reachable node can be considered to have
 * a direction going from the root to its adjacent node. We repeat this for these nodes and so on.
 *
 * ## More terminology related to ordered trees
 *
 * The nodes that are adjacent to a node p are called the children of p.
 *
 * For each child, c of a node p, p is called the parent of c.
 *
 * This definition of **tree** would surprise most programmers because **it does not specify that there is any order to the children of a node**.
 *
 * We would imagine that almost no programmers would think of a tree as having nodes with unordered children.
 *
 * Finally, we do get to an **ordered tree** - which is defined as a tree with each node having arcs (and therefore children) that have an order with
 * respect to that node.
 *
 * ### Another way to define an n-ary tree (ie one that has ordered children)
 *
 * This seems a surprising way to define a tree. To start with an undirected graph, pick a root, have that root convert a undirected graph into
 * a directed graph and then impose an order seems unintuitive (to us at least).
 *
 * Another way would be to have considered a connected, directed, acyclic graph and then defined a root as a node that has no in-arcs.
 *
 * We also need an order to be imposed on each arc with respect to its in-vertex and out-vertex.
 *
 * If there is a single root, then this graph is an n-ary tree.
 *
 * We note that we can't find any author who uses quite this characterisation. Hey ho.
 *
 * To be fair, Carnegie Mellon University do start with a directed tree:
 *
 * > Definition 2.6 (Rooted Tree). A rooted tree is a directed graph such that
 * > 1. One of the vertices is the root and it has no in edges.
 * > 2. All other vertices have one in-edge.
 * > 3. There is a path from the root to all other vertices.
 * > Terminology. When talking about rooted trees, by convention we use the term node
 * > instead of vertex. A node is a leaf if it has no out edges, and an internal node otherwise.
 * > For each directed edge (u, v), u is the parent of v, and v is a child of u. For each path
 * > from u to v (including the empty path with u = v), u is an ancestor of v, and v is a
 * > descendant of u. For a vertex v, its depth is the length of the path from the root to v and
 * > its height is the longest path from v to any leaf. The height of a tree is the height of its
 * > root. For any node v in a tree, the subtree rooted at v is the rooted tree defined by taking
 * > the induced subgraph of all vertices reachable from v (i.e. the vertices and the directed
 * > edges between them), and making v the root. As with graphs, an ordered rooted tree is
 * > a rooted tree in which the out edges (children) of each node are ordered.
 *
 * from ["Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Mathematical Preliminaries"][cmuprelim]
 *
 *
 * That final sentence that defines an ordered rooted tree suggests that the ordering property for out edges might be considered a thing
 * for graphs too - but I can't find any other reference to it in the document.
 *
 * We mention this here because the idea of being able to impose an order on arcs in a directed graph - rather than waiting until we have already defined
 * a tree before imposing an order is exactly what we have done in this implementation.
 *
 * Ok - finally - we can describe this `ImGraph` implementation
 *
 * An ImGraph represents a graph that:
 *
 * 1. is directed
 * 2. is labeled
 * 3. is edge-labeled
 * 4. can be a multi-graph (two nodes can have many arcs between them)
 * 5. can have cycles
 * 6. can be a forest
 * 7. has arcs with an **order with respect to their nodes** (both in and out nodes)
 *
 * ### Property number 7!
 *
 * Just to stress this point: No other author that we know about defines an order on arcs in a directed graph.
 *
 * That does not mean that they don't exist, of course. We just can't find them.
 *
 *
 * <p> If node a is connected to node b via an arc labeled c
 * then we store the arc c in arcsOut and in arcsIn
 * <p> Graphs are immutable - each time you add a node or an arc between two nodes, a new graph is created.
 * <p> The show method<p> Returns a text representation of the graph in the form of an ascii art diagram.
 *
 * <p> This is an example of a graph with arcs labelled art or mod and its ascii-art diagram
 *
 *
 * <p> </p><img src="{@docRoot}/dev/doc-files/graph-diagrams.png"  width=700/>
 *
 *
 *
 *
 *
 *
 *
 * ## References
 *
 * ["Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Mathematical Preliminaries"][cmuprelim]
 *
 * ["Carnegie Mellon University, School of Computer Science: Parallel and Sequential Data Structures and Algorithms, Graphs: Definition, Applications, Representation"][cmugraph]
 *
 * [cmuprelim]: https://www.cs.cmu.edu/afs/cs/academic/class/15210-s15/www/lectures/preliminaries-notes.pdf
 * [cmugraph]: https://www.cs.cmu.edu/afs/cs/academic/class/15210-s15/www/lectures/graph-intro.pdf
 *
 *
 *
 *
 */
public class ImGraph<KEY, DATA, LABEL> extends ImValuesImpl
{

    private ImMap<KEY, DATA> valueMap;
    private ImMap<KEY, ImList<ImArc<KEY, LABEL>>> arcsOut;
    private ImMap<KEY, ImList<ImArc<KEY, LABEL>>> arcsIn;

    private static ImGraph empty = new ImGraph<>(ImMap.empty(), ImMap.empty(), ImMap.empty());

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

    protected ImGraph(ImMap<KEY, DATA> valueMap, ImMap<KEY, ImList<ImArc<KEY, LABEL>>> arcsOut, ImMap<KEY, ImList<ImArc<KEY, LABEL>>> arcsIn)
    {
        this.valueMap = valueMap;
        this.arcsIn = arcsIn;
        this.arcsOut = arcsOut;

    }

    public ImGraph<KEY, DATA, LABEL> shrinkToInclusiveClosureOf(ImSet<LABEL> labels, ImList<KEY> ks)
    {
        // Get the inclusive closure of ks
        // Get the other nodes
        ImList<KEY> closure = getInOrderClosure(Out, labels, ks);
        // System.out.println("closure " + closure);

        ImSet<KEY> otherKeysSet = ImSet.onAll(keys()).minus(closure);
        ImList<KEY> otherKeys = otherKeysSet.toImList();
        // System.out.println("otherKeys " + otherKeys);

        ImMap<KEY, ImList<ImArc<KEY, LABEL>>> arcsIn = this.arcsIn.removeAll(otherKeys).map(v -> v.filter(a -> !otherKeysSet.contains(a.start)));

        return new ImGraph<>(valueMap.removeAll(otherKeys), arcsOut.removeAll(otherKeys), arcsIn);
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
     * If a node with key
     * {@code key}
     * already exists then throw {@link KeyExists}
     */
    public ImGraph<KEY, DATA, LABEL> addNode(KEY key, DATA value)
    {
        if (key == null)
            throw new KeyIsNull(key);
        else if (containsNodeWithKey(key))
            throw new KeyExists(key);
        else
            return new ImGraph<>(valueMap.put(key, value), arcsOut, arcsIn);
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
    public ImGraph<KEY, DATA, LABEL> addNodeToParent(LABEL arcLabel, KEY parentKey, KEY childKey, DATA childValue)
    {
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
     * <p> {@code true}
     *  if the graph contains a node with key
     * {@code key}
     *
     */
    public boolean containsNodeWithKey(KEY key)
    {
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
        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        ImArc<KEY, LABEL> arc = ImArc.on(label, start, end);

        ImList<ImArc<KEY, LABEL>> out = arcsOut.getOrDefault(start, ImList.empty()).push(arc);
        ImList<ImArc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).push(arc);

        return new ImGraph<>(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    /**
     * <p> Add an arc with label
     * {@code label}
     *  from
     * {@code start}
     *  to
     * {@code end}
     *  - adding it after all the existing arcs on the nodes
     *
     */
    public ImGraph<KEY, DATA, LABEL> addArcAsLast(LABEL label, KEY start, KEY end)
    {
        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        ImArc<KEY, LABEL> arc = ImArc.on(label, start, end);

        ImList<ImArc<KEY, LABEL>> out = arcsOut.getOrDefault(start, ImList.empty()).appendElement(arc);
        ImList<ImArc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).appendElement(arc);

        return new ImGraph<>(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    /**
     * <p> Add an arc with label
     * {@code label}
     *  from
     * {@code start}
     *  to
     * {@code end}
     *  - adding it after the arc from
     * {@code start}
     *  to
     * {@code after}
     *  with label
     * {@code label}
     *
     */
    public ImGraph<KEY, DATA, LABEL> addArcAfter(LABEL label, KEY start, KEY end, KEY after)
    {
        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        // The arc that points to key after
        ImArc<KEY, LABEL> arcForAfter = ImArc.on(label, start, after);
        ImArc<KEY, LABEL> arc = ImArc.on(label, start, end);

        // Get a zipper on the list of arcs from start
        ImListZipper<ImArc<KEY, LABEL>> zipper = arcsOut.getOrDefault(start, ImList.empty()).getZipper();

        // Find the arc to after in the list and push the new arc after it
        ImMaybe<ImList<ImArc<KEY, LABEL>>> maybeOut = zipper.find(arcForAfter).map(z -> z.push(arc).close());

        if (!maybeOut.isPresent())
        {
            throw new KeyMissing(start, label, after);
        }
        else
        {
            ImList<ImArc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).push(arc);

            return new ImGraph<>(valueMap, arcsOut.put(start, maybeOut.get()), arcsIn.put(end, in));
        }
    }

    /**
     * <p> Remove the arc with label
     * {@code label}
     *  from
     * {@code start}
     *  to
     * {@code end}
     *
     * If one or more of the nodes do not exist then throw {@link KeyMissing}.
     *
     * If no such arc exists then return the original graph,
     */
    public ImGraph<KEY, DATA, LABEL> removeArc(LABEL label, KEY start, KEY end)
    {
        // System.out.println("removeArc - label " + label + " " + start + " -> " + end);

        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        ImList<ImArc<KEY, LABEL>> out = arcsOut.get(start).filter(a -> !(a.label.equals(label) && a.end.equals(end)));
        ImList<ImArc<KEY, LABEL>> in = arcsIn.get(end).filter(a -> !(a.label.equals(label) && a.start.equals(start)));

        return new ImGraph<>(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    /**
     * <p> Remove the node with key
     * {@code key}
     *
     * If the node is connected to another node then throw {@link NodeHasArcs}.
     *
     */
    public ImGraph<KEY, DATA, LABEL> removeNode(KEY key)
    {
        if (!containsNodeWithKey(key))
            throw new KeyMissing(key);

        var connected = getConnected(In, key).append(getConnected(Out, key));

        if (connected.isNotEmpty())
        {
            throw new NodeHasArcs(key, connected);
        }

        return new ImGraph<>(valueMap.remove(key), arcsOut, arcsIn);
    }

    //
    //    public ImList<KEY> getOut(LABEL label, KEY key)
    //    {
    //        return arcsOut.getOrDefault(key, ImList.empty()).filter(arc -> arc.label.equals(label)).map(arc -> arc.end);
    //    }
    //
    //    public ImList<KEY> getOut(KEY key)
    //    {
    //        return arcsOut.getOrDefault(key, ImList.empty()).map(arc -> arc.end);
    //    }

    private ImMap<KEY, ImList<ImArc<KEY, LABEL>>> getMap(Dir dir)
    {
        return dir == In
               ? arcsIn
               : arcsOut;
    }
    //
    //    public ImList<KEY> getIn(LABEL label, KEY key)
    //    {
    //        return arcsIn.getOrDefault(key, ImList.empty()).filter(arc -> arc.label.equals(label)).map(arc -> arc.start);
    //    }
    //
    //    public ImList<KEY> getIn(KEY key)
    //    {
    //        return arcsIn.getOrDefault(key, ImList.empty()).map(arc -> arc.start);
    //    }

    /**
     * The data value associated with the node with key
     * {@code key}
     */
    public DATA getValue(KEY key)
    {
        return valueMap.get(key);
    }

    /**
     * The nodes that have no incoming arcs.
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     */
    public ImList<KEY> roots()
    {
        return keys().filter(k -> getConnected(In, k).isEmpty());
    }

    /**
     * The nodes that have no outgoing arcs.
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     */
    public ImList<KEY> leaves()
    {
        return keys().filter(k -> getConnected(Out, k).isEmpty());
    }

    /**
     * {@code true}
     * iff any of the nodes in the graph have cycles.
     *
     * A cycle is a path from a node to itself that can be traced by following any arc on a node in the
     * direction
     * {@code Out}
     */
    public boolean hasCycle()
    {
        return roots().any(this::hasCycle);
    }

    private boolean hasCycle(KEY root)
    {
        return hasCycle(ImSet.empty(), root);
    }

    private boolean hasCycle(ImSet<KEY> set, KEY nodeKey)
    {
        //System.out.println(ImList.onAll(set) + " --- " + nodeKey);
        return set.contains(nodeKey)
               ? true
               : getConnected(Out, nodeKey).any(a -> hasCycle(set.add(nodeKey), a));
    }

    /**
     * The text-box representation of the graph in an "ascii-art" form
     *
     * <p> This is an example of a graph with arcs labelled art or mod and its ascii-art diagram
     *
     *
     * <p> </p><img src="{@docRoot}/dev/doc-files/graph-diagrams.png"  width=700/>
     */
    public AbstractTextBox show()
    {
        ImList<ImPair<String, KEY>> pairs = roots().map(r -> ImPair.on("", r));

        return getBoxForPairs(ImPair.on(ImSet.empty(), ImList.on()), pairs).snd;
    }

    /**
     * <p> Showing the text boxes:
     *
     * <pre>{@code
     *        label   key
     * +------/------/--+
     * | |- www -> Bong | <- first line                  getBoxForPairs(...)
     * +----------------+                               /
     * +---------++------------------------------------+
     * | |       || +---------------+                  |
     * | |       || | |- xxx -> Foo |               getBoxForChildren("Foo")
     * | |       || +---------------+              /   |
     * | |       || +---------++------------------+    |
     * | |       || | |       || |- yyy -...      |    |
     * | |       || | |       || |                |    |
     * | |       || | |       || |                |    |
     * | |       || | |       || |                |    |
     * | |       || +---------++------------------+    |
     * | |       || +------------------+               |
     * | |       || | |- yyyyyy -> Bar |               getBoxForChildren("Bar")
     * | |       || +------------------+              /
     * | |       || +------------++------------------+ |
     * | |       || | |          || |- zzz -...      | |
     * | |       || | |          || |                | |
     * | |       || | |          || |                | |
     * | |       || | |          || |                | |
     * | |       || +------------++------------------+ |
     * | |       || +--------------+                   |
     * | |       || | |- x -> Bing |              getBoxForChildren("Bing")
     * | |       || +--------------+             /     |
     * | |       || +-------++------------------+      |
     * | |       || | |     || |- aaa -...      |      |
     * | |       || | |     || |                |      |
     * | |       || | |     || |                |      |
     * | |       || | |     || |                |      |
     * | |       || +-------++------------------+      |
     * +---------++------------------------------------+
     * }</pre>
     *
     */
    ImPair<ImSet<KEY>, AbstractTextBox> getBoxFor(ImSet<KEY> seen, ImPair<String, KEY> labelAndKey)
    {

        KEY key = labelAndKey.snd;
        String label = labelAndKey.fst;

        if (seen.contains(key))
            return ImPair.on(seen, LeafTextBox.with("|- " + label + " -> (" + key + ")"));
        else
        {
            ImSet<KEY> seen2 = seen.add(key);

            // Get the pairs of arc labels and keys and convert the label to a string
            ImList<ImPair<String, KEY>> pairs = getPairs(Out, labelAndKey.snd).map(p -> ImPair.on(p.fst.toString(), p.snd));

            AbstractTextBox firstBox = LeafTextBox.with("|-" + getLabelToDisplay(labelAndKey.fst) + "-> " + labelAndKey.snd);

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

                ImPair<ImSet<KEY>, AbstractTextBox> boxForPairs = getBoxForPairs(start, pairs);

                // Create the left hand box with the vertical line
                String s = "|" + TextUtils.repeat(" ", getLabelToDisplay(labelAndKey.fst).length() + 4);

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
        ImPair<ImSet<KEY>, AbstractTextBox> setAndBox = getBoxFor(z.fst, labelAndKey);
        return ImPair.on(setAndBox.fst, z.snd.push(setAndBox.snd));
    }

    /**
     * The graph that has the same nodes and arcs as the original but with each
     * data value for each node transformed by
     * {@code fn}
     */
    public <NEWDATA> ImGraph<KEY, NEWDATA, LABEL> map(Fn<DATA, NEWDATA> fn)
    {
        return new ImGraph<>(valueMap.map(fn), arcsOut, arcsIn);
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

        ImList<String> nodes = keys().flatMap(this::getGraphVizChunk);

        return TextUtils.join(ImList.join(head, nodes, tail), "\n");
    }

    /**
     *
     * A list of all the keys in the graph
     */
    public ImList<KEY> keys()
    {
        return valueMap.keys();
    }

    /**
     *
     * A list of all the data values in the graph
     */
    public ImList<DATA> values()
    {
        return valueMap.values();
    }

    private ImList<String> getGraphVizChunk(KEY nodeKey)
    {
        ImList<ImPair<LABEL, KEY>> out = getPairs(Out, nodeKey);

        return out.isEmpty()
               ? ImList.on(TextUtils.quote(nodeKey) + ";")
               : out.map(p -> TextUtils.quote(nodeKey) + " -> " + TextUtils.quote(p.snd) +
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
        return getArcsMap(dir).getOrDefault(key, ImList.empty()).map(arc -> ImPair.on(arc.label, arc.getSlot(dir)));
    }

    private ImMap<KEY, ImList<ImArc<KEY, LABEL>>> getArcsMap(Dir dir)
    {
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
     * <p> The list will not include the key
     * {@code key}
     * unless that node is in a cycle - in which case it will contain it
     *
     */
    public ImList<KEY> getClosure(Dir dir, LABEL label, KEY key)
    {
        return getInclusiveClosure(dir, label, getConnected(dir, label, key));
    }

    /**
     * <p> Get the closure of
     * {@code keys}
     *  including each element of
     * {@code keys}
     *  in the direction
     * {@code dir}
     *  chasing the arcs with label
     * {@code label}
     *
     */
    public ImList<KEY> getInclusiveClosure(Dir dir, LABEL label, ImList<KEY> keys)
    {
        return getInclusiveClosure(dir, label, keys, ImSet.empty());
    }

    private ImList<KEY> getInclusiveClosure(Dir dir, LABEL label, ImList<KEY> ks, ImSet<KEY> found)
    {
        return ks.isEmpty()
               ? found.toImList()
               : found.contains(ks.head())
                 ? getInclusiveClosure(dir, label, ks.tail(), found)
                 : getInclusiveClosure(dir, label, ks.tail().append(getConnected(dir, label, ks.head())), found.add(ks.head()));
    }

    /**
     * <p> Get the keys that are connected to
     * {@code key}
     *  by arcs in the direction
     * {@code dir}
     *  that have the label
     * {@code label}
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     *
     */
    public ImList<KEY> getConnected(Dir dir, LABEL label, KEY key)
    {
        // System.out.println("get " + dir + " label " + label + " key " + key);
        return getMap(dir).getOrDefault(key, ImList.empty()).filter(arc -> arc.label.equals(label)).map(arc -> arc.getSlot(dir));
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
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     *
     * <p> The list will not include the key
     * {@code key}
     * unless that node is in a cycle - in which case it will contain it
     *
     */
    public ImList<KEY> getClosure(Dir dir, ImSet<LABEL> labels, KEY key)
    {
        return getInclusiveClosure(dir, labels, getConnected(dir, labels, key), ImSet.empty());
    }

    private ImList<KEY> getInclusiveClosure(Dir dir, ImSet<LABEL> labels, ImList<KEY> ks, ImSet<KEY> found)
    {
        return ks.isEmpty()
               ? found.toImList()
               : found.contains(ks.head())
                 ? getInclusiveClosure(dir, labels, ks.tail(), found)
                 : getInclusiveClosure(dir, labels, ks.tail().append(getConnected(dir, labels, ks.head())), found.add(ks.head()));
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
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
     */
    public ImList<KEY> getConnected(Dir dir, ImSet<LABEL> labels, KEY key)
    {
        // System.out.println("getConnected " + dir + " labels " + labels + " key " + key);
        return getMap(dir).getOrDefault(key, ImList.empty()).filter(arc -> labels.contains(arc.label)).map(arc -> arc.getSlot(dir));
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
        ImList<KEY> neighbours = getConnected(dir, labels, key);

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
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>.
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
    public ImList<KEY> getClosure(Dir dir, KEY key)
    {
        return getInclusiveClosure(dir, getConnected(dir, key), ImSet.empty());
    }

    private ImList<KEY> getInclusiveClosure(Dir dir, ImList<KEY> ks, ImSet<KEY> found)
    {
        return ks.isEmpty()
               ? found.toImList()
               : found.contains(ks.head())
                 ? getInclusiveClosure(dir, ks.tail(), found)
                 : getInclusiveClosure(dir, ks.tail().append(getConnected(dir, ks.head())), found.add(ks.head()));
    }

    /**
     * <p> Get the nodes that are connected to the node with key
     * {@code key}
     *  by any arcs in the direction
     * {@code dir}
     *
     * <p> Returns a <em>set</em> of keys - although represented as a <em>list</em>..
     */
    public ImList<KEY> getConnected(Dir dir, KEY key)
    {
        return getMap(dir).getOrDefault(key, ImList.empty()).map(arc -> arc.getSlot(dir));
    }

    //------------------------------------------------------------------------------------

    /**
     * <p> Get the inclusive in-order (AKA depth-last) closure of
     * {@code key}
     *  in the direction
     * {@code dir}
     *  chasing the arcs with any label
     * <p> let l =  g.getInOrderClosure(d, k)
     * <p> in order means this:
     *
     * <pre>{@code
     * for all a, b in list, rank a < rank b => there is no path from b to a in direction `dir`
     * }</pre>
     * <p> This function is
     * {@code O(n^2)}
     *  I think so ...er... beware
     *
     */
    public ImList<KEY> getInOrderClosure(Dir dir, ImSet<LABEL> labels, ImList<KEY> keys)
    {
        return getInOrderClosure(dir, labels, keys, ImList.on());
    }

    public ImList<KEY> getInOrderClosureOnSingleKey(Dir dir, ImSet<LABEL> labels, KEY key)
    {
        return getInOrderClosure(dir, labels, ImList.on(key));
    }

    private ImList<KEY> getInOrderClosure(Dir dir, ImSet<LABEL> labels, ImList<KEY> keys, ImList<KEY> found)
    {
        return keys.foldl(found, (f, n) -> getInOrderClosureOnSingleKey(dir, labels, n, f));
    }

    private ImList<KEY> getInOrderClosureOnSingleKey(Dir dir, ImSet<LABEL> labels, KEY key, ImList<KEY> found)
    {
        return found.contains(key)
               ? found
               : getInOrderClosure(dir, labels, getConnected(dir, labels, key), found).push(key);
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

    public boolean eq(ImGraph<KEY, DATA, LABEL> other)
    {

        return
                Equals.isEqual(this.keys().toSet(), other.keys().toSet()) &&
                        ImList.and(keys().map(k -> Equals.isEqual(this.getConnected(Out, k).toSet(), other.getConnected(Out, k).toSet())));

    }
}