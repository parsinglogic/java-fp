package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static dev.javafp.lst.ImList.on;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class TestUtilsTest
{

    @Test
    public void testAssertSetsEqual()
    {
        ImList<Integer> one = on(1, 2, 3);
        ImList<Integer> two = on(3, 2, 1);

        TestUtils.assertSetsEqual("", one, two);
    }

    /**
     * These tests show that IJ shows a "CLick to see difference" link.
     * No point in running them as part of automated tests
     */
    @Test
    public void testAssertSimple()
    {
        //        // TestUtils.assertEquals(1, 2);
        //        TestUtils.assertEquals("one", "two");
    }

    @Test
    public void testAssertSameElementsThrowsWhenDifferent2()
    {
        //        ImList<Integer> one = on(1, 2, 3, 4);
        //        ImList<Integer> two = on(1, 2, 5, 4);
        //
        //        TestUtils.assertSameElements(one, two);
        //
        //        TestUtils.failExpectedException(AssertionError.class);
    }

    @Test
    public void testAssertEqualElementsThrowsWhenDifferent()
    {
        //        ImList<Integer> one = on(1, 2, 3, 4);
        //        ImList<Integer> two = on(1, 2, 5, 4);
        //
        //        TestUtils.assertEqualElements(one, two);
        //
        //        TestUtils.failExpectedException(AssertionError.class);
    }

    @Test
    public void testAssertSetsEqualThrows()
    {
        try
        {
            ImList<Integer> one = on(1, 2, 3);
            ImList<Integer> two = on(3, 2, 0);

            say("start");
            TestUtils.assertSetsEqual("", one, two);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {

            say("error is", e);
            assertEquals("Differences:\n"
                    + "present in expected but not in actual\n"
                    + "1\n"
                    + "present in actual but not in expected\n"
                    + "0\n", e.getMessage());
        }

        say("at end");
    }

    @Test
    public void testAssertDoublesEqualThrows()
    {
        try
        {
            TestUtils.assertEQ("one", 1, "two", 2);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {
            assertEquals("Expected one = two but 1 != 2", e.getMessage());
        }
    }

    @Test
    public void testAssertDoublesEqualThrowsWithDelta()
    {
        try
        {
            TestUtils.assertEQ("one", 1, "one", 1 + TestUtils.delta * 2);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {
            assertEquals("Expected one = one but 1 != 1.00000000000002", e.getMessage());
        }
    }

    @Test
    public void testAssertDoublesLEThrows()
    {
        try
        {
            TestUtils.assertLE("a", 1 + TestUtils.delta * 2, "b", 1);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {
            assertEquals("Expected a <= b but 1.00000000000002 > 1", e.getMessage());
        }
    }

    @Test
    public void testAssertSameElementsThrowsWhenDifferent()
    {

        String ne = "ne";

        try
        {
            // Let's defeat the compiler's attempt to intern strings
            ImList<String> one = on("zero", "one");
            ImList<String> two = on("zero", "o" + ne);

            // These are equal...
            TestUtils.assertEqualElements(one, two);

            // ... but not the same
            TestUtils.assertSameElements(one, two);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {
            assertEquals("First difference is at element #2 where expected: one but was: one", e.getMessage());
        }
    }

    @Test
    public void testAssertSameElementsThrowsWhenDifferentSizes()
    {
        try
        {
            ImList<Integer> one = on(1, 2, 3, 4);
            ImList<Integer> two = on(1, 2, 4);

            TestUtils.assertSameElements(one, two);

            TestUtils.failExpectedException(AssertionError.class);

        } catch (AssertionError e)
        {
            assertEquals("Sizes different - expected: 4 but was: 3", e.getMessage());
        }

    }

    @Test
    public void testExpectException()
    {
        TestUtils.assertThrows(() -> {
            throw new RuntimeException();
        }, RuntimeException.class);

    }

}