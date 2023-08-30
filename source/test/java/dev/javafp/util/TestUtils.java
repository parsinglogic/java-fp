/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.eq.Eq;
import dev.javafp.eq.Equals;
import dev.javafp.func.Fn2;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImTriple;

import java.util.Collection;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p> Because we can't remove this class easily, we have renamed it.
 * There is another version of it in jadle-test-helper but this module can't use it
 *
 */
public class TestUtils
{
    // For double tests
    protected final static double delta = 0.00000000000001;

    /**
     * <p> Fails, displaying a message
     * <code>
     * Expected an exception of type Foo.Bar
     * </code>
     * where Foo.bar is the class of the exception specified.
     *
     */
    public static void failExpectedException(Class<?> classExpected)
    {
        fail("Expected an exception of type " + classExpected + " but no exception was thrown");
    }

    public static void assertStringsEqual2(Object expected, Object actual)
    {
        assertEq(toString(expected), toString(actual));
    }

    public static void assertSetsEqual(String message, ImList<?> expected, ImList<?> actual)
    {
        assertSetsEqual(message, expected.toList(), actual.toList());
    }

    public static void assertSetsEqual(ImList<?> expected, ImList<?> actual)
    {
        assertSetsEqual("", expected, actual);
    }

    public static void assertToStringEquals(String expectedString, Object actual)
    {
        assertEq(expectedString, "" + actual);
    }

    public static void assertSetsEqual(String message, Collection<?> expected, Collection<?> actual)
    {
        String s = Equals.getDifferences(expected, actual);

        if (s.length() > 0)
        {
            String m = message.isEmpty() ? "" : message + "\n";
            fail(m + "Differences:\n" + s);
        }
    }

    public static void assertSameElements(ImList<?> expected, ImList<?> actual)
    {
        assertCmpElements(expected, actual, (i, j) -> i == j);
    }

    public static void assertEqualElements(ImList<?> expected, ImList<?> actual)
    {
        assertCmpElements(expected, actual, Equals::isEqual);
    }

    public static void assertCmpElements(ImList<?> expected, ImList<?> actual, Fn2<Object, Object, Boolean> cmp)
    {
        if (expected.size() != actual.size())
            fail(String.format("Sizes different - expected: %d but was: %d", expected.size(), actual.size()));

        ImMaybe<? extends ImTriple<?, ?, Integer>> tripleMaybe =
                ImTriple.zip(expected, actual, ImRange.oneTo(expected.size())).find(p -> !cmp.of(p.e1, p.e2));

        if (tripleMaybe.isPresent())
        {
            ImTriple<?, ?, Integer> t = tripleMaybe.get();
            fail(String.format("First difference is at element #%d where expected: %s but was: %s", t.e3, t.e1, t.e2));
        }
    }

    public static void show(String string, Object things)
    {
        System.out.println(string);
        System.out.println(toString(things));
    }

    public static String toString(Object things)
    {
        return things instanceof ImList<?>
               ? TextUtils.showCollection(((ImList<?>) things).toList())
               : "" + things;
    }

    public static void assertThrows(Runnable f, Class<? extends Exception> expected)
    {
        assertThrows(f, expected, null);
    }

    public static void assertThrows(Runnable f, Class<?> expectedEx, String expectedMessage)
    {
        // Hmm - this is a bit tricky to get the right exception to throw
        boolean noThrow = false;

        try
        {
            f.run();
            noThrow = true;
        } catch (Exception e)
        {
            assertEq("" + expectedEx, "" + e.getClass());
            if (expectedMessage != null)
            {
                assertEq(expectedMessage, e.getMessage());
            }
        }

        if (noThrow)
            TestUtils.failExpectedException(expectedEx);

    }

    private static void fail(String s)
    {
        throw new AssertionError(s);
    }

    public static void assertEq(int one, int two)
    {
        if (one != two)
            fail("expected: " + one + " but was: " + two); //  expected:<bla-blah> but was:<blah-blah-blah>
    }

    /**
     * <p> A version of assertEquals that uses Eq:uals()
     */
    public static void assertEq(Object one, Object two)
    {
        if (!Eq.uals(one, two))
            fail("expected: " + one + " but was: " + two);
    }

    /**
     * <p> A version of assertEquals that uses Eq:uals()
     */
    public static void assertEq(String message, Object one, Object two)
    {
        if (!Eq.uals(one, two))
            fail(message + one + " but was: " + two);
    }

    public static void assertEQ(String one, double oneVal, String two, double twoVal)
    {
        if (!(abs(oneVal - twoVal) < delta))
        {
            String ppOne = TextUtils.prettyPrint(oneVal);
            String ppTwo = TextUtils.prettyPrint(twoVal);
            fail(String.format("Expected %s = %s but %s != %s", one, two, ppOne, ppTwo));
        }
    }

    public static void assertLE(String one, double oneVal, String two, double twoVal)
    {
        if (!(oneVal <= twoVal + delta))
        {
            String ppOne = TextUtils.prettyPrint(oneVal);
            String ppTwo = TextUtils.prettyPrint(twoVal);
            fail(String.format("Expected %s <= %s but %s > %s", one, two, ppOne, ppTwo));
        }
    }

    public static void assertStringEquals(Object s, Object thing)
    {
        assertEq("" + s, "" + thing);
    }

    public static void assertStringEquals(String message, Object s, Object gt)
    {
        assertEq(message, "" + s, "" + gt);
    }

    public static void checkThings(ImList<Object> thingOnes, ImList<Object> thingTwos)
    {
        // Null test
        for (int i : ImRange.oneTo(thingOnes.size()))
        {
            assertFalse("" + thingOnes.at(i), thingOnes.at(i).equals(null));
        }

        // Identical things should be equal
        for (int i : ImRange.oneTo(thingOnes.size()))
        {
            assertTrue("" + thingOnes.at(i), thingOnes.at(i).equals(thingOnes.at(i)));
        }

        // The only things that are equal are the ones at the same index
        for (int i : ImRange.oneTo(thingOnes.size()))
        {
            for (int j : ImRange.oneTo(thingOnes.size()))
            {
                assertEquals(thingOnes.at(i) + " vs " + thingTwos.at(i), i == j, thingOnes.at(i).equals(thingTwos.at(j)));
                assertEquals(thingTwos.at(j) + " vs " + thingOnes.at(i), i == j, thingTwos.at(j).equals(thingOnes.at(i)));
            }
        }
    }
}