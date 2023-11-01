/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.box;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.TextUtils;

/**
 * <p> A text box that does not contain any other text boxes
 * <p> A leaf text box on the empty string has width 0 and height 1
 *
 */

public class LeafTextBox extends AbstractTextBox
{
    private final ImList<String> lines;

    private LeafTextBox(int width, int height, String... strings)
    {
        super(width, height);
        //        this.lines = new ArrayList<String>(strings.length);
        //
        //        int count = 0;
        //        for (String string : strings)
        //        {
        //            count++;
        //            if (count > height)
        //                break;
        //
        //            lines.add(transformISOControlChars(string));
        //        }

        lines = ImList.on(strings).map(s -> transformISOControlChars(s)).take(height);
    }

    public static String transformISOControlChars(String string)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < string.length(); i++)
        {
            sb.append(transformISOChar(string.charAt(i)));
        }

        return sb.toString();
    }

    private static char transformISOChar(char codePoint)
    {
        return isISOControl(codePoint)
               ? '¬'
               : codePoint;
    }

    @Override
    public String getLine(int n)
    {
        if (n > lines.size())
            return TextUtils.repeatString(" ", width);
        else
        {
            String line = lines.at(n);
            if (line.length() < width)
                return line + TextUtils.repeatString(" ", width - line.length());
            else
                return line.substring(0, width);
        }
    }

    public static LeafTextBox centred(String text, int widthToCentreIn)
    {
        String t = detab(text);
        int lead = Math.max(0, (widthToCentreIn - t.length()) / 2);
        return new LeafTextBox(widthToCentreIn, 1, TextUtils.repeatString(" ", lead) + t);
    }

    public static LeafTextBox righted(String text, int width)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);
        int leadCount = Math.max(0, width - p.fst);
        p.snd[0] = TextUtils.repeatString(" ", leadCount) + p.snd[0];

        return new LeafTextBox(p.fst + leadCount, p.snd.length, p.snd);
    }

    public static LeafTextBox lefted(String text, int width)
    {
        return withMargins(text, 0, width);
    }

    public static LeafTextBox withMargin(String text, int marginSize)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);
        p.snd[0] = TextUtils.repeatString(" ", marginSize) + p.snd[0];

        return new LeafTextBox(p.fst + marginSize * 2, p.snd.length, p.snd);
    }

    public static LeafTextBox withMargins(String text, int leftMargin, int width)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);
        p.snd[0] = TextUtils.repeatString(" ", leftMargin) + p.snd[0];

        return new LeafTextBox(Math.max(width, leftMargin + p.fst), p.snd.length, p.snd);
    }

    public static LeafTextBox with(String text)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);

        return new LeafTextBox(p.fst, p.snd.length, p.snd);
    }

    private static ImPair<Integer, String[]> splitIntoLinesAndDetab(String text)
    {
        if (text.endsWith("\n"))
        {
            text = text.substring(0, text.length() - 1);
        }

        String[] lines = text.split("\n");
        int max = 0;

        for (int i = 0; i < lines.length; i++)
        {
            lines[i] = detab(lines[i]);
            max = Math.max(max, lines[i].length());
        }

        return ImPair.on(max, lines);
    }

    public static LeafTextBox with(int width, int height, String text)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);

        return new LeafTextBox(width, height, p.snd);
    }

    private static String detab(String text)
    {
        return TextUtils.detab(4, text);
    }

    //    private static int maxWidth(String[] lines)
    //    {
    //        int max = 0;
    //        for (String line : lines)
    //        {
    //            max = Math.max(max, line.length());
    //        }
    //
    //        return max;
    //    }

    /**
     * <p> A text box with width
     * {@code width}
     *  and the contents
     * {@code text}
     *  wrapped in it
     * The wrapping is on characters - not words
     *
     */
    public static LeafTextBox wrap(int width, String text)
    {
        String[] chunks = TextUtils.splitIntoChunks(width, text);

        return new LeafTextBox(width, chunks.length, chunks);
    }

    private static boolean isISOControl(int codePoint)
    {
        return codePoint != 0x0009 && ((codePoint >= 0x0000 && codePoint <= 0x001F) || (codePoint >= 0x007F && codePoint <= 0x009F));
    }

}