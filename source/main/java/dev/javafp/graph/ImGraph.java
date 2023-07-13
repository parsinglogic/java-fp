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
import dev.javafp.ex.AfterKeyMissing;
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

/**
 * <p> A directed labelled graph. It can have cycles.
 * <p> Not all the nodes need to be connected.
 * <p> Nodes are identified by keys of type {@code KEY}
 * <p> Each node has an object of type {@code DATA} associated with it
 * <p> Each node can be connected to 1 or more other nodes via labelled arcs
 * <p> If node a is connected to node b via an arc labeled c
 * then we store the arc c in arcsOut and in arcsIn
 * <p> Graphs are immutable - each time you add a node or an arc between two nodes, a new graph is created.
 * <p> The show method returns a text representation of the graph in the form of an ascii art diagram.
 *
 * <p> This is an example of a graph with arcs labelled art or mod and its ascii-art diagram
 *
 *
 * <p> </p><img src="{@docRoot}/com/drum/server/utils/im/doc-files/graph-diagrams.png"  width=700/>
 *
 *
 *
 *
 *
 *
 *
 *
 */
public class ImGraph<KEY, DATA, LABEL> extends ImValuesImpl
{

    private ImMap<KEY, DATA> valueMap;
    private ImMap<KEY, ImList<Arc<KEY, LABEL>>> arcsOut;
    private ImMap<KEY, ImList<Arc<KEY, LABEL>>> arcsIn;

    private static ImGraph empty = new ImGraph<>(ImMap.empty(), ImMap.empty(), ImMap.empty());

    public enum Dir
    {
        In, Out
    }

    public static Dir In = Dir.In;
    public static Dir Out = Dir.Out;

    protected ImGraph(ImMap<KEY, DATA> valueMap, ImMap<KEY, ImList<Arc<KEY, LABEL>>> arcsOut, ImMap<KEY, ImList<Arc<KEY, LABEL>>> arcsIn)
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

        ImMap<KEY, ImList<Arc<KEY, LABEL>>> arcsIn = this.arcsIn.removeAll(otherKeys).map(v -> v.filter(a -> !otherKeysSet.contains(a.start)));

        return new ImGraph<>(valueMap.removeAll(otherKeys), arcsOut.removeAll(otherKeys), arcsIn);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(valueMap, arcsOut, arcsIn);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("valueMap", "arcsOut", "arcsIn");
    }

    public ImGraph<KEY, DATA, LABEL> addNode(KEY key, DATA value)
    {
        if (key == null)
            throw new KeyIsNull(key);
        else if (containsNodeWithKey(key))
            throw new KeyExists(key);
        else
            return new ImGraph<>(valueMap.put(key, value), arcsOut, arcsIn);
    }

    public ImGraph<KEY, DATA, LABEL> addNodeToParent(LABEL arcLabel, KEY parentKey, KEY childKey, DATA childValue)
    {
        return addNodeIfMissing(childKey, childValue).addArc(arcLabel, parentKey, childKey);
    }

    public ImGraph<KEY, DATA, LABEL> addNodeIfMissing(KEY key, DATA value)
    {
        return containsNodeWithKey(key)
               ? this
               : addNode(key, value);
    }

    public static <KEY, DATA, LABEL> ImGraph<KEY, DATA, LABEL> empty()
    {
        return Caster.cast(empty);
    }

    public boolean containsNodeWithKey(KEY key)
    {
        return valueMap.get(key) != null;
    }

    public ImGraph<KEY, DATA, LABEL> addArc(LABEL label, KEY start, KEY end)
    {
        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        Arc<KEY, LABEL> arc = Arc.on(label, start, end);

        ImList<Arc<KEY, LABEL>> out = arcsOut.getOrDefault(start, ImList.empty()).push(arc);
        ImList<Arc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).push(arc);

        return new ImGraph<>(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    public ImGraph<KEY, DATA, LABEL> addArcAsLast(LABEL label, KEY start, KEY end)
    {
        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        Arc<KEY, LABEL> arc = Arc.on(label, start, end);

        ImList<Arc<KEY, LABEL>> out = arcsOut.getOrDefault(start, ImList.empty()).appendElement(arc);
        ImList<Arc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).appendElement(arc);

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
        Arc<KEY, LABEL> arcForAfter = Arc.on(label, start, after);
        Arc<KEY, LABEL> arc = Arc.on(label, start, end);

        // Get a zipper on the list of arcs from start
        ImListZipper<Arc<KEY, LABEL>> zipper = arcsOut.getOrDefault(start, ImList.empty()).getZipper();

        // Find the arc to after in the list and push the new arc after it
        ImMaybe<ImList<Arc<KEY, LABEL>>> maybeOut = zipper.find(arcForAfter).map(z -> z.push(arc).close());

        if (!maybeOut.isPresent())
        {
            throw new AfterKeyMissing(start, label, after);
        }
        else
        {
            ImList<Arc<KEY, LABEL>> in = arcsIn.getOrDefault(end, ImList.empty()).push(arc);

            return new ImGraph<>(valueMap, arcsOut.put(start, maybeOut.get()), arcsIn.put(end, in));
        }
    }

    public ImGraph<KEY, DATA, LABEL> removeArc(LABEL label, KEY start, KEY end)
    {
        // System.out.println("removeArc - label " + label + " " + start + " -> " + end);

        if (!containsNodeWithKey(start))
            throw new KeyMissing(start);

        if (!containsNodeWithKey(end))
            throw new KeyMissing(end);

        ImList<Arc<KEY, LABEL>> out = arcsOut.get(start).filter(a -> !(a.label.equals(label) && a.end.equals(end)));
        ImList<Arc<KEY, LABEL>> in = arcsIn.get(end).filter(a -> !(a.label.equals(label) && a.start.equals(start)));

        return new ImGraph<>(valueMap, arcsOut.put(start, out), arcsIn.put(end, in));
    }

    public ImGraph<KEY, DATA, LABEL> removeNode(KEY key)
    {
        if (!containsNodeWithKey(key))
            throw new KeyMissing(key);

        var connected = getConnected(Dir.In, key).append(getConnected(Dir.Out, key));

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

    private ImMap<KEY, ImList<Arc<KEY, LABEL>>> getMap(Dir dir)
    {
        return dir == Dir.In
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

    public DATA getValue(KEY key)
    {
        return valueMap.get(key);
    }

    public ImList<KEY> roots()
    {
        return keys().filter(k -> getConnected(In, k).isEmpty());
    }

    public ImList<KEY> leaves()
    {
        return keys().filter(k -> getConnected(Out, k).isEmpty());
    }

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

    //    /**
    //     * Get the text box for a list of nodes (and the set of already seen nodes)
    //     */
    //    public ImPair<ImSet<KEY>, AbstractTextBox> getTree(ImSet<KEY> set, ImList<KEY> nodes)
    //    {
    //        // Each time we show a node and all its children we need to update the set of already seen nodes
    //        ImPair<ImSet<KEY>, ImList<AbstractTextBox>> p = nodes.foldl(ImPair.on(set, ImList.on()), (z, a) -> show2(z, a));
    //
    //        return ImPair.on(p.fst, TopDownBox.withAllBoxes(p.snd.reverse()));
    //    }
    //
    //    /**
    //     * Given a set of already seen nodes and a list of text boxes, get the text box for a node, add it to
    //     * the list of text boxes (and add to the set of already seen nodes)
    //     */
    //    private ImPair<ImSet<KEY>, ImList<AbstractTextBox>> show2(ImPair<ImSet<KEY>, ImList<AbstractTextBox>> z, KEY node)
    //    {
    //        return show(z.fst, node).map((i, j) -> ImPair.on(i, z.snd.push(j)));
    //
    //        //return ImPair.on(p.fst, z.snd.push(p.snd));
    //    }
    //
    //    /**
    //     * Get a text  box for node given that we have already displayed the nodes in `set`
    //     */
    //    public ImPair<ImSet<KEY>, AbstractTextBox> show(ImSet<KEY> set, ImPair<LABEL, KEY> labelKeyPair)
    //    {
    //        if (set.contains(node))
    //        {
    //            return ImPair.on(set, LeafTextBox.with("(" + node + ")"));
    //        }
    //        else
    //        {
    //            ImSet<KEY> set2 = set.adding(node);
    //            LeafTextBox box = LeafTextBox.with("" + node);
    //            ImList<KEY> kids = getConnected(Out, node);
    //
    //            return kids.isEmpty()
    //                    ? ImPair.on(set2, box)
    //                    : getTree(set2, kids).map((s, b) -> ImPair.on(s, box.above(b.indentBy(3))));
    //        }
    //    }

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

    public <NEWDATA> ImGraph<KEY, NEWDATA, LABEL> map(Fn<DATA, NEWDATA> fn)
    {
        return new ImGraph<>(valueMap.map(fn), arcsOut, arcsIn);
    }

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

    public ImList<KEY> keys()
    {
        return valueMap.keys();
    }

    public ImList<DATA> values()
    {
        return valueMap.values();
    }

    private ImList<String> getGraphVizChunk(KEY nodeKey)
    {
        ImList<ImPair<LABEL, KEY>> out = getPairs(Dir.Out, nodeKey);

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

    private ImMap<KEY, ImList<Arc<KEY, LABEL>>> getArcsMap(Dir dir)
    {
        return dir == Out
               ? arcsOut
               : arcsIn;
    }

    //------------------------------------------------------------------------------------

    /**
     * <p> Get the closure of
     * {@code key}
     *  not including
     * {@code key}
     *  in the direction
     * {@code dir}
     *  chasing the arcs with label
     * {@code label}
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
     */
    public ImList<KEY> getConnected(Dir dir, LABEL label, KEY key)
    {
        // System.out.println("get " + dir + " label " + label + " key " + key);
        return getMap(dir).getOrDefault(key, ImList.empty()).filter(arc -> arc.label.equals(label)).map(arc -> arc.getSlot(dir));
    }

    //------------------------------------------------------------------------------------

    /**
     * <p> Get the closure of
     * {@code key}
     *  not including
     * {@code key}
     *  in the direction
     * {@code dir}
     *  chasing the arcs with labels in the set
     * {@code labels}
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
     * <p> Get the closure of
     * {@code key}
     *  (not including
     * {@code key}
     * ) in the direction
     * {@code dir}
     *  chasing the arcs with any label
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
     * <p> Get the keys that are connected to
     * {@code key}
     *  by arcs in the direction
     * {@code dir}
     *  - whatever labels they have
     *
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

    public ImList<DATA> getValuesFromKeys(ImList<KEY> keys)
    {
        return keys.map(this::getValue);
    }
}