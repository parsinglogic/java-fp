/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.LeftRightBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * <p> A class to allow a convenient way to display debugging information that automatically includes
 * timestamp information and, most usefully, records which method contained the call to
 * {@code say}
 * .
 *
 * <p> The easiest way to think about what it does is to consider it in terms of text boxes,
 * {@link AbstractTextBox}.
 *
 * <p> The main method,
 * {@code say}
 *  takes an array of objects, uses
 * {@link TextUtils#getBoxFrom(Object)}
 * on each to get a text box
 * and it then composes the text boxes  into a
 * {@link LeftRightBox}
 * <p> We call this the
 * {@code message box}
 * .
 * <p> Each of the boxes can have many lines of text.
 * <p> It then generates a "header" box - a text box that contains three text boxes that are composed into a LeftRightBox.
 * <p> These contain some standard information:
 * <ol>
 * <li>
 * <p> The date and timestamp
 * </li>
 * <li>
 * <p> The ms since
 * {@code say}
 *  was last called
 * </li>
 * <li>
 * <p> The method name that called it. We use the stack trace of an exception to work this out.
 * </li>
 * </ol>
 * <p> The data and timestamp text box has a fixed width. If the text in the other two boxes is too wide then it will wrap onto another line
 * <p> This means that the header box has a fixed width. It normally is only one line high.
 * <p> This box is now composed in a LeftRightBox with the message text box and this final box is converted to a String and output.
 *  <p> <img src="{@docRoot}/dev/doc-files/say-output.png"  width=500/>
 *
 */
public class Say
{
    static long start = System.currentTimeMillis();

    /**
     * The buffer to use instead of writing to the standard out if we are in quiet mode
     */
    static CachingBuffer buffer = null;

    private static final AbstractTextBox spaceBox = LeafTextBox.with(" ");

    private static final AbstractTextBox colon = LeafTextBox.with(" : ");

    /**
     * The timezone for the UK
     */
    public static final ZoneId londonZone = ZoneId.of("Europe/London");

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    static ThreadLocal<LocalDateTime> oldDate = new ThreadLocal<>();

    final static int DATE_TIME_WIDTH = 23;
    final static int ELAPSED_MS_WIDTH = 7;
    final static int NAME_WIDTH = 50;

    private static final ImList<Integer> widths = ImList.on(DATE_TIME_WIDTH, ELAPSED_MS_WIDTH, NAME_WIDTH);

    // We store each thread-specific prefix in a thread local
    private static ThreadLocal<String> threadLocalPrefix = new ThreadLocal<>();

    // The default prefix when there isn't one in the thread
    private static String defaultPrefix = "";

    // Set the default prefix
    public static void setDefaultThreadPrefix(String prefix)
    {
        defaultPrefix = prefix;
    }

    /**
     * <p> Get the current prefix in the thread or the default if there isn't one
     */
    public static void setCurrentThreadPrefix(String prefix)
    {
        threadLocalPrefix.set(prefix);
    }

    public static String getPrefix()
    {
        String prefix = threadLocalPrefix.get();

        return prefix == null ? defaultPrefix : prefix;
    }

    public static void removeCurrentThreadPrefix()
    {
        threadLocalPrefix.remove();
    }

    public static String formatDateTime(LocalDateTime t)
    {
        return t.format(formatter);
    }

    public static void printf(String formatString, Object... thingsToPrint)
    {
        say$(1, AbstractTextBox.empty, format(formatString, thingsToPrint));
    }

    public static String format(String formatString, Object... thingsToPrint)
    {
        return String.format(formatString, thingsToPrint);
    }

    /**
     * <p> There is a prefix that apps can set in SayPrefix - a thread local variable
     * <p> The first part of the line shows:
     * <p> the thread id
     * the datetimestamp
     * the ms since the last time we logged
     * the class and method name
     * <p> Then there is a space and then the message
     *
     * <pre>{@code
     * YYYY-MM-DD hh:mm:ss:SSS
     * |                       ms since the last line
     * |                       |        method name                                 message
     * |                       |        |                                           |
     *
     * 1....................23 1.....7  1........................................50 |
     * |                     | |     |  |                                         | |
     * 2020-01-15 12:03:09.897 1234567  DrumApplicationRunner::handleEvents5        xxxxxxxx
     * }</pre>
     *
     */
    private static AbstractTextBox preBox(int extraStackFrameCount)
    {
        LocalDateTime date = LocalDateTime.now();

        AbstractTextBox prefixBox = LeafTextBox.with(getPrefix());

        String elapsed = oldDate.get() == null ? "-" : "" + getMsBetween(date, oldDate.get());

        AbstractTextBox dateTimeBox = LeafTextBox.lefted(formatDateTime(date), DATE_TIME_WIDTH);
        AbstractTextBox elapsedBox = LeafTextBox.righted(elapsed, ELAPSED_MS_WIDTH);

        String abbreviatedName = TextUtils.abbreviate(getClassAndMethod(4 + extraStackFrameCount), NAME_WIDTH);
        AbstractTextBox nameBox = LeafTextBox.lefted(abbreviatedName, NAME_WIDTH);

        oldDate.set(date);

        return LeftRightBox.with(prefixBox, spaceBox, dateTimeBox, spaceBox, elapsedBox, spaceBox, nameBox, spaceBox);
    }

    /**
     * Get  a pair with
     * a list of the header boxes
     * a list of the widths of the header boxes
     *
     */
    private static ImPair<ImList<AbstractTextBox>, ImList<Integer>> getHeaderBoxes(int extraStackFrameCount)
    {
        LocalDateTime date = LocalDateTime.now();

        AbstractTextBox prefixBox = LeafTextBox.with(getPrefix());

        String elapsed = oldDate.get() == null ? "-" : "" + getMsBetween(date, oldDate.get());

        AbstractTextBox dateTimeBox = LeafTextBox.with(formatDateTime(date));
        AbstractTextBox elapsedBox = LeafTextBox.righted(elapsed, ELAPSED_MS_WIDTH);

        String abbreviatedName = TextUtils.abbreviate(getClassAndMethod(4 + extraStackFrameCount), NAME_WIDTH);
        AbstractTextBox nameBox = LeafTextBox.with(abbreviatedName);

        oldDate.set(date);

        return ImPair.on(ImList.on(dateTimeBox, elapsedBox, nameBox), widths);

    }

    public static long getMsBetween(LocalDateTime after, LocalDateTime before)
    {
        return Duration.between(before, after).toMillis();
    }

    private static String getClassAndMethod(int stackCount)
    {
        Thread currentThread = Thread.currentThread();

        StackTraceElement ste = currentThread.getStackTrace()[stackCount];

        return "" + getClassName(ste) + "::" + ste.getMethodName();
    }

    public static String getMethodName(int goUpCount)
    {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2 + goUpCount];

        return "" + getClassName(ste) + "::" + ste.getMethodName();
    }

    private static String getClassName(StackTraceElement ste)
    {
        String fullName = ste.getClassName();
        return ParseUtils.split('.', fullName).last();
    }

    public static long getLocalTime(long ts)
    {
        return ts - start;
    }

    public static void sayNl(Object... things)
    {
        AbstractTextBox preBox = preBox(0);

        ImList<AbstractTextBox> boxes = ImList.on(things).map(t -> TextUtils.getBoxFrom(t));

        print(preBox, TopDownBox.withAllBoxes(boxes));
    }

    public static void println(Object objectToShow)
    {
        say(objectToShow);
    }

    public static void say(Object... things)
    {
        say$(1, things);
    }

    //
    //    public static void log(String pre, Object... things)
    //    {
    //        say$(1, LeafTextBox.with(pre), things);
    //    }

    static void say$(int extraStackFrameCount, Object... things)
    {
        getHeaderBoxes(extraStackFrameCount).map((boxes, widths) -> sayWithHeaderVoid(boxes, widths, things));
    }

    /**
     * To allow me to use sayWithHeader in map
     */
    private static Void sayWithHeaderVoid(ImList<AbstractTextBox> boxes, ImList<Integer> widths, Object... things)
    {
        sayWithHeader(boxes, widths, things);
        return null;
    }

    /**
     * Say `things` - with a header box that is a `LeftRightBox` composed of `boxes`, each adjusted to have a width taken from the
     * corresponding element of `widths`
     *
     */
    public static void sayWithHeader(ImList<AbstractTextBox> boxes, ImList<Integer> widths, Object... things)
    {
        say$$$(boxes, widths, ImList.on(things).map(t -> TextUtils.getBoxFrom(t)));
    }

    private static void say$$$(ImList<AbstractTextBox> boxes, ImList<Integer> widths, ImList<AbstractTextBox> thingBoxes)
    {

        // I could intersperse with spaces - but let's just add 1 to the widths
        ImList<AbstractTextBox> headerBoxesWithSpaces = boxes.zipWith(widths, (a, b) -> a.leftJustifyIn(b + 1));

        // Ok so here we have to do something different on the last box to prevent adding that last space
        ImList<Integer> indexes = ImList.oneTo(thingBoxes.size());
        ImList<AbstractTextBox> thingBoxesWithSpaces = thingBoxes.zipWith(indexes,
                (a, b) -> b == thingBoxes.size() ? a : a.leftJustifyIn(a.width + 1));

        print2(LeftRightBox.withAllBoxes(headerBoxesWithSpaces.append(thingBoxesWithSpaces)));
    }

    private static void print2(AbstractTextBox box)
    {
        finalPrint(box.toString());
    }

    private static void print(AbstractTextBox pre, AbstractTextBox content)
    {
        finalPrint(pre.before(content).toString());
    }

    public static void finalPrint(String formatted)
    {
        if (buffer == null)
            System.out.println(formatted);
        else
            buffer.println(formatted);
    }

    public static void sayBox(Object... things)
    {
        AbstractTextBox preBox = preBox(0);

        ImList<String> bars = ImList.repeat(" | ");

        // Get boxes from the things
        ImList<AbstractTextBox> boxes = ImList.on(things).map(t -> TextUtils.getBoxFrom(t));

        // Get the max height
        int maxHeight = boxes.foldl(0, (z, b) -> Math.max(z, b.height));

        // Create a "vertical bar"
        TopDownBox vBar = TopDownBox.withAll(bars.take(maxHeight));

        // Intersperse the vertical bar
        ImList<AbstractTextBox> boxesWithSep = boxes.intersperse(vBar);

        // Display them
        print(preBox, LeftRightBox.withAll(boxesWithSep));
    }

    public static void printBox(AbstractTextBox box)
    {
        print(preBox(0), box);
    }

    public static void errorln(String message)
    {
        if (isQuiet())
        {
            synchronized (System.out)
            {
                say$(1, AbstractTextBox.empty, message);
            }
        }
        else
            say$(1, AbstractTextBox.empty, message);

    }

    public static String getBufferString()
    {
        return buffer == null ? "" : buffer.getString();
    }

    /**
     * Clear any text in the current output buffer - or do nothing if not in quiet mode
     */
    public static void clearBuffer()
    {
        buffer = buffer == null
                 ? null
                 : new CachingBuffer(1000);
    }

    public static void setQuiet(boolean beQuiet)
    {

        buffer = beQuiet
                 ? new CachingBuffer(1000)
                 : null;
    }

    public static CachingBuffer getBuffer()
    {
        return buffer;
    }

    public static void printNewLines(int count)
    {
        printf(TextUtils.repeatString("%n", count));
    }

    public static void setStart(long st)
    {
        start = st;
    }

    public static boolean isQuiet()
    {
        return buffer != null;
    }

    /**
     * <p> Create a text box with (essentially) two columns
     * <p> The
     * {@code things}
     *  are split into pairs and the first thing in each pair is shown in the first column and the second in the second column.
     * <p> If there are an odd number of things, the last item in the second column is "MISSING"
     * <p> There are actually three columns since the two columns are separated by a column of colons
     * <p> Eg
     * <p> table [ one, one, floccinaucinihilipilification, [99, 100, 101, 102, 103, 104, 105], three,  null, four ]
     * <p> gives:
     * <p> one                           : one
     * floccinaucinihilipilification : [99, 100, 101, 102, 103, 104, 105]
     * three                         : null
     * four                          : MISSING
     *
     */
    public static AbstractTextBox table(Object... things)
    {
        ImList<ImPair<String, String>> pairs = ImList.on(things).group(2).map(p -> ImPair.on("" + p.at(1), "" + p.at(2, "MISSING")));

        AbstractTextBox col1 = TopDownBox.withAllBoxes(pairs.map(p -> LeafTextBox.with(p.fst)));
        AbstractTextBox col3 = TopDownBox.withAllBoxes(pairs.map(p -> TextUtils.getBoxFrom(p.snd)));

        AbstractTextBox col2 = TopDownBox.withAllBoxes(ImList.repeat(colon).take(pairs.size()));

        return LeftRightBox.with(col1, col2, col3);

    }

    /**
     * <p> Generate a text box representing
     * {@code columns}
     *  displayed as ... er columns
     * <p> Each column has a size that is 1 + the max width of the entries in the column
     * <p> So if we have
     *
     * <pre>{@code
     * col1 = [ten, eleven, twelve, thirteen, fourteen]
     * col2 = [one, two, three, four, five, six]
     * col3 = [seven, eight, nine]
     * }</pre>
     * <p> then
     *
     * <pre>{@code
     * Say.formatColumns(col1, col2, col3).toString())
     * }</pre>
     * <p> will be
     *
     * <pre>{@code
     * ten      one   seven
     * eleven   two   eight
     * twelve   three nine
     * thirteen four
     * fourteen five
     *          six
     * }</pre>
     *
     */
    public static AbstractTextBox formatColumns(ImList<?>... columns)
    {

        ImList<LeafTextBox> empties = ImList.repeat(LeafTextBox.with(""));

        // Get a matrix of boxes, as a list of columns
        ImList<ImList<AbstractTextBox>> cols = ImList.on(columns).map(c -> getBoxes(c));

        // Get the width of each column - which is the max of each cell width
        ImList<Integer> widths = cols.map(c -> getWidth(c));

        // Get the table height - the max column length
        int height = Util.maxInt(cols.map(c -> c.size()));

        // Draw the table. We add a repeat list of empty leaf text boxes to each column so that it is easy to
        // take elements from each column when they are all different lengths
        return drawTable(height, widths, cols.map(c -> c.append(empties)));
    }

    private static AbstractTextBox drawTable(int height, ImList<Integer> widths, ImList<ImList<AbstractTextBox>> cols)
    {
        if (height == 0)
        {
            return TopDownBox.withBoxes();
        }
        else
        {
            return drawRow(widths, takeOneFrom(cols)).above(drawTable(height - 1, widths, dropOneFrom(cols)));
        }
    }

    public static void showTable(Object... things)
    {
        say$(1, table(things));
    }

    private static AbstractTextBox drawRow(ImList<Integer> widths, ImList<AbstractTextBox> abstractTextBoxes)
    {
        return LeftRightBox.withAll(widths.zipWith(abstractTextBoxes, (w, b) -> LeftRightBox.withMargins(0, w + 1, b)));
    }

    private static ImList<AbstractTextBox> takeOneFrom(ImList<ImList<AbstractTextBox>> cols)
    {
        return cols.map(c -> c.head());
    }

    private static ImList<ImList<AbstractTextBox>> dropOneFrom(ImList<ImList<AbstractTextBox>> cols)
    {
        return cols.map(c -> c.drop(1));
    }

    private static boolean isOneEmpty(ImList<ImList<AbstractTextBox>> cols)
    {
        return ImList.or(cols.map(c -> c.isEmpty()));
    }

    private static boolean areAllEmpty(ImList<ImList<AbstractTextBox>> cols)
    {
        return ImList.and(cols.map(c -> c.isEmpty()));
    }

    private static Integer getWidth(ImList<AbstractTextBox> c)
    {
        return Util.maxInt(c.map(i -> i.width));
    }

    private static ImList<AbstractTextBox> getBoxes(ImList<?> col)
    {
        return col.map(cell -> TextUtils.getBoxFrom(cell));
    }

    public static void line()
    {
        say("==========================================================================================");
    }

}