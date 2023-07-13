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
import java.util.Collection;
import java.util.Iterator;

/**
 * <p> An immutable version of
 * {@code java.util.SortedSet}
 * .
 * <h2>Introduction</h2>
 *
 */
public class ImSortedSet<T extends Comparable<? super T>> implements Iterable<T>, Serializable
{
    final ImTree<T> tree;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final ImSortedSet<?> empty = new ImSortedSet(ImTree.Nil());

    /**
     * <p> The (singleton) empty set.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> ImSortedSet<T> empty()
    {
        return (ImSortedSet<T>) empty;
    }

    protected ImSortedSet(final ImTree<T> tree)
    {
        this.tree = tree;
    }

    /**
     *
     */
    @SafeVarargs
    public static <A extends Comparable<A>> ImSortedSet<A> onArray(final A... array)
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

    public static <A extends Comparable<A>> ImSortedSet<A> onAll(ImList<A> list)
    {

        return onIterator(list.iterator());
    }

    @SuppressWarnings("unchecked")
    public static <A extends Comparable<A>> ImSortedSet<A> onAll(Collection<? extends A> elementsCollection)
    {
        if (elementsCollection instanceof ImSortedSet)
            return (ImSortedSet<A>) elementsCollection;
        else
        {
            // We tried to use on(Iterator) here but the type checking defeated us
            ImSortedSet<A> s = empty();
            for (A a : elementsCollection)
            {
                s = s.add(a);
            }

            return s;
        }
    }

    public static <A extends Comparable<A>> ImSortedSet<A> on(A element)
    {
        return ImSortedSet.<A>empty().add(element);
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
        return (ImSortedSet) this;
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
    public ImSortedSet<T> add(final T elementToAdd)
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
    public ImSortedSet<T> remove(final T elementToRemove)
    {
        return new ImSortedSet<T>(remove(tree, elementToRemove));
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
    public T find(final T elementToFind)
    {
        final ImTree<T> found = find(tree, elementToFind);

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

    // This version of the method is the one I would prefer to use. See the other version below
    //     public static <A extends Comparable<A>> ImTree<A> find1(final ImTree<A> tree, final A elementToFind)
    //     {
    //          if (tree == ImTree.nil)
    //               return tree;
    //
    //          final int order = elementToFind.compareTo(tree.element);
    //
    //          return order == 0
    //                    ? tree
    //                    : order < 0
    //                              ? find(tree.left, elementToFind)
    //                              : find(tree.right, elementToFind);
    //     }

    /**
     * <p> If an element that
     * <em>equals</em>
     * {@code elementToFind}
     *  is in the set then return it else return
     * {@code null}
     * .
     *
     */
    public static <A extends Comparable<? super A>> ImTree<A> find(ImTree<A> tree, final A elementToFind)
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

    private static <A extends Comparable<? super A>> ImTree<A> add(final ImTree<A> tree, final A elementToAdd)
    {
        if (tree == ImTree.nil)
            return ImTree.on(elementToAdd);

        final int order = elementToAdd.compareTo(tree.getElement());

        return order == 0
               ? ImTree.newBalancedTree(elementToAdd, tree.getLeft(), tree.getRight())
               : order < 0
                 ? ImTree.newBalancedTree(tree.getElement(), add(tree.getLeft(), elementToAdd), tree.getRight())
                 : ImTree.newBalancedTree(tree.getElement(), tree.getLeft(), add(tree.getRight(), elementToAdd));
    }

    private static <A extends Comparable<? super A>> ImTree<A> remove(final ImTree<A> tree, final A elementToRemove)
    {
        return ImTreeZipper.find(ImTreeZipper.onRoot(tree), elementToRemove).removeNode().close();
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
    public boolean contains(Object object)
    {
        return find((T) object) != null;
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
        return this == other
               ? true
               : other instanceof ImSortedSet
                 ? eq((ImSortedSet) other)
                 : false;
    }

    private boolean eq(ImSortedSet<?> otherSortedSet)
    {
        return size() == otherSortedSet.size() && hashCode() == otherSortedSet.hashCode()
               ? elementsEq(iterator(), otherSortedSet.iterator())
               : false;
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

    @Override
    public int hashCode()
    {
        return tree.hashCode();
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
     * {@code collections}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * List<Number> threeFive = Arrays.<Number> asList(3, 5);
     * ImSortedSet<Integer> oneTwo = ImSortedSet.on(1, 2);
     *
     * joinArray(oneTwo, threeFive)          =>  [1, 2, 3, 5]");
     * joinArray(oneTwo, threeFive, oneTwo)  =>  [1, 2, 3, 5, 1, 2]
     * joinArray(on(), on())                 =>  []
     * }</pre>
     * @see #addAll(Iterable)
     * @see #joinIterator(Iterator)
     * @see #join(Collection)
     *
     */
    @SafeVarargs
    public static <A extends Comparable<A>> ImSortedSet<A> joinArray(Collection<? extends A>... collections)
    {
        return joinIterator(ArrayIterator.on(collections));
    }

    /**
     * <p> The ImSortedSet formed out of the elements of each collection in
     * {@code iterator}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #addAll(Iterable)
     * @see #joinArray(Collection...)
     * @see #join(Collection)
     *
     */
    public static <A extends Comparable<A>> ImSortedSet<A> joinIterator(Iterator<Collection<? extends A>> iterator)
    {
        ImSortedSet<A> concat = ImSortedSet.empty();

        while (iterator.hasNext())
            concat = concat.addAll(ImSortedSet.onAll(iterator.next()));

        return concat;
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
     * @see #joinArray(Collection...)
     * @see #joinIterator(Iterator)
     *
     */
    @SuppressWarnings("unchecked")
    public static <A extends Comparable<A>> ImSortedSet<A> join(
            Collection<? extends Collection<? extends A>> collectionOfCollections)
    {
        return joinIterator((Iterator<Collection<? extends A>>) collectionOfCollections.iterator());
    }
}