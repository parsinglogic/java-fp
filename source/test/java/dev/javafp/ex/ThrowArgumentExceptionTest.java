package dev.javafp.ex;

import dev.javafp.lst.ImList;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.javafp.util.TestUtils.failExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ThrowArgumentExceptionTest
{

    @Test
    public void testIfNullOrEmptyThrowsWhenEmpty() throws Exception
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
    public void testIfNullOrEmptyThrowsWhenNull() throws Exception
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
    public void testIfNullOrEmptyDoesNotThrowWhenNotNullOrEmpty() throws Exception
    {
        Throw.Exception.ifNullOrEmpty("wibble", "wobble");
    }

    @Test
    public void testIfEmptyThrowsWhenEmpty() throws Exception
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
    public void testIfTrue() throws Exception
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
    public void testIfFalse() throws Exception
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
    public void testIfEmptyDoesNotThrowWhenNotEmpty() throws Exception
    {
        Throw.Exception.ifEmpty("wibble", "wobble");
    }

    @Test
    public void testIfNullThrowsWhenNull() throws Exception
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
    public void testIfOutOfRange1() throws Exception
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
    public void testIfOutOfRangeThrowsIfMinIsGreaterThanMax() throws Exception
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
    public void testIfIndexNotInCollection() throws Exception
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
    public void testIfOutOfRange2() throws Exception
    {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");

        Throw.Exception.ifOutOfRange("foo", 0, list);
        Throw.Exception.ifOutOfRange("foo", 1, list);

        try
        {
            Throw.Exception.ifOutOfRange("foo", 9, list);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }

        try
        {
            Throw.Exception.ifOutOfRange("foo", -1, list);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }
    }

    @Test
    public void testLessThan() throws Exception
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
    public void testIfOutOfRangeIncludingMinusOne() throws Exception
    {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");

        Throw.Exception.ifOutOfRangeIncludingMinusOne("foo", -1, list);
        Throw.Exception.ifOutOfRangeIncludingMinusOne("foo", 0, list);
        Throw.Exception.ifOutOfRangeIncludingMinusOne("foo", 0, list);
        try
        {
            Throw.Exception.ifOutOfRangeIncludingMinusOne("foo", -2, list);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }

        try
        {
            Throw.Exception.ifOutOfRangeIncludingMinusOne("foo", 2, list);
            failExpectedException(ArgumentOutOfRange.class);
        } catch (ArgumentOutOfRange e)
        {
        }

    }

    @Test
    public void testIfNullDoesNotThrowWhenNotNull() throws Exception
    {
        Throw.Exception.ifNull("wibble", "wobble");
    }

    //    @Test
    //public void testIfNotTypeThrowsWhenWrongType() throws Exception
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
    //public void testIfNotTypeDoesNotThrowWhenRightType() throws Exception
    //    {
    //        ThrowArgument.Exception.ifNotType(new Integer(0), Number.class);
    //    }
}