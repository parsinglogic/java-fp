package dev.javafp.util;

import org.junit.Test;

public class DummiesTest
{

    @Test
    public void testOne()
    {
        new Bang().a();
        new Bang().b(); // it says Foo.a - but it should say Bing.a
        new Bang().d();
    }
}