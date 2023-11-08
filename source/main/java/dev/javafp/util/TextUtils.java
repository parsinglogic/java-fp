/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.HasTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p> Utility for text manipulation.
 */

public class TextUtils
{

    private final static Pattern allowedIdentifierChars = Pattern.compile("[-_a-zA-Z0-9]+");
    private final static Pattern badStartOne = Pattern.compile("[0-9].*");
    private final static Pattern badStartTwo = Pattern.compile("-[0-9].*");

    private static final String DQ = "\"";

    public static String repeatString(String stringToRepeat, int repeatCount)
    {
        return stringToRepeat.repeat(repeatCount);
    }

    /**
     * <p> Convert tabs to spaces in
     * {@code s}
     *  assuming a tab size of
     * {@code tabSize}
     *
     */
    public static String detab(int tabSize, String s)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c != '\t')
            {
                sb.append(c);
            }
            else
            {
                int stop = ((tabSize + sb.length()) / tabSize) * tabSize;
                int spaces = stop - sb.length();

                for (int j = 0; j < spaces; j++)
                    sb.append(" ");
            }
        }

        return sb.toString();
    }

    public static String padToWidth(String stringToPad, int width)
    {
        int gap = width - stringToPad.length();

        // @formatter:off
        return gap == 0
                ? stringToPad
                : gap > 0
                    ? stringToPad + repeatString(" ", gap)
                    : stringToPad.substring(0, width);
        // @formatter:on
    }

    public static String abbreviate(String s, int width)
    {
        Throw.Exception.ifLessThan("width", width, 4);

        return s.length() <= width
               ? s
               : s.substring(0, width - 3) + "...";
    }

    public static String truncate(String s, int width)
    {
        Throw.Exception.ifLessThan("width", width, 0);

        return s.length() <= width
               ? s
               : s.substring(0, width);
    }

    /**
     * <p> An 'optimised' version.
     */
    public static String join(Iterable<?> thingsToJoin, String start, String separator, String end)
    {

        StringBuilder sb = new StringBuilder();
        sb.append(start);

        boolean some = false;
        for (Object t : thingsToJoin)
        {
            sb.append(t);
            sb.append(separator);
            some = true;
        }

        // Remove the last separator if necessary
        if (some)
            sb.setLength(sb.length() - separator.length());

        sb.append(end);
        return sb.toString();
    }

    /**
     * <p> Join - but if it turns out that
     * {@code thingsToJoin}
     *  is empty, return the empty string rather than start + end
     *
     */
    public static String joinMin(Iterable<?> thingsToJoin, String start, String separator, String end)
    {
        String res = join(thingsToJoin, start, separator, end);
        return res.length() == start.length() + end.length() ? "" : res;
    }

    public static String join(Iterator<?> iterator, String start, String separator, String end)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(start);

        while (iterator.hasNext())
        {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(separator);
        }

        sb.append(end);
        return sb.toString();
    }

    /**
     * <p> Join - but if it turns out that
     * {@code thingsToJoin}
     *  is empty, return the empty string rather than start + end
     *
     */
    public static String joinMin(Iterator<?> thingsToJoin, String start, String separator, String end)
    {
        String res = join(thingsToJoin, start, separator, end);
        return res.length() == start.length() + end.length() ? "" : res;
    }

    public static String join(Iterable<?> thingsToJoin, String separator)
    {
        return join(thingsToJoin, "", separator, "");
    }

    public static String join(Iterator<?> iterator, String separator)
    {
        return join(iterator, "", separator, "");
    }

    public static String quote(Object thingToQuote)
    {
        String str = (thingToQuote == null)
                     ? ""
                     : thingToQuote.toString();

        return quote(DQ, str, DQ);
    }

    public static String unquote(String stringToUnQuote)
    {
        return unquote(DQ, stringToUnQuote, DQ);
    }

    public static String unquote(String startQuote, String stringToUnQuote, String endQuote)
    {
        String s = stringToUnQuote.substring(startQuote.length());
        s = s.substring(0, s.length() - endQuote.length());

        // TODO check start and end here
        return s;
    }

    public static String quote(String startQuote, String stringToQuote, String endQuote)
    {
        return startQuote + stringToQuote + endQuote;
    }

    public static String quote(String stringToQuote, String quote)
    {
        return quote + stringToQuote + quote;
    }

    public static String join(Object[] items, String separator)
    {
        return join(Arrays.asList(items), separator);
    }

    public static String[] splitIntoChunks(int chunkSize, String stringToSplit)
    {
        int len = stringToSplit.length();

        List<String> chunks = new ArrayList<>();

        int skip = 0;
        while (skip < len)
        {
            chunks.add(stringToSplit.substring(skip, Math.min(skip + chunkSize, len)));
            skip += chunkSize;
        }

        return chunks.toArray(new String[0]);
    }

    public static char charAtFromEnd(String s, int indexFromEndStartingAtZero)
    {
        return s.charAt(s.length() - 1 - indexFromEndStartingAtZero);
    }

    public static LeftRightBox indent(int spaces, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.padToWidth("", spaces)), box);
    }

    /**
     * <p> Format the collection as a top down list with index numbers to the left of each item
     * <p> TextUtils.showCollection(ImList.on("apples", "blackberries", "cherries"))
     * <p> gives:
     * <p> 1    apples
     * <br/>
     * 2    blackberries
     * 3    cherries
     *
     */
    public static String showCollection(Collection<?> things)
    {
        return showCollection(ImList.onAll(things));
    }

    public static String showCollection(ImList<?> things)
    {
        return getBoxFrom(things).toString();
    }

    public static String showCollection(Object thing)
    {
        return getBoxFrom(thing).toString();
    }

    /**
     * <p> Get a text box from
     * {@code thing}
     * <p> It uses various tricks to do this.
     *
     * <p> If
     * {@code thing}
     *  is a
     * {@code AbstractTextBox}
     *  then it just returns
     * {@code thing}
     * <p> If
     * {@code thing}
     *  is a
     * {@code HasTextBox}
     *  then it just returns
     * {@code thing.getTextBox()}
     * <p> If
     * {@code thing}
     *  is an array or collection then it recursively calls
     * {@code getBoxFrom}
     *  on each element
     * <p> Otherwise it creates a
     * {@link LeafTextBox}
     * on the result of
     * {@code thing.toString()}
     *
     *
     */
    public static AbstractTextBox getBoxFrom(Object thing)
    {
        if (thing != null && thing.getClass().isArray())
            return getBoxFromArray(thing);
        if (thing instanceof Collection)
            return getBoxFromList(ImList.onAll((Collection<?>) thing));
        else if (thing instanceof HasTextBox)
            return ((HasTextBox) thing).getTextBox();
        else if (thing instanceof AbstractTextBox)
            return (AbstractTextBox) thing;
        else
            return LeafTextBox.with(String.valueOf(thing));
    }

    private static AbstractTextBox getBoxFromArray(Object arr)
    {
        return getBoxFromList(ImList.onPrimitiveArray(arr));
    }

    public static AbstractTextBox getBoxFromNamesAndValues(ImList<String> names, ImList<?> things)
    {
        ImList<String> ns = mergeLists(things.size(), names, ImRange.oneTo(things.size()).map(i -> "" + i));

        int maxNameLength = ns.isEmpty() ? 0 : Util.maxInt(ns.map(i -> i.length()));

        return TopDownBox.withAll(mapGetBoxFromPairOver(ns.zip(getTextBoxes(things)), maxNameLength));
    }

    private static ImList<String> mergeLists(int size, ImList<String> one, ImList<String> two)
    {
        if (size == one.size())
            return one;
        else if (size < one.size())
            return one.take(size);
        else
            return one.append(two.drop(one.size()).take(size - one.size()));
    }

    public static TopDownBox getBoxFromList(ImList<?> things)
    {

        ImList<AbstractTextBox> boxes = getTextBoxes(things);

        ImList<String> ints = ImRange.oneTo(boxes.size()).map(i -> "" + i);

        return TopDownBox.withAll(mapGetBoxFromPairOver(ints.zip(boxes), 3));
    }

    private static ImList<AbstractTextBox> mapGetBoxFromPairOver(ImList<ImPair<String, AbstractTextBox>> pairs, int maxNameLength)
    {
        if (pairs.isEmpty())
            return ImList.empty();
        else
        {

            LeftRightBox head = LeftRightBox.with(LeafTextBox.lefted("" + pairs.head().fst + ":", maxNameLength + 2), pairs.head().snd);

            return ImList.cons(head, mapGetBoxFromPairOver(pairs.tail(), maxNameLength));
        }
    }

    private static ImList<AbstractTextBox> getTextBoxes(ImList<?> things)
    {
        return things.isEmpty()
               ? ImList.<AbstractTextBox>empty()
               : ImList.cons(getBoxFrom(things.head()), getTextBoxes(things.tail()));
    }

    /**
     * <p> Print a double value using the minimum number of trailing zeros
     */
    public static String prettyPrint(double d)
    {
        return d == (long) d
               ? String.valueOf((long) d)
               : String.valueOf(d);
    }

    public static String plural(long count, String word)
    {
        return "" + count + " " + word + (count > 1
                                          ? "s"
                                          : "");
    }

    public static String trimRight(String s)
    {
        // I did have a recursive method here
        int pos = s.length();

        while (pos > 0 && s.charAt(pos - 1) == ' ')
            pos--;

        return s.substring(0, pos);
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

    public static String format(Object... xs)
    {
        return join(xs, " ");
    }

    public static String indentBy(int indent, String cs)
    {
        return indent <= 0
               ? cs
               : " ".repeat(indent) + cs;
    }

    public static String rightJustifyIn(int width, String cs)
    {
        if (width <= 0)
            return cs;
        else
        {
            int d = width - cs.length();
            return d <= 0
                   ? cs.substring(-d, width - d)
                   : indentBy(width - cs.length(), cs);
        }
    }

    public static String leftJustifyIn(int width, String cs)
    {
        if (width <= 0)
            return cs;
        else
        {
            int d = width - cs.length();
            return d <= 0
                   ? cs.substring(0, width)
                   : padToWidth(cs, width);
        }
    }

    /**
     *

     */
    public static String centreIn(int width, String cs)
    {
        int d = width - cs.length();
        if (d <= 0)
            return cs.substring(0, width);
        else
        {
            int left = (d - d % 2) >> 1;
            return padToWidth(indentBy(left, cs), width);
        }

    }

}