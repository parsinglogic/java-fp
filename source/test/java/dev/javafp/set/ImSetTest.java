package dev.javafp.set;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import dev.javafp.util.Caster;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TestUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static dev.javafp.set.ImSet.onArray;
import static dev.javafp.util.ImTestHelper.checkExample;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class ImSetTest
{
    static class DodgyHashCode
    {
        int i;

        public DodgyHashCode(int i)
        {
            this.i = i;
        }

        @Override
        public int hashCode()
        {
            return 17;
        }
    }

    @Test
    public void testSizeDoesNotIncrease()
    {
        ImSet<Integer> s = onArray(Integer.valueOf(1));

        assertEquals(1, s.size());
        assertEquals(1, s.add(Integer.valueOf(1)).size());
    }

    @Test
    public void testAddingNullThrows()
    {
        try
        {
            onArray((Integer) null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testIterator()
    {
        ImSet<Integer> s = onArray(1, 2, 3, 4, 5);

        System.out.println(s);

        StringBuilder sb = new StringBuilder();
        for (Integer i : s)
        {
            sb.append(i);
        }

        assertEquals("12345", sb.toString());
    }

    @Test
    public void testToString()
    {
        ImSet<Integer> s = onArray(1, 2, 3, 4, 5);

        assertEquals("[1, 2, 3, 4, 5]", s.toString());
    }

    @Test
    public void testAddWorksWithReplace()
    {
        ImPair<Integer, Integer> p1 = Pai.r(1, 2);
        ImPair<Integer, Integer> p2 = Pai.r(1, 2);
        assertEquals(p1, p2);
        assertNotSame(p1, p2);

        ImSet<ImPair<Integer, Integer>> oldSet = ImSet.on(p1);

        assertNotSame(p2, oldSet.find(p1).get());

        ImSet<ImPair<Integer, Integer>> newSet = oldSet.add(p2, ImSet.Replace.yes);

        assertSame(p2, newSet.find(p1).get());
    }

    @Test
    public void testGetTextBox()
    {
        ImSet<Integer> three = ImSet.on(1, 2, 3);

        assertEquals(3, three.size());

        say(three.anyElement().get().getClass());

        say(ImList.on(three).getClass());
        say(ImList.onAll(three).getClass());

        three.getTextBox();
    }

    @Test
    public void testFindLarge()
    {
        int count = 33;

        ImSet<String> s = ImSet.empty();
        for (int i = 32; i < 32 + count; i++)
        {
            String newElement = "" + (char) i;
            s = s.add(newElement);
            System.err.println("adding " + newElement);
        }

        System.err.println(s);

        System.err.println(s.getStats());

        int iteratorCount = 0;

        Iterator<String> it = s.iterator();

        while (it.hasNext())
        {
            String element = it.next();

            if (element == null)
            {
                System.err.println("oops");
            }
            iteratorCount++;
        }

        assertEquals(count, iteratorCount);

        assertEquals(count, s.size());
        for (int i = 32; i < 32 + count; i++)
        {
            String newElement = "" + (char) i;
            assertEquals("missing " + newElement, true, s.contains(newElement));
        }
    }

    @Test
    public void testFindDodgyHashCodes()
    {

        ImSet<DodgyHashCode> s = ImSet.<DodgyHashCode>empty();
        for (int i = 1; i <= 20000; i++)
        {
            s = s.add(new DodgyHashCode(i));
        }

        System.err.println(s.getStats());
    }

    @Test
    public void testFind()
    {
        ImSet<Integer> s = onArray(1, 2, 3, 4);

        for (int i = 1; i <= 4; i++)
        {
            assertEquals(Integer.valueOf(i), s.find(i).get());
        }
    }

    @Test
    public void testRemoving()
    {
        ImSet<Integer> s = onArray(1, 2, 3, 4);

        for (int j = 1; j <= 4; j++)
        {

            ImSet<Integer> sr = s.remove(j);
            for (int i = 1; i <= 4; i++)
            {
                assertEquals("" + i, (i == j)
                                     ? ImMaybe.nothing()
                                     : ImMaybe.just(i),
                        sr.find(i));
            }
        }
    }

    @Test
    public void testRemoveLastElementGivesTheEmptySet()
    {
        ImSet<Integer> s = onArray(1);

        assertSame(ImSet.empty(), s.remove(1));

    }

    @Test
    public void testMinus()
    {
        ImList<Integer> s1 = ImList.on(1, 3, 5, 6);

        ImSet<Integer> s2 = onArray(1, 2, 3, 4);

        assertEquals("", onArray(2, 4), s2.minus(s1));
    }

    @Test
    public void testIntersection()
    {
        ImSet<Integer> s1 = ImSet.on(1, 3, 5, 6);
        ImSet<Integer> s2 = ImSet.on(7, 3, 5, 8);

        assertEquals(ImSet.on(3, 5), s1.intersection(s2));
    }

    @Test
    public void testMinusOnMany()
    {
        ImList<Integer> start = ImList.on(1, 2, 3, 4, 5);

        ImList<ImList<Integer>> powerSet = start.powerSet();

        powerSet.foreach(s -> ImSetTest.assertMinus(start, s));
    }

    private static void assertMinus(ImList<Integer> l1, ImList<Integer> l2)
    {
        ImSet<Integer> s1 = ImSet.onAll(l1);
        ImSet<Integer> s2 = ImSet.onAll(l2);

        TestUtils.assertSetsEqual("", ImSetTest.removeAll(l1, l2), s1.minus(s2).toImList());
    }

    private static ImList<Integer> removeAll(ImList<Integer> l1, ImList<Integer> l2)
    {
        System.out.println("" + l1 + " minus " + l2);
        return l2.foldl(l1, (a, b) -> a.remove(b));
    }

    @Test
    public void testEquals()
    {
        ImSet<Integer> s = onArray(1, 2, 3, 4);
        ImSet<Integer> s2 = onArray(1, 2, 3, 4);
        Set<Integer> s3 = new HashSet<>();
        s3.addAll(Arrays.asList(1, 2, 3, 4));
        ImSet<Integer> s4 = onArray(1, 2, 3);
        ImSet<Integer> s5 = onArray(1, 2, 3, 5);

        //assertEquals(s, s);
        assertEquals(s, s2);
        assertEquals(s2, s);
        assertFalse(s.equals(null));
        assertFalse(s.equals(s3));
        assertFalse(s.equals(s4));
        assertFalse(s.equals(s5));
    }

    @Ignore
    public static void testEqualsAndHash()
    {

        ImSet<Integer> s1 = onArray(1, 2, 3, 4);
        ImSet<Integer> s2 = onArray(1, 2, 3, 4);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    public void testRemovingDodgyHashCodes()
    {
        int count = 4;
        DodgyHashCode[] dodgyHashCodesArray = new DodgyHashCode[count];

        // Initialise the array with a number of DodgyHashCodes
        for (int i = 0; i < count; i++)
        {
            dodgyHashCodesArray[i] = new DodgyHashCode(i);
        }

        ImSet<DodgyHashCode> s = onArray(dodgyHashCodesArray);
        System.out.println(s);

        for (int j = 1; j <= count; j++)
        {
            // Remove the DodgyHashCode with number j
            ImSet<DodgyHashCode> sr = s.remove(dodgyHashCodesArray[j - 1]);

            // All the other DodgyHashCodes should be there - just not where i = j
            for (int i = 1; i <= count; i++)
            {
                assertEquals((i == j)
                             ? ImMaybe.nothing()
                             : ImMaybe.just(dodgyHashCodesArray[i - 1]),
                        sr.find(dodgyHashCodesArray[i - 1]));
            }
        }
    }

    @Test
    public void testSerializing() throws Exception
    {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);

        ImSet<Integer> s = onArray(1, 2, 3);
        oos.writeObject(s);

        oos.close();

        ByteArrayInputStream ibos = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(ibos);

        ImSet<Integer> s2 = Caster.cast(ois.readObject());

        assertNotNull(s2);
        assertEquals(s.size(), s2.size());
        System.err.println(s2);

        assertEquals(s, s2);

        ois.close();
    }

    @Test
    public void testExampleOnArray() throws Exception
    {
        checkExample(onArray(1, 2, 3, 2), "[1, 2, 3]");
        checkExample(onArray(), "          []");

        try
        {
            onArray(1, null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (NullPointerException e)
        {
            System.err.println(e);
        }
    }

    int testCount = 0;

    public enum OpType
    {
        add, remove, replace
    }

    static class Op
    {
        OpType opType;
        Fooble value;

        public Op(OpType opType, Fooble value)
        {
            this.opType = opType;
            this.value = value;
        }

        @Override
        public String toString()
        {
            return "" + opType + " " + value;
        }
    }

    static class Fooble implements Comparable<Fooble>
    {

        private final int n;

        Fooble(int n)
        {
            this.n = n;
        }

        @Override
        public int hashCode()
        {
            return n % 4;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            Fooble other = (Fooble) obj;
            if (n != other.n)
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "" + n;
        }

        @Override public int compareTo(Fooble o)
        {
            return n - o.n;
        }
    }

    private static Set<Fooble> apply(Op op, Set<Fooble> set)
    {
        switch (op.opType)
        {
        case add:
        case replace:
            set.add(op.value);
            return set;

        case remove:
            set.remove(op.value);
            return set;
        }

        throw new RuntimeException("Unexpected op value");
    }

    private static ImSet<Fooble> apply(Op op, ImSet<Fooble> set)
    {
        switch (op.opType)
        {
        case add:
        case replace:
            return set.add(op.value);

        case remove:
            return set.remove(op.value);
        }
        throw new RuntimeException("Unexpected op value");
    }

    /**
     *
     * The plan is to identify several valid operations we can apply to a set when it contains
     * n items
     *
     * There are n+1 adds, n removes
     *
     * Here we are assuming that the find operation cannot change the set.
     *
     * We can form scripts of valid operations and apply them to both an ordered set and a sorted set
     *
     * So, given the empty HashSet and an empty ImSet we generate all valid scripts up to a length l.
     *
     * After applying each operation we check that the ordered set and the sorted set are the same
     *
     * To make the logic easier we will generate the operations and test them as we go
     *
     * The number of tests for scripts of different length is as follows:
     *
     */
    private void testState(int depth, Set<Fooble> set, ImSet<Fooble> imSet, ImList<Op> opList)
    {
        if (depth == 0)
        {
            // System.out.println("tested " + "" + opList.reversed());

            return;
        }

        if (set.size() == 0 && opList.size() > 0)
        {
            return;
        }

        // Generate the ops
        List<Op> ops = generateOpsFor(set, depth);

        // for each valid operation
        for (Op op : ops)
        {

            // Copy the set
            Set<Fooble> list2 = new HashSet<>(set);

            // Add the op to the op list
            ImList<Op> newOpList = ImList.cons(op, opList);

            testCount++;

            // if (testCount == 2)
            // System.out.println("");

            // Apply the op to the list so that we know what sequence of operations led to an error
            Set<Fooble> setAfterOp = apply(op, list2);

            // Apply the op to the ImSet
            ImSet<Fooble> imSetAfterOp = apply(op, imSet);

            // Check that they are the same
            String expectedSorted = new TreeSet<>(setAfterOp).toString();

            TreeSet<Fooble> actualSorted = new TreeSet<>();
            for (Fooble fooble : imSetAfterOp)
            {
                actualSorted.add(fooble);
            }
            String expected = expectedSorted.toString();
            String actual = actualSorted.toString();

            if (!(expected.equals(actual)) || setAfterOp.size() != imSetAfterOp.size())
            {
                System.err.println("\nFailed on test " + testCount);
                System.err.println("size " + imSetAfterOp.size());
                System.err.println(newOpList.reverse());
                System.err.println("Expected sorted " + expected);
                System.err.println("Actual sorted  " + actual);
                System.err.println("Previous  " + imSet);
                System.err.println("Actual  " + imSetAfterOp);
                System.err.println("Actual hashCode " + imSetAfterOp.hashCode());
                System.err.println("Expected hashCode " + setAfterOp.hashCode());
                fail("" + newOpList.reverse());
            }

            // Check that we can find all the elements
            for (Fooble fooble : setAfterOp)
            {
                assertEquals(fooble, imSetAfterOp.find(fooble).get());
            }

            // If the op was a remove, check that we can't find the element we have removed
            if (op.opType == OpType.remove)
            {
                assertEquals(ImMaybe.nothing(), imSetAfterOp.find(op.value));
            }

            // Check the set in this new state
            this.testState(depth - 1, setAfterOp, imSetAfterOp, newOpList);
        }
    }

    private static List<Op> generateOpsFor(Set<Fooble> set, int depth)
    {
        ArrayList<Op> ops = new ArrayList<>();

        for (int i = 0; i < depth; i++)
        {
            ops.add(new Op(OpType.add, new Fooble(depth)));
        }

        for (Fooble f : set)
        {
            ops.add(new Op(OpType.remove, f));
        }

        for (Fooble f : set)
        {
            ops.add(new Op(OpType.replace, f));
        }

        return ops;
    }

    /**
     * This test compares an ImSortedSet and a TreeSet during a test where we "mutate" both collections
     * using the same mutations. It passes if the two collections are always "equal"
     */
    @Test
    public void testMany()
    {
        ImList<Op> empty = ImList.on();
        for (int i = 1; i <= 6; i++)
        {
            testCount = 0;

            this.testState(i, new HashSet<>(), ImSet.empty(), empty);
            System.out.println(i + "   " + testCount);
        }
    }

    @Test
    public void getList()
    {
        ImSet<Integer> s = ImSet.onArray(1, 2, 3, 4, 5);

        assertEquals(s, ImSet.onIterator(s.toImList().iterator()));
    }
}