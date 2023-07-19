/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.shelf;

import dev.javafp.eq.Eq;
import dev.javafp.ex.ImIndexOutOfBoundsException;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.tree.ImTree;
import dev.javafp.tree.ImTreeIterator;
import dev.javafp.tree.ImTreeZipper;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ArrayIterator;
import dev.javafp.util.Caster;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.NullCheck;
import dev.javafp.util.TextUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> An ordered list of objects with addition, removal and access methods having performance
 * <strong>{@code O}</strong>
 * {@code (log n)}
 * where
 * {@code n}
 *  is the number of elements in the shelf.
 * <h2>Introduction</h2>
 * <p> You could also think of this as an immutable version of
 * {@code org.apache.commons.collections.list.TreeList}
 * .
 * <p> This is the nearest equivalent to java.util.List in the sense that add/find/remove/replace is relatively fast.
 * <p> The idea of the name
 * <em>Shelf</em>
 *  is that, if you have a collection of books on a bookshelf, then
 * the books are arranged in a sequence and it is reasonably quick
 * to add/find/replace/remove a book at an arbitrary position.
 * <img src="doc-files/shelf.png" alt="shelf" style="width: 20%"/>
 * <h2>methods implemented by this class:</h2>
 * <ul>
 * <li>
 * <p> {@link #on(Object...)}
 * </li>
 * <li>
 * <p> {@link #onAll(Iterable)}
 * </li>
 * <li>
 * <p> {@link #empty()}
 * </li>
 * <li>
 * <p> {@link #joinArray(Iterable...)}
 * </li>
 * <li>
 * <p> {@link #joinIterator(Iterator)}
 * </li>
 * <li>
 * <p> {@link #join(Iterable)}
 * </li>
 * <li>
 * <p> {@link #addingAll(Iterable)}
 * </li>
 * <li>
 * <p> {@link #upCast()}
 * </li>
 * <li>
 * <p> {@link #contains(Object)}
 * </li>
 * <li>
 * <p> {@link #size()}
 * </li>
 * <li>
 * <p> {@link #iterator()}
 * </li>
 * <li>
 * <p> {@link #getZipper()}
 * </li>
 * <li>
 * <p> {@link #toString()}
 * </li>
 * </ul>
 * <p> See the
 * <a href="{@docRoot}/im/package-summary.html">
 * package summary
 * </a>
 * for more details of the standard methods.
 * <h2>Query</h2>
 * <ul>
 * <li>
 * <p> {@link #get(int)}
 * </li>
 * <li>
 * <p> {@link #indexOf(Object)}
 * </li>
 * </ul>
 * <h2>Mutation</h2>
 * <ul>
 * <li>
 * <p> {@link #add(Object)}
 * </li>
 * <li>
 * <p> {@link #add(int, Object)}
 * </li>
 * <li>
 * <p> {@link #set(int, Object)}
 * </li>
 * <li>
 * <p> {@link #remove(int)}
 * </li>
 * </ul>
 * <h2>Implementation Notes</h2>
 * <p> A shelf keeps its elements in a balanced binary tree.
 * <p> This shelf:
 *
 * <pre>{@code
 * [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
 * }</pre>
 * <p> has a tree that looks something like this (the exact layout of the tree a will vary depending on
 * the order in which the elements were added):
 *
 * <pre>{@code
 *      4
 *  .......
 *  2     8
 * ...  .....
 * 1 3  6   9
 *     ... ...
 *     5 7 - 10
 * }</pre>
 * <p> If we add
 * {@code 0}
 *  to the front:
 *
 * <pre>{@code
 * [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
 * }</pre>
 * <p> the new tree looks like this:
 *
 * <pre>{@code
 *       4
 *   ........
 *   2      8
 *  ....  .....
 *  1  3  6   9
 * ...   ... ...
 * 0 -   5 7 - 10
 * }</pre>
 * <p> Only
 * {@code 4}
 *  new nodes need to be created - the other nodes can be shared with the previous tree.
 * <p> If we add
 * {@code 11}
 *  to the end:
 *
 * <pre>{@code
 * [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
 * }</pre>
 * <p> The new tree now looks like this:
 *
 * <pre>{@code
 *       4
 *   ........
 *   2      8
 *  ....  .....
 *  1  3  6   10
 * ...   ... ...
 * 0 -   5 7 9 11
 * }</pre>
 * <p> Again,
 * {@code 4}
 *  new nodes are created.
 * <p> See the
 * <a href="{@docRoot}/im/package-summary.html">
 * package summary
 * </a>
 * for more details.
 *
 */
public class ImShelf<T> implements Iterable<T>
{

    public static class ImShelfIterator<T> implements Iterator<T>
    {
        ImTreeIterator<T> treeIterator;

        public ImShelfIterator(ImTreeIterator<T> treeIterator)
        {
            this.treeIterator = treeIterator;
        }

        public boolean hasNext()
        {
            return treeIterator.hasNext();
        }

        public T next()
        {
            return treeIterator.next();
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p> The singleton empty object
     */
    private static ImShelf<?> empty = new ImShelf<Object>();

    /**
     * <p> The tree that holds the elements
     */
    final private ImTree<T> tree;

    /**
     * <p> The constructor for the empty shelf
     */
    private ImShelf()
    {
        this(ImTree.<T>Nil());
    }

    ImShelf(ImTree<T> tree)
    {
        this.tree = tree;
    }

    /**
     * <p> The (singleton) empty shelf.
     */
    @SuppressWarnings("unchecked")
    public static <T> ImShelf<T> empty()
    {
        return on();
    }

    /**
     * <p> The shelf whose elements are obtained from
     * {@code array}
     * .
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements in
     * {@code array}
     *  can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 3)                      => [1, 2, 3]
     * on(new Integer[] { 1, 2, 3 })    => [1, 2, 3]
     * on()                             => []
     * on(null, 2, 3)                   => throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code array}
     *  is
     * {@code null}
     * @see #onAll(Iterable)
     * @see #onIterator(Iterator)
     * @see #on
     *
     */
    @SafeVarargs
    public static <A> ImShelf<A> on(A... array)
    {
        return onIterator(ArrayIterator.on(array));
    }

    /**
     * <p> The shelf whose elements are obtained from
     * {@code elementsCollection}
     * .
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements in
     * {@code elementsCollection}
     *  can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(Arrays.asList(1, 2, 3))    => [1, 2, 3]
     * on(Arrays.asList())           => []
     * on(Arrays.asList(1, null, 3)) => throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code elementsCollection}
     *  is
     * {@code null}
     * @see #onIterator(Iterator)
     * @see #on(Object...)
     * @see #on
     *
     */
    @SuppressWarnings("unchecked")
    public static <A> ImShelf<A> onAll(Iterable<? extends A> elementsCollection)
    {
        return elementsCollection instanceof ImShelf
               ? (ImShelf<A>) elementsCollection
               : onIterator(elementsCollection.iterator());
    }

    /**
     * <p> The shelf whose elements are obtained by iterating over
     * {@code iterator}
     * .
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements in
     * {@code iterator}
     *  can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(Arrays.asList(1, 2, 3).iterator())    => [1, 2, 3]
     * on(Arrays.asList().iterator())           => []
     * on(Arrays.asList(1, null, 3).iterator()) => throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code iterator}
     *  is
     * {@code null}
     * @see #onAll(Iterable)
     * @see #on(Object...)
     * @see #on
     *
     */
    @SuppressWarnings("unchecked")
    public static <A> ImShelf<A> onIterator(Iterator<? extends A> iterator)
    {
        ImShelf<A> result = (ImShelf<A>) empty;

        while (iterator.hasNext())
        {
            result = result.add(iterator.next());
        }

        return result;
    }

    /**
     * <p> Allows
     * {@code ImShelf<U>}
     *  to be cast to
     * {@code ImShelf<T>}
     *  (where U is a subtype of T) without generating a compiler warning.
     * <p> For a detailed description see {@link ImList#upCast()}
     *
     */
    @SuppressWarnings("unchecked")
    public <U> ImShelf<U> upCast()
    {
        return (ImShelf<U>) this;
    }

    /**
     * <p> Add
     * {@code elementToAdd}
     *  at index
     * {@code indexStartingAtOne}
     * .
     * <p> All elements with
     * {@code index >= indexStartingAtOne}
     *  are shuffled up.
     * <p> ImCollections can't contain
     * {@code null}
     *  so
     * {@code elementToAdd}
     *  must not be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 3, 5).add(2, 8)  =>  [1, 8, 2, 3, 5]
     * on(1, 2, 3).add(4, 8)     =>  [1, 2, 3, 8]
     * on(1, 2, 3).add(13, 8)    =>  java.lang.IndexOutOfBoundsException: index should be in the range [1, 4]  but was 13
     * }</pre>
     * <p> @throws IndexOutOfBoundsException
     *
     */
    public ImShelf<T> add(int indexStartingAtOne, T elementToAdd)
    {
        ImIndexOutOfBoundsException.check(indexStartingAtOne, 1, size() + 1, "index");
        NullCheck.check(elementToAdd);

        return new ImShelf<T>(getTree().insert(indexStartingAtOne, elementToAdd));
    }

    /**
     * <p> Add
     * {@code elementToAdd}
     *  at the end.
     * <p> ImCollections can't contain
     * {@code null}
     *  so
     * {@code elementToAdd}
     *  must not be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2).add(3)        =>  [1, 2, 3]
     * on().add(1)            =>  [1]
     * on(1, 2, 3).add(null)  =>  throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException
     *
     */
    public ImShelf<T> add(T elementToAdd)
    {
        return add(size() + 1, elementToAdd);
    }

    /**
     * <p> A string representation of this shelf.
     * <p> This representation is essentially the same as {@link ImList#toString(String)}
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
        return getTree().size();
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public ImShelfIterator<T> iterator()
    {
        return new ImShelfIterator<T>(new ImTreeIterator<T>(getTree()));
    }

    public ImList<T> toImList()
    {
        return ImList.onIterator(iterator());
    }

    /**
     * <p> Set the element at index
     * {@code indexStartingAtOne}
     *  to
     * {@code elementToSet}
     * .
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 3).set(1, 5)  =>  [5, 2, 3]
     * on(1, 2, 3).set(3, 8)  =>  [1, 2, 8]
     * on(1, 2).set(-1, 4)    =>  java.lang.IndexOutOfBoundsException: index should be in the range [1, 3]  but was -1
     * }</pre>
     * <p> @throws IndexOutOfBoundsException
     * @throws NullPointerException
     *
     */
    public ImShelf<T> set(int indexStartingAtOne, T elementToSet)
    {
        ImIndexOutOfBoundsException.check(indexStartingAtOne, 1, size() + 1, "indexStartingAtOne");
        NullCheck.check(elementToSet);

        ImTreeZipper<T> path = ImTreeZipper.onRoot(getTree()).goToIndex(indexStartingAtOne);

        return new ImShelf<T>(path.replaceElement(elementToSet).close());
    }

    /**
     * <p> The element at index
     * {@code indexStartingAtOne}
     * .
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 3).get(1)  =>  1
     * on(1, 2, 3).get(3)  =>  3
     * on(1, 2).get(3)     =>  java.lang.IndexOutOfBoundsException: the size of the collection is 2 but indexStartingAtOne was 3
     * on().get(1)         =>  java.lang.IndexOutOfBoundsException: The collection is empty but indexStartingAtOne was 1
     * }</pre>
     * <p> @throws IndexOutOfBoundsException
     *
     */
    public T get(int indexStartingAtOne)
    {
        return getTree().getNodeAtIndex(indexStartingAtOne).getElement();
    }

    /**
     * <p> Remove the element at index
     * {@code indexStartingAtOne}
     * .
     * <p> All elements with
     * {@code index > indexStartingAtOne}
     *  are shuffled down.
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 8, 2, 3, 5).remove(2)  =>  [1, 2, 3, 5]
     * on(1, 2, 3).remove(0)        =>  java.lang.IndexOutOfBoundsException: indexStartingAtOne should be >= 1  but was 0
     * }</pre>
     * <p> @throws IndexOutOfBoundsException
     * @see  #remove(int)
     *
     */
    public ImShelf<T> remove(int indexStartingAtOne)
    {
        return partitionAtIndex(indexStartingAtOne).snd;
    }

    /**
     * <p> Return a pair containing
     * <ol>
     * <li>
     * <p> the element at index
     * {@code indexStartingAtOne}
     * </li>
     * <li>
     * <p> the shelf with that element removed - elements with
     * {@code index >= indexStartingAtOne}
     *  are shuffled down.
     * </li>
     * </ol>
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 8, 2, 3, 5).remove(2)  =>  (8, [1, 2, 3, 5])
     * on(1, 2, 3).remove(0)        =>  java.lang.IndexOutOfBoundsException: indexStartingAtOne should be >= 1  but was 0
     * }</pre>
     * <p> @throws IndexOutOfBoundsException
     * @see #remove(int)
     *
     */
    public ImPair<T, ImShelf<T>> partitionAtIndex(int indexStartingAtOne)
    {
        ImIndexOutOfBoundsException.check(indexStartingAtOne, size(), "indexStartingAtOne");
        ImTreeZipper<T> path = ImTreeZipper.onRoot(tree).goToIndex(indexStartingAtOne);

        return ImPair.on(path.getElement(), new ImShelf(path.removeNode().close()));
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains
     * {@code elementToLookFor}
     * ,
     * {@code false}
     *  otherwise.
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
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 1).contains(1)     =>  true
     * on(1, 2, 1).contains(null)  =>  throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     *
     */
    public boolean contains(Object elementToLookFor)
    {
        return indexOf(elementToLookFor) != -1;
    }

    /**
     * <p> Returns the index (starting at one) of the first occurrence of
     * {@code elementToLookFor}
     * ,
     * or -1 if no such element exists.
     * <p> More formally, returns the lowest index
     * {@code i}
     *  such that
     *
     * <pre>{@code
     * get(i).equals(elementToLookFor)
     * }</pre>
     * <p> or
     * {@code -1}
     *  if there is no such index.
     *
     * <pre>{@code
     * on(1, 2, 3).indexOf(1)      =>  1
     * on(1, 2, 3).indexOf(1.0)    =>  -1
     * on(1, 2, 1).contains(null)  =>  throws java.lang.NullPointerException: ImCollections can't contain nulls
     * }</pre>
     * @see #indexOf(Object)
     *
     */
    public int indexOf(Object elementToLookFor)
    {
        NullCheck.check(elementToLookFor);

        int index = 0;

        Iterator<T> it = iterator();

        while (it.hasNext())
        {
            index++;
            if (Eq.uals(it.next(), elementToLookFor))
                return index;
        }

        return -1;
    }

    ImShelfZipper<T> getZipperOnIndex(int indexStartingAtOne)
    {
        ImMaybe<ImTreeZipper<T>> m = ImTreeZipper.onIndex(getTree(), indexStartingAtOne);

        if (m.isPresent())
        {
            return new ImShelfZipper<T>(this, m.get());
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    /**
     * <p> A zipper on
     * {@code this}
     * .
     * <p> A
     * <em>zipper</em>
     *  is a bit like a standard java iterator that lets you "mutate" the underlying collection.
     * <p> Zippers allows you to navigate backwards and forwards in the shelf and to insert/replace/remove elements efficiently in batches.
     * The efficiency comes from the fact that the changes are made to the zipper rather than to the shelf directly
     * and several changes can be made in a batch. It is only when you close the zipper that you can see the changes.
     * <p> Of course, shelfs are immutable so the underlying shelf does not change. In fact, zippers are also immutable
     * so as you "change" the zipper you are actually creating a new zipper.
     * Each time you close a zipper you will create a new shelf (provided you have made changes).
     * <p> See the
     * <a href="{@docRoot}/im/package-summary.html">
     * package summary
     * </a>
     * for details of how zippers work.
     *
     */
    public ImShelfZipper<T> getZipper()
    {
        return new ImShelfZipper<T>(this, ImTreeZipper.onLeftmost(getTree()).before());
    }

    /**
     * <p> The shelf where the i-th element is the result of evaluating the single argument function
     * {@code fn}
     *  on
     * the i-th element of
     * {@code this}
     * .
     * <p> See the
     * <a href="{@docRoot}/im/functions/package-summary.html">
     * package summary
     * </a>
     * for more details about functions.
     * <h4>Examples:</h4>
     * <p> To convert a list of objects to a list of their string representations:
     *
     * <pre>{@code
     * Function1<String> toStringFn = FnFactory.on(Object.class).getFn(String.class, "toString");
     * onArray(1, 2).map(toStringFn) =>  "[1, 2]"
     * }</pre>
     * <p> To parse a list of strings to their
     * {@code Integer}
     *  equivalents:
     *
     * <pre>{@code
     * Function1<Integer> parseIntFn = FnFactory.on(Integer.class).getFnStatic(int.class, "parseInt", String.class);
     * ImShelf.onArray("1", "2").map(parseIntFn)  =>  [1, 2]
     * }</pre>
     * <p> To parse with a radix of
     * {@code 16}
     * , we first get the parse function with two arguments, then we create a new function
     * out of this with the arguments reversed. Then, if we supply the radix argument of
     * {@code 16}
     *  to this function,
     * we will have a new function of one argument that does what we want:
     *
     * <pre>{@code
     * Function2<Integer> parseIntWithRadixFn = FnFactory.on(Integer.class).getFnStatic( //
     *         int.class, "parseInt", String.class, int.class);
     *
     * Function1<Integer> parseHexFn = parseIntWithRadixFn.flip().invoke(16);
     * ImShelf.onArray("8", "D", "15").map(parseHexFn)  =>  [8, 13, 21]
     * }</pre>
     *
     */
    public <O> ImShelf<O> map(Fn<T, O> fn)
    {
        ImShelf<O> mapped = ImShelf.empty();

        Iterator<T> it = iterator();

        while (it.hasNext())
        {
            mapped = mapped.add(fn.of(it.next()));
        }

        return mapped;

        // This was my first attempt - but it does not run fn on the elements in a predictable order.
        // Hmm - of course this would not matter in a pure functional language...
        // return new ImShelf<O>(getTree().map(fn));
    }

    ImTree<T> getTree()
    {
        return tree;
    }

    /**
     * <p> The ImShelf formed out of the elements of each collection in
     * {@code collections}
     *  in order.
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * List<Number> threeFive = Arrays.<Number> asList(3, 5);
     * ImShelf<Integer> oneTwo = ImShelf.on(1, 2);
     *
     * concat(oneTwo, threeFive)          =>  [1, 2, 3, 5]");
     * concat(oneTwo, threeFive, oneTwo)  =>  [1, 2, 3, 5, 1, 2]
     * concat(on(), on())                 =>  []
     * }</pre>
     * @see #addingAll(Iterable)
     * @see #joinIterator(Iterator)
     * @see #join(Iterable)
     *
     */
    @SafeVarargs
    public static <A> ImShelf<A> joinArray(Iterable<? extends A>... collections)
    {
        return joinIterator(ArrayIterator.on(collections));
    }

    /**
     * <p> The ImShelf formed out of the elements of each collection in
     * {@code iterator}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #addingAll(Iterable)
     * @see #joinArray(Iterable...)
     * @see #join(Iterable)
     *
     */
    public static <A> ImShelf<A> joinIterator(Iterator<Iterable<? extends A>> iterator)
    {
        ImShelf<A> concat = ImShelf.empty();

        while (iterator.hasNext())
            concat = concat.addingAll(ImShelf.onAll(iterator.next()));

        return concat;
    }

    /**
     * <p> The ImShelf formed out of the elements of each collection in
     * {@code collectionOfCollections}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #addingAll(Iterable)
     * @see #joinArray(Iterable...)
     * @see #joinIterator(Iterator)
     *
     */
    public static <A> ImShelf<A> join(Iterable<? extends Iterable<? extends A>> collectionOfCollections)
    {
        return joinIterator(Caster.cast(collectionOfCollections.iterator()));
    }

    /**
     * <p> The ImShelf formed out of the elements of
     * {@code this}
     *  followed by the elements in
     * {@code other}
     *  in order.
     * <p> ImCollections can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #join(Iterable)
     * @see #joinArray(Iterable...)
     * @see #joinIterator(Iterator)
     *
     */
    public ImShelf<T> addingAll(Iterable<? extends T> other)
    {
        ImShelf<T> otherShelf = ImShelf.onAll(other);
        return new ImShelf<T>(ImTree.merge(tree, otherShelf.getTree()));
    }

    /**
     * <p> Compares
     * {@code another}
     *  with
     * {@code this}
     *  for equality.
     * <p> Returns
     * {@code true}
     *  if and only if the specified object is also an
     * {@code ImShelf}
     * , both
     * lists have the same size, and all corresponding pairs of elements in
     * the two lists are
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
     * <p> In other words, two ImShelfs are defined to be
     * <em>equal</em>
     *  if they contain
     * <em>equal</em>
     *  elements in the same order.
     * <p> In
     * <em>other</em>
     *  other words - this is very similar to the standard Java List
     * {@code equals}
     * .
     *
     */
    @Override
    public boolean equals(Object other)
    {
        return this == other
               ? true
               : other instanceof ImShelf
                 ? eq((ImShelf<?>) other)
                 : false;
    }

    private boolean eq(ImShelf<?> otherShelf)
    {
        return size() == otherShelf.size() && hashCode() == otherShelf.hashCode()
               ? elementsEq(iterator(), otherShelf.iterator())
               : false;
    }

    private boolean elementsEq(ImShelfIterator<?> itOne, ImShelfIterator<?> itTwo)
    {
        while (itOne.hasNext())
        {
            if (!itOne.next().equals(itTwo.next()))
                return false;
        }
        return true;
    }

    /**
     * <p> The hash code value for
     * {@code this}
     * .
     * <p> Because a shelf is immutable, the hash code is calculated only once when the shelf is
     * created and cached.
     * <p> This implementation uses a different algorithm from that used in {@link ImList}.
     * <p> The hash code for a shelf is the sum of the hash codes of its elements. In this regard it
     * is similar to the algorithm used by
     * {@code java.util.AbstractSet}
     *
     */
    @Override
    public int hashCode()
    {
        return tree.hashCode();
    }
}