/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.tree.ImTree;
import dev.javafp.tree.ImTreeIterator;
import dev.javafp.tree.ImTreeZipper;
import dev.javafp.util.ArrayIterator;
import dev.javafp.util.TextUtils;

import java.io.Serializable;
import java.util.Iterator;

/**
 * <p> An immutable version of
 * {@code java.util.TreeSet}
 *
 *
 */
public class ImSortedSet<T extends Comparable<T>> implements Iterable<T>, Serializable
{
    // The cached hashCode value
    private int cachedHashCode = 0;

    // The tree where we store the elements
    ImTree<T> tree;

    @SuppressWarnings({ "rawtypes" })
    private static ImSortedSet empty = new ImSortedSet(ImTree.Nil());

    /**
     * <p> The (singleton) empty set.
     */
    @SuppressWarnings("unchecked")
    public static <A extends Comparable<A>> ImSortedSet<A> empty()
    {
        return (ImSortedSet<A>) empty;
    }

    /**
     * A new instance pointing to `tree` - which is used to store the elements.
     */
    protected ImSortedSet(ImTree<T> tree)
    {
        this.tree = tree;
    }

    /**
     *
     */
    @SafeVarargs
    public static <A extends Comparable<A>> ImSortedSet<A> on(A... array)
    {
        return onIterator(ArrayIterator.on(array));
    }

    public static <A extends Comparable<A>> ImSortedSet<A> onIterator(Iterator<A> iterator)
    {
        ImSortedSet<A> s = empty();

        while (iterator.hasNext())
        {
            s = s.add(iterator.next());
        }

        return s;
    }

    public static <A extends Comparable<A>> ImSortedSet<A> onAll(Iterable<A> iterable)
    {
        return iterable instanceof ImSortedSet
               ? (ImSortedSet<A>) iterable
               : onIterator(iterable.iterator());
    }

    public ImSortedSet<T> removeAll(Iterable<T> iterable)
    {
        ImSortedSet<T> result = this;

        for (T t : iterable)
        {
            result = result.remove(t);
        }

        return result;
    }

    /**
     * <p> Allows
     * {@code ImSortedSet<U>}
     *  to be cast to
     * {@code ImSortedSet<T>}
     *  (where U is a subtype of T) without generating a compiler warning.
     * <p> For a detailed description see {@link ImList#upCast()}
     *
     */
    @SuppressWarnings("unchecked")
    public <X extends Comparable<X>> ImSortedSet<X> upCast()
    {
        return (ImSortedSet<X>) this;
    }

    /**
     * <p> Add
     * {@code elementToAdd}
     *  to the set.
     * <p> The set which contains the same elements as
     * {@code this}
     *  and also contains
     * {@code elementToAdd}
     * .
     * <p> If an
     * <em>equal</em>
     *  element is already in this set then a new set will not be created - instead
     * {@code this}
     *  will be returned.
     *
     */
    public ImSortedSet<T> add(T elementToAdd)
    {
        return new ImSortedSet<T>(add(tree, elementToAdd));
    }

    /**
     * <p> Remove
     * {@code elementToRemove}
     *  from the set.
     * <p> The set which contains the same elements as
     * {@code this}
     *  but does not contain
     * {@code elementToRemove}
     * .
     * <p> If
     * {@code elementToRemove}
     *  is not in the set then a new set will not be created - instead
     * {@code this}
     *  will be returned.
     *
     */
    public ImSortedSet<T> remove(T elementToRemove)
    {
        ImTreeZipper<T> zipperOnNode = ImTreeZipper.find(ImTreeZipper.onRoot(tree), elementToRemove);

        return zipperOnNode.isNil()
               ? this
               : new ImSortedSet(zipperOnNode.removeNode().close());
    }

    /**
     * <p> If an element that
     * <em>equals</em>
     * {@code elementToFind}
     *  is in the set then return it else return
     * {@code null}
     * .
     *
     */
    public T find(T elementToFind)
    {
        ImTree<T> found = find(tree, elementToFind);

        return (found == ImTree.nil)
               ? null
               : found.getElement();
    }

    /**
     * <p> A string representation of
     * {@code this}
     * .
     *
     */
    @Override
    public String toString()
    {
        return "[" + TextUtils.join(iterator(), ", ") + "]";
    }

    /**
     * <p> The number of elements in
     * {@code this}
     * .
     *
     */
    public int size()
    {
        return tree.size();
    }

    /**
     * <p> If an element that
     * <em>equals</em>
     * {@code elementToFind}
     *  is in the set then return it else return
     * {@code null}
     * .
     *
     */
    protected static <A extends Comparable<A>> ImTree<A> find(ImTree<A> tree, A elementToFind)
    {
        // I would prefer to have a recursive method here but I can squeeze a tiny bit of extra
        // performance out of this by not recursing so...
        while (true)
        {
            if (tree == ImTree.nil)
                return tree;

            int order = elementToFind.compareTo(tree.getElement());

            if (order == 0)
                return tree;

            tree = order < 0
                   ? tree.getLeft()
                   : tree.getRight();
        }
    }

    private static <A extends Comparable<A>> ImTree<A> add(ImTree<A> tree, A elementToAdd)
    {
        if (tree == ImTree.nil)
            return ImTree.on(elementToAdd);

        int order = elementToAdd.compareTo(tree.getElement());

        return order == 0
               ? ImTree.newBalancedTree(elementToAdd, tree.getLeft(), tree.getRight())
               : order < 0
                 ? ImTree.newBalancedTree(tree.getElement(), add(tree.getLeft(), elementToAdd), tree.getRight())
                 : ImTree.newBalancedTree(tree.getElement(), tree.getLeft(), add(tree.getRight(), elementToAdd));
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
    public ImTreeIterator<T> iterator()
    {
        return new ImTreeIterator<T>(tree);
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
     * {@code this}
     *  contains
     * at least one element
     * {@code e}
     *  such that
     *
     * <pre>{@code
     * e.equals(elementToLookFor)
     * }</pre>
     *
     */
    @SuppressWarnings("unchecked")
    public boolean contains(T object)
    {
        return find(object) != null;
    }

    /**
     * <p> Compares
     * {@code other}
     *  with
     * {@code this}
     *  for equality.
     * Returns
     * {@code true}
     *  if and only if the specified object is also an
     * {@code ImSortedSet}
     * , both
     * SortedSets have the same size, and:
     *
     * <pre>{@code
     * this.containsAll(other)
     * }</pre>
     * <p> In other words, two SortedSets are defined to be
     * <em>equal</em>
     *  if they contain
     * <em>equal</em>
     *  elements.
     * <p> In
     * <em>other</em>
     *  other words - this is very similar to the standard Java SortedSet
     * {@code equals}
     * .
     *
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other)
    {
        return this == other || other instanceof ImSortedSet && eq((ImSortedSet) other);
    }

    private boolean eq(ImSortedSet<?> otherSortedSet)
    {
        return size() != otherSortedSet.size() || hashCode() != otherSortedSet.hashCode()
               ? false
               : elementsEq(iterator(), otherSortedSet.iterator());
    }

    private boolean elementsEq(ImTreeIterator<?> itOne, ImTreeIterator<?> itTwo)
    {
        while (itOne.hasNext())
        {
            if (!itOne.next().equals(itTwo.next()))
                return false;
        }

        return true;
    }

    public <O extends Comparable<O>> ImSortedSet<O> map(Fn<T, O> fn)
    {
        ImSortedSet<O> result = ImSortedSet.empty();

        Iterator<T> it = iterator();

        while (it.hasNext())
        {
            result = result.add(fn.of(it.next()));
        }

        return result;
    }

    public ImSortedSet<T> addAll(Iterable<? extends T> elementsToAdd)
    {
        ImSortedSet<T> result = this;

        for (T a : elementsToAdd)
        {
            result = result.add(a);
        }

        return result;
    }

    /**
     * <p> The ImSortedSet formed out of the elements of each collection in
     * {@code collectionOfCollections}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #addAll(Iterable)
     *
     */
    public static <A extends Comparable<A>> ImSortedSet<A> join(Iterable<? extends Iterable<? extends A>> iterable)
    {
        ImSortedSet<A> concat = ImSortedSet.empty();

        for (Iterable<? extends A> as : iterable)
            concat = concat.addAll(as);

        return concat;
    }

    @Override
    public int hashCode()
    {
        if (cachedHashCode == 0)
            cachedHashCode = computeHash(10);

        return cachedHashCode;
    }

    public int computeHash(int count)
    {
        return ImList.onAll(this).hashCode(count);
    }

}