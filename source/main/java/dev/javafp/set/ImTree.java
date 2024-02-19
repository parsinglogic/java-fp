/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.eq.Equals;
import dev.javafp.ex.InvalidState;
import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.util.ImMaybe;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p> An immutable "binary tree".
 * <h2>Introduction</h2>
 * <p> This class is the heart of the Immutable collections library.
 * It is intended to be used only as a component of the other classes.
 *
 * <p> An
 * {@code ImTree}
 *  is an AVL tree (a balanced binary tree) where each node stores some arbitrary data.
 *
 * <p> <strong>Note that this is not a traditional sorted binary tree</strong>
 * .
 * <p> It is
 * <em>balanced</em>
 *  but there is no concept of the data that is being stored being
 * {@link Comparable}
 * - this functionality is added by {@link ImSortedSet}.
 * <p> Essentially  {@link ImSortedSet} <strong>is</strong> a traditional sorted binary tree and it uses this
 * class as part of its implementation.
 * <p> Another class that uses this class is {@link dev.javafp.shelf.ImShelf}.
 * <p> A node in an
 * {@code ImTree}
 *  is either a leaf node
 * {@code Nil}
 *  or a
 * {@code Node}
 *  that has two children that
 * are themselves
 * {@code ImTrees}
 * .
 * <p> Each
 * {@code Node}
 *  can contain a value of an arbitrary type and two Integers
 * representing the height and size of the tree rooted at that node. A
 * {@code Nil}
 * node has no data and no left or right child - these are set to
 * {@code null}
 * <p> Note that this definition does not, of itself, specify that the tree is <em>balanced</em>. We enforce that
 * invariant in each method that adds/removes nodes.
 * <p> Consider an example tree with six non Nil nodes:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef.png" alt="tree-abcdef"  width=200/>
 *
 *
 * </p>
 * <p> If we show the nil nodes then it looks like this:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef-with-nils.png"   width=200/>
 * </p>
 * <p> Each
 * {@code Node}
 *  also has a
 * {@code size}
 *  value defined as the sum of the sizes of its children plus one.
 * nil Nodes are considered to have a size of zero.
 * The size represents how many non nil nodes there are in the tree rooted at n.
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef-with-sizes.png" alt="tree-abcdef-with-sizes"    width=200/>
 * </p>
 * <p> Each node also has a height value representing the size of the longest path from that node to a leaf
 * node.
 * <p> The height of a node is the maximum of the heights of its children. Nil nodes are considered to
 * have a height of zero.
 * <p> Let's annotate our example with the heights:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef-with-heights.png" alt="tree-abcdef-with-heights"     width=200/>
 * </p>
 * <p> Because the tree is balanced, this means that the heights of the children of a node will differ
 * by at most one.
 * <p> Each node is considered to have a
 * <em>rank(AKA index)</em>
 *  that represents its position in a pre-order scan of the tree.
 * <p> Ranks start at one (exactly as Nature intended!)
 * <p> Let's annotate our example with the ranks:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef-with-ranks.png" alt="tree-abcdef-with-ranks"    width=200/>
 * </p>
 * <p> We don't store the ranks. To calculate the rank of a node or to find a node at a particular rank we can use
 * the size of child nodes to derive the answer.
 * <h2>Don't go changing to try to please me...</h2>
 * <p> These are
 * <strong>immutable</strong>
 *  Collections so the
 * {@code insert}
 *  and
 * {@code remove}
 *  methods don't actually change existing trees.
 * Instead they create a new tree with a node added or deleted as appropriate, reusing as many of
 * the old nodes as possible.
 * <h2>References</h2>
 * <p> <a href="http://groups.csail.mit.edu/mac/users/adams/BB/92-10.ps"  >Implementing Sets Efficiently in a Functional Language,Stephen Adams</a>
 *
 */
public class ImTree<A> implements Serializable, Iterable<A>
{
    // TODO replace with getters? Deal with nil?
    final private A element;
    final private ImTree<A> left;
    final private ImTree<A> right;

    final private int height;
    final private int size;
    final private int hashCode;

    static final ImTree<?> nil = new ImTree<Object>();
    private static final int MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE = 2;

    /**
     * The empty tree
     */
    public static <A> ImTree<A> Nil()
    {
        return (ImTree<A>) nil;
    }

    protected ImTree()
    {
        this(null, null, null, 0, 0);
    }

    /**
     * Ensure that we maintain the empty tree singleton
     */
    private Object readResolve()
    {
        return getElement() == null
               ? nil
               : this;
    }

    protected ImTree(final A element, final ImTree<A> left, final ImTree<A> right, final int height, final int size)
    {
        this.element = element;
        this.left = left;
        this.right = right;
        this.height = height;
        this.size = size;
        this.hashCode = element == null
                        ? 0
                        : element.hashCode() + left.hashCode() + right.hashCode();
    }

    ImTree(final A a, final ImTree<A> left, final ImTree<A> right)
    {
        this(a, left, right, 1 + Math.max(left.height, right.height), left.size + right.size + 1);
    }

    /**
     * A tree containing a single element,
     * {@code a}
     */
    public static <A> ImTree<A> on(final A a)
    {
        return new ImTree<A>(a, ImTree.<A>Nil(), ImTree.<A>Nil());
    }

    /**
     *
     A tree containing the elements
     * {@code elements}
     */
    public static <A> ImTree<A> on(Collection<A> elements)
    {
        ImTree<A> result = ImTree.Nil();

        int index = 1;
        for (A a : elements)
        {
            result = result.insert(index++, a);
        }

        return result;
    }

    private static enum Balance
    {
        left, right, balanced, unbalanced, leftUnbalanced, rightUnbalanced
    }

    /**
     * <p> A new balanced tree formed from element
     * {@code newA}
     *  and the left subtree
     * {@code newLeft}
     *  and the right subtree
     * {@code newRight}
     * .
     */
    public static <A> ImTree<A> newBalancedTree(final A newA, final ImTree<A> newLeft, final ImTree<A> newRight)
    {

        Balance b = getBalance(newLeft, newRight);

        switch (b)
        {
        case left:
            return newTreeL(newA, newLeft, newRight);

        case right:
            return newTreeR(newA, newLeft, newRight);

        case balanced:
            return new ImTree<A>(newA, newLeft, newRight);
        }

        throw new InvalidState("difference in tree height is too great");
    }

    private static <A> Balance getBalance(final ImTree<A> left, final ImTree<A> right)
    {
        int diff = left.height - right.height;

        if (diff == MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE)
            return Balance.left;

        if (diff == -MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE)
            return Balance.right;

        if (Math.abs(diff) < MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE)
            return Balance.balanced;

        if (diff > MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE)
            return Balance.leftUnbalanced;

        if (diff < -MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE)
            return Balance.rightUnbalanced;

        throw new InvalidState("Unknown balance");
    }

    static <A> ImTree<A> newTreeL(final A element, final ImTree<A> b, final ImTree<A> c)
    {
        final ImTree<A> d = b.getLeft();
        final ImTree<A> e = b.getRight();

        /**
         * We are a. If our left node (b) is too tall then either d or e is the
         * culprit. It can't be both
         *
         *           a
         *       b       c
         *     d   e
         *
         */

        if (e.height > d.height)
        {
            /**
             * Ok - so it is e that is too tall
             * So - e cannot be nil - so it must have children f and g
             *
             *     What it would        After swizzling
             *     be if we didn't
             *     swizzle
             *
             *            a        1        e'
             *        b       c    2      b'  a'
             *      d   e          3     d f g c
             *         f g         4
             */
            return new ImTree<A>(e.getElement(), new ImTree<A>(b.getElement(), d, e.getLeft()), new ImTree<A>(element,
                    e.getRight(), c));
        }
        else
        {
            /**
             * Ok - so either d and e are the same height. They can't both be nil, otherwise b could not be
             * or d is taller than e (in which case d is not nil)
             *
             * d and e might be nil
             *
             *     What it would        After swizzling
             *     be if we didn't
             *     swizzle
             *
             *         a         1       b'
             *       b   c       2     d   a'
             *      d e          3        e c
             *
             */

            return new ImTree<A>(b.getElement(), d, new ImTree<A>(element, e, c));
        }
    }

    static <A> ImTree<A> newTreeR(final A element, final ImTree<A> b, final ImTree<A> c)
    {

        final ImTree<A> d = c.getLeft();
        final ImTree<A> e = c.getRight();

        /**
         * We are a. Our right node c is too tall.
         * So either d or e is the
         * culprit. It can't be both
         *
         *        a
         *      /   \
         *     b     c
         *          / \
         *         d   e
         *
         */

        if (d.height > e.height)
        {
            /**
             * Ok - so it is d that is too tall. We need to do a double rotation.
             *
             *     What it would        After swizzling
             *     be if we didn't
             *     swizzle
             *
             *          a         1       d'
             *        /   \              /  \
             *       b     c      2     a'   c'
             *            / \          / \  / \
             *           d   e    3    b f g   e
             *          / \
             *         f   g      4
             */

            return new ImTree<A>(d.getElement(), new ImTree<A>(element, b, d.getLeft()), new ImTree<A>(c.getElement(),
                    d.getRight(), e));
        }
        else
        {
            /**
             * Ok - so it is e that is too tall or d and e have the same height
             *
             *     What it would        After swizzling
             *     be if we didn't
             *     swizzle
             *
             *
             *          a         1       c'
             *        /   \              /  \
             *       b     c      2     a'   e
             *            / \          / \
             *           d   e    3   b   d
             *
             *
             */
            return new ImTree<A>(c.getElement(), new ImTree<A>(element, b, d), e);
        }
    }

    /**
     * <p> The tree that contains the elements of
     * {@code left}
     *  in their original order followed by the elements of
     * {@code right}
     * in their original order
     *
     */
    public static <A> ImTree<A> merge(final ImTree<A> left, final ImTree<A> right)
    {
        // If the left child is nil then just return the right child (which could, itself be nil)
        if (left == nil)
            return right;

        // and vice versa
        if (right == nil)
            return left;

        /**
         *  Uh oh - both children are non nil.
         */

        if (left.height >= right.height)
        {
            return concat3(left.getElement(), left.getLeft(), merge(left.getRight(), right));
        }
        else
        {
            return concat3(right.getElement(), merge(left, right.getLeft()), right.getRight());
        }
    }

    // end - Constructors and factory methods

    static <A> boolean isNil(final ImTree<A> treeToBeChecked)
    {
        return treeToBeChecked == nil;
    }

    // Shelf

    /**
     * <p> Insert a new node with the value
     * {@code elementToAdd}
     *  at index
     * {@code indexStartingAtOne}
     * <p> If we add node g to this example at index 3 we will generate a new tree with three new nodes
     * a', b' and g. None of the old nodes will be affected.
     *
     * <pre>{@code
     *          a                       a'
     *        /   \                   /   \
     *       /     \                 /     \
     *      b       c  ----+        b'      |
     *     /      /   \    |       / \      |
     *    d      e     f   |      |   g     |
     *    |                |      |         |
     *    +-----------------------+         |
     *                     |                |
     *                     +----------------+
     * }</pre>
     *
     */
    public ImTree<A> insert(final int indexStartingAtOne, final A elementToAdd)
    {
        if (this == ImTree.nil)
            return ImTree.on(elementToAdd);

        final int localIndex = indexStartingAtOne - (getLeft().size + 1);

        return localIndex <= 0
               ? ImTree.newBalancedTree(getElement(), getLeft().insert(indexStartingAtOne, elementToAdd), getRight())
               : ImTree.newBalancedTree(getElement(), getLeft(), getRight().insert(localIndex, elementToAdd));
    }

    /**
     * <p> The tree that is
     * {@code this}
     *  with the element
     * {@code indexStartingAtOne}
     *  replaced with
     * {@code newElement}
     *
     */
    public ImTree<A> replaceAtIndex(final int indexStartingAtOne, final A newElement)
    {
        final int localIndex = indexStartingAtOne - (this.getLeft().size + 1);

        return localIndex == 0
               ? this.getElement() == newElement
                 ? this
                 : ImTree.newBalancedTree(newElement, this.getLeft(), this.getRight())
               : localIndex < 0
                 ? ImTree.newBalancedTree(this.getElement(), this.getLeft().replaceAtIndex(indexStartingAtOne, newElement), this.getRight())
                 : ImTree.newBalancedTree(this.getElement(), this.getLeft(), this.getRight().replaceAtIndex(localIndex, newElement));
    }

    /**
     * The node at index
     * {@code indexStartingAtOne}
     */
    public ImTree<A> getNodeAtIndex(final int indexStartingAtOne)
    {
        Throw.Exception.ifIndexOutOfBounds("indexStartingAtOne", indexStartingAtOne, "this", size());

        final int localIndex = indexStartingAtOne - (getLeft().size + 1);

        return localIndex == 0
               ? this
               : localIndex < 0
                 ? getLeft().getNodeAtIndex(indexStartingAtOne)
                 : getRight().getNodeAtIndex(localIndex);
    }

    /**
     * <p> Return a tree that is the same as
     * {@code this}
     *  but without the element
     * stored at the node with index
     * {@code indexStartingAtOne}
     *
     */
    public ImTree<A> remove(int indexStartingAtOne)
    {
        Throw.Exception.ifIndexOutOfBounds("indexStartingAtOne", indexStartingAtOne, "this", size());

        final int localIndex = indexStartingAtOne - (getLeft().size + 1);

        return localIndex == 0
               ? removeRoot()
               : localIndex < 0
                 ? ImTree.newBalancedTree(getElement(), getLeft().remove(indexStartingAtOne), getRight())
                 : ImTree.newBalancedTree(getElement(), getLeft(), getRight().remove(localIndex));
    }

    /**
     * The tree with its root removed.
     *
     * We are removing a in the following diagrams
     *
     *        a            =>               c
     *       / \
     *      -   c
     *
     *        a            =>               b
     *       / \
     *      b   -
     *
     *
     *        a            =>            merge(b,c)
     *       / \
     *      b   c
     *
     */
    public ImTree<A> removeRoot()
    {
        return merge(getLeft(), getRight());
    }

    // End Shelf

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return isNil(this)
               ? "-"
               : "" + getElement() + (getHeight() > 1
                                      ? " (" + getLeft() + " " + getRight() + ")"
                                      : "");
    }

    /**
     * <p> An ascii-art diagram of this tree
     * <p> For eaxample:
     *
     * <pre>{@code
     *      d
     *    /   \
     *   b     f
     *  / \   /
     * a   c e
     * }</pre>
     *
     */
    public String toBoxString()
    {
        return toBox(this).toString();
    }

    protected static <A> AbstractTextBox toBox(final ImTree<A> tree)
    {
        if (tree == nil)
            return LeafTextBox.with("-");

        final String myText = tree.getElement().toString();

        if ((tree.getLeft() == nil) && (tree.getRight() == nil))
            return LeafTextBox.with(myText);

        final AbstractTextBox leftChildBox = toBox(tree.getLeft());
        final AbstractTextBox rightChildBox = toBox(tree.getRight());

        final int leftWidth = leftChildBox.width;
        final int rightWidth = rightChildBox.width;

        /**
         *     +--------+
         *     |        |
         *     +--------+
         *     +----+  +--------------+
         *     |    |  |              |
         *     +----+  +--------------+
         */
        final int width = Math.max(myText.length(), leftWidth + 1 + rightWidth);

        final LeafTextBox gap = LeafTextBox.centred("", width - (leftWidth + rightWidth));
        final LeftRightBox children = LeftRightBox.with(leftChildBox, gap, rightChildBox);

        final String dots = ".".repeat((leftWidth + 1) / 2 + gap.width + (rightWidth + 1) / 2);
        final String spaceAndDots = " ".repeat(leftWidth / 2) + dots;

        return TopDownBox.with(LeafTextBox.centred(myText, width), LeafTextBox.with(spaceAndDots), children);
    }

    // end - Printing

    /**
     * The string representation of the element at the root of this tree
     */
    public String elementToString()
    {
        return this == nil
               ? "-"
               : getElement().toString();
    }

    /**
     * <p> The name
     * {@code concat3}
     *  is taken directly from [Implementing Sets Efficiently in a Functional Language][adams]
     * <p> Consider an example
     * let's denote newBalancedTree() by BT() and concat3 by C3() (roughly)
     *
     * <pre>{@code
     *    C3(f)
     *      / \
     *    b    g
     *  /   \
     * a     d
     *      / \
     *     c   e
     * }</pre>
     * <p> expanding C3:
     *
     * <pre>{@code
     *  BT(b)
     *  /   \
     * a  C3(f)
     *      / \
     *     d   g
     *    / \
     *   c   e
     * }</pre>
     * <p> Expanding C3:
     *
     * <pre>{@code
     *  BT(b)
     *  /   \
     * a     f
     *      / \
     *     d   g
     *    / \
     *   c   e
     * }</pre>
     * <p> Expanding BT:
     *
     * <pre>{@code
     *      d
     *    /   \
     *   b     f
     *  / \   / \
     * a   c e   g
     * }</pre>
     *
     */
    static <A> ImTree<A> concat3(A element, ImTree<A> left, ImTree<A> right)
    {
        Balance b = getBalance(left, right);

        switch (b)
        {
        case left:
        case leftUnbalanced:
            return newBalancedTree(left.getElement(), left.getLeft(), concat3(element, left.getRight(), right));

        case right:
        case rightUnbalanced:
            return newBalancedTree(right.getElement(), concat3(element, left, right.getLeft()), right.getRight());

        case balanced:
            return new ImTree<A>(element, left, right);
        }

        throw new InvalidState("");
    }

    /**
     * <p> {@code true}
     * if the tree is balanced
     *
     */
    public boolean isBalanced()
    {
        return this == nil
               ? true
               : getBalance(getLeft(), getRight()) == Balance.balanced && getLeft().isBalanced()
                       && getRight().isBalanced();
    }

    /**
     * The list that is the pre-order scan of the elements of this tree
     */
    public ImList<A> toList()
    {
        ImTreeZipper<A> z = ImTreeZipper.onRightmost(this);

        if (z.getFocus() == ImTree.nil)
        {
            return ImList.on();
        }

        ImList<A> result = ImList.on(z.getElement());
        ImMaybe<ImTreeZipper<A>> m = z.previous();

        while (m.isPresent())
        {
            ImTreeZipper<A> current = m.get();
            result = ImList.cons(current.getElement(), (ImList<A>) result);

            m = current.previous();
        }
        return result;
    }

    /**
     * <p> The tree that has the same shape as
     * {@code this}
     *  and where each element is the result of evaluating
     * the single argument function
     * {@code fn}
     *  on
     * the corresponding element of
     * {@code this}
     * .
     *
     */
    public <O> ImTree<O> map(Fn<A, O> fn)
    {
        return isNil(this)
               ? ImTree.<O>Nil()
               : new ImTree<O>(fn.of(getElement()), getLeft().map(fn), getRight().map(fn));
    }

    /**
     * The element at the root node of this tree
     */
    public A getElement()
    {
        return element;
    }

    /**
     * <p> The left sub-tree of
     * {@code this}
     * .
     *
     */
    public ImTree<A> getLeft()
    {
        return left;
    }

    /**
     * <p> The right sub-tree of
     * {@code this}
     * .
     *
     */
    public ImTree<A> getRight()
    {
        return right;
    }

    /**
     * The height of the tree
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * <p> The number of elements in
     * {@code this}
     *
     */
    public int size()
    {
        return size;
    }

    /**
     * The (cached) hashcode for this object.
     */
    @Override
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * <p> Get the rank of the root node of this tree in this tree.
     */
    public int getRank()
    {
        return isNil(this)
               ? 0
               : left.size + 1;
    }

    /**
     * <p> An iterator over the elements in
     * {@code this}
     * .
     * <p> Note that the iterator returned by this method will throw an
     * {@code UnsupportedOperationException}
     *  in response to its
     * {@code remove}
     *  method.
     *
     */
    public ImTreeIterator<A> iterator()
    {
        return ImTreeIterator.on(this);
    }

    /**
     * <p> Compares
     * {@code another}
     *  with
     * {@code this}
     *  for equality.  Returns
     * {@code true}
     *  if and only if the specified object is also an
     * {@code ImTree}
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
        return this == other || other instanceof ImTree<?> && eq((ImTree<?>) other);
    }

    private boolean eq(ImTree<?> otherTree)
    {
        return size() != otherTree.size() || hashCode() != otherTree.hashCode()
               ? false
               : Equals.isEqualIterators(iterator(), otherTree.iterator());
    }

}