package dev.javafp.lst;

import dev.javafp.util.ClassUtils;
import dev.javafp.util.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

public class ImListOnPrimitiveArrayTest extends TestCase
{

    @Test
    public void testDodgy()
    {
        TestUtils.assertThrows(() ->
                {
                    int[] ints = { 2 };
                    ImList<Boolean> is = ImList.onPrimitiveArray(ints);
                    Boolean b = is.head();
                }
                , ClassCastException.class);

    }

    @Test
    public void testDodgy2()
    {
        TestUtils.assertThrows(() ->
                {
                    int[] ints = { 2 };
                    ImList<Boolean> is = ImList.onPrimitiveArray(ints);
                    Boolean b = is.head();
                }
                , ClassCastException.class);

    }

    @Test
    public void testWrongPrimitiveClassThrows()
    {
        TestUtils.assertThrows(() -> ImListOnPrimitiveArray.on(new int[] { 2 }, Boolean.TYPE)
                , ClassCastException.class
                , "You specified component type boolean but source has component type int");

    }

    @Test
    public void testNotAnArrayThrows()
    {
        TestUtils.assertThrows(() -> ImList.onPrimitiveArray("foo")
                , ClassCastException.class
                , "source has class java.lang.String but it should be an array");

    }

    @Test
    public void testAppendElement()
    {
        int[] ints = { 2, 4, 6 };

        ImList<Integer> is = ImList.onPrimitiveArray(ints);

        assertEquals(ImList.on(2, 4, 6, 8), is.appendElement(8));
        assertEquals(ImList.on(6, 8), is.appendElement(8).drop(2));
        assertEquals(ImList.on(2, 4), is.appendElement(8).take(2));
        assertEquals(ImList.on(2, 4), is.take(2));
    }

    @Test
    public void testPush()
    {
        int[] ints = { 2, 4, 6 };

        ImList<Integer> is = ImList.onPrimitiveArray(ints);

        assertEquals("ImConsList", ClassUtils.shortClassName(is.push(Integer.valueOf(1))));
        assertEquals(ImList.on(1, 2, 4, 6), is.push(Integer.valueOf(1)));
    }

    @Test
    public void testAppendElementAfterDrop()
    {
        int[] ints = { 1, 2, 3 };

        ImList<Integer> list = ImList.onPrimitiveArray(ints);

        ImList<Integer> list2 = list.drop(2).appendElement(4).appendElement(5);

        assertEquals("[3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElementAfterDropOnBytes()
    {
        byte[] bytes = { 1, 2, 3 };

        ImList<Byte> list = ImList.onPrimitiveArray(bytes);

        ImList<Byte> list2 = list.drop(2).appendElement((byte) 4).appendElement((byte) 5);

        assertEquals("[3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElementAfterDropOnChars()
    {
        char[] chars = { 'a', 'b', 'c' };

        ImList<Character> list = ImList.onPrimitiveArray(chars);
        assertEquals("[a, b, c]", list.toString());

        ImList<Character> list2 = list.drop(1).appendElement('d').appendElement('e');

        assertEquals("[b, c, d, e]", list2.toString());
    }

    @Test
    public void testAppendElementAfterDropOnBooleans()
    {
        boolean[] bools = { false, false, true };

        ImList<Boolean> list = ImList.onPrimitiveArray(bools);
        assertEquals("[false, false, true]", list.toString());

        ImList<Boolean> list2 = list.drop(1).appendElement(true).appendElement(false);

        assertEquals("[false, true, true, false]", list2.toString());
    }

}