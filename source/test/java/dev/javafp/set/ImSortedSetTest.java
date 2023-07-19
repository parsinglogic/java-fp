package dev.javafp.set;

import dev.javafp.lst.ImList;
import dev.javafp.lst.Range;
import dev.javafp.rand.Rando;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static dev.javafp.util.Say.say;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class ImSortedSetTest
{
    int largeSize = 20;
    int smallSize = largeSize - 3;

    int several = 5;

    int testRuns = 60;

    ImList<Integer> largeList = Range.oneTo(largeSize);
    ImList<Integer> smallList = Range.oneTo(smallSize).shuffle();

    @Test
    public void testAgainstTreeSet()
    {
        /**
         * We iterate a number of times
         *
         * On each iteration,
         * we roll a 4 sided die and, depending on the result, we:
         *
         * 1. add an element
         * 2. remove an element
         * 3. add a few elements
         * 4. remove all the elements
         *
         * Check that the sorted set and the tree set agree with each other
         */

        say("large list", largeList);
        say("small list", smallList);

        // Create two empty sets
        ImSortedSet<Integer> sortedSet = ImSortedSet.empty();
        TreeSet<Integer> treeSet = new TreeSet();

        // Roll a 4 sided die a few times
        ImList<Integer> rs = ImList.unfold(0, i -> Rando.nextIntFromZeroToExclusive(4)).take(testRuns);

        say(rs);

        int count = 0;

        for (Integer i : rs)
        {
            count++;

            switch (i)
            {
            case 0:
            {
                // Add

                // Choose an element from the small list
                int elementToAdd = smallList.at(count % 10 + 1);

                say("add", elementToAdd);

                // Add to both sets
                sortedSet = sortedSet.add(elementToAdd);

                treeSet.add(elementToAdd);

                check(sortedSet, treeSet);
                break;

            }
            case 1:
            {
                // Remove

                // Choose an element from the small list
                int elementToRemove = smallList.at(count % 10 + 1);

                say("remove", elementToRemove);

                // Remove from both sets
                sortedSet = sortedSet.remove(elementToRemove);

                treeSet.remove(elementToRemove);

                check(sortedSet, treeSet);
                break;

            }
            case 2:
            {

                // Add several

                ImList<Integer> js = smallList.shuffle().take(several);
                say("add several", js);

                sortedSet = sortedSet.addAll(js);
                treeSet.addAll(js.toList());

                check(sortedSet, treeSet);
                break;
            }
            case 3:
            {
                // remove all

                say("remove all");

                sortedSet = sortedSet.removeAll(sortedSet);
                treeSet.removeAll(treeSet);

                check(sortedSet, treeSet);
                break;
            }

            default:
                throw new RuntimeException();
            }
        }

    }

    private void check(ImSortedSet<Integer> sortedSet, TreeSet<Integer> treeSet)
    {
        say("sorted set", sortedSet);
        say("tree set", treeSet);

        // the sets should agree about what elements belong to each
        for (Integer i : largeList)
        {
            assertEquals("" + i, treeSet.contains(i), sortedSet.contains(i));
        }

        // they should be the same size
        assertEquals(treeSet.size(), sortedSet.size());

        assertEquals(ImList.onAll(treeSet), ImList.onAll(sortedSet));

        //        say("sortedSet.hashCode()", sortedSet.hashCode());
        //        say("treeSet.hashCode()", treeSet.hashCode());
        //        say("treeSet.stream().toList().hashCode()", treeSet.stream().toList().hashCode());

    }

    @Test
    public void testMapWithDuplicates()
    {
        List<Integer> ints = Arrays.asList(1, 2, 3, 5, 8, 5, 3, 2, 1, 0);
        List<Integer> list = new ArrayList();

        for (Integer integer : ints)
        {
            check(list);
            list.add(integer);
        }
    }

    private void check(List<Integer> list)
    {
        ImList<String> map = ImList.onAll(list).map(j -> j.toString());

        ImSortedSet<String> actual = ImSortedSet.<String>onAll(map);
        assertEquals(ImSortedSet.onAll(list).map(i -> i.toString()), actual);
    }

    int testCount = 0;

    public enum OpType
    {
        add, remove, replace
    }

    static class Op
    {
        OpType opType;
        Integer value;

        public Op(OpType opType, int value)
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

    /**
     * calculate a number that, when added to `sortedSet` will result in that
     * number being added at index `index` where the first element has index 1
     *
     * Also, if the number will be added after all other members of the set then
     * make sure there is enough space between it and the previous element to allow
     * `depth` insertions between them
     */
    private List<Integer> getNumbers(SortedSet<Integer> sortedSet, int depth)
    {
        /**
         * If depth = 3
         *
         *   3    empty   => 4 
         *   2    4       => 2 6
         *   1    2 4 6   => 1 3 5 7
         */

        List<Integer> result = new ArrayList<Integer>();

        int last = 0;
        for (Integer i : sortedSet)
        {
            int gen = last + ((i - last) >> 1);

            if (gen == last || gen == i)
                throw new RuntimeException("generated number " + gen + " is wrong for " + sortedSet);

            result.add(gen);
            last = i;
        }
        result.add(last + (1 << (depth - 1)));

        return result;
    }

    @Test
    public void testGetNumbers() throws Exception
    {
        SortedSet<Integer> set = new TreeSet<Integer>();
        assertEquals(Arrays.asList(4), getNumbers(set, 3));

        SortedSet<Integer> set4 = new TreeSet<Integer>(Arrays.asList(4));
        assertEquals(Arrays.asList(2, 6), getNumbers(set4, 2));

        SortedSet<Integer> set246 = new TreeSet<Integer>(Arrays.asList(2, 4, 6));
        assertEquals(Arrays.asList(1, 3, 5, 7), getNumbers(set246, 1));
    }

    @Test
    public void testGetNumbersThrowsWhenTooClose() throws Exception
    {
        SortedSet<Integer> set;

        set = new TreeSet<Integer>(Arrays.asList(2, 4, 5));
        try
        {
            getNumbers(set, 1);
            TestUtils.failExpectedException(RuntimeException.class);
        } catch (RuntimeException e)
        {
        }
    }

    private SortedSet<Integer> apply(Op op, SortedSet<Integer> sortedSet)
    {
        switch (op.opType)
        {
        case add:
        case replace:
            sortedSet.add(op.value);
            return sortedSet;

        case remove:
            sortedSet.remove(op.value);
            return sortedSet;
        }

        throw new RuntimeException("Unexpected op value");
    }

    private ImSortedSet<Integer> apply(Op op, ImSortedSet<Integer> set)
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
     * <p> The plan is to identify several valid operations we can apply to an ordered set when it contains
     * {@code n}
     *  items
     * <p> There are
     * {@code n+1}
     *  adds,
     * {@code n}
     *  removes
     * <p> Here we are assuming that the find operation cannot change the set.
     * <p> We can form scripts of valid operations and apply them to both an ordered set and a sorted set
     * <p> So, given the empty ordered set and an empty sorted set we generate all valid scripts up to a length l.
     * <p> After applying each operation we check that the ordered set and the sorted set are the same
     * <p> To make the logic easier we will generate the operations and test them as we go
     * <p> The number of tests for scripts of different length is as follows:
     *
     */
    private void testState(int depth, SortedSet<Integer> list, ImSortedSet<Integer> orderedSet, ImList<Op> opList)
    {
        if (depth == 0)
        {
            // System.out.println("tested " + "" + opList.reversed());
            return;
        }

        if (list.size() == 0 && opList.size() > 0)
        {
            return;
        }

        // Generate the ops
        List<Op> ops = generateOpsFor(list, depth);

        // for each valid operation
        for (Op op : ops)
        {

            // Copy the list
            SortedSet<Integer> list2 = new TreeSet<Integer>(list);

            // Add the op to the op list
            ImList<Op> newOpList = ImList.cons(op, opList);

            testCount++;

            // if (testCount == 2)
            // System.out.println("");

            // Apply the op to the list so that we know what sequence of operations led to an error
            SortedSet<Integer> sortedSetAfterOp = apply(op, list2);

            // Apply the op to the shelf
            ImSortedSet<Integer> imSortedSetAfterOp = apply(op, orderedSet);

            // Check that they are the same
            String expected = sortedSetAfterOp.toString();
            String actual = imSortedSetAfterOp.toString();

            if (!(expected.equals(actual)) || sortedSetAfterOp.size() != imSortedSetAfterOp.size())
            {
                System.err.println("\nFailed on test " + testCount);
                System.err.println("size " + imSortedSetAfterOp.size());
                System.err.println(newOpList.reverse());
                System.err.println("Expected SortedSet " + expected);
                System.err.println("Actual OrderedSet  " + actual);
                fail("" + newOpList.reverse());
            }

            // Check that the height is correct
            if (imSortedSetAfterOp.tree.getHeight() > maxHeightOfTree(sortedSetAfterOp.size()))
            {
                System.err.println("\nFailed on test " + testCount);
                System.err.println(newOpList.reverse());
                System.err.println("\nExpected height " + maxHeightOfTree(sortedSetAfterOp.size()) + " but was "
                        + imSortedSetAfterOp.tree.getHeight() + " on test " + testCount);
                System.err.println(imSortedSetAfterOp.tree);
                fail("");
            }

            // Check that we can find all the elements
            for (Integer i : imSortedSetAfterOp)
            {
                assertEquals(i, imSortedSetAfterOp.find(i).get());
            }

            // If the op was a remove, check that we can't find the element we have removed
            if (op.opType == OpType.remove)
            {
                assertEquals(ImMaybe.nothing(), imSortedSetAfterOp.find(op.value));
            }

            // Check the set in this new state

            testState(depth - 1, sortedSetAfterOp, imSortedSetAfterOp, newOpList);
        }
    }

    /**
     * The max height of the tree is log(size) + 1
     */
    private int maxHeightOfTree(int size)
    {
        return size == 0
               ? 0
               : bitsNeededFor(size) + 1;
    }

    private int bitsNeededFor(int n)
    {
        return n <= 1
               ? 1
               : 1 + bitsNeededFor(n >> 1);
    }

    private List<Op> generateOpsFor(SortedSet<Integer> set, int depth)
    {
        ArrayList<Op> ops = new ArrayList<Op>();

        List<Integer> numbers = getNumbers(set, depth);

        for (Integer n : numbers)
        {
            ops.add(new Op(OpType.add, n));
        }

        for (Integer n : set)
        {
            ops.add(new Op(OpType.remove, n));
        }

        for (Integer n : set)
        {
            ops.add(new Op(OpType.replace, n));
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
        ImList<Op> empty = ImList.empty();
        for (int i = 1; i <= 7; i++)
        {
            testCount = 0;
            testState(i, new TreeSet<Integer>(), ImSortedSet.<Integer>empty(), empty);
            System.out.println(i + "   " + testCount);
        }
    }
}