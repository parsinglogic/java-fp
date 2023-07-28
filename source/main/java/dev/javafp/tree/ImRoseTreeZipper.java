/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tree;

import dev.javafp.eq.Equals;
import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.lst.FIterator;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImListZipper;
import dev.javafp.shelf.ImShelf;
import dev.javafp.util.Caster;
import dev.javafp.util.TextUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> A zipper on an {@link ImRoseTree} lets you navigate back and forth in the tree and "mutate" it as you go.
 * <h2>Introduction</h2>
 * <p> In general, zippers on immutable data structures allow you to move around the data structure, making local
 * modifications efficiently and only making the complete new structure when you use {@link #close()}.
 * <p> Recall that a rose tree has a root node containing an element and a list of sub-trees.
 * The definition is recursive - each sub-tree
 * also has an element and a list of
 * sub-trees and so on. Leaf trees are those with no sub-trees - ie their list is empty.
 *
 * <pre>{@code
 * data RoseTree = RoseTree a [RoseTree a]
 * }</pre>
 * <p> A zipper on a rose tree with focus at node
 * {@code n}
 *  is recursively defined as:
 * <ol>
 * <li>
 * <p> a list zipper on
 * {@code n}
 * 's parent sub-trees, focussed on
 * {@code n}
 * </li>
 * <li>
 * <p> a reference to the rose tree zipper for
 * {@code n}
 * 's parent.
 * </li>
 * </ol>
 * <p> The first list zipper as defined above is called the
 * <em>current</em>
 *  list zipper.
 * <p> In this tree:
 *
 * <pre>{@code
 * 1
 * .......
 * 2 3   6
 *   ...
 *   4 5
 * }</pre>
 * <p> a zipper on the node with element 5 would be:
 * <ol>
 * <li>
 * <p> a list zipper on list [4, 5] with the focus on 5 and a reference its parent which is
 * </li>
 * <li>
 * <p> a list zipper on list [2, 3, 6] with the focus on 3 and a reference its parent which is
 * </li>
 * <li>
 * <p> a list zipper on list [1] with the focus on 1 and no parent
 * </li>
 * </ol>
 * <p> In this example the current list zipper is 1. above.
 * <p> (The list zipper on the root is really an implementation detail - we consider the root tree to be in a list of size 1)
 * <p> The {@link #toString()} method would show it like this:
 *
 * <pre>{@code
 * [1] [] <- [2, 3] [6] <- [4] [5]
 * }</pre>
 * <h3>Example</h3>
 * <p> So to mutate node 4 in the example above you could do this:
 *
 * <pre>{@code
 * tree.getZipper().next().next().next().setElement(7).close()
 * }</pre>
 * <p> Which would generate the tree:
 *
 * <pre>{@code
 * 1
 * .......
 * 2 3   6
 *   ...
 *   7 5
 * }</pre>
 * <h2>Navigation</h2>
 * <p> The API to the rose tree zipper is somewhat similar to {@link ImListZipper} but with some differences:
 * <ol>
 * <li>
 * <p> These methods:
 * </li>
 * </ol>
 * <ul>
 * <li>
 * <p> {@link #next()}
 * </li>
 * <li>
 * <p> {@link #prev()}
 * </li>
 * <li>
 * <p> {@link #hasNext()}
 * </li>
 * <li>
 * <p> {@link #hasPrev()}
 * </li>
 * </ul>
 * <p> work on the tree as a whole (rather than just one list of sub-trees).
 * {@code next()}
 *  on a tree that has sub-trees will move to the first sub-tree; otherwise, if
 * it has a next sibling node, it will move to that; otherwise it will recursively try to move next on the parent.
 * This means that repeated calls to
 * {@code next(}
 * ) will iterate over the whole tree.
 * <p> If you use
 * {@code prev()}
 *  on a zipper at the first tree in its parent's list then it will return the parent rather
 * than a list in the initial state.
 * <p> {@code next()}
 *  and
 * {@code prev()}
 *  will always return a zipper that has a focus.
 * <p> These methods:
 * <ul>
 * <li>
 * <p> {@link #nextSibling()}
 * </li>
 * <li>
 * <p> {@link #prevSibling()}
 * </li>
 * <li>
 * <p> {@link #hasNextSibling()}
 * </li>
 * <li>
 * <p> {@link #hasPrevSibling()}
 * </li>
 * </ul>
 * <p> work on the list that contains the focus (rather than the whole tree) and operate exactly like a list zipper.
 * <p> To move to the parent tree you can use  {@link #up()} and to move down to the sub-trees you
 * can use  {@link #down()} - which moves to
 * <em>before</em>
 *  the first sub-tree.
 * <p> Just as with {@link ImShelf.ImShelfIterator#next()}, {@link #prevSibling()} will sometimes return a zipper that has no focus.
 * <p> See the
 * <a href="{@docRoot}/im/package-summary.html">
 * package summary
 * </a>
 * for more details about zippers.
 * @see ImRoseTree
 * @see ImRoseTreeIterator
 * @see ImRoseTreeZipperIterator
 * @see ImListZipper
 *
 */
public class ImRoseTreeZipper<T> implements FIterator<ImRoseTree<T>>
{
    final private ImListZipper<ImRoseTree<T>> lstZipper;
    final private ImRoseTreeZipper<T> parent;

    ImRoseTreeZipper(ImListZipper<ImRoseTree<T>> lstZipper, ImRoseTreeZipper<T> parent)
    {
        this.lstZipper = lstZipper;
        this.parent = parent;
    }

    /**
     * <p> A zipper on
     * {@code tree}
     *  with the focus on the root node.
     *
     */
    ImRoseTreeZipper(ImRoseTree<T> tree)
    {
        this(ImList.on(tree).getZipper().next(), null);
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  except that the focus is
     * {@code newTree}
     * .
     * <p> @throws NoSuchElementException if there is no focus
     *
     */
    public ImRoseTreeZipper<T> setFocus(ImRoseTree<T> newTree)
    {
        return new ImRoseTreeZipper<T>(lstZipper.setFocus(newTree), parent);
    }

    /**
     * <p> The "parent" zipper - incorporating any changes made to the current list.
     * <p> {@code up()}
     *  calls close() on the current list so that the
     * parent zipper focus will be a tree that incorporates any mutations that you
     * made to the sub-trees.
     * <p> This:
     *
     * <pre>{@code
     * ImRoseTree<Integer> tree = ImRoseTree.withNodes(1, leaf(2), ImRoseTree.withElements(3, 4, 5), leaf(6));
     * }</pre>
     * <p> will create a tree like this:
     *
     * <pre>{@code
     * 1
     * .......
     * 2 3   6
     *   ...
     *   4 5
     * }</pre>
     * <p> If we get a zipper on node
     * {@code 3}
     * :
     *
     * <pre>{@code
     * ImRoseTreeZipper<Integer> z = tree.getZipper();
     * ImRoseTreeZipper<Integer> z3 = z.next().next();
     * }</pre>
     * <p> and then get a zipper on node
     * {@code 4}
     * :
     *
     * <pre>{@code
     * ImRoseTreeZipper<Integer> z4 = z3.next();
     * }</pre>
     * <p> If we go up from
     * {@code z4}
     *  without making changes we merely get
     * {@code z3}
     *
     * <pre>{@code
     * z4.up() == z3  =>  "true"
     * }</pre>
     * <p> but if we make changes ... we don't
     *
     * <pre>{@code
     * z4.setElement(7).up() == z3  => "false"
     * }</pre>
     * <p> @throws NoSuchElementException if the zipper is focussed on the root node
     *
     */
    public ImRoseTreeZipper<T> up()
    {
        if (isRoot())
            throw new NoSuchElementException(); // TODO fix
        else
        {
            ImList<ImRoseTree<T>> newList = lstZipper.close();

            // If the new list is the same as my parent node list then nothing has changed
            return newList == parent.getFocus().getSubTrees()
                   ? parent
                   : parent.setFocus(ImRoseTree.withNodes(parent.getFocus().getElement(), newList));
        }
    }

    public boolean isRoot()
    {
        return parent == null;
    }

    /**
     * <p> The focus of the zipper.
     * <p> @throws ZipperHasNoFocusException if there is no focus
     *
     */
    public ImRoseTree<T> getFocus()
    {
        if (lstZipper.getFocus() == null)
            throw new ZipperHasNoFocusException();
        else
            return lstZipper.getFocus();
    }

    /**
     * <p> The underlying tree, with all modifications made.
     * <p> If no modifications have been made then the original tree is returned.
     *
     */
    public ImRoseTree<T> close()
    {
        if (isRoot())
            if (lstZipper.getIndex() == 0)
                throw new RuntimeException();
            else
                return lstZipper.getFocus();
        else
            return up().close();
    }

    /**
     * <p> The zipper whose focus is the next node in the tree.
     * <p> If the node has sub-trees then the next node is the first sub-tree.
     * <p> If the focus is a leaf node, then the next node is the next sibling node, if it exists. If
     * no such sibling node exists we ask the zipper obtained from from
     * {@code up()}
     *  for the next sibling node and
     * continue recursively
     * until one is found or we reach the root.
     * <p> @throws NoSuchElementException if no such node exists
     * @see #hasNext()
     * @see #prev()
     * @see #nextSibling()
     *
     */
    public ImRoseTreeZipper<T> next()
    {
        return getFocus().getSubTrees().isEmpty()
               ? nextAlong()
               : down().nextSibling();
    }

    /**
     * <p> The zipper with the current list on the sub-trees of the focus, positioned
     * before the first sub-tree, if there is one.
     * @see #up()
     *
     */
    public ImRoseTreeZipper<T> down()
    {
        return new ImRoseTreeZipper<T>(getFocus().getSubTrees().getZipper(), this);
    }

    private ImRoseTreeZipper<T> nextAlong()
    {
        if (lstZipper.hasNext())
            return new ImRoseTreeZipper<T>(lstZipper.next(), parent);
        else if (isRoot())
            throw new NoSuchElementException(); // TODO fix
        else
            return up().nextAlong();
    }

    /**
     * <p> Get the element of the tree at the focus.
     * @see #getFocus
     *
     */
    public T getElement()
    {
        return getFocus().getElement();
    }

    /**
     * <p> Set the element of the tree at the focus leaving its sub-trees unchanged.
     * @see #getElement()
     *
     */
    public ImRoseTreeZipper<T> setElement(T newElement)
    {
        return setFocus(getFocus().replaceElement(newElement));
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  but with the last node popped from the first list of the current list
     * zipper.
     * <p> @throws UnsupportedOperationException if the focus of
     * {@code this}
     *  is the root of the tree
     *
     */
    public ImRoseTreeZipper<T> pop()
    {
        if (isRoot())
            throw new UnsupportedOperationException("attempt to remove root node");

        return new ImRoseTreeZipper<T>(lstZipper.pop(), parent);
    }

    /**
     * <p> Push
     * {@code treeToInsert}
     *  before the focus node.
     * <p> For all
     * {@code z}
     *  and
     * {@code t}
     * :
     *
     * <pre>{@code
     * z.pushBefore(t) == z.prevSibling().push(t)
     * }</pre>
     * <p> @throws UnsupportedOperationException if the focus of
     * {@code this}
     *  is the root of the tree
     * @see #push(ImRoseTree)
     * @see #prevSibling()
     *
     */
    public ImRoseTreeZipper<T> pushBefore(ImRoseTree<T> treeToInsert)
    {
        if (isRoot())
            throw new UnsupportedOperationException("Zipper is at the root of the tree");
        else
            return new ImRoseTreeZipper<T>(lstZipper.prev().push(treeToInsert), parent);
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  but with
     * {@code treeToInsert}
     *  pushed onto the first list of the current list
     * zipper.
     * <p> The new focus is
     * {@code treeToInsert}
     * .
     * <p> Eg
     * <p> 1  {2}  3  4
     * <p> 1   2  {5} 3  4
     * <p> If we draw the position of the zipper after the current element
     * <p> 1  2  *  3  4
     * <p> then push(5) results in
     * <p> 1  2  5  *  3  4
     * <p> @throws UnsupportedOperationException if the focus of
     * {@code this}
     *  is the root of the tree
     *
     */
    public ImRoseTreeZipper<T> push(ImRoseTree<T> treeToInsert)
    {
        if (isRoot())
            throw new UnsupportedOperationException("Zipper is at the root of the tree");
        else
            return new ImRoseTreeZipper<T>(lstZipper.push(treeToInsert), parent);
    }

    /**
     * <p> {@code true}
     *  if the focus has a next ,
     * {@code false}
     *  otherwise.
     * @see #next()
     *
     */
    public boolean hasNext()
    {
        return getFocus().getSubTrees().isEmpty()
               ? hasNextAlong()
               : true;
    }

    @Override
    public ImRoseTree<T> get()
    {
        return getFocus();
    }

    private boolean hasNextAlong()
    {
        return lstZipper.hasNext()
               ? true
               : isRoot()
                 ? false
                 : parent.hasNextAlong();
    }

    /**
     * <p> The string representation of this zipper.
     * <p> Each list zipper at each level is shown - the parent zipper shown before the child
     * <p> For clarity, to display each zipper we use its {@link ImListZipper#toString()} method and
     * we use a display function {@link #rootToString(ImRoseTree) } that shows
     * <em>just the root element of the tree</em>
     *  (rather than using
     * {@link ImRoseTree#toString()} which would show the whole tree)
     * <p> So - using this tree:
     *
     * <pre>{@code
     * 1
     * .......
     * 2 3   6
     *   ...
     *   4 5
     * }</pre>
     * <p> If we  get a zipper on it and then repeatedly use
     * {@code next().toString()}
     *  we would get this:
     *
     * <pre>{@code
     * {1 (2 3 (4 5) 6)}
     * {1 (2 3 (4 5) 6)} <- {2}  3 (4 5)  6
     * {1 (2 3 (4 5) 6)} <- 2  {3 (4 5)}  6
     * {1 (2 3 (4 5) 6)} <- 2  3 (4 5)  6 <- {4}  5
     * {1 (2 3 (4 5) 6)} <- 2  3 (4 5)  6 <- 4  {5}
     * {1 (2 3 (4 5) 6)} <- 2  3 (4 5)  {6}
     * }</pre>
     *
     */
    @Override
    public String toString()
    {
        return TextUtils.join(getZippers().map(i -> i.toString()), " <- ");
    }

    /**
     * <p> The method used by {@link #toString()} to display tree nodes.
     */
    public static <TT> String rootToString(ImRoseTree<TT> tree)
    {
        return tree.getElement().toString();
    }

    public ImList<ImListZipper<ImRoseTree<T>>> getZippers()
    {
        return getAllListZippers().reverse();
    }

    private ImList<ImListZipper<ImRoseTree<T>>> getAllListZippers()
    {
        return isRoot()
               ? ImList.on(lstZipper)
               : ImList.cons(lstZipper, parent.getAllListZippers());
    }

    /**
     * <p> Show the path of this zipper with the separator
     * {@code sep}
     * <p> Assuming the following tree has elements of type Integer
     *
     * <pre>{@code
     * 1
     * .......
     * 2 3   6
     *   ...
     *   4 5
     * }</pre>
     * <p> Then
     *
     * <pre>{@code
     * showPath("/")
     * }</pre>
     * <p> on the zipper on node 4
     * <p> would be
     *
     * <pre>{@code
     * 1/3/4
     * }</pre>
     *
     */
    public String showPath(String sep)
    {
        return TextUtils.join(getZippers().map(z -> z.getFocus().getElement().toString()), sep);
    }

    /**
     * <p> An {@link ImRoseTreeZipperIterator} on
     * {@code this}
     *
     */
    public Iterator<ImRoseTreeZipper<T>> iterator()
    {
        return new ImRoseTreeZipperIterator<T>(this);
    }

    /**
     * <p> {@code true}
     *  if the focus has a previous node,
     * {@code false}
     *  otherwise.
     * @see #prev()
     *
     */
    public boolean hasPrev()
    {
        return parent != null;
    }

    /**
     * <p> The zipper whose focus is the previous node in the tree.
     * <p> If the node has no previous sibling then the zipper obtained from
     * {@code up()}
     * is returned if one exists.
     * <p> If the node has a previous sibling then, if it is a leaf node, then it is returned.
     * <p> Otherwise, we return the previous node's last sub-tree node, if it is a leaf node, or continue
     * recursively searching its sub-trees until we find a leaf node.
     * <p> @throws NoSuchElementException if the focus of
     * {@code this}
     *  is the root of the tree
     * @see #hasPrev()
     * @see #next()
     * @see #prevSibling()
     *
     */
    public ImRoseTreeZipper<T> prev()
    {
        if (lstZipper.hasPrev())
            return new ImRoseTreeZipper<T>(lstZipper.prev(), parent).goDownLast();
        else
            return up();
    }

    private ImRoseTreeZipper<T> goDownLast()
    {
        ImList<ImRoseTree<T>> subTrees = getFocus().getSubTrees();

        return subTrees.isEmpty()
               ? this
               : new ImRoseTreeZipper<T>(subTrees.getZipperOnIndex(subTrees.size()), this).goDownLast();
    }

    /**
     * <p> The next sibling node, if there is one.
     * <p> @throws NoSuchElementException if the current list zipper does not have a next node
     * @see #hasNextSibling()
     *
     */
    public ImRoseTreeZipper<T> nextSibling()
    {
        if (isRoot())
            throw new NoSuchElementException("Zipper is at the root of the tree");
        else if (!lstZipper.hasNext())
            throw new NoSuchElementException("Zipper is at the end of the sub-trees list");
        else
            return new ImRoseTreeZipper<T>(lstZipper.next(), parent);
    }

    /**
     * <p> {@code true}
     *  if the focus has a next sibling node,
     * {@code false}
     *  otherwise.
     * @see #nextSibling()
     *
     */
    public boolean hasNextSibling()
    {
        return lstZipper.hasNext();
    }

    /**
     * <p> The previous sibling node, if there is one.
     * <p> @throws NoSuchElementException if the focus of
     * {@code this}
     *  is the root of the tree
     * @see #hasPrevSibling()
     *
     */
    public ImRoseTreeZipper<T> prevSibling()
    {
        if (isRoot())
            throw new NoSuchElementException("Zipper is at the root of the tree");
        else
            return new ImRoseTreeZipper<T>(lstZipper.prev(), parent);
    }

    /**
     * <p> {@code true}
     *  if the focus has a previous sibling node,
     * {@code false}
     *  otherwise.
     * @see #prevSibling()
     *
     */
    public boolean hasPrevSibling()
    {
        return lstZipper.hasPrev();
    }

    /**
     * <p> Compares
     * {@code another}
     *  with
     * {@code this}
     *  for equality.  Returns
     * {@code true}
     *  iff the specified object is also a
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
                 : other instanceof ImRoseTreeZipper
                   ? safeEquals(Caster.cast(other))
                   : false;
    }

    private boolean safeEquals(ImRoseTreeZipper<T> other)
    {
        return lstZipper.equals(other.lstZipper) && Equals.isEqual(parent, other.parent);
    }
}