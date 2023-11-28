package dev.javafp.ex;

import dev.javafp.lst.ImList;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.Collection;

import static dev.javafp.util.TestUtils.failExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ThrowArgumentExceptionTest
{

    @Test
    public void testIfNullOrEmptyThrowsWhenEmpty()
    {
        try
        {
            Throw.Exception.ifNullOrEmpty("wibble", "");
            failExpectedException(EmptyString.class);
        } catch (EmptyString e)
        {
            assertEquals("wibble", e.getMessage());
        }
    }

    @Test
    public void testIfNullOrEmptyThrowsWhenNull()
    {
        try
        {
            Throw.Exception.ifNullOrEmpty("wibble", (Collection<?>) null);
            failExpectedException(NullValue.class);
        } catch (NullValue e)
        {
            assertEquals("wibble", e.getMessage());
        }
    }

    @Test
    public void testIfNullOrEmptyDoesNotThrowWhenNotNullOrEmpty()
    {
        Throw.Exception.ifNullOrEmpty("wibble", "wobble");
    }

    @Test
    public void testIfEmptyThrowsWhenEmpty()
    {
        try
        {
            Throw.Exception.ifEmpty("wibble", "");
            failExpectedException(EmptyString.class);
        } catch (EmptyString e)
        {
            assertEquals("wibble", e.getMessage());
        }
    }

    @Test
    public void testIfTrue()
    {

        Throw.Exception.ifTrue(false, "");
        try
        {
            Throw.Exception.ifTrue(true, "wibble");
            failExpectedException(InvalidState.class);
        } catch (InvalidState e)
        {
            assertTrue(e.getMessage().contains("wibble"));
        }
    }

    @Test
    public void testIfFalse()
    {

        Throw.Exception.ifFalse(true, "");
        try
        {
            Throw.Exception.ifFalse(false, "wibble");
            failExpectedException(InvalidState.class);
        } catch (InvalidState e)
        {
            assertTrue(e.getMessage().contains("wibble"));
        }
    }

    @Test
    public void testIfEmptyDoesNotThrowWhenNotEmpty()
    {
        Throw.Exception.ifEmpty("wibble", "wobble");
    }

    @Test
    public void testIfNullThrowsWhenNull()
    {
        try
        {
            Throw.Exception.ifNull("wibble", null);
            failExpectedException(NullValue.class);
        } catch (NullValue e)
        {
            assertEquals("wibble", e.getMessage());
        }
    }

    @Test
    public void testIfOutOfRange1()
    {
        Throw.Exception.ifOutOfRange("foo", 37, 37, 37);
        Throw.Exception.ifOutOfRange("foo", 0, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);

        try
        {
            Throw.Exception.ifOutOfRange("foo", Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }

        try
        {
            Throw.Exception.ifOutOfRange("foo", Integer.MAX_VALUE, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }
    }

    @Test
    public void testIfOutOfRangeThrowsIfMinIsGreaterThanMax()
    {

        try
        {
            Throw.Exception.ifOutOfRange("foo", 0, 6, 5);
            failExpectedException(UnexpectedChecked.class);
        } catch (InvalidState e)
        {
        }
    }

    @Test
    public void testIfIndexNotInCollection()
    {
        final ImList<String> foos = ImList.on();

        TestUtils.assertThrows(
                () -> Throw.Exception.ifIndexOutOfBounds("index", -1, "foos", foos.size()),
                ImIndexOutOfBounds.class,
                "Index index of foos should be >= 1 but was -1");

        TestUtils.assertThrows(
                () -> Throw.Exception.ifIndexOutOfBounds("index", 1, "foos", foos.size()),
                ImIndexOutOfBounds.class,
                "foos is empty but index index was 1");

        final ImList<String> foos2 = ImList.on("bar");
        TestUtils.assertThrows(
                () -> Throw.Exception.ifIndexOutOfBounds("index", 4, "foos2", foos2.size()),
                ImIndexOutOfBounds.class,
                "The size of foos2 is 1 but index index was 4");
    }

    @Test
    public void testLessThan()
    {
        Throw.Exception.ifLessThan("foo", 0, 0);
        Throw.Exception.ifLessThan("foo", 1, -1);

        try
        {
            Throw.Exception.ifLessThan("foo", 9, 10);
            failExpectedException(ArgumentShouldNotBeLessThan.class);
        } catch (ArgumentShouldNotBeLessThan e)
        {
        }
    }

    @Test
    public void testIfNullDoesNotThrowWhenNotNull()
    {
        Throw.Exception.ifNull("wibble", "wobble");
    }

    //    @Test
    //public void testIfNotTypeThrowsWhenWrongType()
    //    {
    //        try
    //        {
    //            ThrowArgument.Exception.ifNotType(new Integer(0), String.class);
    //            failExpectedException(IllegalArgumentClass.class);
    //        }
    //        catch (IllegalArgumentClass e)
    //        {
    //            assertEquals(
    //                    "Expecting argument of type class java.lang.String but got an object of type class java.lang.Integer",
    //                    e.getMessage());
    //        }
    //    }

    //    @Test
    //public void testIfNotTypeDoesNotThrowWhenRightType()
    //    {
    //        ThrowArgument.Exception.ifNotType(new Integer(0), Number.class);
    //    }
}