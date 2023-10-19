package dev.javafp.util;

import dev.javafp.ex.MaybeHasNothing;
import dev.javafp.lst.ImList;
import junit.framework.TestCase;
import org.junit.Test;

import static dev.javafp.util.ImMaybe.just;
import static dev.javafp.util.ImMaybe.nothing;
import static dev.javafp.util.ImMaybe.with;

public class ImMaybeTest extends TestCase
{

    String thing;

    @Test
    public void testJoin()
    {
        assertEquals(ImMaybe.nothing, ImMaybe.join(ImMaybe.nothing));
        assertEquals(just(1), ImMaybe.join(just(just(1))));
    }

    @Test
    public void testFlatMap()
    {
        assertEquals(just(1), ImMaybe.just(1).flatMap(i -> with(i)));
    }

    @Test
    public void testGetAndJust()
    {
        assertEquals("a", ImMaybe.just("a").get());

        TestUtils.assertThrows(() -> nothing.get(), MaybeHasNothing.class);
    }

    @Test
    public void testNothing()
    {
        assertFalse(nothing.isPresent());
    }

    @Test
    public void tesEquals()
    {
        assertEquals(just("desserts"), just("desserts"));
        assertEquals(nothing, nothing);
    }

    @Test
    public void withNull()
    {
        assertEquals(nothing, with(null));
    }

    @Test
    public void withOkAndBad()
    {
        assertEquals(just("bar"), with("foo", "bar"));
        assertEquals(nothing, with("foo", "foo"));
    }

    @Test
    public void testIsPresent()
    {
        assertEquals(false, nothing.isPresent());
        assertEquals(true, just("william").isPresent());
    }

    @Test
    public void testIfPresentElse()
    {
        assertEquals(0, nothing.ifPresentElse(i -> i, 0));
        assertEquals(3, (int) just(2).ifPresentElse(i -> i + 1, 0));
    }

    @Test
    public void testIfPresentDo()
    {
        just("a").ifPresentDo(i -> thing = i);

        assertEquals("a", thing);

        nothing.ifPresentDo(i -> thing = "b");

        assertEquals("a", thing);

    }

    @Test
    public void testMap()
    {
        assertEquals(just(18), just(17).map(i -> i + 1));
        assertEquals(nothing, nothing.map(i -> i));
    }

    @Test
    public void testOrElse()
    {
        assertEquals(18, (int) just(18).orElse(0));
        assertEquals(0, nothing.orElse(0));
    }

    @Test
    public void testTextBox()
    {
        assertEquals("Just 18", "" + just(18));
        assertEquals("Nothing", "" + nothing);
    }

    @Test
    public void testGetNamesAndValues()
    {
        assertEquals(ImList.on("value"), just(18).getNames());
        assertEquals(ImList.on(18), just(18).getValues());
        assertEquals(ImList.on((Integer) null), nothing.getValues());

    }

}