package dev.javafp.util;

import org.junit.Assert;

import static org.junit.Assert.assertEquals;

public class PipeTest
{

    /**
     * Fails, displaying a message
     * <code>Expected an exception of type Foo.Bar</code>
     * where Foo.bar is the class of the exception specified.
     */
    public void failExpectedException(Class<?> classExpected)
    {
        Assert.fail("Expected an exception of type " + classExpected);
    }

    public void testOne()
    {
        Pipe pipe = new Pipe();

        Sink sink = pipe.getSink();

        Source source = pipe.getSource();

        String expected = "foo";
        sink.write(expected);
        assertEquals(expected, source.read());

        expected = "bar";
        sink.write(expected);
        assertEquals(expected, source.read());

        source.setTimeout(1);
        try
        {
            source.read();
            failExpectedException(RuntimeException.class);
        } catch (RuntimeException e)
        {
            // ok
        }

    }
}