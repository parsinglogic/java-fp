package dev.javafp.ex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static dev.javafp.util.TestUtils.failExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ThrowArgumentExceptionTest
{

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

    public void testIfNullOrEmptyDoesNotThrowWhenNotNullOrEmpty() throws Exception
    {
        Throw.Exception.ifNullOrEmpty("wibble", "wobble");
    }

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

    public void testIfTrue() throws Exception
    {

        Throw.Exception.ifTrue(false, "");
        try
        {
            Throw.Exception.ifTrue(true, "wibble");
            failExpectedException(IllegalState.class);
        } catch (IllegalState e)
        {
            assertTrue(e.getMessage().contains("wibble"));
        }
    }

    public void testIfFalse() throws Exception
    {

        Throw.Exception.ifFalse(true, "");
        try
        {
            Throw.Exception.ifFalse(false, "wibble");
            failExpectedException(IllegalState.class);
        } catch (IllegalState e)
        {
            assertTrue(e.getMessage().contains("wibble"));
        }
    }

    public void testIfEmptyDoesNotThrowWhenNotEmpty() throws Exception
    {
        Throw.Exception.ifEmpty("wibble", "wobble");
    }

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

    public void testIfOutOfRangeThrowsIfMinIsGreaterThanMax() throws Exception
    {

        try
        {
            Throw.Exception.ifOutOfRange("foo", 0, 6, 5);
            failExpectedException(UnexpectedChecked.class);
        } catch (IllegalState e)
        {
        }
    }

    public void testIfIndexNotInCollection() throws Exception
    {

        List<String> foos = new ArrayList<String>();

        try
        {
            Throw.Exception.ifIndexNotInCollection(-1, foos, "foos");
            failExpectedException(InvalidCollectionIndex.class);
        } catch (InvalidCollectionIndex e)
        {
            assertEquals("You tried to access index -1 of foos but index should be >= 0", e.getMessage());
        }

        try
        {
            Throw.Exception.ifIndexNotInCollection(1, foos, "foos");
            failExpectedException(InvalidCollectionIndex.class);
        } catch (InvalidCollectionIndex e)
        {
            assertEquals("You tried to access index 1 of foos but there are none", e.getMessage());
        }

        try
        {
            foos.add("bar");
            Throw.Exception.ifIndexNotInCollection(4, foos, "foos");
            failExpectedException(InvalidCollectionIndex.class);
        } catch (InvalidCollectionIndex e)
        {
            assertEquals("You tried to access index 4 of foos but the largest valid index is 1", e.getMessage());
        }
    }

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

    public void testIfNullDoesNotThrowWhenNotNull() throws Exception
    {
        Throw.Exception.ifNull("wibble", "wobble");
    }

    //    public void testIfNotTypeThrowsWhenWrongType() throws Exception
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

    //    public void testIfNotTypeDoesNotThrowWhenRightType() throws Exception
    //    {
    //        ThrowArgument.Exception.ifNotType(new Integer(0), Number.class);
    //    }
}