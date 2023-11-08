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
 * <p> A text box that does not contain any other text boxes.
 *
 * <p> <img src="{@docRoot}/dev/doc-files/leaf-text-box.png"  width=300/>
 *
 * <p> It consists of 1 or more lines of text - ie Strings.
 * <p> Any tab characters will be expanded to spaces assuming a tab width of 4.
 * <p> There will be no newline characters in any of the lines.
 * <p> If any of the characters used to create the box are ISO control characters, they will be converted using
 * {@link #transformISOControlChars(String)}
 *
 *
 * <p> A
 * {@code LeafTextBox}
 * on the empty string has width
 * {@code 0}
 *  and height
 * {@code 1}
 *
 *
 */

public class LeafTextBox extends AbstractTextBox
{
    private final ImList<String> lines;

    private LeafTextBox(int width, int height, String... strings)
    {
        super(width, height);

        lines = ImList.on(strings).map(s -> transformISOControlChars(s)).take(height);
    }

    private LeafTextBox(int width, int height, ImList<String> lines)
    {
        super(width, height);

        this.lines = lines;
    }

    /**
     * <p> The String
     * {@code string}
     *  with each ISO control characters replaced with
     * {@code ¬}
     *
     */
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
        return Character.isISOControl(codePoint)
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
            return line.length() < width
                   ? line + TextUtils.repeatString(" ", width - line.length())
                   : line.substring(0, width);
        }
    }

    /**
     * <p> A
     * {@code LeafTextBox}
     *  containing
     * {@code text}
     *  centred in
     * {@code width}
     * .
     * <p> The centering is done using space characters.
     * <p> Any tabs in
     * {@code text}
     *  are expanded assuming a tab width of
     * {@code 4}
     *  using
     * {@link TextUtils#detab(int, String)}
     * <p> {@code text}
     *  is not trimmed.
     * <p> If any line of the text,
     * {@code l}
     * , is such that
     * {@code width <= l.size()}
     *  then this line is right trimmed to length
     * {@code width}
     * <p> Otherwise, let
     * {@code d = width - text.size()}
     * <p> if
     * {@code d}
     *  is not even then there will be
     * {@code (d - 1)/2}
     *  spaces on the left and
     * {@code (d - 1)/2 + 1}
     *  spaces on the right
     * <p> Examples:
     *
     * <pre>{@code
     * centred("abc", 7) => "  abc   "
     * centred("abc", 2) => "ab"
     * centred("abc", 4) => "abc "
     * centred("abc\nde", 14) =>
     * "     abc      "
     * "      de      "
     * }</pre>
     *
     */
    public static LeafTextBox centred(String text, int width)
    {
        ImList<String> lines = tidyUp(text).snd.map(i -> TextUtils.centreIn(width, i));

        return new LeafTextBox(width, lines.size(), lines);
    }

    /**
     * <p> A
     * {@code LeafTextBox}
     *  containing
     * {@code text}
     *  right-justified in
     * {@code width}
     * .
     * <p> The justification is done using space characters.
     * <p> Any tabs in
     * {@code text}
     *  are expanded assuming a tab width of
     * {@code 4}
     *  using
     * {@link TextUtils#detab(int, String)}
     * <p>
     * {@code text}
     *  is not trimmed.
     * <p> If any line of the text,
     * {@code l}
     * , is such that
     * {@code width <= l.size()}
     *  then this line is right trimmed to length
     * {@code width}
     * <p> Examples:
     *
     * <pre>{@code
     * righted("abc", 7) => "    abc"
     * righted("abc", 2) => "ab"
     * righted("abc", 4) => "abc "
     * righted("abc\nde", 5) =>
     * "  abc"
     * "   de"
     * }</pre>
     *
     */
    public static LeafTextBox righted(String text, int width)
    {
        ImList<String> lines = tidyUp(text).snd.map(i -> TextUtils.rightJustifyIn(width, i));

        return new LeafTextBox(width, lines.size(), lines);
    }

    /**
     * <p> A
     * {@code LeafTextBox}
     *  containing
     * {@code text}
     *  left-justified in
     * {@code width}
     * .
     * <p> The justification is done using space characters.
     * <p> Any tabs in
     * {@code text}
     *  are expanded assuming a tab width of
     * {@code 4}
     *  using
     * {@link TextUtils#detab(int, String)}
     *
     * <p>
     * {@code text}
     *  is not trimmed.
     * <p> If any line of the text,
     * {@code l}
     * , is such that
     * {@code width <= l.size()}
     *  then this line is right trimmed to length
     * {@code width}
     * <p> Examples:
     *
     * <pre>{@code
     * lefted("abc", 7) => "abc    "
     * lefted("abc", 2) => "ab"
     * lefted("abc", 4) => "abc "
     * lefted("abc\nde", 7) =>
     * "abc    "
     * "de     "
     * }</pre>
     *
     */
    public static LeafTextBox lefted(String text, int width)
    {
        ImList<String> lines = tidyUp(text).snd.map(i -> TextUtils.leftJustifyIn(width, i));

        return new LeafTextBox(width, lines.size(), lines);
    }

    /**
     *
     * <p> A
     * {@code LeafTextBox}
     *  containing
     * {@code text}
     *
     * <p> The width of the box is set to the maximum width of the lines in
     * {@code text}
     * <p> Any tabs in
     * {@code text}
     *  are expanded assuming a tab width of
     * {@code 4}
     *  using
     * {@link TextUtils#detab(int, String)}
     *
     * <p>
     * {@code text}
     *  is not trimmed.
     * <p> Examples:
     *
     * <pre>{@code
     * with("abc") => "abc"
     * with("Happy\nChristmas") =>
     * "Happy    "
     * "Christmas"
     * }</pre>
     *
     */
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

    private static ImPair<Integer, ImList<String>> tidyUp(String text)
    {
        if (text.endsWith("\n"))
        {
            text = text.substring(0, text.length() - 1);
        }

        String[] lines = text.split("\n");
        int max = 0;

        for (int i = 0; i < lines.length; i++)
        {
            lines[i] = transformISOControlChars(detab(lines[i]));
            max = Math.max(max, lines[i].length());
        }

        return ImPair.on(max, ImList.on(lines));
    }

    /**
     *
     * <p> A
     * {@code LeafTextBox}
     * <p> with width
     * {@code width}
     *  and height
     * {@code height}
     *
     *  containing
     * {@code text}
     *
     * <p> The width of the box is set to the maximum width of the lines in
     * {@code text}
     * <p> Any tabs in
     * {@code text}
     *  are expanded assuming a tab width of
     * {@code 4}
     *  using
     * {@link TextUtils#detab(int, String)}
     *
     * <p>
     * {@code text}
     *  is not trimmed.
     * <p> Examples:
     *
     * <pre>{@code
     * with("abc") => "abc"
     * with("Happy\nChristmas") =>
     * "Happy    "
     * "Christmas"
     * }</pre>
     *
     */
    public static LeafTextBox with(int width, int height, String text)
    {
        ImPair<Integer, String[]> p = splitIntoLinesAndDetab(text);

        return new LeafTextBox(width, height, p.snd);
    }

    private static String detab(String text)
    {
        return TextUtils.detab(4, text);
    }

    /**
     * <p> A text box with width
     * {@code width}
     *  and the contents
     * {@code text}
     * <p> <strong>wrapped</strong>
     *  in it
     * The wrapping is on characters - not words.
     * <p> Examples:
     *
     * <pre>{@code
     * wrap(6, "Mind how you go") =>
     * "Mind h\n"
     * "ow you\n"
     * " go\n"
     * }</pre>
     *
     *
     */
    public static LeafTextBox wrap(int width, String text)
    {
        String[] chunks = TextUtils.splitIntoChunks(width, text);

        return new LeafTextBox(width, chunks.length, chunks);
    }

}