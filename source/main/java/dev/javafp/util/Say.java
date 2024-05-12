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
 * <p> An actual output, taken from a test. The code in the test does this:
 *
 * <pre>{@code
 *         ImGraph<String, String, String> g1 = makeTestGraph();
 *
 *         say("testGraph", g1);
 *
 *         say("visGraph", g1.getGraphVizGraph());
 * }</pre>
 *
 * <p> and the output generated looked like this:
 *
 * <pre>{@code
 * 2023-11-30 15:59:44.938       - ImGraphTest::testABCDGraphViz                      testGraph ImGraph: valueMap: [A->, B->, C->, D->, x->, y->, z->]
 *                                                                                                       arcsOut:  [A->[(A, x) art, (A, C) mod], B->[(B, z) art, (B, C) mod], C->[(C, z) art, (C, D) mod], D->[(D, y) art]]
 *                                                                                                       arcsIn:   [C->[(A, C) mod, (B, C) mod], D->[(C, D) mod], x->[(A, x) art], y->[(D, y) art], z->[(B, z) art, (C, z) art]]
 *
 * 2023-11-30 15:59:44.978      39 ImGraphTest::testABCDGraphViz                      visGraph digraph d {
 *                                                                                             rankdir=TD;
 *                                                                                             size="10,10";
 *                                                                                             node [shape = box];
 *                                                                                             "A" -> "x"[ label = "art"];
 *                                                                                             "A" -> "C"[ label = "mod"];
 *                                                                                             "B" -> "z"[ label = "art"];
 *                                                                                             "B" -> "C"[ label = "mod"];
 *                                                                                             "C" -> "z"[ label = "art"];
 *                                                                                             "C" -> "D"[ label = "mod"];
 *                                                                                             "D" -> "y"[ label = "art"];
 *                                                                                             "x";
 *                                                                                             "y";
 *                                                                                             "z";
 *                                                                                             }
 * }</pre>
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

    /**
     * <p> Set the default thread prefix to
     * {@code prefix}.
     *
     * The default default is the empty string.
     *
     */
    public static void setDefaultThreadPrefix(String prefix)
    {
        defaultPrefix = prefix;
    }

    /**
     * <p> Set the current prefix in the thread to
     * {@code prefix}
     *
     */
    public static void setCurrentThreadPrefix(String prefix)
    {
        threadLocalPrefix.set(prefix);
    }

    /**
     * <p> Get the current thread prefix or the default if one has not been set
     */
    public static String getThreadPrefix()
    {
        String prefix = threadLocalPrefix.get();

        return prefix == null ? defaultPrefix : prefix;
    }

    /**
     * <p> Remove the current thread prefix - returning it to the default
     */
    public static void removeCurrentThreadPrefix()
    {
        threadLocalPrefix.remove();
    }

    /**
     * <p> {@code dateTime}
     *  formatted to the pattern
     * {@code "yyyy-MM-dd HH:mm:ss.SSS"}
     *
     */
    public static String formatDateTime(LocalDateTime dateTime)
    {
        return dateTime.format(formatter);
    }

    public static void printf(String formatString, Object... thingsToPrint)
    {
        say$(1, String.format(formatString, thingsToPrint));
    }

    /**
     * <p> Given an extra stack frame count of
     * {@code extraStackFrameCount}
     * , return a
     * {@link ImPair}
     * with two lists:
     * <ol>
     * <li>
     * <p> a list of the standard header boxes
     * </li>
     * <li>
     * <p> a list of the widths of the header boxes
     * </li>
     * </ol>
     * <p> Since one of the standard header boxes is the name of the method that called
     * {@link Say#say(Object...)}
     * we need the extra stack frame count to work out which entry in the stack to use.
     *
     */
    private static ImPair<ImList<AbstractTextBox>, ImList<Integer>> getHeaderBoxes(int extraStackFrameCount)
    {
        LocalDateTime date = LocalDateTime.now();

        String elapsed = oldDate.get() == null ? "-" : "" + getMsBetween(date, oldDate.get());

        AbstractTextBox dateTimeBox = LeafTextBox.with(formatDateTime(date));
        AbstractTextBox elapsedBox = LeafTextBox.righted(elapsed, ELAPSED_MS_WIDTH);

        String abbreviatedName = TextUtils.abbreviate(getClassAndMethod(4 + extraStackFrameCount), NAME_WIDTH);
        AbstractTextBox nameBox = LeafTextBox.with(abbreviatedName);

        oldDate.set(date);

        return ImPair.on(ImList.on(dateTimeBox, elapsedBox, nameBox), widths);

    }

    static long getMsBetween(LocalDateTime after, LocalDateTime before)
    {
        return Duration.between(before, after).toMillis();
    }

    private static String getClassAndMethod(int stackCount)
    {
        Thread currentThread = Thread.currentThread();

        StackTraceElement ste = currentThread.getStackTrace()[stackCount];

        return "" + getClassName(ste) + "::" + ste.getMethodName();
    }

    static String getMethodName(int goUpCount)
    {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[2 + goUpCount];

        return "" + getClassName(ste) + "::" + ste.getMethodName();
    }

    private static String getClassName(StackTraceElement ste)
    {
        String fullName = ste.getClassName();
        return ParseUtils.split('.', fullName).last();
    }

    /**
     * <p> Get the offset of the time
     * {@code ts}
     *  from the time that the VM started
     *
     */
    public static long getLocalTime(long ts)
    {
        return ts - start;
    }

    /**
     * <p> Say
     * {@code box}
     *  with the extra prefix of the thread prefix (abbreviated to 8 characters)
     *
     */
    public static void sayWithThreadPrefix(AbstractTextBox box)
    {
        sayWithExtraHeader(LeafTextBox.with(getThreadPrefix()), box);
    }

    static void sayWithExtraHeader(AbstractTextBox header, AbstractTextBox box)
    {
        getHeaderBoxes(0).consumeIn((boxes, widths) -> sayWithHeader(boxes.push(header), widths.push(header.width), box));
    }

    /**
     * <p> Say
     * {@code things}
     *  but arranging them top-down rather than left-right
     *
     */
    public static void sayTopDown(Object... things)
    {
        sayWithExtraHeader(LeafTextBox.with(getThreadPrefix()), TopDownBox.with(things));
    }

    /**
     * <p> The main diagnostic display method.
     * <p> {@code things}
     *  are converted to text boxes and composed left-right with a single spaces between them.
     * <p> The first line has a number of standard header entries to help with debugging.
     * <p> See the class comments for more details.
     *
     */
    public static void say(Object... things)
    {
        say$(1, things);
    }

    static void say$(int extraStackFrameCount, Object... things)
    {
        getHeaderBoxes(extraStackFrameCount).consumeIn((boxes, widths) -> sayWithHeader(boxes, widths, things));
    }

    /**
     * <p> Say
     * {@code things}
     *  - with a header box that is a
     * {@code LeftRightBox}
     *  composed of
     * {@code boxes}
     * , each adjusted to have a width taken from the
     * corresponding element of
     * {@code widths}
     *
     *
     */
    public static void sayWithHeader(ImList<AbstractTextBox> boxes, ImList<Integer> widths, Object... things)
    {
        sayBoxesWithHeader(boxes, widths, ImList.on(things).map(t -> TextUtils.getBoxFrom(t)));
    }

    private static void sayBoxesWithHeader(ImList<AbstractTextBox> boxes, ImList<Integer> widths, ImList<AbstractTextBox> thingBoxes)
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
        finalPrint(box.toBuilder(true).append('\n'));
    }

    private static void finalPrint(StringBuilder formatted)
    {
        if (buffer == null)
            System.out.append(formatted);
        else
            buffer.print(formatted);
    }

    public static void sayBox(Object... things)
    {
        // Get boxes from the things
        ImList<AbstractTextBox> boxes = ImList.on(things).map(t -> TextUtils.getBoxFrom(t));

        // Create a "vertical bar"
        TopDownBox vBar = TopDownBox.withAll(ImList.repeat(" | ", Util.maxInt(boxes.map(i -> i.height))));

        // Intersperse the vertical bar
        ImList<AbstractTextBox> boxesWithSep = boxes.intersperse(vBar);

        // Display them
        sayWithThreadPrefix(LeftRightBox.withAll(boxesWithSep));
    }

    /**
     * <p> Get the output that has been stored in the buffer while
     * {@code Say}
     *  was in quiet mode, or the empty string if
     * {@code Say}
     *  was not in quiet mode.
     *
     */
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

    /**
     * <p> Set quiet mode on or off, depending on
     * {@code beQuiet}
     *
     */
    public static void setQuiet(boolean beQuiet)
    {
        buffer = beQuiet
                 ? new CachingBuffer(1000)
                 : null;
    }

    /**
     * <p> Say
     * {@code count}
     *  newlines
     *
     */
    public static void printNewLines(int count)
    {
        say$(1, "\n".repeat(count));
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
     * <p> table [ one, one, pathologicopsychological, [99, 100, 101, 102, 103, 104, 105], three,  null, four ]
     * <p> gives:
     *
     * <pre>{@code
     * one                      : one
     * pathologicopsychological : [99, 100, 101, 102, 103, 104, 105]
     * three                    : null
     * four                     : MISSING
     * }</pre>
     *
     */
    public static AbstractTextBox table(Object... things)
    {
        ImList<ImPair<String, String>> pairs = ImList.on(things).group(2).map(p -> ImPair.on("" + p.at(1), "" + p.at(2, "MISSING")));

        ImList<AbstractTextBox> col1 = pairs.map(p -> LeafTextBox.with(p.fst));
        ImList<AbstractTextBox> col2 = pairs.map(p -> TextUtils.getBoxFrom(p.snd));

        int colOneWidth = Util.maxInt(col1.map(i -> i.width));

        return TopDownBox.withAllBoxes(col1.zipWith(col2, (i, j) -> LeftRightBox.with(i.leftJustifyIn(colOneWidth), colon, j)));
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