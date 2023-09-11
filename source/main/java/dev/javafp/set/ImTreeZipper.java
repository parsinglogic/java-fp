/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.ex.Throw;
import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> A
 * <em>zipper</em>
 *  on an
 * {@code ImTree}
 * .
 * <p> One way to think about zippers is that they are the
 * {@code im}
 *  equivalent of
 * {@code java.util.ListIterator}
 *  -
 * they allow bi-directional navigation and "mutation" of the underlying collection.
 * <p> Another way to think about zippers is that they are a way of referring to a part of an
 * {@code im}
 *  collection in the context
 * of the whole collection - this allows us to easily create a new collection with just that part of it modified.
 * <p> Another other way to think about zippers is that they are a breadcrumb trail from a part of a data structure up to the
 * root of that data structure.
 * Zippers were first mentioned in a paper by Gerard Huet in 1997
 *
 * <p> <a href="http://www.st.cs.uni-saarland.de/edu/seminare/2005/advanced-fp/docs/huet-zipper.pdf"  >Functional Pearl: The Zipper, Gerard Huet(September 1997)</a>
 * <p> and have become an important concept in functional programming - although
 *
 * <p> <a href="https://www.fpcomplete.com/haskell/tutorial/lens/" > lenses </a>
 *
 * <p> have now superseded them in FP languages with advanced type systems.
 *
 * <p> Consider this tree:
 *
 * <pre>{@code
 *     a
 *    / \
 *   b   c
 *  / \
 * d   e
 *    /
 *   f
 * }</pre>
 *
 *
 * <p> {@code a}
 * ,
 * {@code b}
 * ,
 * {@code c}
 *  etc are the elements at each node. Let's denote the tree with the root element of
 * {@code a}
 *  as
 * {@code A}
 *  and
 * {@code b}
 *  as
 * {@code B}
 *  etc so that
 * we can distinguish between elements and trees.
 * <p> Then lets look at the zipper that is focussed on the tree
 * {@code F}
 * :
 *
 *
 * <pre>{@code
 *                         (focus, side,    parent)
 *
 *              a     zA = (A,     null,    null)
 *             / \
 *            b   c   zB = (B,     left,    zA)
 *           / \
 *          d   e     zE = (E,     right,   zB)
 *             /
 *            f       zF = (F,     left,    zE)
 * }</pre>
 *
 * <p> In the example above, lets 'modify' our focus node
 * {@code F}
 *  and set the element to
 * {@code g}
 * We get a new zipper, It points to the same parent but its focus element is different
 *
 *
 * <pre>{@code
 *            g      zG = ( G, left, zE)
 * }</pre>
 * <p> Now let's go up:
 *
 * <pre>{@code
 *            e       zE' = (E', right, zB)
 *           /
 *          g
 * }</pre>
 *
 * <p> We have created a new tree,
 * {@code E'}
 * , containing
 * {@code e}
 *  at the root and with a left child of
 * {@code g}
 *  and a right child of
 * {@code Nil}
 * Now let's go up again:
 *
 *
 * <pre>{@code
 *            b        zB' = (B', left, zA)
 *           / \
 *          d   e
 *             /
 *            g
 * }</pre>
 *
 * <p> Again we have created a new tree,
 * {@code B'}
 *  containing
 * {@code b}
 *  at the root with a left child of
 * {@code d}
 *  and a right child of
 * {@code E'}
 * Finally, we go up again:
 *
 *
 * <pre>{@code
 *              a     zA' = (A', null, null)
 *             / \
 *            b   c
 *           / \
 *          d   e
 *             /
 *            g
 * }</pre>
 * <p> We have now created a new tree that is the same as the old one except that it has the element
 * {@code g}
 *  where element
 * {@code f}
 *  used to be.
 * {@code ImTreeZippers}
 *  work on trees that are being rebalanced
 *
 */
public class ImTreeZipper<A>
{
    enum Side
    {
        left, right;

        public Side other()
        {
            return this == left
                   ? right
                   : left;
        }
    }

    // The tree I am focussing on
    final private ImTree<A> focus;

    // The parent zipper
    final private ImTreeZipper<A> parent;

    // The side that 'we came down from our parent on'
    final private Side side;

    private ImTreeZipper(ImTree<A> focus, Side side, ImTreeZipper<A> parent)
    {
        this.focus = focus;
        this.side = side;
        this.parent = parent;
    }

    /**
     * An `ImTreeZipper` on the tree `root`.
     */
    public static <A> ImTreeZipper<A> onRoot(ImTree<A> root)
    {
        return new ImTreeZipper<A>(root, null, null);
    }

    /**
     * A zipper on the leftmost descendant of the tree `root`.
     */
    public static <A> ImTreeZipper<A> onLeftmost(ImTree<A> root)
    {
        return ImTreeZipper.onRoot(root).leftmost();
    }

    /**
     * An `ImTreeZipper` on the rightmost descendant of the tree `root`.
     */
    public static <A> ImTreeZipper<A> onRightmost(ImTree<A> root)
    {
        return ImTreeZipper.onRoot(root).rightmost();
    }

    /**
     * Is this zipper looking at the root of a tree.
     */
    public boolean isRoot()
    {
        return parent == null;
    }

    private Side getSide()
    {
        return side;
    }

    private ImMaybe<ImTreeZipper<A>> nothing()
    {
        return ImMaybe.nothing();
    }

    private ImMaybe<ImTreeZipper<A>> just(ImTreeZipper<A> z)
    {
        return ImMaybe.just(z);
    }

    private ImTreeZipper<A> leftmost()
    {
        return focus == ImTree.Nil() || focus.getLeft() == ImTree.Nil()
               ? this
               : goLeft().leftmost();
    }

    private ImTreeZipper<A> rightmost()
    {
        return focus == ImTree.Nil() || focus.getRight() == ImTree.Nil()
               ? this
               : goRight().rightmost();
    }

    private ImTreeZipper<A> goRight()
    {
        return new ImTreeZipper<A>(focus.getRight(), Side.right, this);
    }

    private ImTreeZipper<A> goLeft()
    {
        return new ImTreeZipper<A>(focus.getLeft(), Side.left, this);
    }

    /**
     * <p> Go up to the parent zipper, rebalancing the focus if required.
     * <p> Our focus  is a tree that might have been replaced.
     * <p> This means that it might be out of balance with the parent's other child.
     * <p> We need to create a balanced tree and make a new parent from that and return it.
     *
     */
    private ImTreeZipper<A> goUp()
    {
        if (focus == parent.getChild(side))
        {
            // No change to our focus so we can just go up to the parent
            return parent;
        }
        else
        {
            // Our focus has changed (compared to the parent's child) so we must create a new node
            ImTree<A> node = side == Side.left
                             ? ImTree.concat3(parent.getElement(), focus, parent.getOtherChild(side))
                             : ImTree.concat3(parent.getElement(), parent.getOtherChild(side), focus);

            // and then create a new zipper on it
            return new ImTreeZipper<A>(node, parent.side, parent.parent);
        }
    }

    private ImTree<A> getOtherChild(Side sd)
    {
        return getChild(sd.other());
    }

    private ImTree<A> getChild(Side sd)
    {
        return sd == Side.left
               ? focus.getLeft()
               : focus.getRight();
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {

        List<String> things = new ArrayList<String>();

        things.add(focus.elementToString());

        ImTreeZipper<A> p = parent;

        Side s = side;
        while (p != null)
        {
            things.add("(" + s + ", " + p.getElement() + ", " + p.getOtherChild(s).elementToString() + ")");

            s = p.getSide();
            p = p.parent;
        }

        return TextUtils.join(things, ", ");
    }

    /**
     * The node that this zipper is focused on.
     */
    public ImTree<A> getFocus()
    {
        return focus;
    }

    /**
     * <p> The tree that this zipper represents, with all modifications made.
     */
    public ImTree<A> close()
    {
        return isRoot()
               ? focus
               : goUp().close();
    }

    /**
     * The zipper whose focus is `elementToFind`.
     */
    public static <A extends Comparable<? super A>> ImTreeZipper<A> find(ImTreeZipper<A> z, final A elementToFind)
    {
        if (z.focus == ImTree.<A>Nil())
        {
            return z;
        }

        final int order = elementToFind.compareTo(z.focus.getElement());

        return order == 0
               ? z
               : order < 0
                 ? find(z.goLeft(), elementToFind)
                 : find(z.goRight(), elementToFind);
    }

    /**
     * Replace the Nil tree with `newNode`
     */
    public ImTreeZipper<A> replaceNil(ImTree<A> newNode)
    {
        if (ImTree.isNil(newNode))
            return this;
        else if (focus == ImTree.Nil())
            return new ImTreeZipper<A>(newNode, side, parent);
        else
            throw new RuntimeException("You tried to replace a node on a non empty focus");
    }

    /**
     * <p> Insert
     * {@code treeToInsert}
     *  after the focus node and return the zipper with the focus on the rightmost node of
     * {@code treeToInsert}
     *
     */
    public ImTreeZipper<A> insertAfter(ImTree<A> treeToInsert)
    {
        return ImTree.isNil(treeToInsert)
               ? this
               : this.isNil()
                 ? replaceNil(treeToInsert).goToLocalRank(treeToInsert.size()).get()
                 : after().replaceNil(treeToInsert).goToLocalRank(treeToInsert.size()).get();
    }

    /**
     * <p> Replace the focus node with a tree that has element
     * {@code newElement}
     *  but has the same children.
     * The focus of the new zipper is now this new node.
     *
     */
    public ImTreeZipper<A> replaceElement(A newElement)
    {
        if (focus == ImTree.Nil())
            throw new ZipperHasNoFocusException();

        return newElement == getElement()
               ? this
               : new ImTreeZipper<A>(new ImTree<A>(newElement, focus.getLeft(), focus.getRight()), side, parent);
    }

    /**
     * Get the element that this zipper is focused on.
     */
    public A getElement()
    {
        return focus.getElement();
    }

    ImTreeZipper<A> before()
    {
        return focus == ImTree.Nil() || focus.getLeft() == null
               ? this
               : focus.getLeft() == ImTree.Nil()
                 ? goLeft()
                 : goLeft().rightmost().goRight();
    }

    /**
     * The zipper whose focus is the node that is the previous one to the current focus
     */
    public ImMaybe<ImTreeZipper<A>> previous()
    {
        return goToLocalRank(ImTree.isNil(focus)
                             ? 0
                             : focus.getRank() - 1);
    }

    ImTreeZipper<A> after()
    {
        return focus == ImTree.Nil() || focus.getRight() == null
               ? this
               : focus.getRight() == ImTree.Nil()
                 ? goRight()
                 : goRight().leftmost().goLeft();
    }

    /**
     * <p> Get the next node along from my node
     */
    public ImMaybe<ImTreeZipper<A>> next()
    {
        return goToLocalRank(focus.getRank() + 1);
    }

    /**
     * <p> An
     * {@code ImMaybe}
     *  containing tree zipper on
     * {@code tree}
     *  pointing to the node at index
     * {@code indexStartingAtOne}
     *  or
     * {@code Nothing}
     *  if
     * {@code indexStartingAtOne > tree.size()}
     * <p> Note that if
     * {@code indexStartingAtOne}
     *  is zero then the zipper is positioned
     * <em>before</em>
     *  the first element.
     *
     */
    public static <A> ImMaybe<ImTreeZipper<A>> onIndex(ImTree<A> tree, int indexStartingAtOne)
    {
        if (indexStartingAtOne > tree.size())
            return ImMaybe.nothing();
        else
            return ImMaybe.just(onRoot(tree).goToIndex(indexStartingAtOne));
    }

    /**
     * <p> The zipper on the underlying tree whose focus has rank
     * {@code indexStartingAtOne}
     *
     */
    public ImTreeZipper<A> goToIndex(int indexStartingAtOne)
    {
        final int rightIndex = indexStartingAtOne - (focus.getRank());

        return rightIndex == 0
               ? this
               : rightIndex < 0
                 ? goLeft().goToIndex(indexStartingAtOne)
                 : goRight().goToIndex(rightIndex);
    }

    /**
     * <p> Return the zipper that is focused on the node that is currently at local rank
     * {@code indexStartingAtOne}
     * .
     *
     * <p> In order to maintain zippers when the tree they are looking at gets balanced, we have to be a little careful
     * <p> Essentially, the tree that the zipper is looking at can change shape when the zipper goes up - if the tree has been modified.
     * <p> Do be clear, we mean that, when we ask a zipper to go up, we will get a new zipper - but we will also get a new tree (or the parts of
     * a new tree) if the tree has been modified.
     *
     * <p> If we do an insertBefore() of the ab tree into the cd tree at index 1
     *
     * <pre>{@code
     * ->  d       b
     *    ...     ...
     * -> c -     a -
     * }</pre>
     * <p> then we get this - showing the zipper
     *
     * <pre>{@code
     * |->   d            |->  d             |-> b
     * |     ...          |    ...              ....
     * |->  c -           |->  b                a  d
     * |   ...               ...                 ...
     * |-> b -               a c                 c -
     *    ...
     *    a -
     *
     * diagram 1         diagram 2          diagram 3
     * }</pre>
     * <p> When the zipper goes up from b, it rebalances to get (2).
     * And when it goes up again to from d it rebalances again to get(3)
     * <p> So we have a problem with implementing next() and previous(). The naive approach (that assumes that
     * the structure of the tree does not change as you go up) won't work.
     * <p> So we used to say that, to do next() on the zipper on b, we looked at the right sibling of b and saw that it
     * was nil and therefore we decided that we must go up. That logic fails with balancing.
     * <p> There is an "invariant" however and that is that the local rank of b can be predicted relative to the
     * focus of the new zipper after going up.
     * <p> In fact, if the old zipper has side left, then after going up, the rank of a node in the focus of the old zipper does not change.
     * <p> The local rank of b is 2 in (1). After going up, the local rank of b in the new focus node is still 2. And, after going
     * up again it still is 2
     * <p> The situation is a little more complicated when the side is right - but still predictable:
     * <p> It increases by the local rank of the focus (before going up)
     *
     * <pre>{@code
     *      b       <-              b       <-                    f      <-
     *    ......                 .......                      .........
     *    a    d    <-           a     f    <-                b       g
     *      ......                 ........               ........  .....
     *      c    f  <-             d      g               a      d      h
     *       ........            .....   ...                  .......
     *       e      g            c   e     h                  c     e
     *             ...
     *               h
     * }</pre>
     * <p> Consider g in the diagrams above
     * <p> It starts at rank 3 in f
     * then rank 5 = 2 (rank of d) + 3
     * then rank 7 =  2 (rank of b) + 5
     * <p> Another way of thinking about it is to observe that when you go up and rebalance the parent node you will not
     * change the order of any of the nodes. If we go up to the right then we just get some extra nodes to our right
     * and going up to the left means that we will get some new nodes to our left - z.parent.left.size + 1 of them.
     *
     */
    protected ImMaybe<ImTreeZipper<A>> goToLocalRank(int indexStartingAtOne)
    {
        //        return ImTree.isNil(getFocus())
        //                ? nothing()
        return indexStartingAtOne > getFocus().size() || indexStartingAtOne < 1
               ? isRoot()
                 ? indexStartingAtOne == 0
                   ? just(goToIndex(indexStartingAtOne).before())
                   : nothing()
                 : goUp().goToLocalRank(indexStartingAtOne + (side == Side.right
                                                              ? parent.getLocalRank()
                                                              : 0))
               : just(goToIndex(indexStartingAtOne));
    }

    /**
     * Remove the current focus from the underlying tree
     */
    public ImTreeZipper<A> removeNode()
    {
        Throw.Exception.ifTrue(isNil(), "You can't remove nil nodes");

        if (isRoot() && (getFocus().size() == 1))
        {
            return onRoot(ImTree.<A>Nil());
        }
        else
        {
            // Do the remove
            ImTreeZipper<A> newZipper = new ImTreeZipper<A>(focus.removeRoot(), side, parent);

            // Go to the element that was before it
            return newZipper.goToLocalRank(getLocalRank() - 1).get();
        }
    }

    /**
     * <p> {@code true}
     *  if the focus is
     * {@code nil}
     * .
     */
    public boolean isNil()
    {
        return ImTree.isNil(getFocus());
    }

    /**
     * <p> Get the root tree of this zipper
     */
    public ImTree<A> getRoot()
    {
        return parent == null
               ? focus
               : parent.getRoot();
    }

    /**
     * <p> The rank of the focus of this wrt itself
     */
    public int getLocalRank()
    {
        return getFocus().getRank();
    }

    /**
     * <p> The rank of the focus of this wrt the root tree
     */
    public int getRank()
    {
        return isRoot() && ImTree.isNil(focus)
               ? 0
               : getRank0();
    }

    private int getRank0()
    {
        int localRank = isNil()
                        ? 0
                        : focus.getLeft().size() + 1;

        if (isRoot())
            return localRank;
        else
        {
            // If the zipper has not been modified then the parent's left child is the same as
            // our focus so we could say this:
            //     int parentLeftChildSize = getfocus().size();
            // but we might have focus that is different from the left child of parent
            int parentLeftChildSize = parent.getFocus().getLeft().size();
            return side == Side.left
                   ? parent.getRank0() + localRank - parentLeftChildSize - 1
                   : parent.getRank0() + localRank;
        }
    }

    /**
     * Thn number of nodes after the current focus
     */
    public int getAfterSize()
    {
        return isRoot()
               ? rightSize()
               : side == Side.right
                 ? parent.getAfterSize() - (leftSize() + 1)
                 : parent.getAfterSize() + rightSize() + 1;
    }

    private int rightSize()
    {
        return isNil()
               ? 0
               : getFocus().getRight().size();
    }

    private int leftSize()
    {
        return isNil()
               ? 0
               : getFocus().getLeft().size();
    }
}