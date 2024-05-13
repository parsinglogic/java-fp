package dev.javafp.eq;

import dev.javafp.ex.InvalidState;
import dev.javafp.lst.ImList;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EqualsTest
{

    @Test
    public void oddArguments()
    {
        try
        {
            Equals.isEqualPairwise(1, 2, 3);
            failExpectedException(InvalidState.class);
        } catch (InvalidState e)
        {
        }
    }

    @Test
    public void testDifferentClasses()
    {
        assertFalse(Equals.isEqual(1, "foo"));

    }

    @Test
    public void testDifferentLstTypes()
    {

        ImList<Integer> lst1 = ImList.on(1, 2, 3);
        ImList<Integer> lst2 = ImList.on(1, 2).appendElement(3);

        assertTrue(Equals.isEqual(lst1, lst2));

    }

    @Test
    public void zeroArguments()
    {
        Equals.isEqualPairwise();
    }

    @Test
    public void nullArguments()
    {
        assertTrue(Equals.isEqual(null, null));
        assertFalse(Equals.isEqual(1, null));
        assertFalse(Equals.isEqual(null, "foo"));
        assertTrue(Equals.isEqual("foo", "foo"));
        assertFalse(Equals.isEqual("foo", "bar"));

    }

    @Test
    public void arrayArguments()
    {
        int[] one = { 1, 2 };
        int[] two = { 1, 2 };
        assertTrue(Equals.isEqual(one, two));

    }

    @Test
    public void pairwiseArguments()
    {
        assertFalse(Equals.isEqualPairwise(1, null, null, "foo"));
        assertTrue(Equals.isEqualPairwise(null, null, "foo", "foo"));
    }

    /**
     * Fails, displaying a message
     * <code>Expected an exception of type Foo.Bar</code>
     * where Foo.bar is the class of the exception specified.
     */
    public void failExpectedException(Class<?> classExpected)
    {
        Assert.fail("Expected an exception of type " + classExpected);
    }

}