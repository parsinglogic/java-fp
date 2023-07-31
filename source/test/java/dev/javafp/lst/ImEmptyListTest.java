package dev.javafp.lst;

import dev.javafp.ex.FunctionNotAllowedOnEmptyList;
import dev.javafp.util.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

public class ImEmptyListTest extends TestCase
{

    @Test
    public void testAppendElement()
    {
        assertEquals(ImList.on(1), ImList.on().appendElement(1));
    }

    @Test
    public void testPush()
    {
        assertEquals(ImList.on(1), ImList.on().push(1));
    }

    @Test
    public void testVarious()
    {
        ImList<Integer> e = ImList.on();

        assertEquals(e, e.drop(100));
        assertEquals(e, e.dropWhile(i -> true));

        assertEquals(e, e.take(100));
        assertEquals(e, e.takeWhile(i -> true));
        assertEquals(e, e.filter(i -> true));
        assertEquals(e, e.allPairs());

        assertEquals(true, e.all(i -> true));
        assertEquals(false, e.any(i -> true));
        assertEquals(true, ImList.and(e.upCast()));
        assertEquals(e, e.cartesianProduct());
        assertEquals(false, e.contains(1));
    }

    @Test
    public void testNotAllowed()
    {
        TestUtils.assertThrows(() -> ImList.on().at(1), FunctionNotAllowedOnEmptyList.class);
        TestUtils.assertThrows(() -> ImList.on().put(1, 1), FunctionNotAllowedOnEmptyList.class);
        TestUtils.assertThrows(() -> ImList.on().head(), FunctionNotAllowedOnEmptyList.class);
        TestUtils.assertThrows(() -> ImList.on().tail(), FunctionNotAllowedOnEmptyList.class);
        TestUtils.assertThrows(() -> ImList.on().last(), FunctionNotAllowedOnEmptyList.class);
    }

    @Test
    public void testAtWithDefault()
    {
        assertEquals(1, ImList.on().at(1, 1));
    }

    @Test
    public void testFilter()
    {
        ImList<Integer> l = ImList.on(1, 2, 3).push(0).filter(i -> i > 6);
        assertEquals(ImList.on(), l);
    }

}