/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * <p> Utility for working with {@link Set}s.
 */

public class SetUtils
{
    private static Random rand = new Random();

    @SafeVarargs
    public static <T> Set<T> union(Collection<T>... setsToAdd)
    {
        HashSet<T> result = new HashSet<T>();

        for (Collection<T> s : setsToAdd)
        {
            result.addAll(s);
        }

        return result;
    }

    @SafeVarargs
    public static <T> Set<T> newSet(T... ts)
    {
        return new HashSet<T>(Arrays.asList(ts));
    }

    public static <T> Set<T> newSet(List<T> ts)
    {
        return new HashSet<T>(ts);
    }

    public static <T> Set<T> diff(Iterable<T> main, Iterable<T> remove)
    {
        HashSet<T> results = new HashSet<T>();

        for (T t : main)
            results.add(t);

        for (T t : remove)
            results.remove(t);

        return results;
    }

    public static <T> Set<T> remove(Set<T> main, T itemToRemove)
    {
        return main.contains(itemToRemove)
               ? diff(main, newSet(itemToRemove))
               : main;
    }

    public static <T> Set<T> chooseRandom(List<T> list)
    {
        int length = rand.nextInt(list.size() + 1);
        Set<T> results = new HashSet<>();

        while (true)
        {
            if (results.size() == length)
                return results;
            else
                results.add(list.get(rand.nextInt(list.size())));
        }
    }

    public static <T> Set<T> emptySet()
    {
        return Collections.emptySet();
    }

    public static <T> Set<T> intersect(Set<T> s1, Set<T> s2)
    {
        return s1.size() <= s2.size()
               ? intersect0(s1, s2)
               : intersect0(s2, s1);
    }

    private static <T> Set<T> intersect0(Set<T> smallerSet, Set<T> largerSet)
    {
        Set<T> results = new HashSet<>();

        for (T t : smallerSet)
            if (largerSet.contains(t))
                results.add(t);

        return results;
    }

}