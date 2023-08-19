/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.HasTextBox;
import dev.javafp.eq.Equals;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.lst.ImList;
import dev.javafp.util.ArrayIterator;
import dev.javafp.util.Hash;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.NullCheck;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> An immutable version of
 * {@code java.util.Set}
 * .
 * <h2>Introduction</h2>
 * </p>
 * <p> An
 * {@code ImSet}
 *  is a collection of objects with the condition that, if an object
 * {@code o}
 *  belongs
 * to the set then, for any other member
 * {@code a}
 *  of the set:
 *
 * <pre>{@code
 * a.equals(o) == false
 * }</pre>
 * <p> We therefore assume that the objects in the set have "reasonable" implementations of
 * {@code hashCode()}
 *  and
 * {@code equals()}
 * <p> A web search for the phrase
 * {@code always override hashCode when you override equals}
 *  will provide more details of
 * the issues involved.
 * <p> {@code ImSets}
 *  cannot contain
 * {@code null}
 * .
 *
 * <h3>Add/Remove/Find/Replace</h3>
 *
 * <p> Note that, since
 * {@code ImSet}
 * s are immutable, you can't <strong>literally</strong> add/remove/replace elements in a set. Rather, you create a <strong>new</strong> set which
 * has the same elements as the previous set
 * except that a certain element is added/removed/replaced.
 *
 * <p> You can "add/replace" elements to the set using {@link #add} {@link #add} {@link #replace} and "remove" them
 * using {@link #remove}.
 * <p> You can test if an element is in the set using {@link #contains} or {@link #find}.
 * <p> Methods that find/add/replace elements are
 * <strong>{@code O}</strong>
 * {@code (log(n))}
 *  where n is the size of the set and log is base 2.
 *
 * <h2>Replacing Elements</h2>
 * <p> Sometimes it is convenient to be able to add an element to a set even though that set already contains an
 * element that is "equal to it".
 *
 * <p> For example {@link ImMap} is implemented as a set of {@link ImMap.Entry} objects where an entry has a key and a value.
 * Two {@link ImMap.Entry} objects are equal
 * iff their keys are equal. So for any pair of entries
 * {@code e1}
 *  and
 * {@code e2}
 * :
 *
 * <pre>{@code
 * e1.equals(e2) == e1.key.equals(e2.key)
 * }</pre>
 * <p> When we want to replace an entry, we want the new entry with its new value to be inserted in the set, even though
 * there is an existing entry with the same key.
 * <p> To allow this, there are methods {@link #replace} and {@link #add}. The default behaviour when using
 * {@link #add}  is to
 * <em>not</em>
 *  replace
 * <h3>Implementation</h3>
 * <p> An ImSet is a sorted set of buckets where
 * elements whose hash codes are the same are stored in the same bucket.
 * <p> Buckets are sorted on the hash value of their elements.
 * <p> To find an element, we first find the bucket with the matching hash value and then look through the bucket to
 * determine if the element is present.
 * <p> For sets of elements with reasonable hash functions, the average bucket size will be one.
 * @see ImSortedSet
 *
 */
public class ImSet<T> implements HasTextBox, Iterable<T>, Serializable
{

    // The cached hashCode value
    private int cachedHashCode = 0;

    /**
     * <p> Enumeration containing values
     * {@code yes}
     *  and
     * {@code no}
     *
     */
    public enum Replace
    {
        /**
         * <p> Replace the element in the set - even if an
         * <em>equal</em>
         *  element already exists.
         *
         */
        yes,

        /**
         * <p> Don't replace the element in the set if an
         * <em>equal</em>
         *  element already exists.
         *
         */
        no
    }

    // All the elements with the same hash code are stored in a bucket
    static class Bucket<B> implements Comparable<Bucket<B>>, Serializable
    {
        private static final Object[] EMPTY_ARRAY = new Object[] {};
        final int hashCode;
        final B[] elements;

        Bucket(final int hashCode, final B[] elements)
        {
            this.hashCode = hashCode;
            this.elements = elements;
        }

        @SuppressWarnings("unchecked")
        public Bucket(final int hashCode)
        {
            this(hashCode, (B[]) EMPTY_ARRAY);
        }

        @SuppressWarnings("unchecked")
        public static <C> Bucket<C> newBucket(final int hashCode, final C element)
        {
            return new Bucket<C>(hashCode, (C[]) new Object[] { element });
        }

        public int compareTo(final Bucket<B> other)
        {
            return Integer.compare(hashCode, other.hashCode);
        }

        // If an equal element is already in this bucket then it should be
        // replaced
        Bucket<B> add(final B newElement, Replace replace)
        {
            return newBucketAddingElement(newElement, replace);
        }

        private Bucket<B> newBucketAddingElement(final B newElement, Replace replace)
        {

            int index = indexOf(newElement);

            if (index >= 0)
            {
                // newElement (or something equal to it) was in there already
                if (replace == Replace.no)
                    return this;

                // Copy the array
                B[] newElementsArray = Arrays.copyOf(elements, elements.length);

                // Put `newElement` in at the correct slot
                newElementsArray[index] = newElement;

                return new Bucket<B>(hashCode, newElementsArray);
            }
            else
            {
                // `newElement` was not there already. Create an array with an extra slot and copy the old
                // array into it

                // Copy the array
                B[] newElementsArray = Arrays.copyOf(elements, elements.length + 1);

                // Put `newElement` in the last slot
                newElementsArray[elements.length] = newElement;

                return new Bucket<B>(hashCode, newElementsArray);
            }
        }

        @SuppressWarnings("unchecked")
            // If an equals element is not in this bucket then the bucket should
            // not change
        Bucket<B> remove(final B elementToRemove)
        {

            int index = indexOf(elementToRemove);

            // If `elementToRemove` is not in the array then we are done
            if (index < 0)
                return this;

            // So - elementToRemove is in the array

            if (size() == 1)
                return new Bucket<B>(hashCode, (B[]) EMPTY_ARRAY);

            // Create a new array
            // a b c d
            // If we are removing b then after the copy the new array looks like this:
            // a b c
            // Now copy from c to d from the old array into the new array:
            // a c d
            B[] newArray = Arrays.copyOf(elements, elements.length - 1);
            System.arraycopy(elements, index + 1, newArray, index, newArray.length - index);

            return new Bucket<B>(hashCode, newArray);
        }

        public B get(final B element)
        {
            int index = indexOf(element);
            return index >= 0
                   ? elements[index]
                   : null;
        }

        /**
         * A String representation of this object
         */
        @Override
        public String toString()
        {
            return "( " + hashCode + ": " + elements + ")";
        }

        /**
         * <p> The number of elements in
         * {@code this}
         *
         */
        public int size()
        {
            return elements.length;
        }

        private int indexOf(Object newElement)
        {
            for (int i = 0; i < elements.length; i++)
            {
                if (elements[i].equals(newElement))
                {
                    return i;
                }
            }
            return -1;
        }

        public boolean hasEqualElements(Object other)
        {
            if (!(other instanceof Bucket))
                return false;

            Bucket<?> otherBucket = (Bucket<?>) other;

            if (size() != otherBucket.size())
                return false;

            for (Object element : otherBucket.elements)
            {
                if (indexOf(element) == -1)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }

    /**
     * <p> The sorted set that contains the buckets
     */
    final ImSortedSet<Bucket<T>> sortedSetOfBuckets;

    /**
     * <p> The number of elements in this set
     */
    final private int size;

    protected String getStats()
    {
        int total = 0;
        int totalElements = 0;
        int max = 0;
        int maxElements = 0;

        ImTreeIterator<Bucket<T>> it = sortedSetOfBuckets.iterator();

        while (it.hasNext())
        {
            Bucket<T> b = it.next();
            total += b.size();
            totalElements += b.elements.length;
            max = Math.max(max, b.size());
            maxElements = Math.max(maxElements, b.elements.length);
        }

        return //
                "# buckets = " + sortedSetOfBuckets.size() + //
                        "\nmax bucket size = " + max + //
                        "\ntotal  = " + total + //
                        "\nheight  = " + sortedSetOfBuckets.tree.getHeight() + //
                        "\naverage bucket size = " + total / (float) sortedSetOfBuckets.size() + //
                        "\nmaxElements  = " + maxElements + //
                        "\ntotalElements  = " + totalElements + //
                        "\naverage elements length = " + totalElements / (float) sortedSetOfBuckets.size();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final ImSet<?> empty = new ImSet(ImSortedSet.empty(), 0);

    /**
     * <p> The (singleton) empty set.
     */
    @SuppressWarnings("unchecked")
    public static <TT> ImSet<TT> empty()
    {
        return (ImSet<TT>) empty;
    }

    /**
     * <p> The singleton empty set
     */
    static <A> ImSet<A> on()
    {
        return ImSet.empty();
    }

    private ImSet(final ImSortedSet<Bucket<T>> node, final int size)
    {
        this.sortedSetOfBuckets = node;
        this.size = size;
    }

    private static <A> ImSet<A> onBucketSet(ImSortedSet<Bucket<A>> node, final int size)
    {
        return size == 0
               ? ImSet.empty()
               : new ImSet(node, size);
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
    public ImSet<T> add(final T elementToAdd)
    {
        return add(elementToAdd, Replace.no);
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
    public ImSet<T> remove(final T elementToRemove)
    {
        return getBucketContaining(elementToRemove).ifPresentElse(i -> removeElementFromBucket(elementToRemove, i), this);
    }

    /**
     * <p> Form the set
     * {@code this}
     *  minus
     * {@code iterable}
     * <p> The intersection of
     * {@code this}
     *  and the complement of
     * {@code iterable}
     *
     */
    public ImSet<T> minus(Iterable<T> iterable)
    {
        ImSet<T> r = this;

        for (T i : iterable)
            r = r.remove(i);

        return r;
    }

    /**
     * <p> Add
     * {@code newElement}
     *  to the set.
     * <p> If an
     * <em>equal</em>
     *  element is already in the set then it will be replaced by
     * {@code newElement}
     *
     */
    public ImSet<T> replace(final T newElement)
    {
        return add(newElement, Replace.yes);
    }

    /**
     * <p> Add
     * {@code element}
     *  to the set.
     * <p> @throws NullPointerException if
     * {@code element == null}
     *
     */
    ImSet<T> add(final T newElement, Replace replace)
    {
        NullCheck.check(newElement);

        return addAtPath(newElement, replace, getPathOn(newElement));
    }

    ImTreeZipper<Bucket<T>> getPathOn(final T newElement)
    {
        // Try to find the path of a bucket with the correct hash in the tree
        return ImTreeZipper.find(ImTreeZipper.onRoot(sortedSetOfBuckets.tree), bucketWith(newElement));
    }

    ImSet<T> addAtPath(final T newElement, Replace replace, final ImTreeZipper<Bucket<T>> path)
    {
        final Bucket<T> bucketFound = path.getFocus().getElement();

        if (bucketFound == null)
        {
            // There wasn't even a bucket with the right hash
            ImTree<Bucket<T>> tree = path.replaceEmptyNode(ImTree.on(bucketWith(newElement))).close();
            return new ImSet<T>(new ImSortedSet<Bucket<T>>(tree), size + 1);
        }
        else
        {
            // There was a bucket with the right hash - let's try adding `newElement`
            Bucket<T> newBucket = bucketFound.add(newElement, replace);

            if (replace == Replace.yes)
            {
                int newSize = size - bucketFound.size() + newBucket.size();
                ImTree<Bucket<T>> tree = path.replaceElement(newBucket).close();
                return new ImSet<T>(new ImSortedSet<Bucket<T>>(tree), newSize);
            }
            else if (newBucket == bucketFound)
                return this;
            else
            {
                ImTree<Bucket<T>> tree = path.replaceElement(newBucket).close();
                return new ImSet<T>(new ImSortedSet<Bucket<T>>(tree), size + 1);
            }
        }
    }

    private Bucket<T> bucketWith(final T newElement)
    {
        return Bucket.newBucket(hashCodeOf(newElement), newElement);
    }

    private static <C> int hashCodeOf(final C element)
    {
        return element.hashCode();
    }

    private ImSet<T> removeElementFromBucket(final T element, final Bucket<T> bucketWithMatchingHashCode)
    {
        // Remove the element from the bucket (it might not be in this bucket of course)
        Bucket<T> newBucket = bucketWithMatchingHashCode.remove(element);

        // If the bucket has not changed then `element` was not in it so we are done
        if (newBucket == bucketWithMatchingHashCode)
            return this;
        else
            // If the size of the bucket is now zero then we must remove the
            // bucket
            // otherwise we must add the new bucket
            return ImSet.onBucketSet(newBucket.size() == 0
                                     ? sortedSetOfBuckets.remove(bucketWithMatchingHashCode)
                                     : sortedSetOfBuckets.add(newBucket)
                    , size - 1);
    }

    private ImMaybe<Bucket<T>> getBucketContaining(final T element)
    {
        // Find the bucket with the matching hash code
        return sortedSetOfBuckets.find(bucketWith(element));
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
    public ImMaybe<T> find(final T elementToFind)
    {
        final int hashCode = hashCodeOf(elementToFind);

        // Create an entry that we can search for
        final Bucket<T> entry = new Bucket<T>(hashCode);
        //
        //        // Find it in the tree
        //        final Bucket<T> foundOrNull = (Bucket<T>) sortedSetOfBuckets.find(entry);
        //
        //        return foundOrNull == null
        //               ? null
        //               : foundOrNull.get(elementToFind);

        return sortedSetOfBuckets.find(entry).flatMap(i -> ImMaybe.with(i.get(elementToFind)));
    }

    /**
     * <p> The set whose elements are obtained from
     * {@code array}
     * .
     * <p> ImSet can't contain
     * {@code null}
     *  so none of the elements in
     * {@code array}
     *  can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(1, 2, 3, 2)  => [1, 2, 3]
     * on()            => []
     * on(1, null)     => throws java.lang.NullPointerException: This collection can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code array}
     *  is
     * {@code null}
     * @see #onAll(Collection)
     * @see #onIterator(Iterator)
     * @see #on
     *
     */
    @SafeVarargs
    public static <T> ImSet<T> onArray(final T... array)
    {
        return onIterator(ArrayIterator.on(array));
    }

    /**
     * <p> The set whose elements are obtained from
     * {@code elementsCollection}
     * .
     * <p> ImSets can't contain
     * {@code null}
     *  so none of the elements in
     * {@code elementsCollection}
     *  can be
     * {@code null}
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(Arrays.asList(1, 2, 1))    => [1, 2]
     * on(Arrays.asList())           => []
     * on(Arrays.asList(1, null, 3)) => throws NullPointerException("This collection can't contain nulls");
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code elementsCollection}
     *  is
     * {@code null}
     * @see #onIterator(Iterator)
     * @see #onArray(Object...)
     * @see #on
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> ImSet<T> onAll(Collection<? extends T> elementsCollection)
    {
        return elementsCollection instanceof ImSet
               ? (ImSet<T>) elementsCollection
               : onIterator(elementsCollection.iterator());
    }

    /**
     * <p> The set whose elements are obtained by iterating over
     * {@code iterator}
     * .
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * on(Arrays.asList(1, 2, 3).iterator())     =>  [1, 2, 3]
     * on(Arrays.asList().iterator())            =>  []
     * on(Arrays.asList(1, null, 3).iterator())  =>  throws java.lang.NullPointerException: This collection can't contain nulls
     * }</pre>
     * <p> @throws NullPointerException if any of the elements in
     * {@code iterator}
     *  is
     * {@code null}
     * @see #onAll(Collection)
     * @see #onArray(Object...)
     * @see #on
     *
     */
    public static <T> ImSet<T> onIterator(Iterator<? extends T> iterator)
    {
        ImSet<T> s = ImSet.empty();

        while (iterator.hasNext())
            s = s.add(iterator.next());

        return s;
    }

    /**
     * <p> The set whose only element is
     * {@code element}
     * .
     * <p> This is convenient when we want to create a list whose only element is an array.
     * .
     * <p> @throws NullPointerException if element is
     * {@code null}
     * @see #onArray(Object...)
     * @see #onIterator(Iterator)
     * @see #onArray(Object[])
     *
     */
    @SafeVarargs
    public static <T> ImSet<T> on(T... elements)
    {
        return ImSet.onArray(elements);
    }

    /**
     * <p> The set whose elements are obtained from the list
     * {@code ts}
     * .
     */
    public static <T> ImSet<T> onAll(ImList<T> ts)
    {
        return ImSet.onIterator(ts.iterator());
    }

    /**
     * <p> The set whose elements are obtained from the set
     * {@code xs}
     * .
     */
    public static <U> ImSet<U> onAll(ImSet<U> xs)
    {
        return xs;
    }

    /**
     * <p> Create a
     * {@code ImSet}
     *  where each element is taken from
     * {@code iterable}
     *
     */
    static <A> ImSet<A> onAll(Iterable<A> iterable)
    {
        return onIterator(iterable.iterator());
    }

    /**
     * <p> The number of elements in
     * {@code this}
     * .
     *
     */
    public int size()
    {
        return size;
    }

    @Override
    public AbstractTextBox getTextBox()
    {
        return this.toList().getTextBox();
    }

    //
    //
    // Paths and Iterators
    //
    //

    static class ImSetIterator<T> implements Iterator<T>
    {
        final private ImTreeIterator<Bucket<T>> oTreeIterator;
        private ArrayIterator<T> elementsIterator;

        public ImSetIterator(final ImSortedSet<ImSet.Bucket<T>> oTree)
        {
            oTreeIterator = oTree.iterator();

            if (oTreeIterator.hasNext())
            {
                final ImSet.Bucket<T> bucket = oTreeIterator.next();
                elementsIterator = new ArrayIterator<T>(bucket.elements);
            }
            else
            {
                elementsIterator = new ArrayIterator<T>();
            }
        }

        /**
         * <p> {@code true}
         *  if the iterator has more elements. (In other words, returns
         * {@code true}
         *  if
         * {@code next()}
         *  would return an element rather than throwing an exception.)
         *
         */
        public boolean hasNext()
        {
            return elementsIterator.hasNext()
                   ? true
                   : oTreeIterator.hasNext();
        }

        /**
         * The next element in the iterator. Throws {@link NoSuchElementException} if no such element exists.
         */
        public T next()
        {
            if (!hasNext())
                throw new NoSuchElementException();

            if (elementsIterator.hasNext())
            {
                return elementsIterator.next();
            }

            // There *must* be another bucket because hasNext() is true and buckets can't be empty
            elementsIterator = new ArrayIterator<T>(oTreeIterator.next().elements);

            return elementsIterator.next();
        }

        /**
         * <p> Throws
         * {@code UnsupportedOperationException}
         * . You can't modify
         * {@code ImSet}
         * s in this way.
         *
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
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
    public Iterator<T> iterator()
    {
        return new ImSetIterator<T>(sortedSetOfBuckets);
    }

    //

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
    public boolean contains(T elementToLookFor)
    {
        return find(elementToLookFor).isPresent();
    }

    public boolean containsAll(ImSet<T> elements)
    {
        if (size() < elements.size())
            return false;
        else
        {
            for (T e : this)
            {
                if (!this.contains(e))
                    return false;
            }
            return true;
        }
    }

    /**
     * <p> Compares
     * {@code other}
     *  with
     * {@code this}
     *  for equality.
     * <p> Returns
     * {@code true}
     *  if the specified object is also an
     * {@code ImSet}
     * , both
     * sets have the same size, and:
     *
     * <pre>{@code
     * this.containsAll(other)
     * }</pre>
     * <p> In other words, two sets are defined to be
     * <em>equal</em>
     *  if they contain
     * <em>equal</em>
     *  elements.
     * <p> In
     * <em>other</em>
     *  other words - this is very similar to the standard Java Set
     * {@code equals}
     * .
     *
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other)
    {
        return this == other || other instanceof ImSet<?> && eq((ImSet) other);
    }

    private boolean eq(ImSet<?> otherSet)
    {
        return size() != otherSet.size() || hashCode() != otherSet.hashCode()
               ? false
               : Equals.isEqualIterators(iterator(), otherSet.iterator());
    }

    public ImSet<T> union(Iterable<? extends T> elementsToAdd)
    {
        ImSet<T> result = this;

        for (T a : elementsToAdd)
        {
            result = result.add(a);
        }

        return result;
    }

    /**
     * <p> Allows
     * {@code ImSet<U>}
     *  to be cast to
     * {@code ImSet<T>}
     *  (where U is a subtype of T) without generating a compiler warning.
     * <p> For a detailed description see ImList::upCast()
     *
     */
    @SuppressWarnings("unchecked")
    public <U> ImSet<U> upCast()
    {
        return (ImSet<U>) this;
    }

    //    /**
    //     * <p> The ImSet formed out of the elements of each collection in
    //     * {@code collections}
    //     *  in order.
    //     * <p> ImCollections can't contain
    //     * {@code null}
    //     *  so none of the elements can be
    //     * {@code null}
    //     * <h4>Examples:</h4>
    //     *
    //     * <pre>{@code
    //     * List<Number> threeFive = Arrays.<Number> asList(3, 5);
    //     * ImSet<Integer> oneTwo = ImSet.on(1, 2);
    //     *
    //     * joinArray(oneTwo, threeFive)          =>  [1, 2, 3, 5]");
    //     * joinArray(oneTwo, threeFive, oneTwo)  =>  [1, 2, 3, 5, 1, 2]
    //     * joinArray(on(), on())                 =>  []
    //     * }</pre>
    //     * @see #union(Iterable)
    //     * @see #joinIterator(Iterator)
    //     * @see #join(Collection)
    //     *
    //     */
    //    @SafeVarargs
    //    public static <T> ImSet<T> joinArray(Collection<? extends T>... collections)
    //    {
    //        return joinIterator(ArrayIterator.on(collections));
    //    }

    /**
     * <p> The ImSet formed out of the elements of each collection in
     * {@code iterator}
     *  in order.
     * <p> ImSet can't contain
     * {@code null}
     *  so none of the elements can be
     * {@code null}
     * @see #union(Iterable)
     *
     */
    public static <T> ImSet<T> join(Iterable<? extends Iterable<? extends T>> iterable)
    {
        ImSet<T> concat = ImSet.empty();

        for (Iterable<? extends T> s : iterable)
        {
            concat = concat.union(ImSet.onAll(s));
        }

        return concat;
    }

    //    /**
    //     * <p> The ImSet formed out of the elements of each collection in
    //     * {@code iterator}
    //     *  in order.
    //     * <p> ImCollections can't contain
    //     * {@code null}
    //     *  so none of the elements can be
    //     * {@code null}
    //     * @see #union(Iterable)
    //     * @see #joinArray(Collection...)
    //     * @see #join(Collection)
    //     *
    //     */
    //    public static <T> ImSet<T> joinIterator(Iterator<Collection<? extends T>> iterator)
    //    {
    //        ImSet<T> concat = ImSet.empty();
    //
    //        while (iterator.hasNext())
    //            concat = concat.union(ImSet.onAll(iterator.next()));
    //
    //        return concat;
    //    }

    //    /**
    //     * <p> The ImSet formed out of the elements of each collection in
    //     * {@code collectionOfCollections}
    //     *  in order.
    //     * <p> ImCollections can't contain
    //     * {@code null}
    //     *  so none of the elements can be
    //     * {@code null}
    //     * @see #union(Iterable)
    //     * @see #joinArray(Collection...)
    //     * @see #joinIterator(Iterator)
    //     *
    //     */
    //    @SuppressWarnings("unchecked")
    //    public static <T> ImSet<T> join(Collection<? extends Collection<? extends T>> collectionOfCollections)
    //    {
    //        return joinIterator((Iterator<Collection<? extends T>>) collectionOfCollections.iterator());
    //    }

    /**
     * <p> A list containing the elements of the set
     */
    public ImList<T> toList()
    {
        return ImList.onAll(this);
    }

    /**
     * <p> An element of the set - or Nothing if the set is empty
     */
    public ImMaybe<T> anyElement()
    {
        ImList<T> elements = toList();

        return elements.isEmpty()
               ? ImMaybe.nothing()
               : ImMaybe.just(elements.head());
    }

    /**
     * {@code true}
     * iff
     * {@code this}
     * is the empty set
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * {@code true}
     * iff
     * {@code this}
     * is not the empty set
     */
    public boolean isNotEmpty()
    {
        return !isEmpty();
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return toList().toString();
    }

    /**
     * The set formed by applying
     {@code fn}
     * to each element
     * This may result in a set wth a different size from
     {@code this}
     */
    public <U> ImSet<U> map(Fn<T, U> fn)
    {
        ImSet<U> result = ImSet.empty();

        for (T t : this)
            result = result.add(fn.of(t));

        return result;
    }

    /**
     * <p> Start with an accumulator
     * {@code z}
     *  and iterate over
     * {@code this}
     * , applying
     * {@code f}
     *  to the
     * {@code z}
     *  and
     * {@code e}
     *  to get a new
     * {@code z}
     * <p> One way to visualise this is to imagine that the function that we are using is the function that adds two numbers - ie
     * the infix
     * {@code +}
     *  operator
     * <p> Then
     *
     * <pre>{@code
     * foldl (+) z [e1, e2, ... en] == [ (...((z + e1) + e2) + ... ) + en ]
     * }</pre>
     * <p> Note that the accumulator,
     * {@code z}
     *  is the first argument to the function.
     * <p> If we extend this to imagine that the function is called * and can be applied using infix notation - like
     * {@code +}
     *  then
     *
     * <pre>{@code
     * foldl (*) z [e1, e2, ... en] == [ (...((z * e1) * e2) * ... ) * en ]
     * }</pre>
     * <p> Note that we are <em>not</em> assuming that
     * {@code *}
     *  is commutative
     *
     */
    public <B> B foldl(B z, Fn2<B, T, B> f)
    {
        for (T i : this)
            z = f.of(z, i);

        return z;
    }

    /**
     * <p> The
     * {@code ImSet}
     *  formed by running
     * {@code fn}
     *  on each element of
     * {@code this}
     *  in order and then joining the resulting
     * {@code ImSet}
     * s
     *
     */
    public <A> ImSet<A> flatMap(Fn<T, ImSet<A>> fn)
    {
        return join(map(fn));
    }

    /**
     * <p> the
     * {@code ImSet}
     *  that is the sub-set of
     * {@code this}
     *  where
     * {@code pred}
     *  is
     * {@code true}
     *  for each element
     *
     */
    public ImSet<T> filter(Fn<T, Boolean> pred)
    {
        ImSet<T> result = ImSet.empty();

        for (T t : this)
            if (pred.of(t))
                result = result.add(t);

        return result;
    }

    /**
     * <p> {@code true}
     *  if any the elements in
     * {@code this}
     *  satisfy
     * {@code pred}
     * <p> Another way of thinking about this is that:
     *
     * <pre>{@code
     * this.any(pred) == ! this.all(not pred)
     * }</pre>
     * <p> so if we apply
     * {@code any}
     *  to the empty list it returns
     * {@code false}
     *  and
     * {@code all}
     *  returns
     * {@code true}
     *
     * <pre>{@code
     * ImSet.on().all(...) == true
     * ImSet.on().any(...) == false
     * }</pre>
     * @see ImSet#all(dev.javafp.func.Fn)
     *
     */
    public boolean any(Fn<T, Boolean> pred)
    {
        for (T t : this)
            if (pred.of(t))
                return true;

        return false;
    }

    /**
     * <p> {@code true}
     *  if all the elements in
     * {@code this}
     *  satisfy
     * {@code pred}
     * <p> Another way of thinking about this is that:
     *
     * <pre>{@code
     * this.all(pred) == ! this.any(not pred)
     * }</pre>
     * <p> so if we apply
     * {@code all}
     *  to the empty list it returns
     * {@code true}
     *  and
     * {@code any}
     *  returns
     * {@code false}
     *
     * <pre>{@code
     * ImSet.on().all(...) == true
     * ImSet.on().any(...) == false
     * }</pre>
     * @see ImSet#any(dev.javafp.func.Fn)
     *
     */
    public boolean all(Fn<T, Boolean> pred)
    {
        return !any(pred);
    }

    public ImSet<T> intersection(ImSet<T> other)
    {
        return other.toList().foldl(ImSet.empty(), (s, e) -> contains(e)
                                                             ? s.add(e)
                                                             : s);
    }

    @Override
    public int hashCode()
    {
        if (cachedHashCode == 0)
            cachedHashCode = computeHash();

        return cachedHashCode;
    }

    /**
     * This hash computation can't rely on any ordering in the elements -  so it has to look at all the elements
     *
     * It uses the same algorithm to combine the hashCodes of the elements as java.util.AbstractSet
     */
    private int computeHash()
    {
        int h = 0;

        for (T t : this)
            h += Hash.hashCodeOf(t);

        return h;
    }
}