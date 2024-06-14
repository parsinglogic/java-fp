/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.HasTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.eq.Eq;
import dev.javafp.eq.Equals;
import dev.javafp.ex.FunctionNotAllowedOnEmptyList;
import dev.javafp.ex.InvalidArgument;
import dev.javafp.ex.SizeOnInfiniteList;
import dev.javafp.ex.Throw;
import dev.javafp.ex.TransposeColsError;
import dev.javafp.ex.TransposeRowsError;
import dev.javafp.func.Fn;
import dev.javafp.func.Fn2;
import dev.javafp.func.FnBlock;
import dev.javafp.func.FnConsumer;
import dev.javafp.func.FnPairConsumer;
import dev.javafp.func.FnProducer;
import dev.javafp.rand.Rando;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImQuartet;
import dev.javafp.tuple.ImTriple;
import dev.javafp.tuple.Pai;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.javafp.lst.ImLazyList.KNOWN_INFINITE;
import static dev.javafp.lst.ImLazyList.UNKNOWN_UNKNOWN;
import static dev.javafp.lst.ImLazyList.UU_BOX_LIMIT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.SIZED;

/**
 * <p> A mainly lazy functional list
 * <h2>Lazy</h2>
 * <p> When you invoke map
 * {@code fn}
 *  on a
 * {@code ImList}
 * , it will not do the eager thing of iterating over the items, applying
 * {@code fn}
 *  and returning a new
 * {@code ImList}
 * .
 * <p> Instead, it creates a
 * {@code ImMappedList}
 *  that is a wrapper around the original list. When you invoke
 * {@code head()}
 *  on this new
 * {@code ImList}
 *  it will
 * apply the function fn to the head of the wrapped
 * {@code ImList}
 *  and return it. If you invoke tail() then it creates a
 * new ImMappedList on the tail of the wrapped list.
 * <p> Most of the standard functions on
 * {@code ImList}
 * s are implemented in this way.
 * <p> To be fair, not all
 * {@code ImList}
 * s are completely lazy. Sometimes, a function has to do some work to determine if the
 * {@code ImList}
 * it returns is empty (and so it must return the {@link dev.javafp.lst.ImEmptyList})
 * <p> This means that we can handle infinite
 * {@code ImList}
 * s quite easily in the traditional lazy functional style.
 * <p> However, this approach does pose some problems
 * <ul>
 * <li>
 * <p> {@link ImList#equalsList} and {@link ImList#size} on an infinite
 * {@code ImList}
 *  will return
 * {@code ⊥}
 *  (ie they will throw a stack overflow)
 * </li>
 * <li>
 * <p> {@link ImList#equalsList} and {@link ImList#size} on a long
 * {@code ImList}
 *  will be expensive.
 * </li>
 * <li>
 * <p> a
 * {@code ImList}
 *  doesn't know if it is infinite or not. Some lists will know this but others will be uncertain.
 * </li>
 * </ul>
 * <p> For most lists that get created in real programs, {@link ImList#equalsList} and {@link ImList#size} will be fine.
 * <p> In fact {@link ImList#hashCode} doesn't need to go right to the end of the
 * {@code ImList}
 * . We could stop after 10, say. Let's do that.
 * <p> Most infinite lists will exist only briefly before having {@link ImList#take} or {@link ImList#drop} applied to them.
 * <p> I have decided not to worry about {@link ImList#equalsList} on infinite lists. I did dally with preventing {@link ImList#equalsList} and {@link ImList#size}
 * on infinite lists - but I came to the conclusion that the extra faff caused by doing this was not worth it.
 * <p> {@link ImList#flush} will convert a
 * {@code ImList}
 *  to a {@link dev.javafp.lst.ImListOnArray}  (not {@link dev.javafp.lst.ImListOnPrimitiveArray})
 *
 * <h2>{@code ImList} the <em>interface</em> is the important type - not the <em>implementations</em></h2>
 * <p> The code that uses
 * {@code ImList}
 *  will always use the
 * {@code ImList}
 *  type - under the covers, there are many different implementations of
 * {@code ImList}
 *  - but these are mainly invisible to the client code.
 * <p> For example {@link ImList#equalsList} only checks if the
 * <em>contents</em>
 *  of the lists are the same - it does not matter what the underlying implementing
 * class happens to be. This is similar to what Java does in {@link java.util.List#equals}
 * <h2>{@code ImList}s on other {@code ImList}s</h2>
 * <ol>
 * <li>
 * <p> ImFilteredList (This is eager - well a bit eager anyway)
 * </li>
 * <li>
 * <p> ImMappedList
 * </li>
 * <li>
 * <p> ImConsList
 * </li>
 * <li>
 * <p> ImAppendList
 * </li>
 * <li>
 * <p> ImTailsList
 * </li>
 * <li>
 * <p> ImScanlList
 * </li>
 * <li>
 * <p> ZippedList
 * </li>
 * <li>
 * <p> ImTakeWhileList
 * </li>
 * <li>
 * <p> DropWhileList
 * </li>
 * </ol>
 * <h2>{@code ImList}s on standard Java collections</h2>
 * <ol>
 * <li>
 * <p> ImListOnList
 * </li>
 * <li>
 * <p> ImListOnArray
 * </li>
 * <li>
 * <p> ListOnIterator
 * </li>
 * </ol>
 * <h2>Infinite {@code ImList}s</h2>
 * <ol>
 * <li>
 * <p> ImRepeatList
 * </li>
 * <li>
 * <p> ImUnfoldList
 * </li>
 * </ol>
 * <h2>Eager functions</h2>
 * <ol>
 * <li>
 * <p> take - creates a ImConsList
 * </li>
 * <li>
 * <p> drop
 * </li>
 * </ol>
 * <h2>Singleton {@link dev.javafp.lst.ImEmptyList}</h2>
 * <p> I have decided to insist on having a singleton empty
 * {@code ImList}
 * . This means that some
 * {@code ImList}
 *  functions have to do some extra work to
 * determine which type of
 * {@code ImList}
 *  to return - for example {@link ImList#filter} might have to traverse the whole
 * {@code ImList}
 *  before it works out that
 * it should return the {@link dev.javafp.lst.ImEmptyList}.
 * <p> The code is much simpler with singleton empty lists though.
 * <h2>To recurse or not?</h2>
 * <p> For a number of these functions, we could implement by
 * <ul>
 * <li>
 * <p> recursive algorithms
 * or
 * </li>
 * <li>
 * <p> we could get an
 * iterator on the underlying
 * {@code ImList}
 *  and do the old-fashioned iteration thing.
 * </li>
 * </ul>
 * <p> Not sure what the best policy is.
 * <h2>{@code ImList}s can contain {@code nulls}</h2>
 *
 */
public interface ImList<A> extends Iterable<A>, Serializable, HasTextBox
{

    /**
     * The empty list
     */
    static ImList empty = new ImEmptyList();

    /**
     * <p>
     * {@code "────────"}
     * (eight dashes) in a
     * {@code LeafTextBox}
     *
     */
    LeafTextBox dash = LeafTextBox.with("────────");

    /**
     * <p> {@code "["}
     *  in a
     * {@code LeafTextBox}
     *
     */
    LeafTextBox open = LeafTextBox.with("[");

    /**
     * <p> {@code "]"}
     *  in a
     * {@code LeafTextBox}
     */
    LeafTextBox close = LeafTextBox.with("]");

    /**
     * <p> {@code ", "}
     *  in a
     * {@code LeafTextBox}
     */
    LeafTextBox comma = LeafTextBox.with(", ");

    /**
     * The first element in
     * {@code this}
     * .
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    A head();

    /**
     *
     * {@code this}
     * without the first element.
     *
     * Throws {@link FunctionNotAllowedOnEmptyList} if the list is empty.
     */
    ImList<A> tail();

    /**
     * <p> The number of elements in
     * {@code this}
     * .
     * Throws
     * {@code SizeOnInfiniteList}
     * if the size is infinite.
     */
    int size();

    /**
     * <p> Compares
     * {@code other}
     *  with
     * {@code this}
     *  for equality.  Returns
     * {@code true}
     *  if and only if the specified object is also a
     * {@code ImList}
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
     * <p> In other words, two ImLists are defined to be
     * equal if they contain
     * <em>equal</em>
     *  elements in the same order.
     * <p> In
     * <em>other</em>
     *  other words - this is very similar to the standard Java List
     * {@code equals}
     * .
     * I can't have a "normal" equals method that overrides equals(Object a)
     * because that is not allowed in Java.
     *
     * <p>I have put an equals method on the two subclasses of this class and they delegate here
     */
    default boolean equalsList(ImList<? extends A> other)
    {
        if (other == null)
        {
            return false;
        }
        else if (other == this)
        {
            return true;
        }
        else if (other.size() != this.size())
        {
            return false;
        }
        else
        {
            ImList<A> one = this;
            ImList<A> two = other.upCast();

            for (int i = 0; i < size(); i++)
            {
                if (!Equals.isEqual(one.head(), two.head()))
                    return false;
                else
                {
                    one = one.tail();
                    two = two.tail();
                }
            }

            return true;
        }
    }

    int resolveSize();

    /**
     * <p> Convert
     * {@code this}
     *  to a {@link ImListOnArray}
     * <p> This means visiting every element.
     * <p> If this is a lazy list, this means causing any side effects and capturing any mutable state into immutable state (eg if reading
     * from a stream or writing something - or any IO).
     * <p> {@link ImListOnArray} and {@link dev.javafp.lst.ImListOnPrimitiveArray} override this - to do nothing
     *
     */
    default ImList<A> flush()
    {
        return ImListOnArray.on(toArray(Object.class));
    }

    /**
     * <p> This returns
     * {@code false}
     *  because the only
     * {@code ImList}
     *  that is empty is the singleton {@link dev.javafp.lst.ImEmptyList}
     *
     */
    default boolean isEmpty()
    {
        return false;
    }

    /**
     * <p> This returns !{@link ImList#isEmpty()}
     */
    default boolean isNotEmpty()
    {
        return !isEmpty();
    }

    /**
     * <p> The singleton {@link dev.javafp.lst.ImEmptyList}
     */
    @SuppressWarnings("unchecked")
    static <A> ImList<A> empty()
    {
        return empty;
    }

    /**
     * <p> The classic functional
     * {@code cons}
     *  (first introduced in Lisp) which creates a new list with
     * {@code head}
     *  as the head and
     * {@code this}
     *  as the tail
     *
     */
    static <A> ImList<A> cons(A head, ImList<A> tail)
    {
        return tail.withHead(head);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  from
     * {@code s}
     *  where each element is a character and
     * {@code l.toString("") == s}
     *
     */
    static ImList<Character> onString(String s)
    {
        return ImListOnString.on(s);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  from
     * {@code reader}
     *  where each element is a String obtained by reading a line from
     * {@code reader}
     *
     */
    static ImList<String> onReader(Reader reader)
    {
        return ImListOnReader.on(new BufferedReader(reader));
    }

    /**
     * <p> Create a
     * {@code ImList}
     * of size 1 containing
     * {@code array}
     * which is an array
     * <p> If you want to create an
     * {@code ImList}
     * that has one element and that element is an array, then you can't use
     * {@link ImList#on(Object[])}
     * because Java will assume that the array is a variable argument list and it will create an
     * {@code ImList}
     * that has the same size as the array.
     */
    public static <A> ImList<A[]> onOne(A[] array)
    {
        return new ImConsList<>(array, empty());
    }

    /**
     * <p> The singleton {@link dev.javafp.lst.ImEmptyList}
     */
    static <A> ImList<A> on()
    {
        return ImList.empty();
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is taken from
     * {@code array}
     *  in order. In fact, the
     * {@code ImList}
     *  that is created is a wrapper
     * on the underlying array - so be aware that if you use an array that someone else might modify, the list will be compromised
     * since we assume that
     * {@code ImList}
     * s are immutable.
     *
     * <p> The fact that it is a wrapper on an array means that certain operations on it are optimised.
     *
     * <p> If you want to create a
     * {@code ImList}
     * that has one element and that element is an array, then you can use
     * {@link ImList#onOne(Object[])}
     */
    @SafeVarargs
    static <A> ImList<A> on(A... array)
    {
        return ImListOnArray.on(array, 0, array.length);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is taken from
     * {@code primitiveArray}
     *  in order.
     * <p> The
     * {@code primitiveArray}
     *  argument must be a Java array of 'primitives' - ie both the following must be
     * {@code true}
     * :
     *
     * <pre>{@code
     * primitiveArray.getClass().isArray())
     * primitiveArray.getClass().getComponentType().isPrimitive()
     * }</pre>
     * <p> If this is not the case then an exception is thrown.
     * <p> The purpose of this class is to be able to wrap an array of primitives in a
     * {@code ImList}
     *  without needing to convert each element to an Object.
     * <p> it is your responsibility to specify the correct type of the
     * {@code ImList}
     *  - ie it must be a type that matches the underlying array's primitive type.
     * <p> If you use this:
     *
     * <pre>{@code
     * ImList<Integer> is = ImList.on(foo);
     * }</pre>
     * <p> then foo must be (eg)
     *
     * <pre>{@code
     * int[] foo = { 2, 4, 6 };
     * }</pre>
     * <p> If you don't do this then trying to access an element will throw an exception.
     * <p> Because the
     * {@code ImList}
     *  that is created is a wrapper
     * on the underlying array - be aware that if you use an array that someone else might modify, the list will be compromised
     * since we assume that
     * {@code ImList}
     * s are immutable.
     * <p> The fact that it is a wrapper on an array means that certain operations on it are optimised.
     *
     */
    static <A> ImList<A> onPrimitiveArray(Object primitiveArray)
    {
        return ImListOnPrimitiveArray.on(primitiveArray);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is taken from
     * {@code list}
     *  in order. In fact, the
     * {@code ImList}
     *  that is created is a wrapper
     * on the underlying
     * {@code List}
     *  - so be aware that if you use an array that someone else might modify, the list will be compromised
     * since we assume that
     * {@code ImList}
     * s are immutable.
     * <p> The fact that it is a wrapper on a
     * {@code ImList}
     *  means that certain operations on it are optimised.
     *
     */
    static <A> ImList<A> onList(List<A> list)
    {
        return ImListOnList.on(list, 0, list.size());
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is taken from
     * {@code it}
     *  in order.
     * <p> Each time you access
     * <p> The underlying iterator is used to get a particular element on a 'just in time' basis. Each time an element is accessed, it is
     * cached.
     * <p> This means that, depending on how you access elements in the list, the iterator will be asked get its next element at ad-hoc times.
     * <p> If you want to control when the underlying iterator gets its elements, you can use {@link ImList#flush()} which
     * will get all the elements from the underlying iterator and cache them.
     *
     */
    static <A> ImList<A> onIterator(Iterator<? extends A> it)
    {
        return ImIteratorList.on(it);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is taken from
     * {@code iterable}
     *  in order.
     * <p> In fact the implementation is:
     *
     * <pre>{@code
     * return onIterator(iterable.iterator());
     * }</pre>
     * @see ImList#onIterator(java.util.Iterator)
     *
     */
    static <A> ImList<A> onAll(Iterable<A> iterable)
    {
        return onIterator(iterable.iterator());
    }

    /**
     * <p> An optimised version that just returns
     * {@code list}
     * to avoid creating a new list
     *
     */
    static <A> ImList<A> onAll(ImList<A> list)
    {
        return list;
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is generated from
     * {@code start}
     *  using the function
     * {@code stepFn}
     * .
     * <p> The resulting list will be:
     * <p> [ start, stepFn.of(start), stepFn.of(stepFn.of(start)), stepFn.of(stepFn.of(stepFn.of(start)))... ]
     * <p> Each element is calculated as needed and cached.
     *
     */
    static <A> ImList<A> unfold(A start, Fn<A, A> stepFn)
    {
        return new ImUnfoldList<>(start, stepFn);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  where each element is
     * {@code thingToRepeat}
     * .
     * <p> The list is infinite.
     *
     */
    static <A> ImList<A> repeat(A thingToRepeat)
    {
        return new ImRepeatList<>(thingToRepeat);
    }

    /**
     * <p> Create a
     * {@code ImList}
     *  of size
     * {@code count}
     *  where each element is
     * {@code thingToRepeat}
     * .
     * <p> The implementation is:
     *
     * <pre>{@code
     * return repeat(thingToRepeat).take(count);
     * }</pre>
     *
     */
    static <A> ImList<A> repeat(A thingToRepeat, int count)
    {
        return repeat(thingToRepeat).take(count);
    }

    /**
     * <p> {@code true}
     *  if all the elements in
     * {@code things}
     *  are
     * {@code true}
     * .
     * <p> Strictly, the definition is:
     * <p> {@code false}
     *  if there is an element,
     * {@code e}
     *  in
     * {@code things}
     *  where
     * {@code e == false}
     * ,
     * {@code true}
     *  otherwise
     * <p> This means that:
     *
     * <pre>{@code
     * and(ImList.on()) == true
     * }</pre>
     *
     */
    static boolean and(ImList<Boolean> things)
    {
        return !things.find(i -> !i).isPresent();
    }

    /**
     * <p> {@code true}
     *  if one or more of the elements in
     * {@code things}
     *  are
     * {@code true}
     * .
     * <p> This means that:
     *
     * <pre>{@code
     * or(ImList.on()) == false
     * }</pre>
     *
     */
    static boolean or(ImList<Boolean> things)
    {
        return things.find(i -> i).isPresent();
    }

    /**
     * <p> The transpose of
     * {@code matrix}
     *  - which must have
     * {@code rows}
     *  rows and
     * {@code cols}
     *  columns.
     * <p> The resullting matrix has
     * {@code columns}
     *  rows and
     * {@code rows}
     *  columns.
     * <p> If
     * {@code matrix}
     *  is [ [1, 2, 3], [4, 5, 6] ] then:
     *
     * <pre>{@code
     * transpose(matrix, 2, 3) = [ [1, 4], [2, 5], [3, 6] ]
     * matrix.at(1)    1 2 3  =>  1 4
     * matrix.at(2)    4 5 6      2 5
     *                            3 6
     * }</pre>
     * <p> If
     * {@code matrix}
     *  does not have the correct number of elements, an exception is thrown
     *
     */
    static <A> ImList<ImList<A>> transpose(ImList<ImList<A>> matrix, int rows, int cols)
    {
        if (rows != matrix.size())
        {
            throw new TransposeRowsError(rows, matrix.size());
        }

        if (matrix.any(r -> r.size() != cols))
        {
            throw new TransposeColsError(cols, matrix);
        }

        if (matrix.size() == 0)
        {
            return repeat(ImList.empty(), cols);
        }
        return cols == 0
               ? ImList.on()
               : ImList.cons(matrix.map(row -> row.head()), transpose(ImList.right(matrix), rows, cols - 1));
    }

    /**
     * <p> Assuming that
     * {@code matrix}
     *  represents a matrix then return
     * {@code matrix}
     *  with the first column removed
     *
     */
    static <A> ImList<ImList<A>> right(ImList<ImList<A>> matrix)
    {
        return matrix.map(row -> row.tail());
    }

    /**
     * <p> A
     * {@code ImList}
     *  of Integers
     * {@code n}
     * ,
     * {@code min <= n < max}
     * , generated from
     * {@code random}
     * .
     * <p> If
     * {@code min >= max}
     *  then return the empty list.
     * <p> The list is infinite and each element is generated as needed.
     *
     */
    static ImList<Integer> randomInts(Random random, int min, int max)
    {
        return randomInts(random, max - min).map(i -> i + min);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of numbers n,
     * {@code 0 <= n < limit}
     * , generated from
     * {@code random}
     * <p> If
     * {@code limit < 0}
     *  then return the empty list.
     *
     */
    static ImList<Integer> randomInts(Random random, int limit)
    {
        return limit < 0
               ? ImList.on()
               : ImList.unfold(random.nextInt(limit), i -> random.nextInt(limit));
    }

    /**
     * <p> A
     * {@code ImList}
     *  of
     * {@code Double}
     * s
     * {@code d}
     * ,
     * {@code 0 <= d < 1.0}
     * , generated from
     * {@code random}
     *
     */
    static ImList<Double> randomDoubles(Random random)
    {
        return ImList.unfold(random.nextDouble(), i -> random.nextDouble());
    }

    /**
     * <p> Generate a
     * {@code ImList}
     *  of elements by invoking
     * {@code producer.doit()}
     *  to generate each element.
     * <p> The list is infinite and each element is generated as needed.
     *
     */
    static <A> ImList<A> generate(FnProducer<A> producer)
    {
        return ImList.unfold(producer.doit(), ignore -> producer.doit());
    }

    /*
     * Default Functions
     */

    /**
     * <p> A String representation of
     * {@code this}
     *  using
     * {@code , }
     *  to separate each element and
     * {@code [}
     *  at the start and
     * {@code ]}
     *  at the end.
     * <p> We are assuming that the string representation of each element does not contain any newlines
     *
     * <pre>{@code
     * [1, 2, 3 ] toS() == "[1, 2, 3]"
     * }</pre>
     *
     * We can't name this method
     * {@code toString}
     * because
     * {@code ImList}
     * is an interface and Java does not allow default methods in interfaces to override methods.
     */
    default String toS()
    {
        return getTextBox().toString();
    }

    /**
     * <p> A String representation of
     * {@code this}
     *  using
     * {@code separator}
     *  to separate each element
     *
     */
    default String toString(String separator)
    {
        return TextUtils.join(this, separator);
    }

    /**
     * <p> A String representation of
     * {@code this}
     *  using
     * {@code showFn}
     * to convert the object to a String
     * {@code separator}
     * to separate each element
     *
     */
    default String show(Fn<A, ?> showFn, String separator)
    {
        return TextUtils.join(this.map(i -> showFn.of(i).toString()), separator);
    }

    /**
     * <p> A String representation of
     * {@code this}
     *  using
     * {@code separatorChar}
     *  to separate each element
     * <p> The implementation is:
     *
     * <pre>{@code
     * return toString( "" + separatorChar);
     * }</pre>
     *
     */
    default String toString(char separatorChar)
    {
        return toString("" + separatorChar);
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by invoking
     * {@code toString}
     *  on each element of
     * {@code this}
     * <p> The implementation is:
     *
     * <pre>{@code
     * return map(i -> i.toString());
     * }</pre>
     *
     */
    default ImList<String> toStringList()
    {
        return map(i -> i.toString());
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
    @Override
    default AbstractTextBox getTextBox()
    {
        if (Sz.getSz(this) == KNOWN_INFINITE)
        {
            return getTextBoxKI(3).before(LeafTextBox.with(" (showing the first 3 elements - list is infinite)"));
        }
        else
        {
            return Sz.getSz(this) == UNKNOWN_UNKNOWN
                   ? getTextBoxUU()
                   : getTextBoxFinite();
        }

        //        return switch (getSz())
        //                {
        //                    case KNOWN_INFINITE -> getTextBoxKI(3).before(LeafTextBox.with(" (showing the first 3 elements - list is infinite)"));
        //                    case UNKNOWN_UNKNOWN -> getTextBoxUU();
        //                    default -> getTextBoxFinite();
        //                };
    }

    /**
     *
     * @deprecated This method is intended for internal use and should not be called by clients.
     */
    @Deprecated
    default AbstractTextBox getTextBoxKI(int limit)
    {
        return getTextBox2(getBoxes(limit));
    }

    /**
     *
     * @deprecated This method is intended for internal use and should not be called by clients.
     */
    @Deprecated
    default AbstractTextBox getTextBoxUU()
    {
        ImList<AbstractTextBox> boxes = getBoxes(UU_BOX_LIMIT);

        return getTextBox2(boxes).before(boxes.size() < UU_BOX_LIMIT ? LeafTextBox.with("") : ImLazyList.UU_MESSAGE);
    }

    private AbstractTextBox getTextBoxFinite()
    {
        return getTextBox2(getBoxes());
    }

    private static AbstractTextBox getTextBox2(ImList<AbstractTextBox> boxes)
    {
        return boxes.find(b -> b.height > 1).isPresent()
               ? getMultilineBoxes(boxes)
               : LeftRightBox.with(open, LeftRightBox.withAllBoxes(boxes.intersperse(comma)), close);
    }

    private static TopDownBox getMultilineBoxes(ImList<AbstractTextBox> boxes)
    {
        int width = Integer.toString(boxes.size()).length() + 1;
        AbstractTextBox line = dash.indentBy(width);
        ImList<AbstractTextBox> cells = ImRange.oneTo(boxes.size()).zipWith(boxes, (i, b) -> LeafTextBox.lefted(i.toString(), width).before(b));

        return TopDownBox.withBoxes(open, LeftRightBox.indent(2, TopDownBox.withAllBoxes(cells.intersperse(line))), close);
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by invoking {@link dev.javafp.util.TextUtils#getBoxFrom(java.lang.Object)}) on each element of
     * {@code this}
     *
     */
    default ImList<AbstractTextBox> getBoxes(int limit)
    {
        return map(i -> TextUtils.getBoxFrom(i)).take(limit);
    }

    default ImList<AbstractTextBox> getBoxes()
    {
        return map(i -> TextUtils.getBoxFrom(i));
    }

    /**
     * <p> The {@link dev.javafp.box.TopDownBox} containing teh textbox of each element of
     * {@code this}
     *
     */
    default TopDownBox getTopDownBox()
    {
        return TopDownBox.withAll(this);
    }

    /**
     * <p> An iterator that iterates over the elements of
     * {@code this}
     *
     */
    @Override
    default Iterator<A> iterator()
    {
        return new ImListIterator(this);
    }

    /*
     *
     * The basic
     * {@code ImList}
     * functions
     *
     */

    /**
     * <p> The shortest sublist, starting at index 1, of
     * {@code this}
     *  where
     * {@code pred.of(e) == true}
     *  for each element
     * {@code e}
     *
     * <pre>{@code
     * [1, 2, 0, 1  takeWhile(i -> i < 2 ) == [1]
     * [] takeWhile(...) == []
     * }</pre>
     * <p> Note that you will only get the
     * <em>first</em>
     *  elements that satisfy
     * {@code pred}
     *  - this function won't look beyond the first element
     * that does
     * <em>not</em>
     *  satisfy
     * {@code pred}
     * .
     * <p> To split the list into two where one list contains all the elements that satisfy
     * {@code pred}
     *  and the other list contains all the other elements
     * you can use
     * {@link ImList#filterIntoTwo(Fn)} ()}
     *
     */
    default ImList<A> takeWhile(Fn<A, Boolean> pred)
    {
        return ImTakeWhileList.on(this, pred);
    }

    /**
     * <p> The shortest sublist, starting at index 1, of
     * {@code this}
     *  where
     * {@code pred.of(e) == true}
     *  for each element
     * {@code e}
     *
     * <pre>{@code
     * [1, 2, 0, 1  takeWhile(i -> i < 2 ) == [1]
     * [] takeWhile(...) == []
     * }</pre>
     * <p> Note that you will only get the
     * <em>first</em>
     *  elements that satisfy
     * {@code pred}
     *  - this function won't look beyond the first element
     * that does
     * <em>not</em>
     *  satisfy
     * {@code pred}
     * .
     * <p> To split the list into two where one list contains all the elements that satisfy
     * {@code pred}
     *  and the other list contains all the other elements
     * you can use
     * {@link ImList#filterIntoTwo(Fn)} ()}
     *
     */
    default ImList<A> takeUntil(Fn<A, Boolean> pred)
    {
        return ImTakeUntilList.on(this, pred);
    }

    /**
     * <p> The first
     * {@code count}
     *  elements of
     * {@code this}
     * . If
     * {@code this.size() < count}
     *
     * then the whole list is returned. If
     * {@code count < 0}
     * then the empty list is returned.
     *
     * <pre>{@code
     * [1, 17, 0, 1] take(2) == [1, 17]
     * ["a", "b"] take(-100) == []
     * }</pre>
     *
     */
    default ImList<A> take(int count)
    {
        Throw.Exception.ifLessThan("count", count, 0);
        return count == 0
               ? ImList.on()
               : ImTakeList.on(this, count);
    }

    /**
     * <p> {@code this}
     *  with the first "run" of elements that satisfy
     * {@code pred}
     *  "removed"
     * <p> For any list
     * {@code ls}
     *  and any predicate
     * {@code pred}
     * :
     *
     * <pre>{@code
     * ls.takeWhile(pred).append(ls.dropWhile(pred)) == ls
     * }</pre>
     * <p> Note that this will only drop the
     * <em>first</em>
     *  elements that satisfy
     * {@code pred}
     *  - this function won't look beyond the first element
     * that does not satisfy
     * {@code pred}
     * .
     * <p> To split the list into two where one list contains all the elements that satisfy
     * {@code pred}
     *  and the other list contains all the other elements
     * you can use
     * {@link ImList#filterIntoTwo(Fn)}
     *
     */
    default ImList<A> dropWhile(Fn<A, Boolean> pred)
    {
        return dropWhile(this, pred);
    }

    /**
     * <p> Helper function for
     * {@code dropWhile}
     *
     */
    private static <A> ImList<A> dropWhile(ImList<A> s, Fn<A, Boolean> fn)
    {
        return s.isEmpty()
               ? s
               : fn.of(s.head())
                 ? dropWhile(s.tail(), fn)
                 : s;
    }

    /**
     * <p> {@code this}
     *  with the first
     * {@code count}
     *  elements "removed"
     * <p> For any list
     * {@code ls}
     * :
     *
     * <pre>{@code
     * ls.take(n).append(ls.drop(n)) == ls
     * }</pre>
     * <p> Because
     * {@code empty}
     *  is a singleton we have to do work up front to determine if
     * the result of drop is the empty list.
     * <p> If we implemented size that was O(1) and returned a maybe then we could avoid this O(n) work
     *
     */
    default ImList<A> drop(int count)
    {
        /*
         * Because empty is a singleton we have to do work up front to determine if
         * the result of drop is the empty list.
         *
         * If we implemented size that was O(1) and returned a maybe then we could avoid this O(n) work
         */
        Throw.Exception.ifLessThan("count", count, 0);

        if (count == 0)
            return this;
        else
        {
            // If size is known finite then we can take a short cut - maybe
            if (Sz.getSz(this) >= 0 && count >= size())
            {
                return ImList.on();
            }
            else
            {
                // We just have to do the tail count times to get the list
                ImList<A> l = this;

                for (int i = 0; i < count; i++)
                {
                    if (l.isEmpty())
                        return l;
                    else
                        l = l.tail();
                }

                return l;
            }
        }
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains element
     * {@code thingToTest}
     * ,
     * {@code false}
     *  otherwise.
     * <p> Note that we return true if there is an element that is
     * <em>equal</em>
     *  to
     * {@code thingToTest}
     *  - we are
     * <em>not</em>
     *  using the
     * Java
     * {@code ==}
     *  to find an element that "is the same as"
     * {@code thingToTest}
     * <p> We use {@link dev.javafp.eq.Equals#isEqual} to do the equality tests.
     *
     */
    default boolean contains(A thingToTest)
    {
        return !isEmpty() && (Eq.uals(head(), thingToTest) || tail().contains(thingToTest));
    }

    /**
     * <p> An {@link java.util.ArrayList} that contains the same elements as
     * {@code this}
     *  (in the same order)
     *
     */
    default List<A> toList()
    {
        ArrayList<A> list = new ArrayList<>();

        for (A t : this)
            list.add(t);

        return list;
    }

    /**
     * <p> A {@link dev.javafp.set.ImSet} that contains the elements of
     * {@code this}
     *  (with any duplicates removed, of course)
     *
     */
    default Set<A> toSet()
    {
        Set<A> set = new HashSet<>();

        for (A t : this)
            set.add(t);

        return set;
    }

    /**
     * <p> A {@list java.util.Set} that contains the elements of
     * {@code this}
     *  (with any duplicates removed, of course)
     *
     */
    default ImSet<A> toImSet()
    {
        return foldl(ImSet.empty(), (s, i) -> s.add(i));
    }

    /**
     * <p> Get the element of
     * {@code this}
     *  at index
     * {@code indexStartingAtOne}
     * <p> if
     * {@code this}
     *  is the empty list, then throw {@link dev.javafp.ex.InvalidState }
     * <p> If no such element exists at that index then throw {@link dev.javafp.ex.ArgumentOutOfRange }
     *
     */
    default A at(int indexStartingAtOne)
    {
        try
        {
            Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());
            return drop(indexStartingAtOne - 1).head();
        } catch (SizeOnInfiniteList e)
        {
            return drop(indexStartingAtOne - 1).head();
        }
    }

    /**
     * <p> Get the element of
     * {@code this}
     *  at index
     * {@code indexStartingAtOne}
     *  or
     * {@code defaultValue}
     *  if no such element exists
     * <p> For the empty list, {@link dev.javafp.lst.ImEmptyList }, this function always returns
     * {@code defaultValue}
     *
     */
    default A at(int indexStartingAtOne, A defaultValue)
    {
        return indexStartingAtOne > size() || indexStartingAtOne < 1
               ? defaultValue
               : at(indexStartingAtOne);
    }

    /**
     * <p> The
     * {@code ImList}
     *  that is the same as
     * {@code this}
     *  except that the element at index
     * {@code indexStartingAtOne}
     *  is
     * {@code thingToPut}
     * <p> If no such element exists at that index then throw {@link dev.javafp.ex.ArgumentOutOfRange }
     *
     */
    default ImList<A> put(int indexStartingAtOne, A thingToPut)
    {
        Throw.Exception.ifOutOfRange("indexStartingAtOne", indexStartingAtOne, 1, size());

        return take(indexStartingAtOne - 1).append(cons(thingToPut, drop(indexStartingAtOne)));
    }

    /**
     * <p> The last element of
     * {@code this}
     *  except that the element at index
     * {@code indexStartingAtOne}
     *  is
     * {@code thingToPut}
     * <p> If no such element exists at that index then throw {@link dev.javafp.ex.ArgumentOutOfRange }
     *
     */
    default A last()
    {
        return at(size());
    }

    /**
     * <p> The
     * {@code ImList}
     *  whose elements are the same as in
     * {@code this}
     *  but where the order is reversed.
     * <p> For all lists,
     * {@code l}
     * :
     *
     * <pre>{@code
     * l.reverse().reverse()) == l
     * }</pre>
     * <h4>Examples:</h4>
     *
     * <pre>{@code
     * [1, 2, 3] reverse()  ==  [3, 2, 1]
     * [] reverse()         ==  []
     * }</pre>
     *
     */
    default ImList<A> reverse()
    {

        if (this.size() <= 1)
            return this;
        else
        {
            // We create an array from this in reverse order and then create a ImListOnArray on that
            //
            // Create a new array
            Object[] target = new Object[this.size()];
            ImList<A> old = this;

            // Write to it in reverse order
            int i = this.size();
            while (!old.isEmpty())
            {
                target[--i] = old.head();
                old = old.tail();
            }

            return ImList.on((A[]) target);
        }
    }

    /**
     * <p> For each element of
     * {@code this}
     *  run
     * {@code action}
     *  on it.
     * <p> This does not create any new objects
     *
     */
    default void foreach(FnConsumer<A> action)
    {
        for (A t : this)
            action.doit(t);
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by running
     * {@code fn}
     *  on each element of
     * {@code this}
     *  in order
     *
     */
    default <B> ImList<B> map(Fn<A, B> fn)
    {
        return ImMappedList.on(this, fn);
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by running
     * {@code fn}
     *  on each element of
     * {@code this}
     *  in order and then joining the resulting
     * {@code ImList}
     * s
     *
     */
    default <T> ImList<T> flatMap(Fn<A, ImList<T>> fn)
    {
        return join(map(fn));
    }

    /**
     * <p> the
     * {@code ImList}
     *  that is the sub-sequence of
     * {@code this}
     *  where
     * {@code pred}
     *  is
     * {@code true}
     *  for each element
     *
     */
    default ImList<A> filter(Fn<A, Boolean> pred)
    {
        return ImFilteredList.on(this, pred);
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains an element,
     * {@code e}
     * , where
     * {@code fn.of(e)}
     *  is
     * {@code true}
     * .
     *
     */
    default boolean contains(Fn<A, Boolean> fn)
    {
        return find(fn).isPresent();
    }

    /**
     * <p> Find the first element,
     * {@code e}
     * , of
     * {@code this}
     * , where
     * {@code fn.of(e)}
     *  is
     * {@code true}
     *  and return
     * {@code ImMaybe.just(e)}
     *  or
     * {@code ImMaybe.nothing}
     * if no such element exists
     *
     */
    default ImMaybe<A> find(Fn<A, Boolean> fn)
    {
        for (A t : this)
            if (fn.of(t))
                return ImMaybe.just(t);

        return ImMaybe.nothing;

    }

    /**
     * <p> The first element,
     * {@code e}
     * , of
     * {@code this}
     *  for which
     * {@code fn.of(e).isPresent()}
     *  and return
     * {@code fn.of(e)}
     * . It stops after the first result is found
     * <p> Used (eg) for searching lists of trees for elements where the search function returns a ImMaybe
     * <p> We simply map using
     * {@code fn}
     *  and then use the fact that map is lazy so we use
     * {@code find}
     *  to find the first result that is present
     * {@code find}
     *  returns a
     * {@code ImMaybe}
     *  of course, so we then have to
     * {@code join}
     *  to remove the outer
     * {@code ImMaybe}
     *  wrapper.
     *
     */
    default ImMaybe<A> findM(Fn<A, ImMaybe<A>> fn)
    {
        return ImMaybe.join(map(i -> fn.of(i)).find(m -> m.isPresent()));
    }

    /**
     * <p> Find the first element,
     * {@code e}
     * , where
     * {@code fn.of(e)}
     *  is
     * {@code true}
     *  and return
     * {@code e}
     * , or
     * {@code def}
     * if no such element exists
     *
     */
    default A findOrElse(Fn<A, Boolean> fn, A def)
    {
        return find(fn).orElse(def);
    }

    /**
     * <p> Find the index,
     * {@code i}
     * , of the first element,
     * {@code e}
     * , of
     * {@code this}
     *  where
     * {@code fn.of(e)}
     *  is
     * {@code true}
     *  and return
     * {@code ImMaybe.just(i)}
     *  or
     * {@code ImMaybe.nothing}
     * if no such element exists
     *
     */
    default ImMaybe<Integer> findIndex(Fn<A, Boolean> fn)
    {
        return findIndex(fn, 1);
    }

    /**
     * <p> Helper method for {@link ImList#findIndex(dev.javafp.func.Fn)}
     */
    private ImMaybe<Integer> findIndex(Fn<A, Boolean> fn, int index)
    {
        return isEmpty()
               ? ImMaybe.nothing
               : fn.of(head())
                 ? ImMaybe.just(index)
                 : tail().findIndex(fn, index + 1);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of sublists of
     * {@code this}
     *  where:
     *
     * <pre>{@code
     * [e1, e2, e3, ..., en] tails == [ [e1, e2, e3, ... en], [e2, e3, ... en], [e3, ... en], ... [en], [] ]
     *
     * [1, 2, 3] tails == [ [1, 2, 3], [2, 3], [3], [] ]
     * }</pre>
     * <p> {@code heads}
     *  and
     * {@code tails}
     *  are related:
     *
     * <pre>{@code
     * ImList.repeat(list, list.size() + 1) == list.heads().zipWith(list.tails(), (a, b) -> a.append(b))
     * }</pre>
     * @see ImList#heads
     *
     */
    default ImList<ImList<A>> tails()
    {
        return ImTailsList.on(this);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of pairs with the first element of each pair taken from
     * {@code this}
     *  and the second taken from
     * {@code other}
     * <p> If
     * {@code this}
     *  and
     * {@code other}
     *  are the same size then:
     *
     * <pre>{@code
     * [a1, a2, ... an] zip [b1, b2, ... bn] == [ (a1, b1), (a2, b2), ... (an, bn) ]
     * }</pre>
     * <p> If the lists are different sizes then we stop when we get to the end of the smaller list.
     *
     */
    default <B> ImList<ImPair<A, B>> zip(ImList<? extends B> other)
    {
        return ImZipWithList.on(this, other, ImPair::on);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of values generated by invoking
     * {@code fn}
     *  on each pair in
     * {@code this.zip(other)}
     *
     * <pre>{@code
     * this.zipWith(other, fn) === this.zip(other).map(p -> fn.of(p.fst, p.snd))
     * }</pre>
     * <p> If the lists are different sizes then we stop when we get to the end of the smaller list.
     *
     */
    default <B, C> ImList<C> zipWith(ImList<? extends B> other, Fn2<A, B, C> fn)
    {
        return ImZipWithList.on(this, other, fn);
    }

    /**
     * <p> A
     * {@code map}
     *  function on
     * {@code this}
     *  - but where the
     * {@code map}
     *  function takes two arguments, the second is an
     * {@code Integer}
     *  and this is
     * is set to the index of the element (starting at 1) for each invokation.
     *
     */
    default <B> ImList<B> mapWithIndex(Fn2<A, Integer, B> fn)
    {
        return this.zipWith(ImRange.step(1, 1), (i, j) -> fn.of(i, j));
    }

    /**
     * <p> A
     * {@code ImList}
     *  with the same size as
     * {@code this}
     *  containing randomly chosen elements from
     * {@code this}
     * <p> It uses the global secure random number generator {@link dev.javafp.rand.Rando}
     *
     */
    default ImList<A> randomPicks()
    {
        return randomInts(new Random(), size()).map(i -> at(i + 1));
    }

    /**
     * <p> Start with an accumulator
     * {@code z}
     *  and iterate
     * <em>backwards</em>
     *  over
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
     * foldr (+) [e1, e2, ... en] z == [ e1 + ( e2 + (... + (en + z)...) ]
     * }</pre>
     * <p> Note that the accumulator,
     * {@code z}
     *  is the second argument to the function.
     * <p> If we extend this to imaginne that the function is called * and can be applied using infix notation - like
     * {@code +}
     *  then
     *
     * <pre>{@code
     * foldr (+) [e1, e2, ... en] z == [ (...((z * e1) * e2) * ... ) * en ]
     * }</pre>
     * <p> Note that we are not assuming that
     * {@code *}
     *  is commutative in its arguments
     *
     */
    default <B> B foldr(Fn2<A, B, B> f, B z)
    {
        return isEmpty()
               ? z
               : f.of(head(), tail().foldr(f, z));
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
    default <B> B foldl(B z, Fn2<B, A, B> f)
    {
        for (A i : this)
            z = f.of(z, i);

        return z;
    }

    /**
     * <p> {@code scanl}
     *  is similar to
     * {@code foldl}
     * , but returns a
     * {@code ImList}
     *  of successive reduced values from the left:
     *
     * <pre>{@code
     * scanl f z [x1, x2, ...] == [z, z `f` x1, (z `f` x1) `f` x2, ...]
     * }</pre>
     * <p> Note that:
     *
     * <pre>{@code
     * last (scanl f z xs) == foldl f z xs.
     * }</pre>
     *
     */
    default <B> ImList<B> scanl(B start, Fn2<B, A, B> f)
    {
        return ImScanlList.on(this, start, f);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of pairs formed from pairing the first element of
     * {@code this}
     *  with all later elements.Then
     * pair the second element with all later elements, and so on
     * <p> Another way to define it is to form the cartesian product of
     * {@code this}
     *  and
     * {@code this}
     *  and then filter that
     * {@code ImList}
     *  for pairs
     * {@code (a, b)}
     *  where:
     *
     * <pre>{@code
     * index(a) < index(b)
     * }</pre>
     * <p> where
     * index(e) is the index of e in
     * {@code this}
     *
     * <pre>{@code
     * makePairsWithHead [1, 2, 3, 4] ImPair::on == [ (1, 2), (1, 3), (1, 4), (2, 3), (2, 4), (3, 4) ]
     * }</pre>
     *
     */
    default ImList<ImPair<A, A>> allPairs()
    {
        ImList<ImList<ImPair<A, A>>> listOfListOfPairs = isEmpty()
                                                         ? empty()
                                                         : tails().map(xs -> makePairsWithHead(xs, ImPair::on));

        return join(listOfListOfPairs);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of pairs formed from invoking the function
     * {@code m.of(xs.head(), xs.at(i))}
     *  for i = 2, 3 ... n
     *
     * <pre>{@code
     * makePairsWithHead [e1, e2, e3, ... en] m == [ m.of(e1,e2), m.of(e1, e3), ... m.of(e1, en) ]
     *
     * makePairsWithHead [1, 2, 3, 4] ImPair::on == [ (1, 2), (1, 3), (1, 4), (2, 3), (2, 4), (3, 4) ]
     * }</pre>
     *
     */
    private static <A> ImList<ImPair<A, A>> makePairsWithHead(ImList<? extends A> xs, Fn2<A, A, ImPair<A, A>> makePairFn)
    {
        ImList<A> ys = xs.upCast();

        return ys.isEmpty()
               ? empty()
               : ys.tail().map(makePairFn.ofFirst(ys.head()));
    }

    /**
     * <p> All
     * <em>combinations</em>
     *  of tuples of size
     * {@code n}
     *  chosen from
     * {@code this}
     * .
     *
     * <p> Let the size of the list be
     * {@code m}
     * .
     * <p> To explain what this function does let's transform the list to a list of integers
     * {@code i}
     *  where
     * {@code 1 ≤ i ≤ m}
     *  and each element with
     * index
     * {@code i}
     *  is mapped to
     * {@code i}
     * .
     *
     * <p> So the list
     *
     * <pre>{@code
     * ["a", "b", "c", "a"]
     * }</pre>
     * <p> would become
     *
     * <pre>{@code
     * [1, 2, 3, 4]
     * }</pre>
     *
     *
     * <p>Each combination is a unique
     * <em>sub-sequence</em>
     * of the list
     *
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <p> A
     * <em>combination</em>
     *  of size
     * {@code n}
     *  has no duplicate elements
     *
     * <pre>{@code
     * [1, 2, 3, 4].allCombinationsOfSize(1) == [[1], [2], [3], [4]]
     * [1, 2, 3, 4].allCombinationsOfSize(2) == [[1, 2], [1, 3], [1, 4], [2, 3], [2, 4], [3, 4],]
     * [1, 2, 3, 4].allCombinationsOfSize(3) == [[1, 2, 3], [1, 2, 4], [1, 3, 4], [2, 3, 4]]
     * [1, 2, 3, 4].allCombinationsOfSize(4) == [[1, 2, 3, 4]]
     * }</pre>
     *
     * <p>If we consider combinations as defined above and then imagine that we then transform each combination of integers to the elements that they represent in the obvious way, then
     * this is what this function does.
     *
     * <p>Using the example above:
     * <pre>{@code
     * ["a", "b", "c", "a"].allCombinationsOfSize(1) == [["a"], ["b"], ["c"], ["a"]]
     * ["a", "b", "c", "a"].allCombinationsOfSize(2) == [["a", "b"], ["a", "c"], ["a", "d"], ["b", "c"], ["b", "a"], ["c", "a"] ]
     * ["a", "b", "c", "a"].allCombinationsOfSize(4) == [["a", "b", "c", "a"]]
     * }</pre>
     *
     * <p> If the list has no duplicates, then it can be transformed to a set of the same size. Each combination of that list represents a unique subset of that set.
     *
     * <p>See <a href="https://www.britannica.com/science/permutation">www.britannica.com/science/permutation</a>
     */
    default ImList<ImList<A>> allCombinationsOfSize(int n)
    {
        if (n > this.size() || this.isEmpty())
            return ImList.on();
        else if (n == 0)
            return ImList.on(ImList.on());
        else if (n == 1)
            return this.map((i -> ImList.on(i)));
        else
            return this.tail().allCombinationsOfSize(n - 1).map(i -> i.push(this.head()))
                    .append(this.tail().allCombinationsOfSize(n));
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by appending
     * {@code this}
     *  to
     * {@code other}
     *
     */
    default ImList<A> prependAll(ImList<A> other)
    {
        return other.append(this);
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by appending
     * {@code this}
     *  to the
     * {@code ImList}
     *  formed from the elements of
     * {@code otherCollection}
     *
     */
    default ImList<A> prependAll(Collection<A> otherCollection)
    {
        return prependAll(ImList.onAll(otherCollection));
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by appending
     * {@code this}
     *  to the
     * {@code ImList}
     *  formed from the elements of
     * {@code things}
     *
     */
    default ImList<A> prepend(A... things)
    {
        return prependAll(ImList.on(things));
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by appending the second element of
     * {@code lists}
     *  to the first and then appending the third element to that and so on
     *
     * <pre>{@code
     * join [e1, e2, e3, ... en] == e1.append(e2).append(e3)....append(en)
     *
     * join [ [1, 2], [3, 4], [5, 6, 7] ] == [1, 2, 3, 4, 5, 6, 7]
     * }</pre>
     *
     */
    static <T> ImList<T> join(ImList<ImList<T>> lists)
    {
        if (lists.isEmpty())
            return empty();
        else if (lists.size() == 1)
            return lists.head();
        else
            return joinLists(lists);
    }

    private static <T> ImList<T> joinLists(ImList<ImList<T>> lists)
    {
        if (allArrayLists(lists))
            return ImListOnArray.joinArrayLists(lists.upCast());
        else
            return ImJoinList.on(lists);
    }

    static <T> boolean allArrayLists(ImList<ImList<T>> lists)
    {
        for (ImList<?> l : lists)
        {
            if (!(l instanceof ImListOnArray<?>))
                return false;
        }

        return true;
    }

    /**
     * <p> The
     * {@code ImList}
     *  formed by appending the second argument to the first and then appending the third argument to that and so on
     *
     * <pre>{@code
     * join [1, 2] [3, 4] [5, 6, 7] == [1, 2, 3, 4, 5, 6, 7]
     * }</pre>
     *
     */
    @SafeVarargs
    static <T> ImList<T> join(ImList<? extends T>... listsArray)
    {
        return join(ImList.on(listsArray).upCast());
    }

    /**
     * <p> Append
     * {@code otherList}
     *  to the end of
     * {@code this}
     * <p> A
     * {@code ImList}
     * {@code ls}
     *  where:
     *
     * <pre>{@code
     * ls.take(n) == this
     * ls.drop(n) == other
     *
     * where n = this.size()
     * }</pre>
     *
     */
    default ImList<A> append(ImList<? extends A> otherList)
    {
        return ImAppendList.on(this, otherList.upCast());
    }

    /**
     * <p> The
     * {@code ImList}
     *  with the same elements as
     * {@code this}
     *  except that
     * {@code element}
     *  has been appended to the end
     *
     */
    default ImList<A> appendElement(A element)
    {
        return append(ImList.on(element));
    }

    /**
     * <p> {@code true}
     *  if
     * {@code itOne}
     *  and
     * {@code itTwo}
     *  contain equal elements in the same order, false otherwise
     *
     */
    default boolean elementsEq(Iterator<?> itOne, Iterator<?> itTwo)
    {
        while (itOne.hasNext())
        {
            if (!itTwo.hasNext() || !Equals.isEqual(itOne.next(), itTwo.next()))
                return false;
        }
        return !itTwo.hasNext();
    }

    /**
     * <p> In mathematics, the
     * <em>power set</em>
     *  of a set,
     * {@code S}
     *  is a set of all the subsets of
     * {@code S}
     *  including the empty set and
     * {@code S}
     *  itself.
     * <p> This function generates all the possible sub-sequences of
     * {@code this}
     * .
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <p> If
     * {@code this}
     *  has no duplicate elements then the sub-sequences are essentially the same as the power set - in that there are he same number of them
     * and each sub-sequence corresponds (contains the same elements) to an element of the power set.
     * <p> If
     * {@code this.size() == n}
     *  then there will be 2^n possible sub-sequencea. If there are duplicate elements in
     * {@code this}
     *  then there will be
     * duplicate elements in the sub-sequences
     * {@code ImList}
     *
     * <pre>{@code
     * [1, 2, 3] powerSet == [ [1, 2, 3], [1, 2], [1, 3], [1], [2, 3], [2], [3], [] ]
     * [1, 2, 2] powerSet == [ [1, 2, 2], [1, 2], [1, 2], [1], [2, 2], [2], [2], [] ]
     * }</pre>
     * <p> @return
     *
     */
    default ImList<ImList<A>> powerSet()
    {
        if (isEmpty())
            return ImList.on(empty());
        else
        {
            ImList<ImList<A>> tp = tail().powerSet();

            return tp.map(i -> cons(head(), i)).append(tp);
        }
    }

    /**
     * <p> A list of
     * {@link ImQuartet}
     *  where the
     * {@code nth}
     *  element is formed from
     * {@code as.at(n)}
     * ,
     * {@code bs.at(n)}
     * ,
     * {@code cs.at(n)}
     * ,
     * {@code ds.at(n)}
     *
     */
    public static <C, D, A, B> ImList<ImQuartet<A, B, C, D>> tuple4On(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        return ImQuartetList.on(as, bs, cs, ds);
    }

    /**
     * <p> A list of
     * {@link ImTriple}
     *  where the
     * {@code nth}
     *  element is formed from
     * {@code as.at(n)}
     * ,
     * {@code bs.at(n)}
     * ,
     * {@code cs.at(n)}
     *
     */
    public static <B, A, C> ImList<ImTriple<A, B, C>> tuple3On(ImList<A> as, ImList<B> bs, ImList<C> cs)
    {
        return ImTripleList.on(as, bs, cs);
    }

    /**
     * <p> The
     * {@code ImList}
     *  with head
     * {@code head}
     *  and tail
     * {@code this}
     *
     */
    default ImList<A> withHead(A head)
    {
        return this.push(head);
    }

    /**
     * <p> The
     * {@code ImList}
     *  with head
     * {@code head}
     *  and tail
     * {@code this}
     *
     */
    default ImList<A> push(A head)
    {
        return new ImConsList<>(head, this);
    }

    /**
     * <p> A
     * {@code ImList}
     *  of
     * {@code ImList}
     * s where
     * the
     * {@code i}
     * th element is
     * {@code this}
     *  but with
     * {@code thing}
     *  inserted at index
     * {@code i}
     * .
     *
     * <pre>{@code
     * [1, 2, 3].generateListsByInjecting(0) ==  [ [0, 1, 2, 3], [1, 0, 2, 3], [1, 2, 0, 3], [1, 2, 3, 0] ]
     * }</pre>
     *
     */
    default ImList<ImList<A>> generateListsByInjecting(A thing)
    {
        return new ImIpList<>(empty(), thing, this);
    }

    /**
     * <p> A
     * {@code ImList}
     *  containing all the possible permutations of the elements of
     * {@code this}
     *
     * <pre>{@code
     * [1, 2, 3] permutations == [ [1, 2, 3], [2, 1, 3], [2, 3, 1], [1, 3, 2], [3, 1, 2], [3, 2, 1] ]
     * }</pre>
     * <p> Since
     * {@code ImList}
     * s can contain duplicate elements, the permutaions might not all be distinct:
     *
     * <pre>{@code
     * [1, 1] permutations == [ [1, 1], [1, 1] ]
     * }</pre>
     *
     */
    default ImList<ImList<A>> permutations()
    {
        return isEmpty()
               ? on(empty())
               : tail().permutations().flatMap(i -> i.generateListsByInjecting(head()));
    }

    /**
     * <p> Allows
     * {@code ImList<U>}
     *  to be cast to
     * {@code ImList<T>}
     *  (where U is a subtype of T) without generating a compiler error - or even a warning.
     * <p> In general, Java does not regard this cast as type safe because of the "cuckoo in the nest" problem:
     * <h4>The cuckoo in the nest problem</h4>
     * <p> Let's say we have a 'List' of 'Strings' - an ordinary mutable Java List
     *
     * <pre>{@code
     * List<String> strings = new ArrayList<String>(...);
     * }</pre>
     * <p> Should you be able to make this assignment (an implicit cast)?
     *
     * <pre>{@code
     * List<Object> nest = strings;   // List<String> is being implicitly cast to List<Object>
     * }</pre>
     * <p> At first glance this looks reasonable. Since every object in
     * {@code nest}
     *  is a
     * {@code String}
     *  it is also an
     * {@code Object}
     *  - so we can get an element from
     * {@code nest}
     *  and use a method that is defined in
     * {@code Object}
     *  and there shouldn't be a problem.
     * <p> Indeed this would be ok.
     * <p> The problem is that Java
     * {@code Lists}
     *  are
     * <em>mutable</em>
     * .
     * <p> So we could now do this:
     *
     * <pre>{@code
     * Object cuckoo = new Object();
     * nest.add(cuckoo);
     * }</pre>
     * <p> Again, at first glance, this seems ok too.
     * {@code nest}
     *  is declared as a list of
     * {@code Object}
     * s, that
     * {@code cuckoo}
     *  you added was an
     * {@code Object}
     * , so what's the problem?
     * <p> Well, the problem is with the
     * {@code List}
     *  we started with -
     * {@code strings}
     * .
     * <p> {@code strings}
     *  and
     * {@code nest}
     *  point to the same
     * {@code List}
     * . The
     * {@code List}
     *  that we just added
     * {@code cuckoo}
     *  to.
     * <p> So, from the point of view of
     * {@code strings}
     * , we now have a
     * {@code List}
     *  of
     * {@code String}
     * s that contains one element that actually isn't a
     * {@code String}
     * .
     *
     * <pre>{@code
     * strings.get(0).length();       // java.lang.ClassCastException: java.lang.Object cannot be cast to java.lang.String
     * }</pre>
     * <p> So Java does not allow you to cast a
     * {@code List}
     *  of
     * {@code Strings}
     *  to a
     * {@code List}
     *  of
     * {@code Object}
     * s.
     * <p> In fact, Java goes even further. If you declare
     * <em>any</em>
     *  class as having a type variable
     * {@code A}
     *  (say), then it assumes that you are going to be storing an
     * object of type
     * {@code A}
     *  in instances of that class so if you had:
     *
     * <pre>{@code
     * Wibble<String>  stringWibble = new Wibble(...);
     * }</pre>
     * <p> then Java will try to prevent you from doing this:
     *
     * <pre>{@code
     * Wibble<Object> objectWibble = stringWibble;
     * }</pre>
     * <p> on the basis that a similar problem to that of
     * {@code List}
     * s will occur.
     * <p> {@code java_fp}
     *  has
     * {@code ImList}
     *  which Java treats just like a
     * {@code Wibble}
     *  - or any other class with a type parameter. It will prevent you saying this:
     *
     * <pre>{@code
     * ImList<String> strings = ImList.on(...);
     * ImList<Object> nest = strings;
     * }</pre>
     * <p> But, in
     * {@code java_fp}
     * , since
     * {@code ImList}
     * s are
     * <em>immutable</em>
     * , it is always safe to (implicitly) cast them in this way. You can't add a
     * {@code Cuckoo}
     *  to
     * {@code nest}
     * because - well - you can't add
     * <em>anything</em>
     *  to a
     * {@code ImList}
     *  - in the sense of mutating it.
     * <p> Having established that it is safe to cast in this way, you will find that it is surprisingly difficult
     * to actually do it without the Java compiler getting very cross.
     * <p> Let's look at an example.
     * <p> Assuming
     * {@code ints}
     *  and
     * {@code numbers}
     *  declared like this:
     *
     * <pre>{@code
     * ImList<Integer> ints;
     * ImList<Number>  ns;
     * }</pre>
     * <p> Let's see what the Java compiler has to say when we naively try various approaches (These error/warnings are from the
     * Eclipse Java compiler):
     * <p> This
     * <em>obviously</em>
     *  produces an error:
     *
     * <pre>{@code
     * ns = ints;                                      => Type mismatch: cannot convert from ImList<Integer> to ImList<Number>
     * }</pre>
     * <p> This also produces an error:
     *
     * <pre>{@code
     * ns = (ImList<Number>) ints;                        => Cannot cast from ImList<Integer> to ImList<Number>
     * }</pre>
     * <p> In desperation (how many times have we all been here?) we try this:
     *
     * <pre>{@code
     * ns = (ImList<? extends Number>) ints;               => Type mismatch: cannot convert from ImList<capture#1-of ? extends Number> to ImList<Number>
     * }</pre>
     * <p> This (somewhat recondite) cast
     * <em>is</em>
     *  allowed, merely generating a warning:
     *
     * <pre>{@code
     * ns = (ImList<Number>) (ImList<? extends Number>) ints; => Type safety: Unchecked cast from ImList<capture#1-of ? extends Number> to ImList<Number>
     * }</pre>
     * <p> For your comfort and safety, we have provided a function,
     * {@code upCast}
     * ,  to make it easier to do these casts.
     * <h4>Using {@code upCast}:</h4>
     * <p> You can do this:
     *
     * <pre>{@code
     * ns = ints.<Number>upCast();
     * }</pre>
     * <p> In fact, in this particular case, it is even simpler:
     *
     * <pre>{@code
     * ns = ints.upCast();
     * }</pre>
     *
     */
    @SuppressWarnings("unchecked")
    default <U> ImList<U> upCast()
    {
        return (ImList<U>) this;
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
     * ImList.on().all(...) == true
     * ImList.on().any(...) == false
     * }</pre>
     * @see ImList#all(dev.javafp.func.Fn)
     *
     */
    default boolean any(Fn<A, Boolean> pred)
    {
        return or(map(pred));
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
     * ImList.on().all(...) == true
     * ImList.on().any(...) == false
     * }</pre>
     * @see ImList#any(dev.javafp.func.Fn)
     *
     */
    default boolean all(Fn<A, Boolean> pred)
    {
        return and(map(pred));
    }

    /**
     * <p> The cartesian product of each element (a list) of l with each other element
     * <p> Given a list of lists:
     *
     * <pre>{@code
     * [ L1, L2, L3, ... Ln ]
     * }</pre>
     * <p> the result of
     * {@code cross}
     *  is a list of tuples:
     *
     * <pre>{@code
     * (e1, e2, e3, ... en) where e1 ∈ L1, e2 ∈ L2, e3 ∈ L3, ... en ∈ Ln,
     *
     * cross [ ["a", "b"], [1, 2], [true, false] ] ==
     *     [ ["a", 1, true], ["a", 1, false], ["a", 2, true], ["a", 2, false],
     *       ["b", 1, true], ["b", 1, false], ["b", 2, true], ["b", 2, false] ]
     * }</pre>
     *
     */
    static <T> ImList<ImList<T>> cross(ImList<ImList<? extends T>> l)
    {
        if (l.isEmpty())
            return ImList.on(ImList.on());
        else
        {
            ImList<ImList<T>> rest = cross(l.tail());
            return l.head().flatMap(i -> rest.map(list -> list.withHead(i)));
        }
    }

    /**
     * <p> The cartesian product of
     * {@code list}
     *  and itself
     *
     * <pre>{@code
     * cartesianProduct [1, 2] ==  [ (1, 1), (1, 2), (2, 1), (2, 2)]
     * }</pre>
     *
     */
    private static <T> ImList<ImPair<T, T>> cartesianProduct(ImList<? extends T> list)
    {
        return cartesianProduct(list, list);
    }

    /**
     * <p> The cartesian product of
     * {@code this}
     *  and itself
     *
     * <pre>{@code
     * cartesianProduct [1, 2] ==  [ (1, 1), (1, 2), (2, 1), (2, 2)]
     * }</pre>
     * @see ImList#cartesianProduct(ImList, ImList)
     *
     */
    default ImList<ImPair<A, A>> cartesianProduct()
    {
        return cartesianProduct(this);
    }

    /**
     * <p> The cartesian product of
     * {@code one}
     *  and
     * {@code two}
     *
     * <pre>{@code
     * The cartesian product of two sets A, B is the set of all pairs (a, b) where a ∈ A and b ∈ B
     *
     *  cartesianProduct ["a", "b"] [3, 4, 5] ==  [("a", 3), ("a", 4), ("a", 5), ("b", 3), ("b", 4), ("b", 5)]
     * }</pre>
     *
     */
    static <T, U> ImList<ImPair<T, U>> cartesianProduct(ImList<? extends T> one, ImList<? extends U> two)
    {
        return cartesianProduct(one, two, ImPair::on);
    }

    /**
     * <p> Form the cartesian product of
     * {@code one}
     *  and
     * {@code two}
     *  and then apply
     * {@code f}
     *  to each element of the resulting pair
     *
     * <pre>{@code
     * The cartesian product of two sets A, B is the set of all pairs (a, b) where a ∈ A and b ∈ B
     * }</pre>
     *
     */
    static <T, U, V> ImList<V> cartesianProduct(ImList<? extends T> one, ImList<? extends U> two, Fn2<T, U, V> f)
    {
        return one.isEmpty()
               ? ImList.on()
               : two.map(i -> f.of(one.head(), i)).append(cartesianProduct(one.tail(), two, f));
    }

    /**
     * <p> {@code true}
     *  if the
     * {@code this}
     *  is a sub-sequence of
     * {@code other}
     *
     * <pre>{@code
     * [1, 5, 7] isSubSequence [0, 1, 2, 3, 1, 1, 5, 1, 7, 11] == true
     * }</pre>
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <pre>{@code
     * [a, b, c] has sub-sequences [], [a], [b], [c], [b, c], [a, c], [a, b]
     * }</pre>
     *
     */
    default boolean isSubSequence(ImList<? extends A> other)
    {
        return isEmpty() || (!other.isEmpty() && (Eq.uals(head(), other.head())
                                                  ? tail().isSubSequence(other.tail())
                                                  : isSubSequence(other.tail())));
    }

    /**
     * <p> The
     * {@code ImList}
     *  that is the same as
     * {@code this}
     *  except that all occurrences of
     * {@code thingToRemove}
     *  are missing
     *
     * <pre>{@code
     * [1, 2, 3, 4, 1, 4 ] remove(4) == [1, 2, 3, 1]
     * [1, 2, 3, 4, 1, 4 ] remove(11) == [1, 2, 3, 4, 1, 4 ]
     * }</pre>
     *
     */
    default ImList<A> remove(A thingToRemove)
    {
        return filter(i -> !Eq.uals(i, thingToRemove));
    }

    /**
     * <p> The
     * {@code ImList}
     *  that is the same as
     * {@code this}
     *  except that all the elements that are also in
     * {@code elementsToRemove}
     *  are missing
     *
     * <pre>{@code
     * [1, 2, 3, 4, 1, 4 ] removeAll [1, 4, 1, 11] == [2, 3]
     * }</pre>
     *
     */
    default ImList<A> removeAll(ImList<? extends A> elementsToRemove)
    {
        return filter(i -> !elementsToRemove.upCast().contains(i));
    }

    /**
     * <p> The
     * {@code ImList}
     *  that is the same as
     * {@code this}
     *  except that the element with index
     * {@code indexToRemoveStartingAtOne}
     *  is missing.
     * <p> If
     * {@code indexToRemoveStartingAtOne < 1}
     *  or
     * {@code indexToRemoveStartingAtOne > this.size()}
     *  then the returned ImList will be
     * {@code this}
     *
     */
    default ImList<A> removeAt(int indexToRemoveStartingAtOne)
    {
        return indexToRemoveStartingAtOne < 1
               ? this
               : isEmpty()
                 ? this
                 : indexToRemoveStartingAtOne == 1
                   ? tail()
                   : cons(head(), tail().removeAt(indexToRemoveStartingAtOne - 1));
    }

    /**
     * <p> A
     * {@code ImList}
     *  that has the same elements as
     * {@code this except that each element }
     * e
     * {@code where}
     * pred.of(e) = true
     *  is replaced with
     * {@code replacement}
     *
     * <pre>{@code
     * [1, 2, 3, 4, 5, 88] replace(isEven, 0) == [1, 0, 3, 0, 5, 0]
     * }</pre>
     *
     */
    default ImList<A> replace(Fn<A, Boolean> pred, A replacement)
    {
        return map(i -> pred.of(i) ? replacement : i);
    }

    /**
     * <p> Split
     * {@code this}
     *  into two
     * {@code ImList}
     * s after index
     * {@code oneBasedIndexToSplitAfter}
     * <p> If
     * {@code oneBasedIndexToSplitAfter}
     *  is 0 then the first
     * {@code ImList}
     *  is empty
     * <p> If oneBasedIndexToSplitAfter >= this.size() then the second list is empty
     * <p> if
     * {@code oneBasedIndexToSplitAfter < 0}
     *   then {@link dev.javafp.ex.ArgumentShouldNotBeLessThan} is thrown
     *
     */
    default ImPair<ImList<A>, ImList<A>> splitAfterIndex(int oneBasedIndexToSplitAfter)
    {
        Throw.Exception.ifLessThan("oneBasedIndexToSplitAfter", oneBasedIndexToSplitAfter, 0);

        return Sz.getSz(this) >= 0 && oneBasedIndexToSplitAfter >= this.size()
               ? (ImPair.on(this, ImList.on()))
               : heads().zip(tails()).at(oneBasedIndexToSplitAfter + 1);
    }

    /**
     * <p> Split
     * {@code this}
     *  into two
     * {@code ImList}
     * s a at the first occurrence of element
     * {@code e}
     * <p> Returns a
     * {@link ImPair}
     * ,
     * {@code p}
     * where
     *
     * {@code ImList.join(p.fst(), ImList.on(e), p.snd()) === list}
     *  and
     * {@code p.fst()}
     *  does not contain
     * {@code e}
     * .
     * <p> If
     * {@code list}
     *  does not contain
     * {@code e}
     * then
     * {@code p.fst() === list}
     *
     */
    default ImPair<ImList<A>, ImList<A>> splitBeforeElement(A c)
    {
        return splitBeforeElement(c, ImList.on(), this);
    }

    private ImPair<ImList<A>, ImList<A>> splitBeforeElement(A c, ImList<? super A> first, ImList<? extends A> rest)
    {

        return rest.isEmpty()
               ? ImPair.on(first.upCast(), ImList.on())
               : Eq.uals(rest.head(), c)
                 ? ImPair.on(first.upCast(), rest.upCast())
                 : splitBeforeElement(c, first.appendElement(rest.head()), rest.tail());
    }

    /**
     * <p> A
     * {@code ImList}
     *  with
     * {@code sep}
     *  added after each element (apart from the last element)
     *
     * <pre>{@code
     * [a, b, c] intersperse x == [a, x, b, x, c]
     * [] intersperse x == []
     * }</pre>
     *
     */
    default ImList<A> intersperse(A sep)
    {
        if (isEmpty())
            return this;
        else
        {
            ImList<ImList<A>> lists = tail().zipWith(repeat(sep, size() - 1), (i, j) -> ImList.on(j, i));
            return join(lists).withHead(head());
        }
    }

    /**
     * <p> Copy the elements of
     * {@code this}
     *  to a new array of type
     * {@code clazz}
     *
     */
    default A[] toArray(Class<?> clazz)
    {
        return toArray((A[]) (Array.newInstance(clazz, size())));
    }

    /**
     * <p> Copy the elements of
     * {@code this}
     *  to
     * {@code targetArray}
     *
     * We have to iterate over the list, copying it one element at a time, because the list might be lazy
     *
     */
    private A[] toArray(A[] targetArray)
    {
        int i = 0;
        for (A thing : this)
            targetArray[i++] = thing;

        return targetArray;
    }

    /**
     * <p> Convenience method to let us easily iterate over
     * {@code ImList}
     * s of {@link ImPair}
     * <p> This lets us do things like this:
     *
     * <pre>{@code
     * ImList.foreachPair(pairs, (i, j) ->  assertEquals(i.equals(j), i == j));
     * }</pre>
     *
     */
    static <A, B> void foreachPair(ImList<ImPair<A, B>> list, FnPairConsumer<A, B> action)
    {
        for (ImPair<A, B> p : list)
            action.doit(p.fst, p.snd);
    }

    /**
     * <p> Given lists
     * {@code a}
     * ,
     * {@code b}
     *  return all the possible lists
     * {@code ls}
     *  where each
     * {@code ImList}
     * {@code l}
     *  in
     * {@code ls}
     *  has both
     * {@code a}
     *  and
     * {@code b}
     *  as sub-sequences and
     * {@code |a| + |b| = |l|}
     *
     * <pre>{@code
     * If a and b have any elements that are equal to each other, then not all elements in ls will be distinct
     * If a and b have no elements that are equal then all elements in ls will be distinct
     * }</pre>
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <pre>{@code
     * [a, b, c] has sub-sequences [], [a], [b], [c], [b, c], [a, c], [a, b]
     *
     * interleave [1, 2], [3, 4] -> [1, 2, 3, 4], [1, 3, 2, 4], [1, 3, 4, 2], [3, 1, 2, 4], [3, 1, 4, 2], [3, 4, 1, 2]
     * }</pre>
     *
     */
    static <B> ImList<ImList<B>> interleave(ImList<? extends B> a, ImList<? extends B> b)
    {
        return a.isEmpty()
               ? ImList.on(b).upCast()
               : b.isEmpty()
                 ? ImList.on(a).upCast()
                 : interleave(a.tail(), b).map(i -> i.push(a.head())).append(interleave(a, b.tail()).map(i -> i.push(b.head())));
    }

    /**
     * <p> A
     * {@code ImList}
     *  of sublists of
     * {@code this}
     *
     * <pre>{@code
     * [e1, e2, e3, ..., en] heads == [ [], [e1], [e1, e2], [e1, e2, e3], ..., [e1, e2, e3, ..., en] ]
     * }</pre>
     * @see ImList#tails()
     *
     */
    default ImList<ImList<A>> heads()
    {
        if (Sz.getSz(this) == KNOWN_INFINITE)
            return ImRange.step(0, 1).map(n -> take(n));
        else
            return ImRange.inclusive(0, size()).map(n -> take(n));
    }

    /**
     * <p> A {@link ImListZipper} on
     * {@code this}
     *  that is positioned
     * "before the start of the list"
     *
     */
    default ImListZipper<A> getZipper()
    {
        return ImListZipper.on(this);
    }

    /**
     * <p> A {@link ImListZipper} focused on the element of
     * {@code this}
     *  with index
     * {@code indexStartingAtOne}
     *
     */
    default ImListZipper<A> getZipperOnIndex(int indexStartingAtOne)
    {
        if (indexStartingAtOne < 1 || indexStartingAtOne > size())
        {
            throw new NoSuchElementException();
        }
        else
        {
            return new ImListZipper<>(take(indexStartingAtOne).reverse(), drop(indexStartingAtOne));
        }
    }

    /**
     * <p> A
     * {@code ImList}
     *  of
     * {@code ImList}
     * s,
     * {@code parts}
     * , where:
     *
     * <pre>{@code
     * ImList.join(parts) == this
     * all elements e of parts have e.size <= groupSize
     * if this is not the empty list then all elements, e of parts have e.size > 0
     * if groupSize is not a factor of this.size() then parts.last() will have size this.size() % groupSize
     * }</pre>
     * <p> If
     * {@code maxSublistSize < 1}
     * then
     * {@link dev.javafp.ex.ArgumentShouldNotBeLessThan}
     * is thrown.
     *
     */
    default ImList<ImList<A>> group(int groupSize)
    {
        Throw.Exception.ifLessThan("groupSize", groupSize, 1);

        return (groupSize >= size())
               ? ImList.on(this)
               : cons(take(groupSize), drop(groupSize).group(groupSize));
    }

    /**
     * <p> The sub-sequence of
     * {@code this}
     *  such that no two elements are equal
     *
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <pre>{@code
     * [7, 7, 11, 7, 11, 7, 17, 19, 7] nub ==  [7, 11, 17, 19]
     * }</pre>
     *
     * <pre>{@code
     * [a, b, c] has sub-sequences [], [a], [b], [c], [b, c], [a, c], [a, b]
     * }</pre>
     *
     */
    default ImList<A> nub()
    {
        return isEmpty()
               ? empty()
               : tail().filter(i -> !Eq.uals(i, head())).nub().push(head());
    }

    /**
     * <p> The sub-sequence of
     * {@code this}
     *  such that no two elements are equal according to
     * {@code eq}
     *
     * <pre>{@code
     * [7, 7, 11, 7, 11, 7, 17, 19, 7] nub ==  [7, 11, 17, 19]
     * }</pre>
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <pre>{@code
     * [a, b, c] has sub-sequences [], [a], [b], [c], [b, c], [a, c], [a, b]
     * }</pre>
     *
     */
    default ImList<A> nub(Fn2<A, A, Boolean> eq)
    {

        return isEmpty()
               ? empty()
               : tail().filter(i -> !eq.of(i, head())).nub(eq).push(head());

    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains all the elements in
     * {@code elementsToCheck}
     * ,
     * {@code false}
     *  otherwise
     *
     */
    default boolean containsAll(Iterable<? extends A> elementsToCheck)
    {
        return ImList.onAll(elementsToCheck).all(e -> contains(e));
    }

    /**
     * <p> A
     * {@code ImList}
     *  that has the same elements as
     * {@code this}
     *  but sorted based on the natural ordering of the objects
     *
     */
    default ImList<A> sort()
    {
        return ImList.onList(stream().sorted().collect(Collectors.toList()));
    }

    /**
     * <p> A
     * {@code ImList}
     *  that has the same elements as
     * {@code this}
     *  but sorted based on the natural ordering of the object returned by
     * {@code getterFn}
     *
     */
    default <B extends Comparable<B>> ImList<A> sort(Function<A, B> getterFn)
    {
        return ImList.onList(stream().sorted(Comparator.comparing(getterFn)).collect(Collectors.toList()));
    }

    /**
     * <p> A
     * {@code ImList}
     *  that has the same elements as
     * {@code this}
     *  but sorted based on the comparator function
     * {@code comparatorFn}
     *
     */
    default ImList<A> sort(Comparator<A> comparatorFn)
    {
        return ImList.onList(stream().sorted(comparatorFn).collect(Collectors.toList()));
    }

    /**
     * <p> Return a {@link java.util.stream.Stream} for
     * {@code this}
     * . We simply return a
     * {@code Stream}
     *  on the
     * {@code Spliterator}
     *  for this
     * {@code ImList}
     *  - one
     * that is a squential stream.
     *
     */
    default Stream<A> stream()
    {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * <p> Return a {@link java.util.Spliterator} for
     * {@code this}
     * . We simply return a
     * {@code Spliterator}
     *  on the iterator for this list.
     * <p> The characteristics for the
     * {@code Spliterator}
     *  are set to
     * {@code SIZED | IMMUTABLE}
     *
     */
    @Override
    default Spliterator<A> spliterator()
    {
        return Spliterators.spliterator(iterator(), size(), SIZED | IMMUTABLE);
    }

    /**
     * <p> Return two lists where the first
     * {@code ImList}
     *  is the smallest sublist of
     * {@code this}
     *  where
     * {@code pred}
     *  is true and the second is the rest of the list
     * <p> Note that
     * {@code pred}
     *  is applied to the first part of the list for each "iteration" - not the list element
     *
     */
    default ImPair<ImList<A>, ImList<A>> splitWhileListIsTrueFor(Fn<ImList<A>, Boolean> pred)
    {
        if (isEmpty())
            return Pai.r(ImList.on(), ImList.on());
        else
        {
            /*
                this = [1, 2, 3]

                ps          ( [],        [1, 2, 3] )
                            ( [1],       [2, 3]    )
                            ( [1, 2]     [3]       )
                            ( [1, 2, 3], []        )


                psWithPrev  ( [1],       [2, 3]    )    ( [],        [1, 2, 3] )
                            ( [1, 2]     [3]       )    ( [1],       [2, 3]    )
                            ( [1, 2, 3], []        )    ( [1, 2]     [3]       )

             */
            ImList<ImPair<ImList<A>, ImList<A>>> ps = this.heads().zip(this.tails());
            ImList<ImPair<ImPair<ImList<A>, ImList<A>>, ImPair<ImList<A>, ImList<A>>>> psWithPrev = ps.tail().zip(ps);

            ImList<ImPair<ImPair<ImList<A>, ImList<A>>, ImPair<ImList<A>, ImList<A>>>> pairs = psWithPrev.dropWhile(p -> pred.of(p.fst.fst));
            return pairs.isEmpty()
                   ? Pai.r(this, ImList.on())
                   : pairs.head().snd;
        }
    }

    /**
     * <p> Return two lists where the first
     * {@code ImList}
     *  is the smallest prefix of
     * {@code this}
     *  where
     * {@code pred}
     *  is true and the second is the rest of the list
     */
    default ImPair<ImList<A>, ImList<A>> splitWhile(Fn<A, Boolean> pred)
    {
        return this.isEmpty()
               ? Pai.r(ImList.on(), ImList.on())
               : this.heads().zip(this.tails()).dropWhile(p -> testOnHeadIfItExists(pred, p.snd)).head();
    }

    private boolean testOnHeadIfItExists(Fn<A, Boolean> pred, ImList<A> list)
    {
        return list.isEmpty()
               ? false
               : pred.of(list.head());
    }

    /**
     * <p> Given a list, return a
     * {@code ImList}
     *  of lists where each sublist,
     * {@code l}
     *  is the smallest
     * {@code ImList}
     *  such that
     * {@code pred(l)}
     *  is true
     *
     */
    default ImList<ImList<A>> group$(Fn<ImList<A>, Boolean> pred)
    {
        if (isEmpty())
            return ImList.on();
        else
        {
            ImPair<ImList<A>, ImList<A>> pair = splitWhileListIsTrueFor(pred);
            return cons(pair.fst, pair.snd.group$(pred));
        }
    }

    /**
     * <p> Split
     * {@code this}
     * into
     * {@code n}
     * lists.
     *
     * <p> In other words:
     *
     * <p> A
     * {@code ImList}
     *  of
     * {@code ImList}
     * s,
     * {@code parts}
     * , where:
     *
     * <pre>{@code
     * parts.size() == n
     * ImList.join(parts) == this
     * the elements of parts, which are lists, have sizes that differ by at most 1
     * if some parts are larger than others then the larger parts are at the front
     * }</pre>
     *
     */
    default ImList<ImList<A>> splitIntoParts(int n)
    {
        if (isEmpty())
            return ImList.repeat(ImList.on(), n);
        else if (n == 0)
            return ImList.on();
        else
        {
            // Get the sublists size (truncating)
            int slSize = size() / n;

            if (slSize > 0)
            {
                // There might be a remainder
                //    1     2     3     4   remainder
                // |-----|-----|-----|-----|---
                //
                int rem = size() - slSize * n;

                // That remainder must be < n

                // If rem > 0, let's make the first rem sublists bigger by one

                return rem == 0
                       ? splitIntoMaxSize(slSize)
                       : take((slSize + 1) * rem).splitIntoMaxSize((slSize + 1)).append(drop((slSize + 1) * rem).splitIntoMaxSize(slSize));
            }
            else
                return splitIntoMaxSize(1).append(ImList.repeat(ImList.on(), n - size()));
        }
    }

    /**
     * <p> A
     * {@code ImList}
     *  of
     * {@code ImList}
     * s,
     * {@code parts}
     * , where:
     *
     * <pre>{@code
     * ImList.join(parts) == this
     * all elements e of parts have e.size <= maxSublistSize
     * if this is not the empty list then all elements, e of parts have e.size > 0
     * if maxSublistSize is not a factor of this.size() then parts.last() will have size this.size() % maxSublistSize
     * }</pre>
     * <p> If
     * {@code maxSublistSize < 1}
     * then
     * {@link dev.javafp.ex.ArgumentShouldNotBeLessThan}
     * is thrown.
     *
     */
    default ImList<ImList<A>> splitIntoMaxSize(int maxSublistSize)
    {
        return group(maxSublistSize);
    }

    /**
     * <p> A
     * {@code ImList}
     * ,
     * {@code pairs}
     *  of pairs (a, b) where:
     *
     * <pre>{@code
     * pairs = [ (this.at(1), this.at(2)), (this.at(2), this.at(3)), ..., (this.at(n-1), this.at(n))]
     * }</pre>
     * <p> where
     * {@code n = this.size}
     * <p> If
     * {@code this.size()}
     *  is not even then {@link dev.javafp.ex.InvalidState} is thrown.
     *
     */
    default ImList<ImPair<A, A>> toPairs()
    {
        Throw.Exception.ifTrue(this.size() % 2 != 0, "size is " + this.size() + " which is not even");
        return this.group(2).map(l -> ImPair.on(l.at(1), l.at(2)));
    }

    /**
     * <p> A pair of
     * {@code ImList}
     * s
     * {@code (p1,p2)}
     *  with:
     *
     * <pre>{@code
     * p1 = this.takeWhile(pred)
     * p2 = this.dropWhile(pred)
     * }</pre>
     * <p> The actual implementation is more efficient than this
     * @see ImList#takeWhile(dev.javafp.func.Fn)
     * @see ImList#dropWhile(dev.javafp.func.Fn)
     * @see ImList#cutIntoThree(dev.javafp.func.Fn)
     *
     */
    default ImPair<ImList<A>, ImList<A>> cutIntoTwo(Fn<A, Boolean> pred)
    {
        return this.splitWhile(pred);
    }

    private static <A> ImPair<ImList<A>, ImList<A>> cutIntoTwo(ImList<A> first, Fn<A, Boolean> fn, ImList<A> second)
    {
        if (second.isNotEmpty() && fn.of(second.head()))
            return cutIntoTwo(first.push(second.head()), fn, second.tail());
        else
            return ImPair.on(first, second);
    }

    /**
     * <p> Three
     * {@code ImList}
     * s
     * {@code (p1,p2,p3)}
     *  with:
     *
     * <pre>{@code
     * this == ImList.join(p1, p2, p3)
     * pred false for all in p1,
     * pred true for all in p2,
     * if p3 is not empty then pred false for p3.head()
     * }</pre>
     * <p> Note that we are not making any assertions about
     * {@code p3.tail()}
     *
     * <pre>{@code
     * [ 1, 2, 99, 88, 3, 44] cutIntoThree(i -> i > 50) == ( [1, 2], [99, 88], [3, 44] )
     * [ 1, 2, 99, 88]        cutIntoThree(i -> i > 50) == ( [1, 2], [99, 88], [] )
     * }</pre>
     * @see ImList#takeWhile(dev.javafp.func.Fn)
     * @see ImList#dropWhile(dev.javafp.func.Fn)
     * @see ImList#cutIntoTwo(dev.javafp.func.Fn)
     *
     */
    default ImTriple<ImList<A>, ImList<A>, ImList<A>> cutIntoThree(Fn<A, Boolean> pred)
    {
        // get the first pair
        ImPair<ImList<A>, ImList<A>> one = cutIntoTwo(Fn.not(pred));

        ImPair<ImList<A>, ImList<A>> two = one.snd.cutIntoTwo(pred);

        return ImTriple.on(one.fst, two.fst, two.snd);
    }

    /**
     * <p> A pair of lists
     * {@code (a, b)}
     *  where:
     *
     * <pre>{@code
     * a and b are sub-sequences of
     * {@code this}
     *
     * {@code pred}
     *  is true for all elements in a and false for all elements in b
     * }</pre>
     *
     * <p> A <em>sub-sequence</em>,
     * {@code s}
     * , of a
     * {@code ImList}
     * {@code l}
     *  is a
     * {@code list}
     *  that can be obtained from
     * {@code l}
     *  by deleting 0 or more elements
     *
     * <pre>{@code
     * [a, b, c] has sub-sequences [], [a], [b], [c], [b, c], [a, c], [a, b]
     * }</pre>
     * @see ImList#takeWhile(dev.javafp.func.Fn)
     * @see ImList#dropWhile(dev.javafp.func.Fn)
     *
     */
    default ImPair<ImList<A>, ImList<A>> filterIntoTwo(Fn<A, Boolean> pred)
    {
        return ImPair.on(filter(a -> pred.of(a)), filter(Fn.not(pred)));
    }

    /**
     * <p> A
     * {@code ImList}
     *  where the elements are the same as those of
     * {@code this}
     *  but they are "shuffled" (like shuffling a pack of cards) using the
     * global secure random number generator {@link dev.javafp.rand.Rando}
     *
     */
    default ImList<A> shuffle()
    {
        List<A> list = toList();
        Collections.shuffle(list, Rando.random);

        return ImList.onAll(list);
    }

    /**
     * <p> A list of integers
     * {@code [min, min + 1, min + 2, ... max]}
     * <p> If
     * {@code min > max}
     *  then return
     * {@code []}
     * <p> If
     * {@code min == max}
     *  then return
     * {@code [min]}
     *
     */
    public static ImList<Integer> inclusive(int min, int max)
    {
        return inclusive(min, max, 1);
    }

    /**
     * <p> A list of integers
     * {@code [min, min + step, min + 2*step, ... max]}
     * <p> If
     * {@code min > max}
     *  then return
     * {@code []}
     * <p> If
     * {@code step}
     * is not a factor of
     * {@code max - min}
     *  then throw
     * {@link InvalidArgument}
     *
     */
    public static ImList<Integer> inclusive(int min, int max, int step)
    {

        if (min > max)
            return ImList.on();

        if ((max - min) % step != 0)
            throw new InvalidArgument("step", step, String.format("value must divide max - min but step = %d, min = %d, max = %d", step, min, max));

        return ImRangeList.inclusive(min, max, step);
    }

    /**
     * <p> A list of integers
     * {@code [0, 1, 2, ... maxIndexPlusOne - 1]}
     * <p> If
     * {@code maxIndexPlusOne <= 0}
     *  then return
     * {@code []}
     * <p> If
     * {@code maxIndexPlusOne == 1 }
     *  then return
     * {@code [0]}
     *
     */
    public static ImList<Integer> zeroTo(int maxIndexPlusOne)
    {
        return inclusive(0, maxIndexPlusOne - 1);
    }

    /**
     * The same as
     * {@code inclusive(1, max)}
     */
    public static ImList<Integer> oneTo(int max)
    {
        return inclusive(1, max);
    }

    /**
     * <p> A list of integers (an
     * <strong>infinite</strong>
     *  list in fact)
     *
     * {@code [start, start + step, start + 2*step, ... ]}
     *
     *
     */
    public static ImList<Integer> step(int start, int step)
    {
        return ImList.unfold(start, i -> i + step);
    }

    /**
     * <p> Do
     * {@code fn}
     * {@code count}
     *  times
     *
     */
    public static void nTimesDo(int count, FnBlock fn)
    {
        for (int i = 0; i < count; i++)
        {
            fn.doit();
        }
    }

    /**
     * <p> A {@link LeftRightBox} that has the text-box representation each element of the list, separated by
     * {@code sep}
     *
     * <p> Used for building rows of tables to represent lists.
     *
     */
    default AbstractTextBox getRow(String sep)
    {
        return LeftRightBox.withAll(getBoxes(UU_BOX_LIMIT).intersperse(LeafTextBox.with(sep)));
    }

    /**
     * <p> A {@link TopDownBox} that has the text-box representation each element of the list, separated by
     * {@code sep}
     *
     * <p> Used for building columns of tables to represent lists.
     *
     */
    default AbstractTextBox getColumn(String sep)
    {
        return TopDownBox.withAll(getBoxes(UU_BOX_LIMIT).intersperse(LeafTextBox.with(sep)));
    }

}