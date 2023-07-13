/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.lst.ImList;

/**
 * <p> In mathematics, a subsequence is a sequence that can be derived from another sequence by deleting some elements
 * without changing the order of the remaining elements. For example, the sequence
 * <p> [A,B,D]
 * <p> is a subsequence of
 * <p> [A,B,C,D,E,F]
 * <p> Not to be confused with sublist of course.
 * <p> Mixer is class to mix two lists a and b
 * <p> mix a b generates a list of lists l where each element, e of l
 * <ol>
 * <li>
 * <p> is not equal to any other element of l
 * </li>
 * <li>
 * <p> is such that a and b are both subsequences of e
 * </li>
 * </ol>
 * <p> For example:
 * <p> Mixer.mix(ImList.on(1, 2), ImList.on(3, 4))
 * <p> generates
 * <p> [[1, 2, 3, 4], [1, 3, 2, 4], [1, 3, 4, 2], [3, 1, 2, 4], [3, 1, 4, 2], [3, 4, 1, 2]]
 *
 */

public class Mixer
{
    /**
     * <p> I think this is the Haskell...
     * <p> mix :: [a] -> [a] -> [a]
     * mix [] b = [b]
     * mix a [] = [a]
     * mix (a:as) (b:bs) = (map (a:) mix as (b:bs)) ++ (map (b:) mix (a:as) bs))
     *
     */
    public static <T> ImList<ImList<T>> mix(ImList<T> one, ImList<T> two)
    {
        if (one.isEmpty())
            return ImList.on(two);

        if (two.isEmpty())
            return ImList.on(one);

        return mapConsOver(one.head(), mix(one.tail(), two)).append(mapConsOver(two.head(), mix(one, two.tail())));
    }

    @SafeVarargs
    public static <T> ImList<ImList<T>> mixAll(ImList<T>... listsArray)
    {
        return mixAll(ImList.on(listsArray));
    }

    public static <T> ImList<ImList<T>> mixAll(ImList<ImList<T>> lists)
    {
        if (lists.isEmpty() || lists.size() == 1)
            return lists;
        else if (lists.size() == 2)
            return mix(lists.head(), lists.tail().head());
        else
            return flatMapMixOver(lists.head(), mixAll(lists.tail()));

    }

    // TODO - simplify to use map - Van jul-2015
    private static <T> ImList<ImList<T>> mapConsOver(T head, ImList<ImList<T>> lists)
    {
        return lists.isEmpty()
               ? lists
               : ImList.cons(lists.head().withHead(head), mapConsOver(head, lists.tail()));
    }

    public static <T> ImList<ImList<T>> flatMapMixOver(ImList<T> list, ImList<ImList<T>> lists)
    {
        return lists.isEmpty()
               ? lists
               : mix(list, lists.head()).append(flatMapMixOver(list, lists.tail()));
    }

}