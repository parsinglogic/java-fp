/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tree;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.eq.Eq;
import dev.javafp.ex.InvalidState;
import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.lst.ImList;
import dev.javafp.util.Caster;
import dev.javafp.util.TextUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * <p> An immutable version of a Rose Tree (Multi-way Tree) - a tree in which each node has a
 * <em>value</em>
 *  and
 * an arbitrary number of
 * <em>sub-trees</em>
 * .
 * <p> Note that RoseTrees are not the same as B-Trees.
 * <h2>Introduction</h2>
 *
 * </p>
 * <p> A rose tree has a root node containing an element and an ordered list  of sub-trees.
 * The definition is recursive - each sub-tree
 * also has an element and a list of
 * sub-trees and so on. Leaf trees are those with no sub-trees - ie the list is empty.
 * <p> For example, the tree with element
 * {@code a}
 *  and three sub nodes:
 * <ol>
 * <li>
 * <p> The leaf node
 * {@code b}
 * </li>
 * <li>
 * <p> The tree with element
 * {@code c}
 *  and three leaf nodes
 * {@code e}
 * ,
 * {@code f}
 * ,
 * {@code g}
 * </li>
 * <li>
 * <p> The tree with element
 * {@code d}
 *  and the sub tree of the leaf node
 * {@code h}
 * </li>
 * </ol>
 * <p> <img src="{@docRoot}/dev/doc-files/rose-tree-a.png" alt="rose-tree-a"  width="200" />
 * </p>
 * <p> We could also represent it like this:
 * <p> <img src="{@docRoot}/dev/doc-files/rose-tree-b.png" alt="rose-tree-b"   width="200"/>
 * </p>
 * <p> or like this:
 *
 * <pre>{@code
 * a
 * .........
 * b c     d
 *   ..... .
 *   e f g h
 * }</pre>
 * <p> This text representation is an example of the what {@link #toBoxString()} produces.
 * <h2>Creation</h2>
 * <ul>
 * </ul>
 * <h2>Query</h2>
 * <ul>
 * <li>
 * <p> {@link #contains(A)}
 * </li>
 * <li>
 * <p> {@link #getElement()}
 * </li>
 * <li>
 * <p> {@link #getSubTrees()}
 * </li>
 * <li>
 * <p> {@link #getNodeAtIndex(int)}
 * </li>
 * <li>
 * <p> {@link #size()}
 * </li>
 * </ul>
 * <h2>Mutation</h2>
 * <ul>
 * <li>
 * <p> {@link #replaceElement(A)}
 * </li>
 * <li>
 * <p> {@link #map(Fn)}
 * </li>
 * </ul>
 * <h2>Iteration</h2>
 * <ul>
 * <li>
 * <p> {@link #iterator()}
 * </li>
 * <li>
 * <p> {@link #getZipper()}
 * </li>
 * <li>
 * <p> {@link #getZipperIterator()}
 * </li>
 * </ul>
 * <h2>String representation</h2>
 * <ul>
 * <li>
 * <p> {@link #toString()}
 * </li>
 * <li>
 * <p> {@link #toBoxString()}
 * </li>
 * </ul>
 * <h2>Implementation</h2>
 * <p> We represent the children of a node by a (functional) list.
 * <h2>Notation</h2>
 * <p> In this library our convention is to call a
 * <em>binary</em>
 *  tree a
 * <em>Tree</em>
 *  and a
 * <em>multi-way</em>
 *  tree a
 * <em>Rose Tree</em>
 * .
 * <p> In the examples below we are assuming that
 * {@code ImRoseTree}
 *  has been statically imported:
 *
 * <pre>{@code
 * import static im.ImRoseTree.*;
 * }</pre>
 * <p> which means that we can use (eg)Ï
 *
 * <pre>{@code
 * withNodes(1, leaf(2), leaf(3))
 * }</pre>
 * <p> instead of
 *
 * <pre>{@code
 * ImRoseTree.withNodes(1, ImRoseTree.leaf(2), ImRoseTree.leaf(3))
 * }</pre>
 * @see ImRoseTreeZipper
 * @see ImRoseTreeIterator
 * @see ImRoseTreeZipperIterator
 *
 */
public class ImRoseTree<A> implements Iterable<A>, Serializable
{
    // The element at this node
    final private A element;

    // The children of this element
    final private ImList<ImRoseTree<A>> children;

    final private int size;

    private ImRoseTree(A element, ImList<ImRoseTree<A>> children)
    {
        this.element = element;
        this.children = children;

        size = children.foldl(1, (z, l) -> z + l.size);
    }

    private ImRoseTree()
    {
        this.element = null;
        this.children = ImList.empty();
        size = 0;
    }

    /**
     * <p> A rose tree with element
     * {@code element}
     *  and sub-trees that are
     * {@code nodes}
     * .
     *
     */
    public static <A> ImRoseTree<A> withNodes(A element, ImList<ImRoseTree<A>> nodes)
    {
        return new ImRoseTree<A>(element, nodes);
    }

    /**
     * <p> A rose tree with element
     * {@code element}
     *  and sub-trees that are
     * {@code nodes}
     * .
     * <p> Examples:
     *
     * <pre>{@code
     * withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7))
     * }</pre>
     * <p> produces the tree
     *
     * <pre>{@code
     * 1
     * .........
     * 2     6 7
     * .....
     * 3 4 5
     * }</pre>
     *
     */
    @SafeVarargs
    public static <A> ImRoseTree<A> withNodes(A element, final ImRoseTree<A>... nodes)
    {
        return new ImRoseTree<A>(element, ImList.on(nodes));
    }

    /**
     * <p> A rose tree with element
     * {@code element}
     *  and sub-trees that are all leaves, with elements
     * {@code elements}
     * .
     *
     */
    public static <A> ImRoseTree<A> withElements(A element, final ImList<A> elements)
    {
        return new ImRoseTree<A>(element, elements.map(i -> leaf(i)));
    }

    /**
     * <p> A rose tree with parent
     * {@code parent}
     *  and sub-trees that are all leaves, with children
     * {@code children}
     * .
     * <p> Examples:
     *
     * <pre>{@code
     * withElements("c", "e", "f", "g");
     * }</pre>
     * <p> produces the tree:
     *
     * <pre>{@code
     * c
     * .....
     * e f g
     * }</pre>
     *
     */
    @SafeVarargs
    public static <A> ImRoseTree<A> withElements(A parent, final A... children)
    {
        return withElements(parent, ImList.on(children));
    }

    /**
     * <p> A rose tree with element
     * {@code element}
     *  and no sub-trees.
     *
     */
    public static <A> ImRoseTree<A> leaf(A element)
    {
        return withNodes(element, ImList.on());
    }

    /**
     * <p> The number of elements/nodes in
     * {@code this}
     * .
     *
     */
    public int size()
    {
        return size;
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains
     * {@code elementToLookFor}
     * .
     * <p> More formally, returns
     * {@code true}
     *  if and only if
     * {@code this.toArray()}
     *  contains
     * at least one element
     * {@code e}
     *  such that:
     *
     * <pre>{@code
     * e.equals(elementToLookFor)
     * }</pre>
     * <h4>Examples</h4>
     * <p> This code:
     *
     * <pre>{@code
     * ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c", withElements("d", "h")));
     * }</pre>
     * <p> produces this tree:
     *
     * <pre>{@code
     *     a
     *     ...
     *     b c
     *       ...
     *       d h
     * }</pre>
     * <p> and then:
     *
     * <pre>{@code
     * t.contains("d")  =>  true
     * }</pre>
     *
     */
    public boolean contains(A elementToLookFor)
    {
        for (A element : this)
        {
            if (elementToLookFor.equals(element))
                return true;
        }

        return false;
    }

    /**
     * <p> An iterator over the elements in
     * {@code this}
     *  in
     * <em>pre-order</em>
     *  sequence.
     * <p> The
     * <em>pre-order</em>
     *  sequence of a tree is
     * the root element followed by the pre-order sequence of the first child tree, followed by the
     * pre-order sequence of the second child tree and so on.
     * <p> Note that the iterator returned will throw an
     * {@code UnsupportedOperationException}
     *  in response to
     * {@code remove()}
     * .
     * <p> The pre-order sequence of this tree:
     *
     * <pre>{@code
     * a
     * .........
     * b c     d
     *   ..... .
     *   e f g h
     * }</pre>
     * <p> is
     *
     * <pre>{@code
     * a b c e f g d h
     * }</pre>
     *
     */
    public Iterator<A> iterator()
    {
        return new ImRoseTreeIterator<A>(this);
    }

    /**
     * <p> A string representation of this rose tree.
     * <p> The root element is shown first. If there are any child trees then it is followed by
     * {@code (}
     *  then the "toString" of each of the child trees in turn
     * followed by
     * {@code )}
     * .
     * <h4>Examples</h4>
     *
     * <pre>{@code
     * withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toString()  => "1 (2 (3 4 5) 6 7)"
     * }</pre>
     *
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(element);

        if (children.size() > 0)
        {
            sb.append(" (");
            sb.append(TextUtils.join(children, " "));
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * <p> The representation of
     * {@code this}
     * as an {@link AbstractTextBox}
     * <p> If the class extends {@link dev.javafp.val.ImValuesImpl} then the default
     * {@code toString}
     *  method will use this method
     * and then convert the result to a
     * {@code String}
     *
     */
    public AbstractTextBox getTextBox()
    {
        // Get the leaf box for this node

        // If I have any children
        // Get the leaf box for each child
        // stack them top down
        // indent them
        // stack the leaf box and these top down

        return children.isEmpty()
               ? LeafTextBox.with("" + element)
               : LeafTextBox.with("" + element).above(TopDownBox.withAllBoxes(children.map(i -> i.getTextBox())).indentBy(2));
    }

    /**
     * An ImList containing ImRoseTrees in order
     *
     *
     * @return
     */
    public ImList<ImRoseTree<A>> toImList()
    {
        return getZipper().getList(); //ImFIteratorList.on(getZipper());
    }

    /**
     * <p> The element at this node.
     */
    public A getElement()
    {
        return element;
    }

    /**
     * <p> The list  of sub-trees of this tree node.
     * <p> Note that these are sub-trees of type
     * {@code ImRoseTree<A>}
     *  not elements of type
     * {@code A}
     * .
     * <h4>Examples</h4>
     *
     * <pre>{@code
     * ImRoseTree<String> t = withElements("c", "e", "f", "g");
     *
     * t.getSubTrees().size()                       =>  3
     * t.getSubTrees().get(1).getClass().getName()  =>  "im.ImRoseTree"
     * }</pre>
     * <p> Note that, if you want to mutate this list, you might be tempted to get a zipper on it like this:
     *
     * <pre>{@code
     * z = t.getSubTrees().getZipper()    // Don't use this to try mutate the sub-trees
     * }</pre>
     * <p> and then mutate it. This won't work - because, although you will be able to generate a new
     * mutated list of sub-trees, the parent node won't know about it.
     * <p> To mutate a tree you can use a {@link ImRoseTreeZipper}
     *
     */
    public ImList<ImRoseTree<A>> getSubTrees()
    {
        return children;
    }

    /**
     * <p> A
     * <em>zipper</em>
     *  on this rose tree.
     *
     */
    public ImRoseTreeZipper<A> getZipper()
    {
        return new ImRoseTreeZipper<>(this);
    }

    /**
     * <p> An ascii-art diagram of the tree.
     * <h4>Examples</h4>
     *
     * <pre>{@code
     * System.err.println(withNodes(1, withElements(2, 3, 4, 5), leaf(6), leaf(7)).toBoxString());
     * }</pre>
     * <p> gives:
     *
     * <pre>{@code
     * 1
     * .........
     * 2     6 7
     * .....
     * 3 4 5
     * }</pre>
     *
     */
    public String toBoxString()
    {
        return toBox().toString();
    }

    /**
     * <p> The node at index
     * {@code indexStartingAtOne}
     *  in the pre-order sequence.
     * <p> See {@link #iterator()} for a description of the pre-order sequence.
     * <h4>Examples:</h4>
     * <p> Given this code:
     *
     * <pre>{@code
     * ImRoseTree<String> t = withNodes("a", leaf("b"), withElements("c", "e", "f", "g"), withElements("d", "h"));
     * }</pre>
     * <p> which constructs this tree:
     *
     * <pre>{@code
     * a
     * .........
     * b c     d
     *   ..... .
     *   e f g h
     * }</pre>
     * <p> then
     *
     * <pre>{@code
     *  t.getNodeAtIndex(5).getElement()  => "f"
     * }</pre>
     * <p> The corresponding indexes are shown below:
     *
     * <pre>{@code
     * 1
     * .........
     * 2 3     7
     *   ..... .
     *   4 5 6 8
     * }</pre>
     *
     */
    public ImRoseTree<A> getNodeAtIndex(final int indexStartingAtOne)
    {
        Throw.Exception.ifIndexOutOfBounds("indexStartingAtOne", indexStartingAtOne, "this", size());

        if (indexStartingAtOne == 1)
        {
            return this;
        }

        int count = 1;
        for (ImRoseTree<A> node : children)
        {
            if (indexStartingAtOne <= count + node.size())
            {
                return node.getNodeAtIndex(indexStartingAtOne - count);
            }

            count += node.size();
        }

        throw new InvalidState("Can't get here");
    }

    public AbstractTextBox toBox()
    {
        AbstractTextBox myTopBox = LeafTextBox.with("" + element);

        if (getSubTrees().isEmpty())
            return myTopBox;

        LinkedList<AbstractTextBox> boxes = new LinkedList<AbstractTextBox>();

        for (ImRoseTree<A> t : getSubTrees())
        {
            boxes.add(t.toBox());
        }

        AbstractTextBox space = LeafTextBox.with(" ");

        // generateListsByInjecting the child boxes with spaces
        ImList<AbstractTextBox> bs = ImList.onList(boxes);

        LeftRightBox childBoxes = LeftRightBox.withAll(bs.intersperse(space));

        /**
         * <p> Calculate the line of dots underneath the top box.
         * <p> If we make the width of the dots the width of all my child boxes it will look like this: (eg)
         * 1
         * .....
         * 2 3
         * ...
         * 4 5
         * <p> We reduce the width to only extend as far as the end of the top box of the last child. It will
         * now look like this:
         * <p> 1
         * ...
         * 2 3
         * ...
         * 4 5
         *
         */
        int rawWidth = childBoxes.width;

        ImRoseTree<A> lastChild = getSubTrees().at(getSubTrees().size());

        int reducedWidth = rawWidth - (boxes.getLast().width - ("" + lastChild.element).length());

        final AbstractTextBox dots = LeafTextBox.with(".".repeat(reducedWidth));

        return TopDownBox.with(myTopBox, dots, childBoxes);
    }

    /**
     * <p> The rose tree that has the same sub-trees as
     * {@code this}
     *  but has
     * {@code newElement}
     *  as its element.
     * <p> If
     * {@code newElement == element}
     *  then return
     * {@code this}
     *
     */
    public ImRoseTree<A> replaceElement(A newElement)
    {
        return newElement == element
               ? this
               : withNodes(newElement, children);
    }

    /**
     * <p> An iterator on a zipper on this rose tree.
     */
    public Iterator<ImRoseTreeZipper<A>> getZipperIterator()
    {
        return getZipper().iterator();
    }

    /**
     * <p> The rose tree that has the same shape as this and where the i-th element is the
     * result of evaluating the single argument function
     * {@code fn}
     *  on
     * the i-th element of
     * {@code this}
     * .
     * <h4>Examples</h4>
     *
     * <pre>{@code
     * ImRoseTree<String> t = withElements("c", "e", "f", "g");
     *
     * Function1<String> toUpperFn = FnFactory.on(String.class).getFn(String.class, "toUpperCase");
     *
     * t.map(toUpperFn)  =>  C (E F G)
     * }</pre>
     *
     */
    public <O> ImRoseTree<O> map(Fn<A, O> fn)
    {
        return mapS(fn, this);
    }

    private static <T, O> ImRoseTree<O> mapS(Fn<T, O> f, ImRoseTree<T> tree)
    {

        /**
         * <p> We have a function, fn
         * <p> fn :: A -> O
         * <p> We have a rose tree with an element of type A at the root and a list of rose trees that are the children.
         * <p> If only we had a function that operated on a rose tree and applied the function fn to each element of it.
         * If we had that function we could map it over the children.
         * <p> Wait - that is the very function we are in now. We just have to partially apply it to f
         *
         */
        Fn2<Fn<T, O>, ImRoseTree<T>, ImRoseTree<O>> mapFunction = ImRoseTree::mapS;
        return new ImRoseTree<O>(f.of(tree.element), tree.children.map(mapFunction.ofFirst(f)));
    }

    /**
     * <p> Compares
     * {@code another}
     *  with
     * {@code this}
     *  for equality.  Returns
     * {@code true}
     *  if and only if the specified object is also a
     * {@code rose tree}
     * , both
     * trees have the same size, and all corresponding pairs of elements in
     * the two trees are
     * <em>equal</em>
     * .
     * <p> {@code e1}
     *  and
     * {@code e2}
     *  being
     * <em>equal</em>
     *  means
     *
     * <pre>{@code
     * e1.equals(e2) == true
     * }</pre>
     *
     */
    @Override
    public boolean equals(Object other)
    {
        return this == other
               ? true
               : other == null
                 ? false
                 : other instanceof ImRoseTree
                   ? safeEquals(Caster.cast(other))
                   : false;
    }

    private boolean safeEquals(ImRoseTree<A> two)
    {
        return size != two.size()
               ? false
               : element.equals(two.element) && Eq.uals(children, two.children);
    }

}
