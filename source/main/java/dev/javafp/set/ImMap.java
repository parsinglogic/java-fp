/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.HasTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.eq.Eq;
import dev.javafp.eq.Equals;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet.Replace;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import dev.javafp.util.Hash;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.NullCheck;
import dev.javafp.util.TextUtils;
import dev.javafp.util.Util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p> An immutable version of
 * {@code java.util.Map}
 * .
 * <h2>Introduction</h2>
 * </p>
 * <p> A collection of (key-value pairs) and the property that finding a value given a key is
 * <p> As with
 * {@code java.util.Map}
 * , the fundamental methods are {@link #get}, {@link #put} and {@link #remove} although
 * {@code put()}
 *  and
 * {@code remove}
 *  have a different meaning since
 * {@code ImMaps}
 *  are immutable.
 * <p> Instead of modifying the map in place, {@link #put} creates a new map containing the new
 * key value pair and {@link #remove} creates a new map not containing it.
 * <h3>Examples:</h3>
 * <p> Create an empty map:
 *
 * <pre>{@code
 * ImMap<String, String> mEmpty = new ImMap<String, String>();
 * mEmpty.isEmpty()  =>  true
 * }</pre>
 * <p> Put an entry:
 *
 * <pre>{@code
 * ImMap<String, String> mOne = mEmpty.put("a", "Aardvark");
 * }</pre>
 * <p> The new map will have the entry
 *
 * <pre>{@code
 * mOne.size()    =>  1
 * mOne.get("a")  =>  "Aardvark"
 * }</pre>
 * <p> But the old one will be unchanged:
 *
 * <pre>{@code
 * mEmpty.isEmpty()  => true
 * }</pre>
 * <p> Put another entry
 *
 * <pre>{@code
 * ImMap<String, String> mTwo = mOne.put("b", "Bear");
 * }</pre>
 * <p> The new map,
 * {@code mTwo}
 *  has two entries:
 *
 * <pre>{@code
 * mTwo.get("a")  => "Aardvark"
 * mTwo.get("b")  => "Bear"
 * }</pre>
 * <p> And, of course,
 * {@code mOne}
 *  has not changed:
 *
 * <pre>{@code
 * mOne.size()    =>  1
 * mOne.get("a")  =>  "Aardvark"
 * mOne.get("b")  =>  null
 * }</pre>
 * <p> You can remove entries:
 *
 * <pre>{@code
 * ImMap<String, String> mThree = mTwo.remove("a");
 * mThree.get("a")  => null
 * }</pre>
 * <p> If you remove them when they weren't there:
 *
 * <pre>{@code
 * ImMap<String, String> mFive = mTwo.remove("z");
 * }</pre>
 * <p> The new map is the same as the old one:
 *
 * <pre>{@code
 * mFive == mTwo  =>  true
 * }</pre>
 * <p> You can replace entries:
 *
 *
 * <pre>{@code
 * ImMap<String, String> mSix = mTwo.put("b", "Bear");
 * mSix == mTwo  =>  true
 * }</pre>
 * <p> If you replace entries with
 * <em>equal</em>
 *  but non identical entries then you get a new map:
 *
 * <pre>{@code
 *  ImMap<String, String> mSeven = mTwo.put("b", "Bear".substring(1));
 *  mSeven == mTwo  =>  false
 * }</pre>
 *
 */
public class ImMap<K, V> implements Iterable<ImPair<K, V>>, Serializable, HasTextBox
{

    private int cachedHashCode = 0;

    final protected ImSet<Entry<K, V>> entrySet;

    /**
     * <p> The singleton empty map
     */
    final private static ImMap<?, ?> empty = new ImMap();

    /**
     * <p> The (singleton) empty map.
     */
    @SuppressWarnings("unchecked")
    public static <T, U> ImMap<T, U> empty()
    {
        return (ImMap<T, U>) empty;
    }

    /**
     * <p> An
     * {@code ImMap}
     *  entry (key-value pair).
     *
     * <p> Note that
     * {@code Entry}
     *  is *very peculiar *in that its
     * {@code equals}
     *  and
     * {@code hashCode}
     *  functions only consider the
     * {@code key}
     *  - not the
     * {@code value}
     * <p> This is what allows us to have a set of
     * {@code Entry}
     *  objects and find the entry given just the
     * {@code key}
     * <p> It does mean that we have to do some more work when calculating
     * {@code hashCode()}
     *  for the
     * {@code ImMap}
     *
     *
     */
    private static class Entry<KEY, VALUE> implements Serializable, HasTextBox
    {
        public final KEY key;
        public final VALUE value;

        private Entry(KEY key, VALUE value)
        {
            this.key = key;
            this.value = value;
        }

        /**
         * <p> {@code true}
         *  if
         * {@code other}
         *  is an instance of
         * {@code ImMap.Entry}
         *  and
         * {@code this.key.equals(other.key)}
         * .
         * <p> This implementation is designed to allow
         * {@code ImMap}
         *  to be implemented as an
         * {@code ImSet<Entry>}
         * .
         *
         */
        @Override
        public boolean equals(Object other)
        {
            return other instanceof Entry
                   ? key.equals(((Entry<?, ?>) other).key)
                   : false;
        }

        /**
         * <p> The hash code value for
         * {@code this}
         * .
         * <p> The algorithm is simply to return the hashCode of the key.
         *
         */
        @Override
        public int hashCode()
        {
            return key.hashCode();
        }

        public Integer hashCodeIncludingValue()
        {
            return ImList.on(key, value).hashCode();
        }

        /**
         * <p> The key of this key-value pair.
         */
        public KEY getKey()
        {
            return key;
        }

        /**
         * <p> The value of this key-value pair.
         * <p> Note that it cannot be
         * {@code null}
         * .
         *
         */
        public VALUE getValue()
        {
            return value;
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
        public AbstractTextBox getTextBox()
        {
            return TextUtils.getBoxFrom(key).before(LeafTextBox.with("->")).before(TextUtils.getBoxFrom(value));
        }

        /**
         * A String representation of this object
         */
        @Override
        public String toString()
        {
            return "" + key + " -> " + value;
        }
    }

    /**
     * <p> The empty map.
     */
    public ImMap()
    {
        entrySet = ImSet.empty();
    }

    private ImMap(ImSet<Entry<K, V>> entrySet)
    {
        this.entrySet = entrySet;
    }

    public ImList<ImPair<K, V>> keyValuePairs()
    {
        return entrySet.toList().map(e -> ImPair.on(e.key, e.value));
    }

    /**
     * <p> The map where each entry at key
     * {@code k}
     * is created by applying
     * {@code fn}
     *  to the value at
     * {@code k}
     *  in
     * {@code this}
     * .
     *
     */
    public <V2> ImMap<K, V2> map(Fn<V, V2> fn)
    {
        return ImMap.onSet(entrySet.map(e -> new Entry<>(e.key, fn.of(e.value))));
    }

    /**
     * <p> A map containing a single entry -
     * {@code key}
     *  pointing to
     * {@code value}
     * .
     *
     */
    public static <KEY, VALUE> ImMap<KEY, VALUE> on(KEY key, VALUE value)
    {
        return ImMap.<KEY, VALUE>empty().put(key, value);
    }

    private static <K, V> ImMap<K, V> onSet(ImSet<Entry<K, V>> set)
    {
        return set.isEmpty()
               ? empty()
               : new ImMap<>(set);
    }

    /**
     *
     * <p> A map where, for all values,
     * {@code v}
     * , in
     * {@code values}
     * , each key is obtained by applying
     * {@code getKeyFn}
     *  to
     * {@code v}
     *  and each value is
     * {@code v}
     * .
     * <p> In other words:
     *
     *
     * <p> A map,
     * {@code m}
     * , from
     * {@code A -> ImList<B>}
     *  where
     *
     * <pre>{@code
     * foreach v in values, m.get(getKeyFn.of(v)) == v
     * }</pre>
     *
     */
    public static <A, B> ImMap<A, B> from(ImList<B> values, Fn<B, A> getKeyFn)
    {
        ImList<Entry<A, B>> es = values.map(v -> new Entry<>(getKeyFn.of(v), v));

        return es.foldl(empty(), (entries, newEntry) -> entries.putEntry(newEntry));
    }

    /**
     * <p> A map where, for all pairs
     * {@code (k, v)}
     * in
     * {@code pairs}
     *  each key is
     * {@code k}
     *  and each value is
     * {@code v}
     */
    public static <A, B> ImMap<A, B> fromPairs(ImList<ImPair<A, B>> pairs)
    {
        return pairs.foldl(empty(), (m, p) -> m.put(p.fst, p.snd));
    }

    /**
     * <p> A map, m, from
     * {@code A -> ImList<B>}
     *  where
     *
     * <pre>{@code
     * foreach v in values, m.get(getKeyFn.of(v)).contains(v)
     * }</pre>
     * <p> We preserve the order of the values in the lists of values in the map
     *
     */
    public static <A, B> ImMap<A, ImList<B>> fromMulti(ImList<B> values, Fn<B, A> getKeyFn)
    {
        return values.foldr((v, map) -> map.updateValue(getKeyFn.of(v), ImList.on(), l -> l.push(v)), empty());
    }

    /**
     * <p> Create a map of keys to lists of values from a list of pairs.
     * <p> We preserve the order of the values in the pairs in the list values in the map
     *
     * <pre>{@code
     * [a, 3] [a, 1] [a, 2]
     * }</pre>
     * <p> will result in:
     *
     * <pre>{@code
     * a -> [3, 1, 2]
     * }</pre>
     *
     */
    public static <A, B> ImMap<A, ImList<B>> fromMulti(ImList<ImPair<A, B>> pairs)
    {
        return pairs.foldr((p, m) -> m.updateValue(p.fst, ImList.on(), l -> l.push(p.snd)), empty());
    }

    /**
     * <p> Create a map from a standard map
     */
    public static <A, B> ImMap<A, B> fromMap(Map<A, B> sourceMap)
    {
        return fromMapEntries(sourceMap.entrySet());
    }

    private static <A, B> ImMap<A, B> fromMapEntries(Set<Map.Entry<A, B>> entries)
    {
        ImMap<A, B> result = empty();

        for (Map.Entry<A, B> entry : entries)
        {
            result = result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * The value at key
     * {@code key}
     * or
     * {@code def}
     * if no such key exists
     */
    public V getOrDefault(K key, V def)
    {
        V v = get(key);

        return v == null
               ? def
               : v;
    }

    /**
     * The {@link Map}
     * that contains the same keys and values as
     * {@code this}
     */
    public Map<K, V> toMap()
    {
        HashMap<K, V> result = new HashMap<>();
        for (Entry<K, V> entry : entrySet)
        {
            result.put(entry.key, entry.value);
        }

        return result;
    }

    /**
     * <p> The map with the value at
     * {@code key}
     *  transformed by
     * {@code transform}
     * . If there is no such value, use
     * {@code defaultValue}
     *  as the argument to
     * {@code transform}
     * and put it into the map using the key supplied
     *
     */
    public ImMap<K, V> updateValue(K key, V defaultValue, Fn<V, V> transfom)
    {
        return put(key, transfom.of(getOrDefault(key, defaultValue)));
    }

    /**
     * <p> The map with the same key-value pairs as
     * {@code this}
     *  with the (possible) difference
     * that the key
     * {@code key}
     *  is now mapped to
     * {@code value}
     *
     */
    public ImMap<K, V> put(K key, V value)
    {
        NullCheck.check(key);
        NullCheck.check(value);

        return putEntry(new Entry<K, V>(key, value));
    }

    private ImMap<K, V> putEntry(Entry<K, V> newEntry)
    {
        return ImMap.onSet(entrySet.add(newEntry, Replace.yes));
    }

    /**
     * <p> The value that
     * {@code key}
     *  maps to (or
     * {@code null}
     *  if no such mapping exists).
     *
     */
    public V get(K key)
    {
        return entrySet.find(getEntry(key)).ifPresentElse(i -> i.value, null);
    }

    /**
     * <p> The value that
     * {@code key}
     *  maps to
     *
     */
    public ImMaybe<V> getMaybe(K key)
    {
        return entrySet.find(getEntry(key)).map(i -> i.value);
    }

    /**
     * <p> The map with the same key-value pairs as
     * {@code this}
     *  with the (possible) difference
     * that the key
     * {@code key}
     *  is now no longer mapped to
     * {@code value}
     * .
     *
     */
    public ImMap<K, V> remove(K key)
    {
        ImSet<Entry<K, V>> newSet = entrySet.remove(getEntry(key));

        return newSet == entrySet
               ? this
               : onSet(newSet);
    }

    private Entry<K, V> getEntry(K key)
    {
        return new Entry<K, V>(key, null);
    }

    /**
     * <p> The map with
     * {@code keys}
     *  removed.
     *
     */
    public ImMap<K, V> removeAll(Iterable<K> keysToRemove)
    {
        return Util.foldl(keysToRemove, this, (z, i) -> z.remove(i));
    }

    /**
     * <p> The number of key-value entries in
     * {@code this}
     * .
     *
     */
    public int size()
    {
        return entrySet.size();
    }

    /**
     * <p> {@code true}
     *  if
     * {@code this}
     *  contains no entries.
     *
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * <p> {@code true}
     *  if
     * {@code other}
     *  is an instance of
     * {@code ImMap}
     *  and
     * {@code other}
     *  "contains the same entries" as
     * {@code this}
     * .
     * <p> More formally:
     *
     * <pre>{@code
     *  other.size() == this.size()
     * }</pre>
     * <p> and for all
     * {@code k}
     *  and
     * {@code v}
     *  such that
     *
     * <pre>{@code
     *  v = this.get(k)
     * }</pre>
     * <p> then for all
     * {@code j}
     *  such that
     * {@code j}
     * <em>equals</em>
     * {@code k}
     * :
     *
     * <pre>{@code
     *  other.get(j)
     * }</pre>
     * <p> <em>equals</em>
     *  v.
     * <p> As usual, a
     * <em>equals</em>
     *  b means:
     *
     * <pre>{@code
     *  a.equals(b) == true
     * }</pre>
     *
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;

        if (other instanceof ImMap)
        {
            ImMap otherMap = (ImMap) other;
            return size() == otherMap.size() && hasEqualEntries(otherMap);
        }

        return false;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean hasEqualEntries(ImMap<K, V> other)
    {
        return Equals.isEqualIteratorsUsing(this.entrySet.iterator(), other.entrySet.iterator(), (a, b) -> equalsIncludingValue(a, b));
    }

    private Boolean equalsIncludingValue(Entry<K, V> a, Entry<K, V> b)
    {
        return Eq.uals(a.key, b.key) && Eq.uals(a.value, b.value);
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
    public AbstractTextBox getTextBox()
    {
        return entrySet.getTextBox();
    }

    /**
     * The keys in this map as a list.
     */
    public ImList<K> keys()
    {
        return ImList.onAll(entrySet).map(e -> e.key);
    }

    /**
     * The keys in this map as a set
     */
    public ImSet<K> keysSet()
    {
        return entrySet.map(e -> e.key);
    }

    /**
     * The values in this map as a list.
     */
    public ImList<V> values()
    {
        return ImList.onAll(entrySet).map(e -> e.value);
    }

    /**
     * <p> The key/value pairs in this map.
     */
    public ImList<ImPair<K, V>> pairs()
    {
        return ImList.onAll(entrySet).map(e -> Pai.r(e.key, e.value));
    }

    /**
     * A String representation of this object
     */
    @Override
    public String toString()
    {
        return entrySet.toString();
    }

    @Override
    public int hashCode()
    {
        return cachedHashCode == 0
               ? cachedHashCode = computeHash()
               : cachedHashCode;
    }

    /**
     * We use the hashcode of the entry set - kinda.
     *
     * We actually get the hash of the entry key and the value - the entry normally only considers
     * the key for its hash and equality
     */
    protected int computeHash()
    {
        return Hash.hashCodeOfIterableUsing(entrySet, e -> e.hashCodeIncludingValue());
    }

    /**
     * <p> An iterator on the key-value pairs in
     * {@code this}
     * .
     *
     */
    public Iterator<ImPair<K, V>> iterator()
    {
        return keyValuePairs().iterator();
    }

}