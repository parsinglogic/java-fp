package dev.javafp.util;

import dev.javafp.ex.FlatMapFunctionReturnedNull;
import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ChatTest
{
    @Test
    public void testThrowsWhenNullIsReturned()
    {
        TestUtils.assertThrows(
                () -> Chat.Right("a").flatMap(s -> null), FlatMapFunctionReturnedNull.class);
    }

    @Test
    public void testTryCatch()
    {
        assertFalse(Chat.tryCatch(() -> 3 / 0, "oops").isOk);
        assertTrue(Chat.tryCatch(() -> 3 + 4, "oops").isOk);
    }

    @Test
    public void testCombine()
    {
        Chat<String> a = Chat.Left("a");
        Chat<String> b = Chat.Left("b");
        Chat<String> c = Chat.Right("c", "c");

        assertEquals(Chat.Left(ImList.on("a", "b", "c"), ImList.on("c")), Chat.combine(a, b, c));
        assertEquals(false, Chat.combine(a, b, c).isOk());

    }
}