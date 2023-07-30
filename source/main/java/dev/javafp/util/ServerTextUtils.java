/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.lst.ImList;

import java.util.regex.Pattern;

/**
 * <p> Utility for text manipulation.
 */

public class ServerTextUtils
{

    private final static Pattern allowedIdentifierChars = Pattern.compile("[-_a-zA-Z0-9]+");
    private final static Pattern badStartOne = Pattern.compile("[0-9].*");
    private final static Pattern badStartTwo = Pattern.compile("-[0-9].*");

    private static ImList<String> names = ParseUtils.split(' ', "zero one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen");
    private static ImList<String> tensNames = ParseUtils.split(' ', "twenty thirty forty fifty sixty seventy eighty ninety");

    /**
     * <p> Split
     * {@code stringToSplit}
     *  into chunks of length
     * {@code length}
     * <p> If s1, s2, ... , sn-1, sn are the strings then s1 to sn-1 have length
     * {@code length}
     *  and sn has
     * {@code length <= length}
     * <p> {@code stringToSplit}
     *  =
     * {@code s1 + s2 + ... + sn-1 + sn}
     *
     */
    public static ImList<String> splitIntoChunks(String stringToSplit, int length)
    {
        return stringToSplit.length() <= length
               ? ImList.on(stringToSplit)
               : ImList.cons(stringToSplit.substring(0, length), splitIntoChunks(stringToSplit.substring(length), length));
    }

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

    public static String toWord(int i)
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

    public static String toWord2(int i)
    {
        switch (i)
        {
        case 0:
            return "zero";
        case 1:
            return "one";
        case 2:
            return "two";
        case 3:
            return "three";
        case 4:
            return "four";
        case 5:
            return "five";
        case 6:
            return "six";
        case 7:
            return "seven";
        case 8:
            return "eight";
        case 9:
            return "nine";
        case 10:
            return "ten";
        case 11:
            return "eleven";
        case 12:
            return "twelve";
        case 13:
            return "thirteen";
        case 14:
            return "fourteen";
        case 15:
            return "fifteen";
        case 16:
            return "sixteen";
        case 17:
            return "seventeen";
        case 18:
            return "eighteen";
        case 19:
            return "nineteen";
        case 20:
            return "twenty";

        default:
            return "unknown";
        }

    }

    public static String checkCssIdentifier(String identifierToCheck)
    {
        if (identifierToCheck.isEmpty())
            return "CSS identifier can't be the empty string";
        else if (!allowedIdentifierChars.matcher(identifierToCheck).matches())
            return "CSS identifier " + TextUtils.quote(identifierToCheck) + " contains invalid characters - each char must match [-_a-zA-Z0-9]";
        else if (badStartOne.matcher(identifierToCheck).matches())
            return "CSS identifier " + TextUtils.quote(identifierToCheck) + " starts with a digit - which is not allowed";
        else if (badStartTwo.matcher(identifierToCheck).matches())
            return "CSS identifier " + TextUtils.quote(identifierToCheck) + " starts with a hyphen followed by a digit - which is not allowed";
        else
            return null;
    }
}