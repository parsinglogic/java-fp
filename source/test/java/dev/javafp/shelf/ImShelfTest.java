package dev.javafp.shelf;

import dev.javafp.ex.ImIndexOutOfBoundsException;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.rand.Rando;
import dev.javafp.tree.ImTreeFactory;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImTestHelper;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static dev.javafp.shelf.ImShelf.on;
import static dev.javafp.util.ImTestHelper.checkExample;
import static dev.javafp.util.Say.say;
import static dev.javafp.util.TestUtils.failExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ImShelfTest
{

    @Test
    public void testAddingingAtStart() throws Exception
    {
        ImShelf<Character> s = ImShelf.empty();

        s = s.add(1, 'c');
        System.out.println(s);
        assertElementEquals('c', s.get(1));

        s = s.add(1, 'b');
        System.out.println(s);
        assertElementEquals('b', s.get(1));
        assertElementEquals('c', s.get(2));

        s = s.add(1, 'a');
        System.out.println(s);
        assertElementEquals('a', s.get(1));
        assertElementEquals('b', s.get(2));
        assertElementEquals('c', s.get(3));
    }

    @Test
    public void testExampleConcatShowingtheTree() throws Exception
    {
        List<Integer> first = ImTestHelper.makeList(10, 1);
        ImShelf<Integer> one = ImShelf.onAll(first);

        System.err.println(one);
        System.err.println(one.tree.toBoxString());

        ImShelf<Integer> four = one.add(1, 0);

        System.err.println(four);
        System.err.println(four.tree.toBoxString());

        ImShelf<Integer> five = four.add(11);

        System.err.println(five);
        System.err.println(five.tree.toBoxString());
    }

    @Test
    public void testGet1() throws Exception
    {
        ImShelf<Integer> shelf = ImShelf.empty();
        try
        {
            shelf.get(1);
            failExpectedException(IndexOutOfBoundsException.class);
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testMatchesList() throws Exception
    {
        ImShelf<Integer> shelf = ImShelf.empty();
        List<Integer> list = new LinkedList<Integer>();
        int size = 100;
        Random rand = new Random(1111);

        shelf = shelf.add(1);
        list.add(1);

        int i = 0;
        for (int count = 0; count < size; count++)
        {
            i++;
            int pos = rand.nextInt(list.size());

            shelf = shelf.add(pos + 1, i);
            list.add(pos, i);

            assertEquals(list.toString(), shelf.toString());
        }
    }

    @Test
    public void testAddingingAtEnd() throws Exception
    {
        ImShelf<Character> s = ImShelf.empty();

        s = s.add(1, 'a');
        System.out.println(s);
        assertElementEquals('a', s.get(1));

        s = s.add(2, 'b');
        System.out.println(s);
        assertElementEquals('a', s.get(1));
        assertElementEquals('b', s.get(2));

        s = s.add(3, 'c');
        System.out.println(s);
        assertElementEquals('a', s.get(1));
        assertElementEquals('b', s.get(2));
        assertElementEquals('c', s.get(3));
    }

    @Test
    public void testIterator() throws Exception
    {
        ImShelf<Character> s = ImShelf.empty();

        s = s.add('a');
        s = s.add('b');
        s = s.add('c');

        ImShelf.ImShelfIterator<Character> it = s.iterator();

        assertEquals(true, it.hasNext());
        assertEquals(Character.valueOf('a'), it.next());
        assertEquals(true, it.hasNext());
        assertEquals(Character.valueOf('b'), it.next());
        assertEquals(true, it.hasNext());
        assertEquals(Character.valueOf('c'), it.next());
        assertEquals(false, it.hasNext());
    }

    @Test
    public void testAsList() throws Exception
    {
        assertEquals(ImList.on(1, 8, 2, 3, 5), ImShelf.on(1, 8, 2, 3, 5).toImList());
    }

    @Test
    public void testAddingInThree() throws Exception
    {
        ImShelf<Character> s = tt("b1 a2 c2");
        System.out.println(s);

        ImShelf<Character> sx;

        sx = s.add(1, 'x');
        System.out.println(sx);
        assertTreeIs("b1 a2 x3 c2", sx);

        sx = s.add(2, 'x');
        System.out.println(sx);
        assertTreeIs("b1 a2 -3 x3 c2", sx);

        sx = s.add(3, 'x');
        System.out.println(sx);
        assertTreeIs("b1 a2 c2 x3", sx);

        sx = s.add(4, 'x');
        System.out.println(sx);
        assertTreeIs("b1 a2 c2 -3 x3", sx);
    }

    @Test
    public void testRemove1() throws Exception
    {
        ImShelf<Character> s = tt("a1");

        ImShelf<Character> sr = s.remove(1);
        assertTreeIs("", sr);
    }

    @Test
    public void testRemove1FromEmpty() throws Exception
    {
        ImShelf<Character> s = ImShelf.empty();

        try
        {
            s.remove(1);
            failExpectedException(ImIndexOutOfBoundsException.class);
        } catch (ImIndexOutOfBoundsException e)
        {
        }

    }

    @Test
    public void testRemove1FromThree() throws Exception
    {
        ImShelf<Character> s = tt("b1 a2 c2");

        assertTreeIs("b1 -2 c2", s.remove(1));
        assertTreeIs("c1 a2", s.remove(2));
        assertTreeIs("b1 a2", s.remove(3));
    }

    @Test
    public void testAddingToThree() throws Exception
    {
        ImShelf<Character> s = tt("b1 a2 c2");

        assertTreeIs("b1 a2 c2 -3 x3", s.add('x'));
    }

    @Test
    public void testSet1() throws Exception
    {
        ImShelf<Character> s = tt("a1");

        assertTreeIs("b1", s.set(1, 'b'));
    }

    @Test
    public void testSet1OnThree() throws Exception
    {
        ImShelf<Character> s = tt("b1 a2 c2");

        assertTreeIs("b1 x2 c2", s.set(1, 'x'));
        assertTreeIs("x1 a2 c2", s.set(2, 'x'));
        assertTreeIs("b1 a2 x2", s.set(3, 'x'));
    }

    @Test
    public void testExampleAddingAtIndex() throws Exception
    {
        checkExample(on(1, 2, 3, 5).add(2, 8), "[1, 8, 2, 3, 5]");
        checkExample(on(1, 2, 3).add(4, 8), "[1, 2, 3, 8]");

        try
        {
            on(1, 2, 3).add(13, 8);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }

    }

    @Test
    public void testExampleRemove() throws Exception
    {
        checkExample(on(1, 8, 2, 3, 5).remove(2), "[1, 2, 3, 5]");

        try
        {
            on(1, 2, 3).remove(0);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }
    }

    @Test
    public void testExampleGet1() throws Exception
    {
        checkExample(on(1, 2, 3).get(1), "1");
        checkExample(on(1, 2, 3).get(3), "3");

        try
        {
            on().get(1);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }

        try
        {
            on(1, 2).get(3);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }

    }

    @Test
    public void testExampleSet1() throws Exception
    {
        checkExample(on(1, 2, 3).set(1, 5), "[5, 2, 3]");
        checkExample(on(1, 2, 3).set(3, 8), "[1, 2, 8]");

        try
        {
            on(1).set(1, null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }

        try
        {
            on(1, 2).set(-1, 4);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }

    }

    @Test
    public void testExampleadd() throws Exception
    {
        checkExample(on(1, 2).add(3), "[1, 2, 3]");
        checkExample(on().add(1), "    [1]");

        try
        {
            on(1, 2, 3).add(null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (Exception e)
        {
            System.err.println(e);
        }
    }

    @Test
    public void testExampleContains() throws Exception
    {
        checkExample(on(1, 2, 3).contains(1), "true");
        checkExample(on(1, 2, 3).contains(1.0), "false");

        try
        {
            checkExample(on().contains(null), "false");
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (NullPointerException e)
        {
            System.err.println(e);
        }
    }

    @Test
    public void testExampleIndexOf() throws Exception
    {
        checkExample(on(1, 2, 3).indexOf(1), "1");
        checkExample(on(1, 2, 3).indexOf(5), "-1");

        try
        {
            on().indexOf(null);
            TestUtils.failExpectedException(NullPointerException.class);
        } catch (NullPointerException e)
        {
            System.err.println(e);
        }

    }

    @Test
    public void testExampleMap() throws Exception
    {
        // To convert a list of objects to a list their string representations:

        checkExample(on(1, 2).map(i -> i.toString()), "[1, 2]");

        // To parse a list of strings to their Integer equivalents:

        checkExample(on("1", "2").map(i -> Integer.parseInt(i)), "[1, 2]");

        // To parse with a radix of 16, we first get the parse function with two arguments, then we create a new function
        // out of this with the arguments reversed. Then, if we supply the radix argument of 16 to this function,
        // we will have a new function of one argument that does what we want:
        //
        //        Function2<Integer> parseIntWithRadixFn = FnFactory.on(Integer.class).getFnStatic( //
        //                int.class, "parseInt", String.class, int.class);
        //
        //        Function1<Integer> parseHexFn = parseIntWithRadixFn.flip().invoke(16);
        //        checkExample(onArray("8", "D", "15").map(parseHexFn), "[8, 13, 21]");
    }

    @Test
    public void testOnIteratorAllArrayIsTheSameAsEmpty() throws Exception
    {
        checkExample(on() == ImShelf.empty(), "true");
    }

    @Test
    public void testOnIteratorAllArray() throws Exception
    {
        List<Number> threeFive = Arrays.<Number>asList(3, 5);

        assertEquals(2, ImShelf.onAll(threeFive).size());
        assertEquals(2, ImShelf.<Number>onAll(threeFive).size());
    }

    @Test
    public void testExampleAddingAll() throws Exception
    {
        List<Integer> threeFive = Arrays.<Integer>asList(3, 5);
        ImShelf<Integer> oneTwo = on(1, 2);

        checkExample(oneTwo.addAll(threeFive), "[1, 2, 3, 5]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExampleJoin() throws Exception
    {

        ImShelf<Integer> oneTwo = on(1, 2);
        ImShelf<Integer> threeFive = on(3, 5);
        ImShelf<ImShelf<Integer>> f = on(oneTwo, threeFive);

        checkExample(ImShelf.join(f), "[1, 2, 3, 5]");
    }

    @Test
    public void testExampleEquals() throws Exception
    {
        checkExample(on().equals(null), "false");
        checkExample(on(1).equals(on(1)), "true");
    }

    @Test
    public void testHashCode() throws Exception
    {
        assertEquals(on().hashCode(), on().hashCode());
        assertEquals(on(1).hashCode(), on(1).hashCode());
    }

    private void assertElementEquals(Character expected, Object actual)
    {
        assertEquals(expected, actual);
    }

    int uniqueValue = 0;

    public enum OpType
    {
        add, remove, replace
    }

    static class Op
    {
        OpType opType;
        int index;

        public Op(final OpType opType, final int index)
        {
            this.opType = opType;
            this.index = index;
        }

        @Override
        public String toString()
        {
            return "" + opType + " " + index;
        }
    }

    private List<Integer> apply(final Op op, final List<Integer> list, final int value)
    {
        final int index = op.index - 1;
        switch (op.opType)
        {
        case add:
            list.add(index, value);
            return list;

        case remove:
            list.remove(index);
            return list;

        case replace:
            list.set(index, value);
            return list;
        }

        throw new RuntimeException("Unexpected op value");
    }

    private ImShelf<Integer> apply(final Op op, final ImShelf<Integer> shelf, final int value)
    {
        final int index = op.index;
        switch (op.opType)
        {
        case add:
            return shelf.add(index, value);

        case remove:
            return shelf.remove(index);

        case replace:
            return shelf.set(index, value);
        }
        throw new RuntimeException("Unexpected op value");
    }

    /**
     *
     * The plan is to identify several valid operations we can apply to a shelf when it contains n items
     *
     * There are n+1 adds, n removes and n replaces
     *
     * Here we are assuming that the get operation cannot change the shelf.
     *
     * We can form scripts of valid operations and apply them to both a shelf and a list.
     *
     * So, given the empty shelf and an empty list we generate all valid scripts up to a length l.
     *
     * After applying each operation we check that the shelf and the list are the same
     *
     * To make the logic easier we will generate the operations and test them as we go
     *
     * The number of tests for scripts of different length is as follows:
     *
     *     1   1
     *     2   5
     *     3   24
     *     4   151
     *     5   1119
     *     6   11604
     *     7   132061
     *     8   1725965
     *     9   25452540
     *
     * If we take a short cut that stops the tests if we get to a state where the list is empty:
     *
     *     1   1
     *     2   5
     *     3   23
     *     4   145
     *     5   1165
     *     6   11387
     *     7   130409
     *     8   1710973
     *     9   25291519
     *
     * If we don't move to the next state after a remove operation:
     *
     *     1   1
     *     2   5
     *     3   23
     *     4   129
     *     5   903
     *     6   7657
     *     7   76255
     *     8   871361
     *     9   11229335
     *
     */
    private void testState(final int depth, final List<Integer> list, final ImShelf<Integer> shelf,
            final ImList<Op> opList)
    {
        if (depth == 0)
        {
            // System.out.println("tested " + "" + opList.reversed());
            // System.out.println(list);
            return;
        }

        if (list.size() == 0 && opList.size() > 0)
        {
            return;
        }

        // Generate the ops
        final List<Op> ops = generateOpsFor(list.size());

        // for each valid operation
        for (final Op op : ops)
        {

            // Copy the list
            final List<Integer> list2 = new ArrayList<Integer>(list);

            // Add the op to the op list so that we know what sequence of operations led to an error
            final ImList<Op> newOpList = ImList.cons(op, (ImList<Op>) opList);

            uniqueValue++;

            // Apply the op to the list
            final List<Integer> listAfter = apply(op, list2, uniqueValue);

            // Apply the op to the shelf
            final ImShelf<Integer> shelfAfter = apply(op, shelf, uniqueValue);

            // Check that they are the same
            final String listS = listAfter.toString();
            final String shelfS = shelfAfter.toString();

            if (!(listS.equals(shelfS)) || listAfter.size() != shelfAfter.size() || !checkHashCode(shelfAfter))
            {
                System.err.println("\nFailed on test " + uniqueValue);
                System.err.println("size " + shelfAfter.size());
                System.err.println(newOpList.reverse());

                System.err.println("Expected list " + listAfter);
                System.err.println("Actual shelf  " + shelfAfter);
                System.err.println("Previous shelf " + shelf);
                fail("" + newOpList.reverse());
            }

            // If the op was a remove then don't move to the next state
            // if (op.opType != OpType.remove)
            testState(depth - 1, listAfter, shelfAfter, newOpList);
        }
    }

    private boolean checkHashCode(ImShelf<Integer> shelf)
    {
        int h = 0;
        for (Integer i : shelf)
        {
            h += i.hashCode();
        }

        return h == shelf.hashCode();
    }

    private List<Op> generateOpsFor(final int size)
    {
        final ArrayList<Op> ops = new ArrayList<Op>();

        for (int i = 1; i <= size + 1; i++)
        {
            ops.add(new Op(OpType.add, i));
        }

        for (int i = 1; i <= size; i++)
        {
            ops.add(new Op(OpType.replace, i));
        }

        for (int i = 1; i <= size; i++)
        {
            ops.add(new Op(OpType.remove, i));
        }

        return ops;
    }

    @Test
    public void testMany()
    {
        final ImList<Op> empty = ImList.empty();
        for (int i = 1; i <= 7; i++)
        {
            uniqueValue = 0;
            testState(i, new ArrayList<Integer>(), ImShelf.<Integer>empty(), empty);
            System.out.println(i + "   " + uniqueValue);
        }
    }

    @Test
    public void testJoinMatchesImListJoin()
    {
        ImList<ImList<Integer>> listOfLists = generateLists(0, 3, 10, ImRange.step(1, 1));

        // Create a shelf of shelf's from the list of listOfLists
        ImShelf<ImShelf<Integer>> ss = listOfLists.foldl(ImShelf.empty(), (z, i) -> z.add(ImShelf.onAll(i)));

        // Join should give the same result
        assertEquals(ImList.join(listOfLists), ImShelf.join(ss).toImList());
    }

    public <A> ImList<ImList<A>> generateLists(int min, int maxExclusive, int count, ImList<A> thingsToUse)
    {
        ImList<Integer> rs = ImList.unfold(0, i -> Rando.nextInt(min, maxExclusive)).take(count);

        return rec(rs, count, thingsToUse);
    }

    private <A> ImList<ImList<A>> rec(ImList<Integer> sizes, int count, ImList<A> thingsToUse)
    {
        if (count == 0)
            return ImList.on();
        else
        {
            ImPair<ImList<A>, ImList<A>> pair = thingsToUse.splitAfterIndex(sizes.head());
            return ImList.cons(pair.fst, rec(sizes.tail(), count - 1, pair.snd));
        }
    }

    @Test
    public void testGenerateLists()
    {
        ImList<ImList<Integer>> lists = generateLists(2, 5, 5, ImRange.step(1, 1));
        say(lists);

        ImList<Integer> joined = ImList.join(lists);
        assertEquals(ImRange.step(1, 1).take(joined.size()), joined);
    }

    public static ImShelf<Character> tt(final String string)
    {
        return new ImShelf<Character>(new ImTreeFactory(string).create());
    }

    public static void assertTreeIs(final String expectedTokens, final ImShelf<Character> actual)
    {
        ImTestHelper.assertTreesAreEqual(tt(expectedTokens), actual);
    }

}