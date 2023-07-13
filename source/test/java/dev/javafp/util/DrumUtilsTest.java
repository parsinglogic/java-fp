package dev.javafp.util;

import junit.framework.TestCase;

public class DrumUtilsTest extends TestCase
{
    public void testEscapeXmlDoesSimpleRoundTrip() throws Exception
    {
        String s = "abcde<>&'\"fghi\n\r";
        System.out.println(DrumUtils.escapeXml(s));
        assertEquals(s, DrumUtils.unescapeXml(DrumUtils.escapeXml(s)));
    }

    public void testEscapeXmlDoesEntities() throws Exception
    {
        String s = "&apos;'&quot;\"&amp;&&lt;<&gt;>\n\r";
        assertEquals(s, DrumUtils.unescapeXml(DrumUtils.escapeXml(s)));
    }

}