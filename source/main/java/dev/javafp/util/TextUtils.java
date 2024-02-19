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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * <p> A class containing static functions for text manipulation.
 */
public class TextUtils
{

    private static final String DQ = "\"";

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

    /**
     * <p> Right-pad
     * {@code stringToPad}
     *  with spaces to make its length
     * {@code width}
     *  or take the first
     * {@code width}
     * characters of
     * {@code stringToPad}
     * or leave it alone - depending on the size of
     * {@code stringToPad}
     *
     */
    public static String padOrTrimToWidth(String stringToPad, int width)
    {
        int gap = width - stringToPad.length();

        return gap == 0
               ? stringToPad
               : gap > 0
                 ? stringToPad + " ".repeat(gap)
                 : stringToPad.substring(0, width);

    }

    /**
     * <p> if
     * {@code s.length() > width}
     *  then
     * right-trim
     * {@code s}
     *  to
     * {@code width - 3}
     *  characters and add
     * {@code "..."}
     * otherwise
     * {@code s}
     * <p> If
     * {@code width < 4}
     *  an exception is thrown
     * <p> Examples
     *
     * <pre>{@code
     * abbreviate("Pooling", 6) == "Poo..."
     * abbreviate("abracadabra", 5) == "ab..."
     * }</pre>
     *
     */
    public static String abbreviate(String s, int width)
    {
        Throw.Exception.ifLessThan("width", width, 4);

        return s.length() <= width
               ? s
               : s.substring(0, width - 3) + "...";
    }

    /**
     * <p> if
     * {@code s.length() > width}
     *  then
     * right-trim
     * {@code s}
     *  to
     * {@code width}
     *  characters
     * otherwise
     * {@code s}
     * <p> If
     * {@code width < 0}
     *  an exception is thrown
     * <p> Examples
     *
     * <pre>{@code
     * truncate("Hope", 3) == "Hop"
     * truncate("food", 17) == "food"
     * truncate("wibble", 0) == ""
     * }</pre>
     *
     */
    public static String truncate(String s, int width)
    {
        Throw.Exception.ifLessThan("width", width, 0);

        return s.length() <= width
               ? s
               : s.substring(0, width);
    }

    /**
     * <p> A String with the string representation of each element in
     * {@code thingsToJoin}
     *  separated by
     * {@code separator}
     *  with
     * {@code start}
     *  at the start and
     * {@code end}
     *  at the end.
     * <p> Examples
     *
     * <pre>{@code
     * join([1, 2, 3], "{", "-", "}") == "{1-2-3}"
     * join(["a", "b"], "/", "", "/") == "/ab/"
     * }</pre>
     *
     */
    public static String join(Iterable<?> thingsToJoin, String start, String separator, String end)
    {
        return join(thingsToJoin.iterator(), start, separator, end);
    }

    /**
     * <p> A String with the string representation of each element in
     * {@code iterator}
     *  separated by
     * {@code separator}
     *  with
     * {@code start}
     *  at the start and
     * {@code end}
     *  at the end.
     * <p> Examples
     *
     * <pre>{@code
     * join([1, 2, 3], "{", "-", "}") == "{1-2-3}"
     * join(["a", "b"], "/", "", "/") == "/ab/"
     * }</pre>
     *
     */
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
     * <p> A String with the string representation of each element in
     * {@code items}
     *  separated by
     * {@code separator}
     *
     */
    public static String join(Object[] items, String separator)
    {
        return join(Arrays.asList(items), separator);
    }

    /**
     * <p> Do the same as
     * {@link TextUtils#join(Iterator, String, String, String)}
     *
     * - but if it turns out that
     * {@code iterator}
     *  is empty, return the empty string rather than start + end
     *
     */
    public static String joinMin(Iterator<?> iterator, String start, String separator, String end)
    {
        String res = join(iterator, start, separator, end);
        return res.length() == start.length() + end.length() ? "" : res;
    }

    /**
     * <p> Do the same as
     * {@link TextUtils#join(Iterable, String, String, String)}
     *
     * - but if it turns out that
     * {@code iterator}
     *  is empty, return the empty string rather than start + end
     *
     */
    public static String joinMin(Iterable<?> thingsToJoin, String start, String separator, String end)
    {
        return joinMin(thingsToJoin.iterator(), start, separator, end);
    }

    /**
     * <p> Do the same as
     *
     *  {@code join(thingsToJoin, String, "", "")}
     *
     */
    public static String join(Iterable<?> thingsToJoin, String separator)
    {
        return join(thingsToJoin, "", separator, "");
    }

    /**
     * <p> Do the same as
     *
     *  {@code join(iterator, String, "", "")}
     *
     */
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

    /**
     *
     * The string `startQuote` + `stringToQuote` + `endQuote`
     */
    public static String quote(String startQuote, String stringToQuote, String endQuote)
    {
        return startQuote + stringToQuote + endQuote;
    }

    /**
     *
     * The string `quoteChars` + `stringToQuote` + `quoteChars`
     */
    public static String quote(String stringToQuote, String quoteChars)
    {
        return quoteChars + stringToQuote + quoteChars;
    }

    /**
     * <p> The character that is
     * {@code indexFromEndStartingAtZero}
     *  positions from the end of the string.
     * <p> So
     * {@code charAtFromEnd("wibble", 0) == 'e'}
     * <p> Throw
     * {@link dev.javafp.ex.ArgumentOutOfRange}
     * if
     * {@code indexFromEndStartingAtZero}
     *  is out of range.
     *
     *
     */
    public static char charAtFromEnd(String s, int indexFromEndStartingAtZero)
    {
        Throw.Exception.ifOutOfRange("s", indexFromEndStartingAtZero, 0, s.length() - 1);
        return s.charAt(s.length() - 1 - indexFromEndStartingAtZero);
    }

    public static LeftRightBox indent(int spaces, AbstractTextBox box)
    {
        return LeftRightBox.with(LeafTextBox.with(TextUtils.padOrTrimToWidth("", spaces)), box);
    }

    /**
     * <p> Get the text box representing
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
     *  then it returns
     * {@code thing.getTextBox()}
     * <p> If
     * {@code thing}
     *  is an array then it converts the array to an 
     * {@link ImList}
     *  and calls 
     * {@code getBoxFromList}
     *  on the list.
     * <p> To convert the array to an 
     * {@link ImList}
     * , if the array contains primitives it uses
     * {@link ImList#onPrimitiveArray(Object)}
     * otherwise it uses
     * {@link ImList#on(Object[])}
     * <p> Otherwise it creates a
     * {@link LeafTextBox}
     * on the result of
     * {@code String.valueOf(thing)}
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

    /**
     * We have to check if this array is a primitive array or if it has objects in it and call different
     * functions accordingly
     */
    private static AbstractTextBox getBoxFromArray(Object arr)
    {
        return arr.getClass().getComponentType().isPrimitive()
               ? getBoxFromList(ImList.onPrimitiveArray(arr))
               : getBoxFromList(ImList.on((Object[]) arr));
    }

    /**
     * <p> Format the collection as a top down list with index numbers to the left of each item.
     * <p> Example
     *
     * <pre>{@code
     * TextUtils.showCollection(ImList.on("apples", "blackberries", "cherries"))
     *
     * gives:
     *
     * 1    apples
     * 2    blackberries
     * 3    cherries
     * }</pre>
     *
     */
    static TopDownBox getBoxFromList(ImList<?> things)
    {
        ImList<AbstractTextBox> boxes = getTextBoxes(things);

        ImList<String> ints = ImRange.oneTo(boxes.size()).map(i -> "" + i);

        return TopDownBox.withAll(mapGetBoxFromPairOver(ints.zip(boxes), 3));
    }

    /**
     *
     * @param names
     * @param things
     * @return
     */
    public static AbstractTextBox getBoxFromNamesAndValues(ImList<String> names, ImList<?> things)
    {
        // If names is shorter than things, fill it up with numbers representing the index of the thing
        // names = "a" "b" "c", things = h i j k l m => "a" "b" "c" "4" "5" "6"
        ImList<String> ns = names.append(ImRange.inclusive(names.size() + 1, things.size()).map(i -> "" + i));

        int maxNameLength = ns.isEmpty() ? 0 : Util.maxInt(ns.map(i -> i.length()));

        return TopDownBox.withAll(mapGetBoxFromPairOver(ns.zip(getTextBoxes(things)), maxNameLength));
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
               ? ImList.on()
               : ImList.cons(getBoxFrom(things.head()), getTextBoxes(things.tail()));
    }

    /**
     * <p> Print a double value using the minimum number of trailing zeros.
     * <p> Where all the numbers after the . are 0, omit the . and any 0's
     */
    public static String prettyPrint(double d)
    {
        return d == (long) d
               ? String.valueOf((long) d)
               : String.valueOf(d);
    }

    /**
     * <p> If
     * {@code count}
     *  > 1, add an "s" to
     * {@code word}
     * , otherwise leave it alone
     *
     */
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

    public static String format(Object... xs)
    {
        return join(xs, " ");
    }

    /**
     * {@code s}
     *  left-padded with
     * {@code indent}
     * spaces
     * <p> Throw
     * {@link dev.javafp.ex.ArgumentShouldNotBeLessThan}
     * if
     * {@code indent < 0}
     *
     */
    public static String indentBy(int indent, String s)
    {
        Throw.Exception.ifLessThan("indent", indent, 0);

        return " ".repeat(indent) + s;
    }

    /**
     * <p> If
     * {@code width < s.length()}
     *  then
     * {@code s}
     *  left-trimmed to
     * {@code width}
     *  otherwise
     * {@code s}
     *  left-padded with spaces to
     * {@code width}
     * <p> Throw
     * {@link dev.javafp.ex.ArgumentShouldNotBeLessThan}
     * if
     * {@code width < 0}
     *
     */
    public static String rightJustifyIn(int width, String s)
    {
        Throw.Exception.ifLessThan("width", width, 0);

        int d = width - s.length();
        return d <= 0
               ? s.substring(-d, width - d)
               : indentBy(width - s.length(), s);

    }

    /**
     * <p> If
     * {@code width < s.length()}
     *  then
     * {@code s}
     *  right-trimmed to
     * {@code width}
     *  otherwise
     * {@code s}
     *  right-padded with spaces to
     * {@code width}
     * <p> Throw
     * {@link dev.javafp.ex.ArgumentShouldNotBeLessThan}
     * if
     * {@code width < 0}
     *
     */
    public static String leftJustifyIn(int width, String s)
    {
        Throw.Exception.ifLessThan("width", width, 0);

        return width - s.length() <= 0
               ? s.substring(0, width)
               : padOrTrimToWidth(s, width);

    }

    /**
     * <p> {@code cs}
     *  left-padded and right-padded with spaces so that it is centred and has length
     * {@code width}
     * <p> If
     * {@code width - cs.length()}
     *  is not even then the left-pad string will be one shorter than the right-pad string.
     * <p> Examples
     *
     * <pre>{@code
     * TextUtils.centreIn(7, "abc") == "  abc  "
     * TextUtils.centreIn(6, "abc") == " abc  "
     * }</pre>
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
            return padOrTrimToWidth(indentBy(left, cs), width);
        }

    }

}