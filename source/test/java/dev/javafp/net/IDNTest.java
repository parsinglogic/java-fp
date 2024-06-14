package dev.javafp.net;

import org.junit.Test;

import java.net.IDN;

import static org.junit.Assert.assertEquals;

public class IDNTest
{

    @Test
    public void testOne()
    {
        assertEquals("xn--lzg", IDN.toASCII("€"));
        assertEquals("xn--xn--lzg-s17c.xn--lzg.xn--lzg.", IDN.toASCII("€xn--lzg.xn--lzg.xn--lzg."));
        //        assertEquals("xn--xn--lzg-s17c.xn--lzg.xn--lzg.a", IDN.toASCII("€xn--lzg.xn--lzg.xn--lzg.%41"));
        //        assertEquals("xn--xn--lzg-s17c.a.xn--lzg.xn--lzg", IDN.toASCII("€xn--lzg.%41.xn--lzg.xn--lzg"));
        assertEquals("xn--lzag", IDN.toASCII("xn--lzag"));
        assertEquals("", IDN.toASCII("%E2%99%A5"));
    }

}
