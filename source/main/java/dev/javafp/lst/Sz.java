/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import static dev.javafp.lst.ImLazyList.KNOWN_INFINITE;
import static dev.javafp.lst.ImLazyList.UNKNOWN_FINITE;
import static dev.javafp.lst.ImLazyList.UNKNOWN_UNKNOWN;

/**
 * <p> The size of a list - deals with infinite sizes.
 *
 * <p> I have been trying to fix a 'problem' with lists
 * <p> I don't now think that this is the best solution to the problem but I am leaving it
 * in for now
 * <p> I want to have lazy lists
 * <p> I want to be able to have infinite lists and to be able to get the size of finite lists efficiently.
 * <p> If we can calculate the size as we create the list then that seems to be the easiest and most
 * efficient way.
 * <p> To call size on an infinite list and have it throw an exception instead of looping.
 * <p> I introduced the idea of four states for the size
 *
 * <pre>{@code
 * KI known infinite   - repeat and unfold are these
 * KF known finite     - onArray, onList
 * UF unknown finite   - take on a UU, filter on a KF
 * UU unknown unknown  - filter on a KI
 * }</pre>
 * <p> known finite means that we know what the size of the list is when we create it.
 * unknown finite means that we know the list is finite but we dont know what its size is until we
 * have run all code
 * <p> The idea is that if we append two lists awe can work out what the size type is by combining the
 * size types of the two lists it is referring to
 * <p> So let's look at the size types of all the lists:
 * <p> lists that don't use another ImList:
 *
 * <pre>{@code
 * unfold       KI
 * repeat       KI
 * empty        KF
 * onArray      KF
 * onList       KF
 * ImIteratorList UU (It might get the iterator from another ImList)
 * }</pre>
 * <p> Really we should have the ability to tell what sort of iterator we are on
 * <p> We could classify them using the scheme
 *
 * <pre>{@code
 *                 KF | KI | UF | UU
 *
 * filter          UF | UU | UF | UU
 * takeWhile       UF | UU | UF | UU
 * take            UF | KF | UF | UF
 *
 * dropWhile       KF | KI | UF | UU
 * drop            KF | KI | UF | UU
 * map             KF | KI | UF | UU
 * ip              KF | KI | UF | UU
 * cons            KF | KI | UF | UU
 * tails           KF | KI | UF | UU
 * scanl           KF | KI | UF | UU
 * }</pre>
 * <p> join, append
 *
 * <pre>{@code
 *        | KF | KI | UF | UU |
 *
 *     KF | KF | KI | UF | UU |
 *
 *     KI |    | KI | KI | KI |
 *
 *     UF |    |    | UF | UU |
 *
 *     UU |    |    |    | UU |
 * }</pre>
 * <p> zipWith
 * | KF | KI | UF | UU |
 *
 * <pre>{@code
 *     KF | KF | KF | UF | KF |
 *
 *     KI |    | KI | UF | UU |
 *
 *     UF |    |    | UF | UF |
 *
 *     UU |    |    |    | UU |
 * }</pre>
 * <p> I kinda failed because I can't prevent size from looping.
 * It can loop on these bad boys:
 *
 * <pre>{@code
 * filter
 * takeWhile
 * ImIteratorList
 * }</pre>
 * <p> (but not dropWhile since it will have looped when it was called there is no separate list class for this)
 * <p> This means that any lists that are based on these lists will potentially loop when size() is
 * called on them too.
 * <p> I can throw an execption for lots of cases when things have infinite size - but not all cases - which is a pity
 * <p> I have made some functions non recursive.
 *
 * <pre>{@code
 * size
 * foldl
 * drop
 * take
 * }</pre>
 * <p> My experiments - see testStack in ImList - show that we need
 *
 * <pre>{@code
 * -Xss1024k
 * }</pre>
 * <p> to allow recursion to a depth of about 30 million
 * <p> If you run LstTest::testPermutationsLarge
 * <p> You will see that I can lazily create a large list and get its size using no recursion (or very little)
 * <h2>Lazy lists</h2>
 * <p> These also reduce recursion in that they act as trampolines
 * <p> take would normallly recurse as many times as the number of elements taken
 * <p> When it is a list we don't recurse because we return a new list when tail is called.
 * <p> This happens automatically in Haskell with thunks.
 * <p> ImMaybe lazy lists are poor mans thunks...
 * <h2>New Idea</h2>
 * <p> Don't have infinite lists.
 * <p> We would still have size types of KF and UF but we could then arrange it so that all functions would terminate.
 *
 */
public class Sz
{

    public static int addOne(int sz)
    {
        if (sz >= 0)
        {
            return sz + 1;
        }
        else
            return sz;
    }

    public static int take(int szNotZero, int countStartingAtOne)
    {
        if (szNotZero >= 1)
        {
            return Math.min(countStartingAtOne, szNotZero);
        }
        else if (szNotZero == UNKNOWN_UNKNOWN)
        {
            return UNKNOWN_FINITE;
        }
        else if (szNotZero == KNOWN_INFINITE)
        {
            return countStartingAtOne;
        }
        else
            return szNotZero;
    }

    public static int zipWith(int szOne, int szTwo)
    {
        if (szOne >= 0 && szTwo >= 0)
            return Math.min(szOne, szTwo);
        else if (szOne == KNOWN_INFINITE && szTwo == KNOWN_INFINITE)
            return KNOWN_INFINITE;
        else if (szOne == UNKNOWN_FINITE || szTwo == UNKNOWN_FINITE)
            return UNKNOWN_FINITE;
        else if (szOne >= 0 || szTwo == KNOWN_INFINITE)
            return szOne;
        else if (szTwo >= 0 || szOne == KNOWN_INFINITE)
            return szTwo;
        else
            // if (szOne == UNKNOWN_UNKNOWN || szTwo == UNKNOWN_UNKNOWN)
            return UNKNOWN_UNKNOWN;
    }

    public static int append(int szOne, int szTwo)
    {
        int s = Math.min(szOne, szTwo);

        return s < 0
               ? s
               : szOne + szTwo;
    }

    public static int filter(int sz)
    {
        if (sz >= 0)
            return UNKNOWN_FINITE;
        else if (sz == KNOWN_INFINITE)
            return UNKNOWN_UNKNOWN;
        else
            return sz;
    }

    public static <A> int join(ImList<ImList<A>> l)
    {
        return UNKNOWN_UNKNOWN;
    }

    public static <A> int doAdds(int sz, ImList<ImList<A>> tl)
    {

        for (ImList<A> l : tl)
        {
            sz = append(sz, l.getSz());

            if (sz < 0)
                return sz;
        }

        return sz;

    }

    public static int takeWhile(int sz)
    {
        return sz >= 0
               ? UNKNOWN_FINITE
               : sz == KNOWN_INFINITE
                 ? UNKNOWN_UNKNOWN
                 : sz;
    }

    public static int dropWhile(int sz)
    {
        return sz >= 0
               ? UNKNOWN_FINITE
               : sz;
    }
}