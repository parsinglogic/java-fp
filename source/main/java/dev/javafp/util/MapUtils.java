/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.IllegalState;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p> Utility for working with {@link Map}s.
 */

public class MapUtils
{
    /**
     * <p> The later maps in the array will overwrite any earlier ones
     */
    @SafeVarargs
    public static <K, V> Map<K, V> union(Map<K, V>... mapsToUnion)
    {
        Map<K, V> resultMap = new HashMap<K, V>();

        for (Map<K, V> m : mapsToUnion)
            resultMap.putAll(m);

        return resultMap;
    }

    public static <K, V> Map<K, V> diff(Map<K, V> one, Map<K, V> two)
    {
        Map<K, V> resultMap = new HashMap<>();
        resultMap.putAll(one);

        for (K key : two.keySet())
        {
            resultMap.remove(key);
        }

        return resultMap;
    }

    public static <K, V> Map<K, V> copy(Map<K, V> mapToCopy)
    {
        HashMap<K, V> map = new HashMap<>();
        map.putAll(mapToCopy);
        return map;
    }

    public static <K, V> Map<K, V> put(Map<K, V> map, K key, V value)
    {
        map.put(key, value);
        return map;
    }

    public static <K, V> ImMaybe<V> get(Map<K, V> map, K key)
    {
        return ImMaybe.with(map.get(key));
    }

    public static <K, V> Map<K, V> intersect(Set<K> keys, Map<K, V> map)
    {
        HashMap<K, V> results = new HashMap<>();

        for (K t : keys)
        {
            V value = map.get(t);

            if (value != null)
                results.put(t, value);
        }

        return results;
    }

    public static <K, V> Map<K, ImList<V>> getMultiMap(ImList<ImPair<K, V>> m)
    {
        Map<K, ImList<V>> map = new HashMap<>();

        for (ImPair<K, V> pair : m)
        {
            if (pair.fst == null)
                throw new IllegalState("Null keys are not allowed in maps. Value was " + pair.snd);

            ImList<V> list = map.getOrDefault(pair.fst, ImList.on());

            map.put(pair.fst, list.withHead(pair.snd));
        }

        return map;
    }

    public static <K, V> Map<K, V> getSimpleMap(ImList<ImPair<K, V>> m)
    {
        Map<K, V> map = new HashMap<>();

        for (ImPair<K, V> pair : m)
        {
            map.put(pair.fst, pair.snd);
        }

        return map;
    }

    public static <K, V> Map<K, V> createMap(K key, V value)
    {
        Map<K, V> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    public static <K, V> ImList<ImPair<K, V>> getListFrom(Map<K, V> map)
    {
        return ImList.onAll(map.entrySet()).map(e -> ImPair.on(e));
    }

    public static <K, V> ImList<ImPair<K, ImList<V>>> getAssociations(ImList<ImPair<K, V>> pairs)
    {
        return getListFrom(getMultiMap(pairs));
    }

}