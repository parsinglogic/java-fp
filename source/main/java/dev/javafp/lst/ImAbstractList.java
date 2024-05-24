package dev.javafp.lst;

import dev.javafp.util.Hash;

abstract class ImAbstractList<A> implements ImList<A>
{

    protected int cachedHashCode = 0;

    /**
     * The (cached) hashcode for this object.
     */
    @Override
    public int hashCode()
    {
        return cachedHashCode == 0
               ? cachedHashCode = computeHash()
               : cachedHashCode;
    }

    protected int computeHash()
    {
        return Hash.sizeMultiplier * this.size() + Hash.hashCodeOfIterableWithFirstElements(Hash.sampleSize, this);
    }

    /**
     * <p> When serialising, we turn every ImList into a ImListOnArray
     */
    protected Object writeReplace()
    {
        return ImList.on(toArray(Object.class));
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return toS();
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     * equals
     * {@code other}
     *
     * <p> Equality for lists means that both lists have the same size and the
     * {@code i}
     * th element of
     * {@code this}
     *  equals the
     * {@code i}
     * th element of
     * {@code other}
     *
     */
    @Override
    public boolean equals(Object other)
    {
        return other instanceof ImList
               ? equalsList((ImList<A>) other)
               : false;
    }

}
