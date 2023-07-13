package dev.javafp.lst;

import org.junit.Assert;
import org.junit.Test;

import static dev.javafp.lst.ImList.on;

public class ImListOnArrayTest
{

    @Test
    public void testAppendElement()
    {
        ImList<Integer> list = on(1, 2, 3);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.appendElement(4).appendElement(5);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[1, 2, 3, 4, 5]", list2.toString());

        ImList<Double> ds = ImList.on(1.2, 3.4);

        Assert.assertEquals(ImListOnArray.class, ds.getClass());

        ImList<Double> dds = ds.map(i -> i + 1);
        Assert.assertEquals(ImMappedList.class, dds.getClass());

    }

    @Test
    public void testAppendElement3()
    {
        Integer[] a = { 0, 1, 2, 3 };

        ImList<Integer> list = ImListOnArray.on(a, 1, 3);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.appendElement(4).appendElement(5);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[1, 2, 3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElement2()
    {
        Integer[] ints = { 1, 2, 3 };

        ImList<Integer> list = on(ints);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.appendElement(4).appendElement(5);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[1, 2, 3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElementAfterDrop()
    {
        ImList<Integer> list = on(1, 2, 3);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.drop(2).appendElement(4).appendElement(5);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[3, 4, 5]", list2.toString());
    }

    @Test
    public void testAppendElementAfterTake()
    {
        ImList<Integer> list = on(1, 2, 3);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.take(2).appendElement(4).appendElement(5);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[1, 2, 4, 5]", list2.toString());
    }

    @Test
    public void testDrop()
    {
        ImList<Integer> list = on(1, 2, 3);

        Assert.assertEquals(ImListOnArray.class, list.getClass());

        ImList<Integer> list2 = list.drop(2);

        Assert.assertEquals(ImListOnArray.class, list2.getClass());

        Assert.assertEquals("[3]", list2.toString());

        Assert.assertEquals(on(), list.drop(3));

        Assert.assertEquals(on(), list.drop(4));
    }

    @Test
    public void testToArray()
    {
        ImList<Integer> arrayList = ImList.on(1, 2, 3, 4);

        Assert.assertEquals(ImListOnArray.class, arrayList.getClass());

        Assert.assertSame("The array obtained by toArray should be the internal array", arrayList.toArray(Integer.class), arrayList.toArray(Integer.class));
        Assert.assertNotSame("The array obtained by toArray should not be the internal array", arrayList.toArray(Number.class), arrayList.toArray(Number.class));

        ImList<Integer> tail = arrayList.tail();

        Assert.assertNotSame("The array obtained by toArray should not be the internal array", tail.toArray(Integer.class), tail.toArray(Integer.class));

    }

    @Test
    public void testOne()
    {

        Integer[] a = { 1, 2, 3 };

        ImList<Integer> list = ImListOnArray.on(a, 0, 3);

        Assert.assertEquals("[1, 2, 3]", list.toString());

    }

    @Test
    public void testTail()
    {

        Integer[] a = { 1, 2, 3 };

        ImList<Integer> list = ImListOnArray.on(a, 0, 2).tail();

        Assert.assertEquals("[2]", list.toString());

    }

    @Test
    public void testTwo()
    {

        Integer[] a = { 1, 2, 3 };

        ImList<Integer> list = ImListOnArray.on(a, 0, 2);

        Assert.assertEquals("[1, 2]", list.toString());

    }

    @Test
    public void testEquals()
    {
        ImList<String> xs = ImList.on("a");
        ImList<String> ys = ImList.on("a");

        Assert.assertEquals(xs, xs);
        Assert.assertEquals(xs, ys);

    }

}