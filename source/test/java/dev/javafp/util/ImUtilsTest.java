package dev.javafp.util;

import junit.framework.TestCase;

public class ImUtilsTest extends TestCase
{
    public void testEscapeXmlDoesSimpleRoundTrip()
    {
        String s = "abcde<>&'\"fghi\n\r";
        System.out.println(ImUtils.escapeXml(s));
        assertEquals(s, ImUtils.unescapeXml(ImUtils.escapeXml(s)));
    }

    public void testEscapeXmlDoesEntities()
    {
        String s = "&apos;'&quot;\"&amp;&&lt;<&gt;>\n\r";
        assertEquals(s, ImUtils.unescapeXml(ImUtils.escapeXml(s)));
    }

}