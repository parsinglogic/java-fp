package dev.javafp.map;

import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import dev.javafp.util.Caster;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static dev.javafp.util.ImTestHelper.checkExample;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ImMapTest
{
    @Test
    public void testOne() throws Exception
    {
        ImMap<String, String> map = new ImMap();

        ImMap<String, String> map1 = map.put("a", "b");

        assertEquals("b", map1.get("a"));

        ImMap<String, String> map2 = map1.remove("a");

        assertEquals(null, map2.get("a"));
    }

    @Test
    public void testRemove()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);
        ImList<ImPair<Integer, Integer>> pairs = list.cartesianProduct();

        ImMap<Integer, ImList<Integer>> map = ImMap.fromMulti(pairs);

        assertEquals(3, map.size());

        assertSame(ImMap.empty(), map.remove(1).remove(2).remove(3));
    }

    @Test
    public void testRemoveAll()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);
        ImList<ImPair<Integer, Integer>> pairs = list.cartesianProduct();

        ImMap<Integer, ImList<Integer>> map = ImMap.fromMulti(pairs);

        assertEquals(3, map.size());

        assertEquals(ImMap.empty(), map.removeAll(list));
    }

    @Test
    public void testPutNullKeyThrows()
    {
        try
        {
            ImMap<String, String> map = new ImMap<String, String>();
            map.put(null, "b");
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (NullPointerException e)
        {
        }
    }

    @Test
    public void testPutNullValueThrows()
    {
        try
        {
            ImMap<String, String> map = new ImMap<String, String>();
            map.put("a", null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (NullPointerException e)
        {
        }
    }

    @Test
    public void testToString()
    {
        ImMap<String, String> m1 = new ImMap<String, String>().put("a", "b").put("b", "a");
        System.out.println(m1.toString());
    }

    @Test
    public void testEquals()
    {
        ImMap<String, String> m1 = new ImMap<String, String>();

        m1 = m1.put("a", "b");
        m1 = m1.put("b", "a");

        ImMap<String, String> m2 = new ImMap<String, String>();

        m2 = m2.put("b", "a");
        m2 = m2.put("a", "b");

        ImMap<String, String> m3 = m2.put("b", "d");
        ImMap<String, String> m4 = m2.remove("b");
        m4 = m4.put("b", "a");
        ImMap<String, String> mempty = new ImMap<String, String>();
        ImMap<Integer, String> m5 = new ImMap<Integer, String>();
        m5.put(1, "");

        assertEquals(m1, m1);
        assertEquals(m1, m2);
        assertEquals(m1, m4);
        assertEquals(mempty, mempty);
        assertEquals(m3, m3);
        assertFalse(m1.equals(mempty));
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(m3));
        assertFalse(mempty.equals(m3));
        assertFalse(m1.equals(m5));
    }

    @Test
    public void testHashCode()
    {
        ImMap<String, String> m1 = new ImMap<String, String>().put("a", "b").put("b", "a");
        ImMap<String, String> m2 = new ImMap<String, String>().put("a", "b").put("b", "a");

        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertEquals(m1.remove("a").remove("b").hashCode(), m2.remove("b").remove("a").hashCode());
    }

    @Test
    public void hashCodeExample()
    {
        // Create an empty map
        ImMap<String, String> mEmpty = new ImMap<String, String>();
        checkExample(mEmpty.isEmpty(), "true");

        // Put an entry
        ImMap<String, String> mOne = mEmpty.put("a", "Aardvark");

        checkExample(mOne.size(), "1");
        checkExample(mOne.get("a"), "Aardvark");

        // the empty map is not changed
        checkExample(mEmpty.isEmpty(), "true");

        // put another entry
        ImMap<String, String> mTwo = mOne.put("b", "Bear");

        checkExample(mTwo.get("a"), "Aardvark");
        checkExample(mTwo.get("b"), "Bear");

        // mOne has not changed
        checkExample(mOne.size(), "1");
        checkExample(mOne.get("a"), "Aardvark");
        checkExample(mOne.get("b"), "null");

        // You can remove entries
        ImMap<String, String> mThree = mTwo.remove("a");
        checkExample(mThree.get("a"), "null");

        // And remove them if they are not present
        ImMap<String, String> mFive = mTwo.remove("z");
        checkExample(mFive == mTwo, "true");

        // replace entries
        ImMap<String, String> mFour = mTwo.put("b", "Buffalo");
        checkExample(mFour.get("b"), "Buffalo");

        // replace entries with identical ones
        ImMap<String, String> mSix = mTwo.put("b", "Bear");
        checkExample(mSix == mTwo, "false");

        // replace entries with equal but non identical ones
        ImMap<String, String> mSeven = mTwo.put("b", "xBear".substring(1));
        checkExample(mSeven == mTwo, "false");
    }

    @Test
    public void testSerializing() throws Exception
    {
        ByteArrayOutputStream bos;
        ObjectOutputStream oos;

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);

        ImMap<String, String> map = new ImMap<String, String>();

        map = map.put("a", "b");
        oos.writeObject(map);

        oos.close();

        ByteArrayInputStream ibos = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(ibos);

        ImMap<String, String> map2 = Caster.cast(ois.readObject());

        assertNotNull(map2);

        assertEquals(map, map2);

        ois.close();
    }

    int testCount = 0;

    public enum OpType
    {
        add, remove, put
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

        /**
         * A String representation of this object
         */
        @Override
        public String toString()
        {
            return "" + opType + " " + value;
        }
    }

    static class Fooble
    {
        private final int n;

        Fooble(int n)
        {
            this.n = n;
        }

        /**
         * The (cached) hashcode for this object.
         */
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
            if (getClass() != obj.getClass())
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

    }

    private Map<Fooble, Integer> apply(Op op, Map<Fooble, Integer> map)
    {
        switch (op.opType)
        {
        case add:
            map.put(op.value, op.value.n);
            return map;

        case remove:
            map.remove(op.value);
            return map;

        case put:
            map.put(op.value, op.value.n + 100);
            return map;
        }

        throw new RuntimeException("Unexpected op value");
    }

    private ImMap<Fooble, Integer> apply(Op op, ImMap<Fooble, Integer> map)
    {
        switch (op.opType)
        {
        case add:
            return map.put(op.value, op.value.n);

        case remove:
            return map.remove(op.value);

        case put:
            return map.put(op.value, op.value.n + 100);
        }
        throw new RuntimeException("Unexpected op value");
    }

    /**
     *
     * The plan is to identify all several valid operations we can apply to a map when it contains
     * n items that will "mutate" it.
     *
     * There are n+1 adds, n removes, n puts
     *
     * We can form scripts of valid operations and apply them to both an ImMap and a HashMap
     *
     * So, given the empty ImMap and an empty hashMap set we generate all valid scripts up to a length l.
     *
     * After applying each operation we check that the two collections are "equal"
     *
     * To make the logic easier we will generate the operations and test them as we go
     *
     */
    private void testState(int depth, Map<Fooble, Integer> map, ImMap<Fooble, Integer> imMap, ImList<Op> opList)
    {
        if (depth == 0)
        {
            // System.out.println("tested " + "" + opList.reversed());

            return;
        }

        if (map.size() == 0 && opList.size() > 0)
        {
            return;
        }

        // Generate the ops
        List<Op> ops = generateOpsFor(map, depth);

        // for each valid operation
        for (Op op : ops)
        {
            // Copy the set
            Map<Fooble, Integer> map2 = new HashMap<Fooble, Integer>(map);

            // Add the op to the op list
            ImList<Op> newOpList = ImList.cons(op, ImList.on(ops));

            testCount++;

            // if (testCount == 2)
            // System.out.println("");

            // Apply the op to the list so that we know what sequence of operations led to an error
            Map<Fooble, Integer> mapAfterOp = apply(op, map2);

            // Apply the op to the map
            ImMap<Fooble, Integer> imMapAfterOp = apply(op, imMap);

            // Check that they are "the same"
            String expected = trace(mapAfterOp);
            String actual = trace(imMapAfterOp);

            if (!(expected.equals(actual)) || mapAfterOp.size() != imMapAfterOp.size())
            {
                System.err.println("\nFailed on test " + testCount);
                System.err.println(newOpList.reverse());
                System.err.println("Expected size " + mapAfterOp.size());
                System.err.println("Actual size   " + imMapAfterOp.size());
                System.err.println("Expected Map  " + expected);
                System.err.println("Actual ImMap  " + actual);
                fail("" + newOpList.reverse());
            }

            checkHashCode(imMapAfterOp, mapAfterOp);

            // If the op was a remove, check that we can't find the element we have removed
            if (op.opType == OpType.remove)
            {
                assertEquals(null, imMapAfterOp.get(op.value));
            }
            else
            {
                assertTrue(null != imMapAfterOp.get(op.value));
            }

            // Check the map in this new state
            testState(depth - 1, mapAfterOp, imMapAfterOp, newOpList);
        }
    }

    private void checkHashCode(ImMap<Fooble, Integer> imMap, Map<Fooble, Integer> map)
    {
        //        int h = 0;
        //        for (ImMap.Entry<Fooble, Integer> entry : imMap)
        //        {
        //            h += imMap.getMyHashOf(entry);
        //        }
        //
        //        assertEquals(h, imMap.hashCode());
        //        assertEquals(map.hashCode(), imMap.hashCode());
    }

    private String trace(Map<Fooble, Integer> setAfterOp)
    {
        TreeSet<String> stringsInOrder = new TreeSet<String>();
        for (Map.Entry<Fooble, Integer> entry : setAfterOp.entrySet())
        {
            stringsInOrder.add("" + entry.getKey() + "->" + entry.getValue());
        }

        return stringsInOrder.toString();
    }

    private String trace(ImMap<Fooble, Integer> mt)
    {
        TreeSet<String> stringsInOrder = new TreeSet<String>();
        for (ImMap.Entry<Fooble, Integer> entry : mt)
        {
            stringsInOrder.add("" + entry.key + "->" + entry.value);
        }

        return stringsInOrder.toString();
    }

    private List<Op> generateOpsFor(Map<Fooble, Integer> set, int depth)
    {
        ArrayList<Op> ops = new ArrayList<Op>();

        for (int i = 0; i < depth; i++)
        {
            ops.add(new Op(OpType.add, new Fooble(depth)));
        }

        for (Fooble f : set.keySet())
        {
            ops.add(new Op(OpType.remove, f));
        }

        for (Fooble f : set.keySet())
        {
            ops.add(new Op(OpType.put, f));
        }

        return ops;
    }

    @Test
    public void testMany()
    {
        ImList<Op> empty = ImList.on();
        for (int i = 1; i <= 6; i++)
        {
            testCount = 0;

            testState(i, new HashMap<Fooble, Integer>(), new ImMap<Fooble, Integer>(), empty);
            System.out.println(i + "   " + testCount);
        }
    }

    @Test
    public void testFrom()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);
        ImList<ImPair<Integer, Integer>> pairs = list.cartesianProduct();

        System.out.println(pairs);

        ImMap<Integer, ImList<Integer>> map = ImMap.fromMulti(pairs);

        assertEquals(list.size(), map.size());

        assertEquals(list, map.get(1));
        assertEquals(list, map.get(2));
        assertEquals(list, map.get(3));
    }

    @Test
    public void testToAndFromMap()
    {
        ImList<ImPair<Fooble, String>> pairs = ImRange.oneTo(30).map(i -> Pai.r(new Fooble(i), "" + i));

        ImMap<Fooble, String> start = ImMap.fromPairs(pairs);

        assertEquals(start, ImMap.fromMap(start.toMap()));
    }
}