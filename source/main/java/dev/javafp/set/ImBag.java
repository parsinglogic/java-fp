package dev.javafp.set;

import dev.javafp.lst.ImList;
import dev.javafp.util.Util;
import dev.javafp.val.ImValuesImpl;

import java.util.Iterator;

/**
 * A Set like collection that records how many times each object appears.
 *
 */
public class ImBag<A> extends ImValuesImpl
{

    private static ImBag<?> empty = new ImBag(ImMap.empty());

    // The elements mapped to the number of times each element appears
    private final ImMap<A, Integer> counts;

    private ImBag(ImMap<A, Integer> counts)
    {
        this.counts = counts;
    }

    /**
     * <p> The bag whose elements are obtained from the list
     * {@code xs}
     * .
     */
    public static <T> ImBag<T> onAll(ImList<T> xs)
    {
        return ImBag.onIterator(xs.iterator());
    }

    /**
     * <p> The bag whose elements are obtained by iterating over
     * {@code iterator}
     * .
     *
     */
    public static <T> ImBag<T> onIterator(Iterator<T> iterator)
    {

        ImBag<T> s = ImBag.empty();

        while (iterator.hasNext())
            s = s.add(iterator.next());

        return s;
    }

    /**
     * <p> Add
     * {@code elementToAdd}
     *  to this.
     *
     * <p> If an
     * <em>equal</em>
     *  element is already in this bag then its count will be incremented - otherwise
     *  its count will be set to
     * {@code count}
     * .
     *
     */
    public ImBag<A> add(A elementToAdd)
    {
        return add(elementToAdd, 1);
    }

    /**
     * <p> Add
     * {@code elementToAdd}
     * {@code count}
     *  times
     *  to this.
     *
     * <p> If an
     * <em>equal</em>
     *  element is already in this bag then its count will be incremented by
     * {@code count}
     *  - otherwise
     *  its count will be set to
     * {@code count}
     * .
     */
    public ImBag<A> add(A elementToAdd, int count)
    {
        return new ImBag<A>(counts.put(elementToAdd, counts.getOrDefault(elementToAdd, 0) + 1));
    }

    /**
     * <p> The (singleton) empty bag.
     */
    public static <T> ImBag<T> empty()
    {
        return (ImBag<T>) empty;
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(counts);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("counts");
    }

    /**
     * {@code true}
     * iff
     * {@code this}
     * is the empty bag
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * <p> The number of elements in
     * {@code this}
     * .
     */
    public int size()
    {
        return Util.sumInt(counts.values());
    }
}
