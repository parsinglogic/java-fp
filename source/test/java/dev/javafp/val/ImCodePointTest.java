package dev.javafp.val;

import dev.javafp.ex.InvalidState;
import dev.javafp.lst.ImList;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ImCodePointTest
{

    String poo = "💩";
    String wrong = "\uFFFF";
    char wrongChar = '\uFFFF';

    @Test
    public void testWrong()
    {
        say("wrong", wrong);

        say("size", wrong.length());
        say("isDefined", Character.isDefined(wrongChar));
        say("isValid", Character.isValidCodePoint(wrongChar));

        ImCodePoint.on(0xffff);
    }

    @Test
    public void testGetCodePointArray()
    {

        // räksmörgås
        // ♥

        say(poo.codePointAt(0)); // 128169 "\uD83D\uDCA9"

        ImCodePoint[] uu = { ImCodePoint.on(0x2665), ImCodePoint.valueOf('a'), ImCodePoint.on(0x2665) };
        ImCodePoint[] pooArray = { ImCodePoint.on(128169), ImCodePoint.on(128169) };
        ImCodePoint[] pooArray2 = { ImCodePoint.on(128169), ImCodePoint.on(0x2665), ImCodePoint.valueOf('a'), ImCodePoint.on(0x2665), ImCodePoint.on(128169),
                ImCodePoint.valueOf('b') };

        assertEquals(0, ImCodePoint.getCodePointArray("").length);
        assertArrayEquals(new ImCodePoint[] { ImCodePoint.valueOf('a') }, ImCodePoint.getCodePointArray("a"));

        assertArrayEquals(uu, ImCodePoint.getCodePointArray("♥a♥"));
        assertArrayEquals(pooArray, ImCodePoint.getCodePointArray("💩💩"));
        assertArrayEquals(pooArray2, ImCodePoint.getCodePointArray("💩♥a♥💩b"));

    }

    @Test
    public void testDecodePercents()
    {
        assertEquals("♥", ImCodePoint.decodePercents(ImList.onString("%E2%99%A5")).toString(""));
        assertEquals("", ImCodePoint.decodePercents(ImList.onString("")).toString(""));
        assertEquals("123%XYZ", ImCodePoint.decodePercents(ImList.onString("123%XYZ")).toString(""));
        assertEquals("123%XYZ", ImCodePoint.decodePercents(ImList.onString("123%25XYZ")).toString(""));
        assertEquals("%%", ImCodePoint.decodePercents(ImList.onString("%%")).toString(""));
        assertEquals("👨‍🦲", ImCodePoint.decodePercents(ImList.onString("%F0%9F%91%A8%E2%80%8D%F0%9F%A6%B2")).toString(""));
        assertEquals(":;=@[]^|¥/?@", ImCodePoint.decodePercents(ImList.onString("%3A%3B%3D%40%5B%5D%5E%7C%C2%A5%2F%3F%40")).toString(""));
        assertEquals("�", ImCodePoint.decodePercents(ImList.onString("%ED%BF%BF")).toString(""));
    }

    @Test
    public void testIsHexDigit()
    {
        ImList<ImCodePoint> yes = ImList.onString("0123456789abcdefABCDEF");

        yes.foreach(i -> i.isHexDigit());
    }

    @Test
    public void testHexDigitsToHex()
    {
        assertEquals(0, ImCodePoint.valueOf('0').hexDigitAsInt());
        assertEquals(0xFF, ImCodePoint.hexDigitsToByte(0xF, 0xF) & 0xFF);
        assertEquals(0x33, ImCodePoint.hexDigitsToByte(0x3, 0x3) & 0xFF);
        assertEquals(0xE2, ImCodePoint.hexDigitsToByte(0xE, 0x2) & 0xFF);
    }

    @Test
    public void testOnThrowsWhenCodePointIsInvalid()
    {

        //  Surrogates are D800–DBFF

        TestUtils.assertThrows(() -> ImCodePoint.on(0xD800), InvalidState.class);
        TestUtils.assertThrows(() -> ImCodePoint.on(0xDFFF), InvalidState.class);
        TestUtils.assertThrows(() -> ImCodePoint.on(Integer.MAX_VALUE), InvalidState.class);
        TestUtils.assertThrows(() -> ImCodePoint.on(Integer.MIN_VALUE), InvalidState.class);
    }

    @Test
    public void testToHexFormatString()
    {
        assertEquals("0x41", ImCodePoint.valueOf('A').toHexString());
    }

}