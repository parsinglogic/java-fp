package dev.javafp.val;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;

import java.nio.charset.StandardCharsets;

/**
 * <p> A class to represent a Unicode code point in all its 21 bit glory.
 *
 * <p> It is also home to some helper functions.
 *
 *
 * <p> For a good overview, see https://en.wikipedia.org/wiki/Universal_Character_Set_characters
 *
 * <p> The idea is that we convert Strings into Lists of ImCodePoints and then, processing is simplified - compared to
 * UTF-16 in normal Java Strings
 *
 * <p> To try encoding/decoding:
 *
 * <pre>{@code
 * https://www.coderstool.com/utf16-encoding-decoding
 * https://checkserp.com/encode/unicode/
 * }</pre>
 * <p> A Unicode code point
 * <p> An example:
 *
 * <pre>{@code
 * Character                                  ♥
 * Character name                             BLACK HEART SUIT
 * Hex code point                             2665
 * Decimal code point                         9829
 * Hex UTF-8 bytes                            E2 99 A5
 * percent encoding                           %E2%99%A5
 * Octal UTF-8 bytes                          342 231 245
 * UTF-8 bytes as Latin-1 characters bytes    â 0x99 ¥
 *
 *
 *
 * 👨 man
 *     U+1F468
 *     \xf0\x9f\x91\xa8
 *     %F0%9F%91%A8
 *
 * 👨‍🦲 Bald man
 *     U+1F468 U+200D U+1F9B2
 *     \xf0\x9f\x91\xa8\xe2\x80\x8d\xf0\x9f\xa6\xb2
 *
 * 👨‍🦰 Man with red hair
 *    U+1F468 U+200D U+1F9B0
 *    \xf0\x9f\x91\xa8\xe2\x80\x8d
 *    %F0%9F%91%A8%E2%80%8D%F0%9F%A6%B0
 *
 *
 * }</pre>
 * <p> Valid UTF-8 byte sequences
 *
 * <pre>{@code
 *    Code Points    First Byte Second Byte Third Byte Fourth Byte
 *   U+0000 -   U+007F   00 - 7F
 *   U+0080 -   U+07FF   C2 - DF    80 - BF
 *   U+0800 -   U+0FFF   E0         A0 - BF     80 - BF
 *   U+1000 -   U+CFFF   E1 - EC    80 - BF     80 - BF
 *   U+D000 -   U+D7FF   ED         80 - 9F     80 - BF
 *   U+E000 -   U+FFFF   EE - EF    80 - BF     80 - BF
 *  U+10000 -  U+3FFFF   F0         90 - BF     80 - BF    80 - BF
 *  U+40000 -  U+FFFFF   F1 - F3    80 - BF     80 - BF    80 - BF
 * U+100000 - U+10FFFF   F4         80 - 8F     80 - BF    80 - BF
 * }</pre>
 */
public class ImCodePoint extends ImValuesImpl implements Comparable<ImCodePoint>
{
    //     " !\"#$%& '()*+,-./"
    //     "0123456789:;<=>?"
    //     "@ABCDEFGHIJKLMNO"
    //     "PQRSTUVWXYZ[\\]^_"
    //     "`abcdefghijklmno"
    //     "pqrstuvwxyz{|}~"

    /**
     * <p> The Unicode
     * {@code SPACE}
     *  codepoint
     */
    public static final ImCodePoint SPACE = ImCodePoint.valueOf(' ');

    /**
     * <p> The Unicode
     * {@code PERCENT SIGN}
     *  codepoint
     */
    public static final ImCodePoint PERCENT = ImCodePoint.valueOf('%');

    /**
     * <p> The Unicode
     * {@code COLON}
     *  codepoint
     */
    public static final ImCodePoint COLON = ImCodePoint.valueOf(':');

    /**
     * <p> The Unicode
     * {@code Uppercase Latin alphabet}
     *  and
     * {@code Lowercase Latin alphabet}
     */
    public static final ImSet<ImCodePoint> alpha = alpha();

    /**
     * <p> The codePoint as an
     * {@code int}
     */
    public final int codePoint;

    private ImCodePoint(int codePoint)
    {
        this.codePoint = codePoint;
    }

    /**
     * <p> The codepoint represented by
     * {@code codePoint}
     * <p> If
     * {@code codePoint}
     *  does not represent a defined codepoint or it is a surrogate then throw an exception    */
    public static ImCodePoint on(int codePoint)
    {
        Throw.Exception.ifTrue(!Character.isDefined(codePoint) || isSurrogate(codePoint), "" + codePoint + " is not a valid code point");
        return new ImCodePoint(codePoint);
    }

    /**
     *
     * <p> The codepoint represented by
     * {@code s}
     * .
     * <p> {@code s}
     *  is expected to have either one or two characters - in standard Java UTF-16 encoding.
     */
    public static ImCodePoint on(String s)
    {
        return ImCodePoint.on(s.codePointAt(0));
    }

    /**
     *
     * <p> The codepoint represented by
     * {@code bmpCharacter}
     * . Since it is a single character, it should be a character from the basic multilingual plane.
     */
    public static ImCodePoint valueOf(char bmpCharacter)
    {
        return on(bmpCharacter);
    }

    /**
     * <p> The codepoints in
     * {@code s}
     *  as an array
     */
    public static ImCodePoint[] getCodePointArray(String s)
    {
        return s.codePoints().mapToObj(i -> ImCodePoint.on(i)).toArray(ImCodePoint[]::new);
    }

    /**
     * The set of upper and lower case alpha characters
     */
    private static ImSet<ImCodePoint> alpha()
    {
        ImSet<ImCodePoint> alpha = ImSet.on();

        for (int i = 'A'; i <= 'Z'; i++)
            alpha = alpha.add(ImCodePoint.on(i));

        for (int i = 'a'; i <= 'z'; i++)
            alpha = alpha.add(ImCodePoint.on(i));

        return alpha;
    }

    @Override
    public AbstractTextBox getTextBox()
    {
        return LeafTextBox.with(toHexString());
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(codePoint);
    }

    @Override
    public String toString()
    {
        return Character.toString(codePoint);
    }

    /**
     * <p> The codepoint as a hex number format string.
     *
     * <pre>{@code
     * ImCodePoint.valueOf('A').toHexString() == "0x41"
     * }</pre>
     */
    public String toHexString()
    {
        return String.format("0x%X", codePoint);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("codePoint");
    }

    /**
     * <p> Given a unicode string,
     * {@code cs}
     * , that contains unicode code points and some code points that are percent encoded strings
     * for example %0A or %99 or %FF
     * <p> Convert each run of such percent encoded strings to the equivalent bytes and then convert to unicode code points.
     * <p> We can't use URLDecoder because it changes "+" to " "
     */
    public static ImList<ImCodePoint> decodePercents(ImList<ImCodePoint> cs)
    {
        // Cut into a pair of unicode strings at '%'
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = cs.cutIntoTwo(c -> !Eq.uals(PERCENT, c));

        // If there is no '%' then just return the unicode code points
        if (pair.snd.isEmpty())
            return cs;
        else
        {
            // extract a % run from the second part, converted into a list of bytes, and the rest of the code points
            ImPair<ImList<Byte>, ImList<ImCodePoint>> pp = getPercentRun(pair.snd);

            // Decode the bytes to unicode code points
            return pp.fst.isEmpty()
                   ? pair.fst.appendElement(pair.snd.head()).append(decodePercents(pair.snd.tail()))
                   : pair.fst.append(decodeBytes(pp.fst)).append(decodePercents(pp.snd));
        }
    }

    /**
     * Implementation of {@link java.lang.Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(ImCodePoint other)
    {
        return codePoint - other.codePoint;
    }

    /**
     * <p> Decode the bytes in
     * {@code bytes}
     *  in UTF-8 to a list of Unicode code points
     */
    static ImList<ImCodePoint> decodeBytes(ImList<Byte> bytes)
    {
        if (bytes.isEmpty())
            return ImList.on();
        else
        {
            byte[] bs = new byte[bytes.size()];

            int i = 0;
            for (byte j : bytes)
            {
                bs[i++] = j;
            }

            return ImList.onString(new String(bs, StandardCharsets.UTF_8));
        }
    }

    private static ImPair<ImList<Byte>, ImList<ImCodePoint>> getPercentRun(ImList<ImCodePoint> cps)
    {
        if (cps.size() < 3)
            return ImPair.on(ImList.on(), cps);
        else if (Eq.uals(PERCENT, cps.head())
                && cps.at(2).isHexDigit()
                && cps.at(3).isHexDigit()
        )
        {
            byte b = hexDigitsToByte(cps.at(2).hexDigitAsInt(), cps.at(3).hexDigitAsInt());
            return conss(b, getPercentRun(cps.drop(3)));
        }
        else
            return ImPair.on(ImList.on(), cps);
    }

    private static ImPair<ImList<Byte>, ImList<ImCodePoint>> conss(byte b, ImPair<ImList<Byte>, ImList<ImCodePoint>> percentRun)
    {
        return ImPair.on(percentRun.fst.push(b), percentRun.snd);
    }

    /**
     * <p> If this codepoint is a hex digit (upper or lower case) then return the int that it represents, else
     * throw an exception.
     */
    public int hexDigitAsInt()
    {
        if (isNumeric())
            return codePoint - ImCodePoint.valueOf('0').codePoint;
        else if (isAtoF())
            return codePoint - ImCodePoint.valueOf('A').codePoint + 10;
        else if (isaTof())
            return codePoint - ImCodePoint.valueOf('a').codePoint + 10;
        else
            return Throw.Exception.ifYouGetHere(toString() + " is not a hex digit");
    }

    /**
     * <p> {@code true}
     *  if this codepoint represents a numeric ASCII character,
     * {@code false}
     *  otherwise
     */
    public boolean isNumeric()
    {
        return codePoint >= 0x30 && codePoint <= 0x39;
    }

    /**
     * `true` if this codepoint represents a numeric ASCII character, `false` otherwise
     */
    public static boolean isSurrogate(int codePoint)
    {
        return 0xD800 <= codePoint && codePoint <= 0xDFFF;
    }

    /**
     * <p> {@code true}
     *  if this codepoint represents a lowercase or uppercase ASCII letter,
     * {@code false}
     *  otherwise
     */
    public boolean isAsciiAlpha()
    {
        return isaTof() || isAtoF();
    }

    /**
     * <p> {@code true}
     *  if this codepoint represents an uppercase ASCII letter,
     * {@code false}
     *  otherwise
     */
    private boolean isAtoF()
    {
        return codePoint >= 0x41 && codePoint <= 0x46;
    }

    /**
     * <p> {@code true}
     *  if this codepoint represents a lowercase ASCII letter,
     * {@code false}
     *  otherwise
     */
    private boolean isaTof()
    {
        return codePoint >= 0x61 && codePoint <= 0x66;
    }

    /**
     * <p> {@code true}
     *  if this codepoint represents a hex digit,
     * {@code false}
     *  otherwise
     */
    public boolean isHexDigit()
    {
        return isNumeric() || isAtoF() || isaTof();
    }

    /**
     * <p> The byte that the two numbers
     * {@code hex1}
     *  and
     * {@code hex2}
     *  represent
     * when treated as a two digit hex number
     *
     */
    static byte hexDigitsToByte(int hex1, int hex2)
    {
        Throw.Exception.ifOutOfRange("hex1", hex1, 0, 15);
        Throw.Exception.ifOutOfRange("hex2", hex2, 0, 15);
        return (byte) ((hex1 << 4) + hex2);
    }

    /**
     * <p> The set of codepoints in
     * {@code cs}
     */
    public static ImSet<ImCodePoint> setOn(String cs)
    {
        return ImSet.onStream(cs.codePoints().mapToObj(ImCodePoint::on));
    }

    /**
     * <p> The set of codepoints in
     * {@code is}
     */
    public static ImSet<ImCodePoint> setOn(Integer... is)
    {
        return on(is).toImSet();
    }

    /**
     * <p> The list of codepoints in
     * {@code is}
     */
    public static ImList<ImCodePoint> on(Integer... is)
    {
        return ImList.on(is).map(i -> ImCodePoint.on(i));
    }

}
