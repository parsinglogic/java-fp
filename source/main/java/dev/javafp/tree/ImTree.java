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
import dev.javafp.eq.Equals;
import dev.javafp.ex.InvalidState;
import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSortedSet;
import dev.javafp.util.Caster;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p> An immutable binary tree.
 * <h2>Introduction</h2>
 * <p> This class is the heart of the Immutable collections library.
 * It is intended to be used only as a component of the other classes.
 * <p> An
 * {@code ImTree}
 *  is an AVL tree (a balanced binary tree) where each node stores some arbitrary data.
 * <p> Note that, in this class, there is no concept of the data that is being stored being Comparable.
 * This functionality is added by {@link ImSortedSet}.
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
 *  has no data.
 * <p> Note that this definition does not, of itself, specify that the tree is balanced. We enforce that
 * invariant in each method that adds/removes nodes.
 * <p> Consider an example tree with six non nil nodes:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef.png" alt="tree-abcdef"  width=200/>
 *
 *
 * </p>
 * <p> If we show the nil nodes then it looks like this:
 * <p> <img src="{@docRoot}/dev/doc-files/tree-abcdef-with-nulls.png"   width=200/>
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
 * <em>rank</em>
 *  that represents its position in the tree in a pre-order scan.
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

    public static final ImTree<?> nil = new ImTree<Object>();
    private static final int MAX_SUBTREE_HEIGHT_DIFFERENCE_PLUS_ONE = 2;

    @SuppressWarnings("unchecked")
    public static <A> ImTree<A> Nil()
    {
        return (ImTree<A>) nil;
    }

    protected ImTree()
    {
        this(null, null, null, 0, 0);
    }

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

    public static <A> ImTree<A> on(final A a)
    {
        return new ImTree<A>(a, ImTree.<A>Nil(), ImTree.<A>Nil());
    }

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
         * <p> We are a. If our left node (b) is too tall then either d or e is the
         * culprit. It can't be both
         * a
         * b       c
         * d   e
         *
         */

        if (e.height > d.height)
        {
            /**
             * <p> Ok - so it is e that is too tall
             * So - e cannot be nil - so it must have children f and g
             * <p> What it would        After swizzling
             * be if we didn't
             * swizzle
             *
             * <pre>{@code
             *     a        1        e'
             * b       c    2      b'  a'
             * }</pre>
             * <p> d   e          3     d f g c
             * f g         4
             *
             */
            return new ImTree<A>(e.getElement(), new ImTree<A>(b.getElement(), d, e.getLeft()), new ImTree<A>(element,
                    e.getRight(), c));
        }
        else
        {
            /**
             * <p> Ok - so either d and e are the same height. They can't both be nil, otherwise b could not be
             * or d is taller than e (in which case d is not nil)
             * <p> d and e might be nil
             * <p> What it would        After swizzling
             * be if we didn't
             * swizzle
             *
             * <pre>{@code
             *  a         1       b'
             * }</pre>
             * <p> b   c       2     d   a'
             * d e          3        e c
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
         * <p> We are a. Our right node c is too tall.
         * So either d or e is the
         * culprit. It can't be both
         * a
         * /
         * <br/>
         * b     c
         * /
         * <br/>
         * d   e
         *
         */

        if (d.height > e.height)
        {
            /**
             * <p> Ok - so it is d that is too tall. We need to do a double rotation.
             * <p> What it would        After swizzling
             * be if we didn't
             * swizzle
             *
             * <pre>{@code
             *     a         1       d'
             *   /   \              /  \
             *  b     c      2     a'   c'
             *       / \          / \  / \
             *      d   e    3    b f g   e
             *     / \
             *    f   g      4
             * }</pre>
             *
             */
            return new ImTree<A>(d.getElement(), new ImTree<A>(element, b, d.getLeft()), new ImTree<A>(c.getElement(),
                    d.getRight(), e));
        }
        else
        {
            /**
             * <p> Ok - so it is e that is too tall or d and e have the same height
             *
             * <pre>{@code
             * What it would        After swizzling
             * be if we didn't
             * swizzle
             *
             *
             *      a         1       c'
             *    /   \              /  \
             *   b     c      2     a'   e
             *        / \          / \
             *       d   e    3   b   d
             * }</pre>
             *
             */
            return new ImTree<A>(c.getElement(), new ImTree<A>(element, b, d), e);
        }
    }

    public static <A> ImTree<A> merge(final ImTree<A> left, final ImTree<A> right)
    {
        // If the left child is nil then just return the right child (which could, itself be nil)
        if (left == nil)
            return right;

        // and vice versa
        if (right == nil)
            return left;

        /**
         * <p> Uh oh - both children are non nil.
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

    public static <A> ImTree<A> replaceAtIndex(final ImTree<A> tree, final int indexStartingAtOne, final A newElement)
    {
        final int localIndex = indexStartingAtOne - (tree.getLeft().size + 1);

        return localIndex == 0
               ? tree.getElement() == newElement
                 ? tree
                 : ImTree.newBalancedTree(newElement, tree.getLeft(), tree.getRight())
               : localIndex < 0
                 ? ImTree.newBalancedTree(tree.getElement(),
                replaceAtIndex(tree.getLeft(), indexStartingAtOne, newElement), tree.getRight())
                 : ImTree.newBalancedTree(tree.getElement(), tree.getLeft(),
                replaceAtIndex(tree.getRight(), localIndex, newElement));
    }

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
     * <p> The tree with its root removed.
     * <p> We are removing a in the following diagrams
     *
     * <pre>{@code
     *    a            =>               c
     *   / \
     *  -   c
     *
     *    a            =>               b
     *   / \
     *  b   -
     *
     *
     *    a            =>            merge(b,c)
     *   / \
     *  b   c
     * }</pre>
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

        final int leftWidth = leftChildBox.getWidth();
        final int rightWidth = rightChildBox.getWidth();

        /**
         *
         * <pre>{@code
         * +--------+
         * |        |
         * +--------+
         * +----+  +--------------+
         * |    |  |              |
         * +----+  +--------------+
         * }</pre>
         *
         */
        final int width = Math.max(myText.length(), leftWidth + 1 + rightWidth);

        final LeafTextBox gap = LeafTextBox.centred("", width - (leftWidth + rightWidth));
        final LeftRightBox children = LeftRightBox.with(leftChildBox, gap, rightChildBox);

        final String dots = TextUtils.repeat(".", (leftWidth + 1) / 2 + gap.getWidth() + (rightWidth + 1) / 2);
        final String spaceAndDots = TextUtils.repeat(" ", leftWidth / 2) + dots;

        return TopDownBox.with(LeafTextBox.centred(myText, width), LeafTextBox.with(spaceAndDots), children);
    }

    // end - Printing

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
     *   C3(f)
     *     / \
     *   b    g
     * /   \
     * }</pre>
     * <p> a     d
     * /
     * <br/>
     * c   e
     * <p> expanding C3:
     *
     * <pre>{@code
     * BT(b)
     * /   \
     * }</pre>
     * <p> a  C3(f)
     * /
     * <br/>
     * d   g
     * /
     * <br/>
     * c   e
     * <p> Expanding C3:
     *
     * <pre>{@code
     * BT(b)
     * /   \
     * }</pre>
     * <p> a     f
     * /
     * <br/>
     * d   g
     * /
     * <br/>
     * c   e
     * <p> Expanding BT:
     *
     * <pre>{@code
     *     d
     *   /   \
     *  b     f
     * / \   / \
     * }</pre>
     * <p> a   c e   g
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
     *
     */
    public boolean isBalanced()
    {
        return this == nil
               ? true
               : getBalance(getLeft(), getRight()) == Balance.balanced && getLeft().isBalanced()
                       && getRight().isBalanced();
    }

    public ImList<A> toList()
    {
        ImTreeZipper<A> z = ImTreeZipper.onRightmost(this);

        if (z.getFocus() == ImTree.nil)
        {
            return Caster.cast(ImList.empty());
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
        return new ImTreeIterator(this);
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