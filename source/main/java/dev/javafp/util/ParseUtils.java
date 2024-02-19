/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p> Utility for basic parsing of strings.
 */
public class ParseUtils
{

    private static final Pattern digitsPattern = Pattern.compile("[0-9]+");

    /**
     * <p> Skip over any characters at the start of
     * {@code stringToParse}
     *  that are == to
     * {@code charToSkipOver}
     *
     * <pre>{@code
     * ("xx", "y") == skipOverAny('x', "xxy")
     * ("", "y")   == skipOverAny('x', "y")
     * ("", "")    == skipOverAny('x', "")
     * ("", "")    == skipOverAny('x', "xxx")
     * }</pre>
     *
     */
    public static ImPair<String, String> skipOverAny(char charToSkipOver, String stringToParse)
    {
        return skipOverAny(charToSkipOver, stringToParse, 0);
    }

    private static ImPair<String, String> skipOverAny(char charToSkipOver, String stringToParse, int skipped)
    {
        return stringToParse.isEmpty() || (stringToParse.charAt(0) != charToSkipOver)
               ? ImPair.on(("" + charToSkipOver).repeat(skipped), stringToParse)
               : skipOverAny(charToSkipOver, stringToParse.substring(1), skipped + 1);
    }

    /**
     * <p> return the prefix and suffix of the first occurrence of
     * {@code charToSplitAt}
     *  in
     * {@code stringToParse}
     *  or
     * {@code null}
     *  if
     * {@code stringToParse}
     * does not contain
     * {@code charToSplitAt}
     *
     * <pre>{@code
     * ("ab", "c")     == splitAt('/', "ab/c")
     * ("abc", "")     == splitAt('/', "abc/")
     * ("", "abc")     == splitAt('/', "/abc")
     * ("abc", null)   == splitAt('/', "abc")
     * }</pre>
     *
     */
    public static ImPair<String, String> splitAt(char charToSplitAt, String stringToParse)
    {
        return splitAt(charToSplitAt, stringToParse, new StringBuilder());
    }

    private static ImPair<String, String> splitAt(char charToSplitAt, String stringToParse, StringBuilder firstString)
    {
        if (stringToParse.isEmpty())
            return ImPair.on(firstString.toString(), null);

        if (stringToParse.charAt(0) == charToSplitAt)
            return ImPair.on(firstString.toString(), stringToParse.substring(1));

        firstString.append(stringToParse.charAt(0));
        return splitAt(charToSplitAt, stringToParse.substring(1), firstString);
    }

    /**
     * <p> Return a ImPair containing the prefix and suffix of the first occurrence of
     * {@code separator}
     *  in
     * {@code stringToParse}
     *  or a ImPair with
     * {@code null}
     * ,
     * {@code stringToParse}
     *  if
     * {@code stringToParse}
     * does not contain
     * {@code separator}
     *
     * <pre>{@code
     * ("ab", "c")     == splitAt('/', "ab/c")
     * ("abc", "")     == splitAt('/', "abc/")
     * ("", "abc")     == splitAt('/', "/abc")
     * (null, "abc")   == splitAt('/', "abc")
     * (null, "" )     == splitAt('/', "")
     * }</pre>
     *
     */
    public static ImPair<String, String> splitAt(String separator, String stringToParse)
    {
        int i = stringToParse.indexOf(separator);

        return i == -1
               ? ImPair.on(null, stringToParse)
               : ImPair.on(stringToParse.substring(0, i), stringToParse.substring(i + separator.length()));
    }

    /**
     * <p> Assume there is a vnf representation of a string at the start of
     * {@code stringToParse}
     *  and return the
     * string and the rest of
     * {@code stringToParse}
     *
     * <pre>{@code
     * ("ab", "") == getStringFromVnf( "2 ab")
     * ("abc", "def") == getStringFromVnf('/', "3 abcdef")
     * }</pre>
     *
     */
    public static ImPair<String, String> getStringFromVnf(String stringToParse)
    {
        ImPair<String, String> p = splitAt(' ', stringToParse);

        int count = Integer.parseInt(p.fst);

        return ImPair.on(p.snd.substring(0, count), p.snd.substring(count));
    }

    public static String getStringMatching(Pattern pattern, String stringToParse)
    {
        Matcher matcher = pattern.matcher(stringToParse);

        return matcher.find()
               ? stringToParse.substring(matcher.start(), matcher.end())
               : "";
    }

    public static String getRest(String prefix, String stringToLookAt)
    {
        return prefix.length() >= stringToLookAt.length()
               ? ""
               : stringToLookAt.substring(prefix.length());
    }

    public static String getFieldAtIndex(String line, char sep, int indexStartingAtOneOrMinusOne)
    {

        Throw.Exception.ifTrue(indexStartingAtOneOrMinusOne == 0, "indexStartingAtOneOrMinusOne must not be zero");
        ImList<String> ss = split(sep, line);

        int iStartingAtOne = indexStartingAtOneOrMinusOne < 1
                             ? ss.size() + 1 + indexStartingAtOneOrMinusOne
                             : indexStartingAtOneOrMinusOne;

        return (1 <= iStartingAtOne) && (iStartingAtOne <= ss.size())
               ? ss.at(iStartingAtOne)
               : "";
    }

    public static String removeFieldAtIndex(String line, char sep, int indexStartingAtOne)
    {
        Throw.Exception.ifTrue(indexStartingAtOne == 0, "indexStartingAtOne must not be zero");
        ImList<String> ss = split(sep, line);

        int iStartingAtOne = indexStartingAtOne < 1
                             ? ss.size() + 1 + indexStartingAtOne
                             : indexStartingAtOne;

        return TextUtils.join(ss.removeAt(iStartingAtOne), "" + sep);
    }

    /**
     * <p> Splits
     * {@code s}
     *  at the separator
     * {@code sep}
     * , giving an
     * {@link ImList }
     * ,
     * {@code ls}
     * such that
     * <p> TextUtils.join(ls, sep) == s
     * <p> (or
     * {@code []}
     * - if
     * {@code s}
     *  is
     * {@code null}
     * )
     * <pre>{@code
     * split('/', null)   => [ ]
     * split('/', "a")    => ["a"]
     * split('/', "")     => [""]
     * split('/', /a//")  => ["", "a", "", ""]
     * }</pre>
     *
     */
    public static ImList<String> split2(char sep, String s)
    {
        if (s == null)
            return ImList.on();
        else
        {
            ImPair<String, String> p = ParseUtils.splitAt(sep, s);

            return ImList.cons(p.fst, split2(sep, p.snd));
        }
    }

    /**
     * <p> Splits
     * {@code s}
     *  at the separator
     * {@code sep}
     * , giving an
     * {@link ImList }
     * ,
     * {@code ls}
     * such that
     * <p> TextUtils.join(ls, sep) == s
     * <p> (or
     * {@code []}
     * - if
     * {@code s}
     *  is
     * {@code null}
     * )
     * <pre>{@code
     * split('/', null)   => [ ]
     * split('/', "a")    => ["a"]
     * split('/', "")     => [""]
     * split('/', "/a//")  => ["", "a", "", ""]
     * }</pre>
     *
     * <p> This function is lazy.
     */
    public static ImList<String> split(char sep, String s)
    {
        if (s == null)
            return ImList.on();
        else
        {
            // Get a list of pairs of strings.
            // Before the tail():
            // "a/b//c" -> [("", "a/b//c"), ("a", "b//c"), ("b", "/c"), ("", "c"), ("c", null)]
            //
            // After the tail():
            // "a/b//c" -> [("a", "b//c"), ("b", "/c"), ("", "c"), ("c", null)]
            ImList<ImPair<String, String>> ps = ImList.unfold(Pai.r("", s), p -> ParseUtils.splitAt(sep, p.snd)).tail();

            //            say(ps.take(4));

            // Take until we find the pair with null as its 2nd element and use the first element of each pair
            return ps.takeUntil(p -> p.snd == null).map(p -> p.fst);
        }
    }

    public static boolean isDigits(String p)
    {
        return digitsPattern.matcher(p).matches();
    }

}