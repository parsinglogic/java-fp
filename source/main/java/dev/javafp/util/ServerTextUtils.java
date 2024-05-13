/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;

import java.util.regex.Pattern;

/**
 * <p> Utility for text manipulation.
 */
public class ServerTextUtils
{

    private final static Pattern allowedIdentifierChars = Pattern.compile("[-_a-zA-Z0-9]+");
    private final static Pattern badStartOne = Pattern.compile("[0-9].*");
    private final static Pattern badStartTwo = Pattern.compile("-[0-9].*");

    private static ImList<String> names = ParseUtils.split(',', ",one,two,three,four,five,six,seven,eight,nine,ten,eleven,twelve,thirteen,fourteen,fifteen,sixteen,seventeen,eighteen,nineteen");
    private static ImList<String> tensNames = ParseUtils.split(' ', "twenty thirty forty fifty sixty seventy eighty ninety");
    private static ImList<String> placeNames = ImList.on("", "thousand", "million", "billion", "quadrillion", "quintillion", "sextillion");

    /**
     * <p> Split
     * {@code stringToSplit}
     *  into words delimited by spaces
     * <p> "   ab cde      fgh      " => [ "ab", "cde", "fgh" ]
     *
     */
    public static ImList<String> splitIntoWords(String stringToSplit)
    {
        return ImList.on(stringToSplit.split(" +"));
    }

    static long thou = 1000;

    public static String toWords(long n)
    {
        return toWords_(n).trim();
    }

    private static String toWords_(long n)
    {
        if (n == 0)
            return "zero";
        else
        {
            ImList<ImPair<Long, Long>> ps = ImList.unfold(ImPair.on(n, 0L), p -> ImPair.on(p.fst / thou, p.fst % thou)).tail();

            ImList<ImPair<Long, Long>> ps2 = ps.takeWhile(p -> !p.equals(ImPair.on(0L, 0L)));

            ImList<String> magnitudes = ps2.map(p -> componentToWords(p.snd));

            ImList<String> nearly3 = magnitudes.zipWith(placeNames, (m, p) -> m.equals("") ? "" : m + " " + p);

            // Remove empty string elements

            ImList<String> nearly = nearly3.filter(s -> !s.isEmpty());

            // Do the final and

            if (addFinalAnd(nearly, ps2.head().snd))
            {
                ImList<String> one = nearly.tail().reverse();
                return TextUtils.join(one, ", ") + " and " + nearly.head();
            }
            else
                return TextUtils.join(nearly.reverse(), ", ");
        }
    }

    public static String componentToWords(long i)
    {

        int hundreds = (int) (i / 100);
        int tensAndUnits = (int) (i % 100);

        String h = toWords(hundreds);
        String t = toWords(tensAndUnits);

        return hundreds == 0
               ? t
               : tensAndUnits > 0
                 ? h + " hundred and " + t
                 : h + " hundred";
    }

    private static boolean addFinalAnd(ImList<String> nearly, long n)
    {
        return nearly.size() > 1 && n < 100 && n > 0
               ? true
               : false;

    }

    /**
     */
    private static String toWords(int i)
    {
        if (i >= 0 && i < 100)
        {
            if (i < 20)
                return names.at(i + 1);
            {
                int tens = i / 10;
                int units = i % 10;

                String first = tensNames.at(tens - 1);

                if (units == 0)
                    return first;
                else
                    return first + " " + names.at(units + 1);
            }
        }
        else
            return "" + i;
    }

}