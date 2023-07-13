package dev.javafp.util;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * This is to help me understand how Java works in various area
 */

public class GeneralJavaTests
{

    @Test
    public void testOne()
    {

        ImList<Integer> l = ImList.on(1);
        foo(l);
    }

    @Test
    public void testJavaToStringConventions()
    {
        List<String> ss = new ArrayList();
        assertEquals("[]", ss.toString());

        ss.add("a");
        ss.add("b");
        assertEquals("[a, b]", ss.toString());

        assertEquals(Object.class, ss.toArray().getClass().getComponentType());

        List<Double> ds = new ArrayList();
        ds.add(1.234);
        assertEquals("[1.234]", ds.toString());

        List<Character> cs = new ArrayList();
        cs.add('a');
        assertEquals("[a]", cs.toString());

        Set<Character> sc = new HashSet();
        sc.add('a');
        sc.add('b');
        assertEquals("[a, b]", sc.toString());

        String[] sa = { "a" };

        // No useful toString on arrays
        assertTrue(sa.toString().startsWith("[Ljava.lang.String;"));

    }

    @Test
    public void testJavaListEqualsConventions()
    {
        List<String> ss = new ArrayList();
        List<? extends String> ss2 = ss;
        List<?> ss3 = ss;

        Collection<String> ss4 = ss;
        Collection<? extends String> ss5 = ss;

        ss.add("a");
        ss.add("b");

        Set<Character> sc = new HashSet();
        sc.add('a');
        sc.add('b');

        // Sets and Lists are not equal
        assertNotEquals(ss, sc);

        //  Different list class objects can be equal
        List<String> ls = new LinkedList();

        ls.add("a");
        ls.add("b");

        assertEquals(ls, ss);

    }

    @Test
    public void testQuestions()
    {
        List<A> ss = new ArrayList();
        List<? extends A> ss2 = ss;
        List<?> ss3 = ss;

        Collection<A> ss4 = ss;
        Collection<? extends A> ss5 = ss;
    }

    @Test
    public void testAssignments()
    {
        var l = ImList.on(1);
        Say.say(ClassUtils.shortClassName(l));
    }

    @Test
    public void testFinal()
    {
        final String s = "";

        s.contains("aaa");

        // s = "";
    }

    private <T> void foo(ImList<? extends Number> l)
    {
        Number head = l.head();

        // l.appendElement(Integer.valueOf(1));
    }

    @Test
    public void testThingie()
    {
        new B().foo();
    }

    interface A
    {
        default void foo()
        {
            Say.say("foo");
        }
    }

    class B implements A
    {

    }

    class C extends B
    {
        public void foo()
        {
            super.foo();
        }
    }

    @Test
    public void addOnSetDoesNotReplaceElements()
    {
        ImPair<Integer, Integer> p1 = Pai.r(1, 2);
        ImPair<Integer, Integer> p2 = Pai.r(1, 2);
        assertEquals(p1, p2);
        assertNotSame(p1, p2);

        Set<ImPair<Integer, Integer>> oldSet = new HashSet<>();

        oldSet.add(p1);

        // Get the point
        ImPair<Integer, Integer> p = oldSet.iterator().next();

        // It is p1
        assertSame(p1, p);

        // Add p2 - will it replace p1
        oldSet.add(p2);

        // Get the point
        ImPair<Integer, Integer> pp = oldSet.iterator().next();

        // p2 has *not* been added. It is still p1
        assertSame(p1, pp);

        // but if we remove it and add p2
        oldSet.remove(p1);
        oldSet.add(p2);

        // Now it is p2
        ImPair<Integer, Integer> ppp = oldSet.iterator().next();
        assertSame(p2, ppp);

    }

}