package dev.javafp.lst;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.eq.Eq;
import dev.javafp.ex.ArgumentShouldNotBeLessThan;
import dev.javafp.ex.IllegalState;
import dev.javafp.ex.TransposeColsError;
import dev.javafp.ex.TransposeRowsError;
import dev.javafp.func.Fn;
import dev.javafp.rand.Rando;
import dev.javafp.set.ImSet;
import dev.javafp.time.Timer;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import dev.javafp.util.ClassUtils;
import dev.javafp.util.Constants;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ServerTextUtils;
import dev.javafp.util.Sums;
import dev.javafp.util.TestUtils;
import dev.javafp.util.Util;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.javafp.lst.ImList.cartesianProduct;
import static dev.javafp.lst.ImList.cons;
import static dev.javafp.lst.ImList.cross;
import static dev.javafp.lst.ImList.empty;
import static dev.javafp.lst.ImList.interleave;
import static dev.javafp.lst.ImList.join;
import static dev.javafp.lst.ImList.on;
import static dev.javafp.lst.ImList.randomInts;
import static dev.javafp.lst.ImList.repeat;
import static dev.javafp.lst.ImList.transpose;
import static dev.javafp.lst.ImList.unfold;
import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ImListTest implements Constants
{

    @Test
    public void testOnAll()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertEquals("[1, 2, 3]", list.toS());
    }

    @Test
    public void testOnOne()
    {
        Integer[] a = { 1, 2, 3 };
        ImList<Integer[]> list = ImList.onOne(a);

        assertEquals(1, list.size());
    }

    @Test
    public void testPrepend()
    {
        ImList<Integer> list = on(3, 4);

        assertEquals("[1, 2, 3, 4]", "" + list.prepend(1, 2));
    }

    @Test
    public void testToString()
    {
        ImList<Integer> s = on(1, 2, 3, 4, 5);

        assertEquals("[1, 2, 3, 4, 5]", s.toString());

    }

    @Test
    public void testToStringNested()
    {
        ImList<Integer> s = on(1, 2);

        ImList<ImList<Integer>> ss = on(s, s);
        assertEquals("[[1, 2], [1, 2]]", ss.toString());
    }

    @Test
    public void testSplitAtA()
    {
        ImList<Character> input = ImListOnString.on("abc");

        input.heads().foreach(in ->
                input.foreach(c ->
                        {
                            ImPair<ImList<Character>, ImList<Character>> p = in.splitBeforeElement(c);

                            assertEquals("on char " + c, in, p.fst.append(p.snd));
                            assertFalse("fst contains " + c, p.fst.contains(c));

                            if (!in.contains(c))
                                assertEquals("in does not contain " + c, p.snd, ImList.on());
                        }
                )
        );
    }

    @Test
    public void testToStringMultiline()
    {
        ImList<String> s = on("a\nb", "c\nd", "e", "f");

        String expected = "[\n" +
                "  1 a\n" +
                "    b\n" +
                "    ────────\n" +
                "  2 c\n" +
                "    d\n" +
                "    ────────\n" +
                "  3 e\n" +
                "    ────────\n" +
                "  4 f\n" +
                "]\n";

        assertEquals(expected, s.toString());
    }

    @Test
    public void testSize()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertEquals(3, list.size());
    }

    @Test
    public void testAppend()
    {
        ImList<Integer> one = on(1, 2, 3);
        ImList<Integer> two = on(4, 5);

        assertEquals("[1, 2, 3, 4, 5]", one.append(two).toString());
    }

    @Test
    public void testPrependAllWithUpCast()
    {
        // This shows that aupCast can get round some restrictions
        ImList<Number> one = on(1, 2, 3);
        ImList<Integer> two = on(4, 5);

        assertEquals("[4, 5, 1, 2, 3]", one.prependAll(two.upCast()).toString());
    }

    @Test
    public void testListsCanContainNulls()
    {
        ImList<Integer> cons = ImList.<Integer>on().withHead(null);
        ImList<Integer> nulls = cons(null, cons);
        ImList<Integer> nullsList = on(Arrays.asList(null, null));

        assertEquals(1, empty.hashCode());
        assertEq(nulls, nulls);
        int expected = ImLazyList.HASH_CONSTANT * ImLazyList.HASH_CONSTANT;
        assertEquals(expected, nulls.hashCode());
        assertEquals(expected, nullsList.hashCode());
    }

    @Test
    public void testEquals()
    {
        ImList<Integer> zero = on();
        ImList<Integer> one = on(1, 2, 3);
        ImList<Integer> two = on(1, 2, 3);
        ImList<Integer> three = on(1, 2, 3, 4).take(3);
        ImList<Integer> four = on(0, 1, 2).map(i -> i + 1);
        ImList<Integer> five = on(2, 3).withHead(1);
        ImList<Integer> six = four.filter(i -> i > 0);
        ImList<Integer> seven = on(Arrays.asList(1, 2, 3));

        assertEq(zero, on());

        ImList<ImList<Integer>> all = on(one, two, three, four, five, six, seven);

        for (ImPair<ImList<Integer>, ImList<Integer>> p : all.allPairs())
        {
            assertEq(p.fst, p.snd);
            assertEq(p.snd, p.fst);

            for (int j = 0; j < 4; j++)
            {
                assertEq(p.snd.take(j), p.fst.take(j));
                assertEq(p.fst.take(j), p.snd.take(j));
            }
        }

        ImList<ImList<Integer>> some = on(one, two, three);

        // I have to use a var here since the eclipse compiler won't compile it otherwise
        ImList<ImList<Integer>> map = some.powerSet().map(i -> join(i));

        for (ImPair<ImList<Integer>, ImList<Integer>> p : map.allPairs())
        {
            assertEquals(p.fst.toString().equals(p.snd.toString()), Eq.uals(p.fst, p.snd));
        }

        assertEquals(three.hashCode(), one.hashCode());
    }

    @Test
    public void testEquals3()
    {
        ImList<Number> xs = ImList.on(1, 2);
        ImList<Integer> ys = ImList.on(1, 2);
        assertTrue(xs.equalsList(ys));
        assertTrue(ys.equals(xs));

        assertTrue(((Object) ys).equals((Object) xs));

    }

    @Test
    public void testEquals2()
    {
        ImList<Integer> one = on(1, 2, 3);
        ImList<Integer> two = ImList.<Integer>empty().withHead(3).withHead(2).withHead(1);

        assertEquals(one, two);

        assertTrue(one.equalsList(two));
    }

    @Test
    public void testEqualsWithArrays()
    {
        int[] a0 = { 1, 2, 3 };
        int[] a1 = { 1, 2, 3 };
        int[] a2 = { 1, 2, 4 };

        byte[] b0 = { 3, 4, 5 };
        byte[] b1 = { 3, 4, 5 };
        byte[] b2 = { 3, 4, 6 };

        ImList<Object> l1 = ImList.on(a1, b1);
        ImList<Object> l2 = ImList.on(a2, b2);

        assertTrue(Eq.uals(ImList.on(a0), ImList.on(a1)));
        assertTrue(Eq.uals(ImList.on(b0), ImList.on(b1)));

        assertFalse(Eq.uals(l1, l2));

    }

    private <A> void assertEq(ImList<A> one, ImList<A> two)
    {
        if (!Eq.uals(one, two))
            fail();
    }

    @Test
    public void testPowerSet()
    {
        assertEquals("[[1, 2, 3], [1, 2], [1, 3], [1], [2, 3], [2], [3], []]", ImList.on(1, 2, 3).powerSet().toString());
    }

    @Test
    public void testAllPairs()
    {
        assertSame(ImList.on(), empty());
        ImList<Integer> oneTwoThree = ImList.on(1, 2, 3);
        String expected = "[(1, 2), (1, 3), (2, 3)]";
        assertEquals(expected, oneTwoThree.allPairs().toString());
        assertEquals(expected, oneTwoThree.cartesianProduct().filter(p -> p.fst < p.snd).toString());
    }

    @Test
    public void testTails()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);

        assertEquals("[[1, 2, 3], [2, 3], [3], []]", list.tails().toString());

        assertEquals(ImList.repeat(list, list.size() + 1), list.heads().zipWith(list.tails(), (a, b) -> a.append(b)));
    }

    @Test
    public void testSplitWhile()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);

        //        assertEquals("([], [1, 2, 3])", list.splitWhile(i -> i > 2).toString());
        assertEquals("([1, 2], [3])", list.splitWhile(i -> i < 3).toString());
        assertEquals("([1, 2, 3], [])", list.splitWhile(i -> true).toString());

    }

    @Test
    public void testSplit$()
    {
        ImList<Integer> list = ImList.on(1, 2, 3, 1, 1, 1);

        assertEquals("[2, 3, 1, 1, 1]", list.dropWhile(i -> i < 2).toString());
        assertEquals("[1]", list.takeWhile(i -> i < 2).toString());
        assertEquals("([1], [2, 3, 1, 1, 1])", list.splitWhileListIsTrueFor(l -> l.size() <= 1).toString());
        assertEquals("([1, 2], [3, 1, 1, 1])", list.splitWhileListIsTrueFor(l -> sum(l) <= 3).toString());
        assertEquals("([], [1, 2, 3, 1, 1, 1])", list.splitWhileListIsTrueFor(l -> sum(l) <= 0).toString());
        assertEquals("([1, 2, 3, 1, 1, 1], [])", list.splitWhileListIsTrueFor(l -> true).toString());
    }

    @Test
    public void testGroup$()
    {
        ImList<Integer> list = ImList.on(1, 2, 3, 1, 1, 1);

        assertEquals("[[1, 2], [3], [1, 1, 1]]", list.group$(l -> sum(l) <= 3).toString());
    }

    @Test
    public void testGetTextBox()
    {
        ImList<Integer> list = ImList.on(1, 2, 3, 1, 1, 1);

        assertEquals("[[1, 2], [3], [1, 1, 1]]", list.group$(l -> sum(l) <= 3).toString());
    }

    @Test
    public void testGroup$WithRandomWidths()
    {
        ImList<ImList<Integer>> ls = randomInts(new Random(), 3).take(30).heads();

        for (ImList<Integer> ll : ls)
        {
            checkGroup(ll, l -> sum(l) <= 3);
        }
    }

    void checkGroup(ImList<Integer> listToGroup, Fn<ImList<Integer>, Boolean> pred)
    {
        ImList<ImList<Integer>> groups = listToGroup.group$(pred);

        // All the groups pass the predicate
        assertEquals(ImMaybe.nothing(), groups.find(l -> !pred.of(l)));

        // None of the groups are empty
        assertEquals(ImMaybe.nothing(), groups.find(l -> l.isEmpty()));

        // They all add up to the original list
        assertEquals(listToGroup, ImList.join(groups));
    }

    @Test
    public void testSplitIntoCount()
    {
        ImList<Integer> list = ImRange.oneTo(6);

        assertEquals("[[1, 2, 3, 4, 5, 6]]", list.splitIntoParts(1).toString());
        assertEquals("[[1, 2, 3], [4, 5, 6]]", list.splitIntoParts(2).toString());
        assertEquals("[[1, 2], [3, 4], [5, 6]]", list.splitIntoParts(3).toString());
        assertEquals("[[1, 2], [3, 4], [5], [6]]", list.splitIntoParts(4).toString());
        assertEquals("[[1, 2], [3], [4], [5], [6]]", list.splitIntoParts(5).toString());
        assertEquals("[[1], [2], [3], [4], [5], [6]]", list.splitIntoParts(6).toString());
        assertEquals("[[1], [2], [3], [4], [5], [6], []]", list.splitIntoParts(7).toString());
        assertEquals("[[1], [2], [3], [4], [5], [6], [], []]", list.splitIntoParts(8).toString());
    }

    @Test
    public void testSplitIntoCount2()
    {
        ImList<Integer> list = ImRange.oneTo(2);
        assertEquals("[[1, 2]]", list.splitIntoParts(1).toString());

    }

    @Test
    public void testSplitIntoMaxSize()
    {
        ImList<Integer> list = ImRange.oneTo(3);

        TestUtils.assertThrows(() -> list.splitIntoMaxSize(0), ArgumentShouldNotBeLessThan.class);

        assertEquals("[[1], [2], [3]]", list.splitIntoMaxSize(1).toString());
        assertEquals("[[1, 2], [3]]", list.splitIntoMaxSize(2).toString());
        assertEquals("[[1, 2, 3]]", list.splitIntoMaxSize(3).toString());
        assertEquals("[[1, 2, 3]]", list.splitIntoMaxSize(4).toString());

    }

    public static Double sum(ImList<Integer> ds)
    {
        return ds.foldl(0.0, (z, i) -> z + i);
    }

    @Test
    public void testHeads()
    {
        ImList<Integer> list = ImList.on(1, 2, 3);

        ImList<ImList<Integer>> hs = list.heads();

        say(hs.getClass());

        say(hs.toString());

        //        assertEquals("[[], [1], [1, 2], [1, 2, 3]]", list.heads().toString());
        //        assertEquals("[[]]", ImList.on().heads().toString());
    }

    @Test
    public void testMap()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertEquals("[3, 4, 5]", list.map(i -> i + 2).toS());

        assertEquals((Integer) 4, list.map(i -> i + 3).head());
    }

    @Test
    public void testUnfold()
    {
        ImList<String> strings = unfold(ONE, i -> i + 1).map(i -> "" + i).take(5);

        TestUtils.assertSameElements(strings, strings);
    }

    @Test
    public void testStreams()
    {

        Stream<Integer> s = Stream.of(1, 2, 3);

        ImList<Integer> expected = ImList.on(1, 2, 3);

        Iterator<Integer> iterator = s.iterator();

        assertEquals(expected, ImList.onIterator(iterator));
    }

    @Test
    public void testRandomPicks()
    {
        int top = 10;
        ImList<Integer> list = ImRange.inclusive(0, top);

        int count = 1000;
        ImList<Integer> picks = list.randomPicks().take(count);

        float total = (float) picks.foldl(ZERO, (i, j) -> i + j);

        double average = total / count;

        // the average should be roughly top/2

        double diff = Math.abs((top / 2 - average) / top);

        assertEquals(true, diff < 0.05);
    }

    @Test
    public void testOnIterator()
    {
        ImList<Integer> list = ImList.onIterator(Arrays.asList(1, 2).iterator());

        assertEquals(list.toString(), list.toString());
    }

    @Test
    public void testRepeat()
    {
        ImList<Integer> list = repeat(12).take(3);

        assertEquals(on(12, 12, 12), list);
    }

    @Test
    public void testFilter()
    {
        ImList<Integer> list = on(1, 2, 3, 4);

        assertEquals(on(1, 2, 3, 4), list.filter(i -> i > 0));
    }

    @Test
    public void testFind()
    {
        ImList<Integer> list = on(1, 2, 3, 4);

        assertEquals(ImMaybe.just(3), list.find(i -> i == 3));
    }

    @Test
    public void testFindM()
    {
        ImList<Integer> list = on(1, 2, 3, null, null);

        assertEquals(ImMaybe.just(3), list.findM(i -> i > 2 ? ImMaybe.just(i) : ImMaybe.nothing()));
    }

    @Test
    public void testFindIndex()
    {
        ImList<Integer> list = on(1, 2, 3, 4);

        assertEquals(ImMaybe.just(3), list.findIndex(i -> i == 3));
    }

    @Test
    public void testToArray2()
    {
        Integer[] arr = { 1, 2, 3, 4 };

        ImList<Integer> arrLst = ImList.on(1, 2, 3, 4).map(i -> i + 0);

        Integer[] actuals = arrLst.toArray(Integer.class);
        assertArrayEquals(arr, actuals);
    }

    @Test
    public void testJavaArrays()
    {
        Integer[] arr = { 1, 2, 3, 4 };

        // This doesn't even need a cast!
        Object[] oarr = arr;

        assertEquals(Integer.class, oarr.getClass().getComponentType());

        TestUtils.assertThrows(() -> oarr[0] = "", ArrayStoreException.class);

        List<Integer> integerList = new ArrayList<>();

        // The cast does not compile
        // List<Object> objectList = (List<Object>)integerList;

    }

    @Test
    public void testToObjectArray()
    {
        Object[] arr = { 1, 2, 3, 4 };

        ImList<Integer> names = ImList.on(1, 2, 3, 4);

        ImList<Integer> ns = names.map(n -> n + 0);

        Integer[] ss = ns.toArray(Integer.class);

        assertArrayEquals(arr, ss);
    }

    @Test
    public void testToObjectArray2()
    {
        Object[] arr = { "a", "b" };

        ImList<String> names = ImList.on("a", "b");

        ImList<String> ns = names.map(n -> n + "");

        String[] ss = ns.toArray(String.class);

        assertArrayEquals(arr, ss);
    }

    @Test
    public void testSort()
    {
        System.out.println("Making the lists");
        ImList<Integer> large = ImRange.oneTo(100);
        ImList<Integer> large2 = large.reverse().flush();
        List<Integer> before = large2.toList();

        System.out.println("running old sort");

        ImPair<Timer, ImList<Integer>> p = Timer.time(() -> on(before.stream().sorted().collect(Collectors.toList())).flush());

        assertEquals(large, p.snd);
        System.out.println(p.fst.getMillis());

        System.out.println("running new sort");

        ImPair<Timer, ImList<Integer>> p2 = Timer.time(() -> large2.sort().flush());

        assertEquals(large, p2.snd);
        System.out.println(p2.fst.getMillis());
    }

    @Test
    public void docExample1()
    {

        ImList<Integer> numbers = ImList.unfold(1, i -> i + 1);
        ImList<Integer> squares = numbers.map(i -> i * i);

        ImList<String> words = numbers.map(i -> ServerTextUtils.toWord(i));

        ImList<String> results = words.zipWith(squares, (s, i) -> s + " squared is " + i);

        say(results.take(3).toString("\n"));
    }

    @Test
    public void docExample2()
    {
        ImList<Integer> numbers = ImList.unfold(1, i -> i + 1);
        ImList<Integer> squares = numbers.map(i -> i * i);

        ImList<String> words = numbers.map(i -> ServerTextUtils.toWord(i));

        ImList<String> results = words.zipWith(squares, (s, i) -> s + " squared is " + i);

        say(results.take(3).toString("\n"));

        ImList<Object> objects = ImList.on(numbers, squares, words, results, results.take(3));
        objects = objects.appendElement(objects);

        ImList<String> names = ImList.on("numbers", "squares", "words", "results", "results.take(3)", "objects");

        AbstractTextBox output = TopDownBox.withAll(names).before(LeafTextBox.with(" ")).before(TopDownBox.withAll(objects.map(o -> ClassUtils.simpleNameOf(o))));

        say(output);

        say(ClassUtils.simpleNameOf(objects));

    }

    @Test
    public void testSortWithGetter()
    {
        ImList<ImPair<Integer, String>> unsorted = on(Pai.r(1, "c"), Pai.r(3, "a"), Pai.r(2, "b"));

        ImList<ImPair<Integer, String>> sorted = unsorted.sort(p -> p.snd);
        assertEquals(ImList.on(Pai.r(3, "a"), Pai.r(2, "b"), Pai.r(1, "c")), sorted);

        ImList<ImPair<Integer, String>> sorted1 = unsorted.sort(p -> p.fst);
        assertEquals(ImList.on(Pai.r(1, "c"), Pai.r(2, "b"), Pai.r(3, "a")), sorted1);
    }

    @Test
    public void testSortWithComparator()
    {
        ImList<ImPair<Integer, String>> unsorted = on(Pai.r(1, "c"), Pai.r(3, "a"), Pai.r(2, "b"));

        ImList<ImPair<Integer, String>> sorted = unsorted.sort((p, q) -> Eq.uals(p.fst, q.fst)
                                                                         ? p.snd.compareTo(q.snd)
                                                                         : p.fst.compareTo(q.fst));

        assertEquals(ImList.on(Pai.r(1, "c"), Pai.r(2, "b"), Pai.r(3, "a")), sorted);

    }

    @Test
    public void testContains()
    {
        ImList<String> strings = on("", "1", "2");

        assertEquals(true, strings.contains(""));
        assertEquals(true, strings.contains("1"));
        assertEquals(true, strings.contains("2"));

        assertEquals(false, strings.contains((String) null));
    }

    @Test
    public void testContainsWithNull()
    {
        ImList<String> strings = on(null, "1", "2");

        System.out.println(strings);

        assertEquals(false, strings.contains(""));
        assertEquals(true, strings.contains("1"));
        assertEquals(true, strings.contains("2"));

        assertEquals(true, strings.contains((String) null));
    }

    @Test
    public void testContainsAll()
    {
        ImList<Number> xs = ImList.on(1, 2, 3);
        ImList<Integer> ys = ImList.on(1, 2, 3);

        xs.containsAll(ys.upCast());
    }

    @Test
    public void testGenerateListsByInjecting()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertEquals("[[0, 1, 2, 3], [1, 0, 2, 3], [1, 2, 0, 3], [1, 2, 3, 0]]", "" + list.generateListsByInjecting(0));
    }

    @Test
    public void testPermutations()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertEquals("[[1, 2, 3], [2, 1, 3], [2, 3, 1], [1, 3, 2], [3, 1, 2], [3, 2, 1]]", "" + list.permutations());

        new ImIpList(ImList.on(), 2, ImList.on());
    }

    @Test
    public void testJoin()
    {
        ImList<Integer> list1 = on(1, 2, 3);
        ImList<Integer> list2 = on(4, 5);
        ImList<Integer> list3 = on();
        ImList<Number> list4 = on();

        assertEquals("[1, 2, 3, 4, 5]", "" + join(on(list1, list2, list3)));
        assertEquals("[1, 2, 3, 4, 5]", "" + join(list1, list2, list3, list4));

        assertEquals(5, join(on(list1, list2, list3)).size());
    }

    @Test
    public void testIntersperseOnEmptyList()
    {
        ImList<String> list1 = on();

        assertEquals(on(), list1.intersperse("-"));
    }

    @Test
    public void testIntersperse()
    {
        ImList<String> list1 = on("a", "b", "c");

        assertEquals(on("a", "-", "b", "-", "c"), list1.intersperse("-"));
    }

    @Test
    public void testPermutationsLarge()
    {
        int n = 9;
        ImList<Integer> list = ImRange.oneTo(n);

        System.out.println("start");
        ImList<ImList<Integer>> perms = list.permutations();
        System.out.println("done perms");
        System.out.println(perms.at(Sums.factorial(n)));
        assertEquals(Sums.factorial(n), perms.size());
    }

    @Test
    public void testAllPairsLarge()
    {
        int n = 9;
        ImList<Integer> list = ImRange.oneTo(n);

        //        System.out.println("start");
        ImList<ImList<Integer>> perms = list.permutations();
        //        System.out.println("done perms");
        ImList<ImPair<ImList<Integer>, ImList<Integer>>> allPairs = perms.allPairs();
        System.out.println("done allPairs");
        System.out.println(allPairs.take(100).size());
    }

    @Ignore
    public void testStack()
    {

        System.out.println(Integer.MAX_VALUE);
        recurse("khgkhkjdhg", "khgkhkjdhg", 1);
    }

    private int recurse(String s, String s2, int i)
    {
        if (i % 1000 == 0)
            System.out.println(i);
        return i < Integer.MAX_VALUE
               ? recurse(s, s2, i + 1)
               : i - 1;
    }

    @Test
    public void testTake()
    {
        assertEquals("[1, 2, 3, 4, 5]", "" + ImRange.oneTo(9).take(5));
        assertEquals("[1, 2, 3]", "" + ImRange.oneTo(3).take(5));
    }

    @Test
    public void testDrop()
    {
        assertEquals("[6, 7, 8, 9]", "" + ImRange.oneTo(9).drop(5));
        assertEquals("[]", "" + ImRange.oneTo(9).drop(9));
        assertEquals("[]", "" + ImRange.oneTo(9).drop(10));
    }

    @Test
    public void testPut()
    {
        assertEquals("[1, 2, 0, 4]", "" + ImRange.oneTo(4).put(3, 0));
        assertEquals("[0, 2, 3, 4]", "" + ImRange.oneTo(4).put(1, 0));
        assertEquals("[1, 2, 3, 0]", "" + ImRange.oneTo(4).put(4, 0));
    }

    @Test
    public void testPutThrows()
    {
        try
        {
            on().put(1, 0);
            fail();
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testDropWhile()
    {
        assertEquals("[6, 7, 8, 9]", "" + ImRange.oneTo(9).dropWhile(i -> i < 6));
    }

    @Test
    public void testRemove()
    {
        assertEquals(on(1, 2), on(3, 1, 2, 3).remove(3));
    }

    @Test
    public void testRemoveWithNulls()
    {
        assertEquals(on(null, 2), on(3, null, 2, 3).remove(3));
        assertEquals(on(2), on(null, 2, null).remove(null));
    }

    @Test
    public void testRemoveAll()
    {
        ImList<Integer> start = on(null, 1, 3, 2, 3, 3);
        ImList<Integer> large = on(null, 1, 2, 3);

        large.permutations().all(ls -> start.removeAll(ls).isEmpty());
    }

    @Test
    public void testRemoveAt()
    {

        ImList<Integer> oneTo9 = ImRange.oneTo(9);

        ImList<ImList<Integer>> expected = oneTo9.map(i -> oneTo9.remove(i));
        ImList<ImList<Integer>> actual = oneTo9.map(i -> oneTo9.removeAt(i));

        assertEquals(expected, actual);

        assertEquals(ImList.on(1), ImList.on(1).removeAt(200));
        assertEquals(ImList.on(1), ImList.on(1).removeAt(-150));
        assertEquals(ImList.on(2, 3), ImList.on(1, 2, 3).removeAt(1));
    }

    @Test
    public void testRemoveAtNegativeIndex()
    {
        assertEquals(on(1, 2), on(1, 2).removeAt(-1));
    }

    @Test
    public void testArrays()
    {
        //        say(ImRange.oneTo(6).toArray(Integer.class).getClass());
        //        ImList<Integer> flush = ImRange.oneTo(6).flush();
        //        say(flush.toArray(Integer.class).getClass());

        ImList<Object> on = ImList.on(1, 2, 3);

        say(on.toArray(Integer.class).getClass());
    }

    @Test
    public void testToArray4()
    {
        Integer[] os = { 1, 2 };

        say(Arrays.copyOfRange(os, 0, 1, Integer[].class).getClass());

        //        Object[] a = new Object[1];
        //        Integer b = 1;
        //        a[0] = b;
        //
        //        Arrays.copyOf(a, 1, Integer[].class);
        //        Integer[] c = (Integer[]) a;
    }

    @Test
    public void testRemoveAtOnInfiniteLists()
    {
        ImList<Integer> start = ImRange.step(1, 1);

        ImList<Integer> result = start.removeAt(3);

        assertEquals(Integer.valueOf(4), result.at(3));
        assertEquals(ImLazyList.KNOWN_INFINITE, result.getSz());
    }

    @Test
    public void testSplitAt()
    {
        ImList<Integer> start = ImRange.oneTo(5);

        assertEquals(ImPair.on(ImList.on(1, 2, 3), ImList.on(4, 5)), start.splitAfterIndex(3));

        // Split the list several times
        ImList<ImPair<ImList<Integer>, ImList<Integer>>> pairs = ImRange.inclusive(0, 6).map(i -> start.splitAfterIndex(i));

        // Adding together the first part and the second part should always give the original list
        assertTrue(ImPair.map(pairs, (i, j) -> i.append(j)).all(s -> s.equalsList(start)));

        pairs.foreach(p -> say(p));

        // Check the size of the first part
        pairs.zipWith(ImRange.inclusive(0, 6), (p, n) -> checkSplitSize(start, p.fst, p.snd, n)).flush();
    }

    @Test
    public void testSplitAtOnInfiniteLists()
    {
        ImList<Integer> start = ImRange.step(1, 2);

        ImPair<ImList<Integer>, ImList<Integer>> pair = start.splitAfterIndex(2);

        assertEquals(on(1, 3), pair.fst);
        assertEquals(ImLazyList.KNOWN_INFINITE, pair.snd.getSz());
    }

    private boolean checkSplitSize(ImList<Integer> orig, ImList<Integer> fst, ImList<Integer> snd, Integer n)
    {
        boolean ok;
        if (n < 0)
            ok = fst.size() == 0;
        else if (n > orig.size())
            ok = fst.size() == orig.size();
        else
            ok = fst.size() == n;

        if (!ok)
            fail(String.format("splitAt failed: %s splitAt %d != %s ++ %s", orig, n, fst, snd));

        return true;
    }

    @Test
    public void testAt()
    {
        assertEquals(Integer.valueOf(9), ImRange.oneTo(9).at(9));
    }

    @Test
    public void testLast()
    {
        assertEquals("b", on("a", "b").last());
    }

    @Test
    public void testLastThrows()
    {
        try
        {
            on().last();
            fail();
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testAtThrows()
    {
        try
        {
            on().at(1);
            fail();
        } catch (Exception e)
        {
        }
    }

    @Test
    public void testPermutations2()
    {
        ImList<Integer> list = on(1);

        assertEquals("[[1]]", "" + list.permutations());
    }

    @Test
    public void testPermutations3()
    {
        ImList<Integer> list = on(1, 2, 3);

        ImList<ImList<Integer>> perms = list.permutations();

        TestUtils.assertSameElements(perms, perms);
    }

    @Test
    public void testAll()
    {
        ImList<Integer> list = on(1, 2, 3);
        ImList<Integer> empty = on();

        assertTrue(list.all(i -> i > 0));
        assertFalse(list.all(i -> i > 2));

        assertTrue(empty.all(i -> i > 0));
    }

    @Test
    public void testAny()
    {
        ImList<Integer> list = on(1, 2, 3);

        assertFalse(list.any(i -> i > 3));

        assertFalse(ImList.on().any(i -> i == null));
    }

    @Test
    public void testTransposeSingle()
    {
        ImList<Integer> r1 = on(1);

        ImList<ImList<Integer>> m = on(r1);

        assertEquals(m, transpose(m, 1, 1));
    }

    @Test
    public void testTranspose0cols()
    {
        ImList<ImList<Integer>> m = on(on());

        assertEquals(on(), transpose(m, 1, 0));
    }

    @Test
    public void testTranspose0rows()
    {
        ImList<ImList<Integer>> m = on();

        assertEquals(repeat(on(), 2), transpose(m, 0, 2));
    }

    @Test
    public void testTransposeRowsError()
    {
        ImList<ImList<Integer>> m = on();

        try
        {
            transpose(m, 1, 2);
        } catch (TransposeRowsError e)
        {
            assertEquals("rows = 1 but matrix rows = 0", e.getMessage());
        }
    }

    @Test
    public void testTransposeColsError()
    {
        ImList<ImList<Integer>> m = on(on(1));

        try
        {
            transpose(m, 1, 2);
        } catch (TransposeColsError e)
        {
            assertEquals("rows = 2 but matrix = [[1]]", e.getMessage());
        }
    }

    @Test
    public void testTransposeTransposeIsIdentity()
    {
        ImList<Integer> r1 = on(1, 2, 3);
        ImList<Integer> r2 = on(4, 5, 6);

        ImList<ImList<Integer>> m2 = on(r1, r2);

        assertEquals(m2, transpose(transpose(m2, 2, 3), 3, 2));

        for (int rows = 0; rows < 4; rows++)
            for (int cols = 0; cols < 4; cols++)
            {
                ImList<ImList<Integer>> m = createMatrix(rows, cols);

                ImList<ImList<Integer>> mt = transpose(m, rows, cols);

                assertEquals(m, transpose(mt, cols, rows));

                //                if (rows > 0 && cols > 0)
                for (int r = 1; r <= rows; r++)
                {
                    for (int c = 1; c <= cols; c++)
                    {

                        assertEquals("" + r + " " + c, get(r, c, m), get(c, r, mt));
                    }
                }
            }
    }

    @Test
    public void testCross()
    {
        ImList<Integer> r1 = on(1, 2);
        ImList<ImList<Integer>> r2 = repeat(r1, 3);

        assertEquals("[[1, 1, 1], [1, 1, 2], [1, 2, 1], [1, 2, 2], [2, 1, 1], [2, 1, 2], [2, 2, 1], [2, 2, 2]]", cross(r2.upCast()).toString());

        //TODO improve the checks here - see testCartesianProduct
    }

    @Test
    public void testCross2()
    {

        ImList<String> one = on("1", "2");
        ImList<String> two = on("a", "b");
        ImList<String> three = on("x", "y", "z");

        assertEquals("[[1, a, x], [1, a, y], [1, a, z], [1, b, x], [1, b, y], [1, b, z], [2, a, x], [2, a, y], [2, a, z], [2, b, x], [2, b, y], [2, b, z]]",
                cross(ImList.on(one, two, three)).toString());

    }

    @Test
    public void testCartesianProduct()
    {
        ImList<Integer> r1 = on(1, 2);
        ImList<Integer> r2 = on(2, 3, 4);

        ImList<ImPair<Integer, Integer>> things = cartesianProduct(r1, r2);

        // All the pairs must have elements that belong to the sets
        assertTrue(things.filter(p -> !(r1.contains(p.fst) && r2.contains(p.snd))).isEmpty());

        // There must be r1.size() * r2.size() distinct elements
        assertEquals(r1.size() * r2.size(), things.toSet().size());
    }

    private Integer get(int r, int c, ImList<ImList<Integer>> m)
    {
        return m.at(r).at(c);
    }

    @Test
    public void testArgsError()
    {
        for (int rows = 0; rows < 3; rows++)
            for (int cols = 0; cols < 3; cols++)
                checkArgsError(rows, cols);
    }

    private void checkArgsError(int rows, int cols)
    {
        ImList<ImList<Integer>> m = createMatrix(rows, cols);

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
            {
                // rows and i must agree. If rows != 0 then cols and j must agree
                assertEquals("m = " + m + "     rows = " + rows + " cols = " + cols + " i,j = " + i + "," + j, //
                        i == rows && (rows == 0 || j == cols), tryTranspose(m, i, j));
            }
    }

    private boolean tryTranspose(ImList<ImList<Integer>> m, int i, int j)
    {
        try
        {
            transpose(m, i, j);
            return true;
        } catch (TransposeColsError | TransposeRowsError e)
        {
            return false;
        }
    }

    private ImList<ImList<Integer>> createMatrix(int rows, int cols)
    {
        ImList<Integer> rowOne = ImRange.oneTo(cols).map(j -> Integer.valueOf(j));

        return unfold(rowOne, r -> r.map(i -> i + cols)).take(rows);
    }

    @Test
    public void testSerialization() throws Exception
    {
        ImList<Integer> list = on(Arrays.asList(1, 2, 3));

        checkCanBeSerialized(list);
        checkCanBeSerialized(list.map(i -> i + 1));
        checkCanBeSerialized(ImList.onIterator(list.iterator()));
    }

    @Test
    public void testGroup()
    {
        ImRange.oneTo(7).foreach(n ->
        {
            ImList<Integer> list = ImRange.oneTo(n);

            ImList<ImList<Integer>> ls = list.group(3);

            assertEquals((n - 1) / 3 + 1, ls.size());

            assertEq(list, join(ls));
        });
    }

    @Test
    public void testIsSubsequence()
    {
        ImList<Integer> large = on(0, 1, 1, 2, 3, 4);

        assertEquals(true, large.powerSet().all(ls -> ls.isSubSequence(large)));
    }

    @Test
    public void testIsSubsequenceWithNulls()
    {
        ImList<Integer> large = on(0, 1, 2, null);

        assertEquals(true, large.powerSet().all(ls -> ls.isSubSequence(large)));
    }

    @Test
    public void testNub()
    {
        assertEquals(on(1, 2, 3), on(1, 2, 3, 1, 2, 3, 3, 3).nub());

        ImList<Integer> rl = randomInts(new Random(), 3).take(10);

        ImList<Integer> nub = rl.nub();

        assertTrue(nub.size() <= rl.size());
        assertEquals(rl.toImSet(), nub.toImSet());
        assertTrue(nub.isSubSequence(rl));
    }

    @Test
    public void testToImSet()
    {
        ImList<Integer> is = ImRange.oneTo(3);
        assertEquals(ImSet.onIterator(is.iterator()), is.toImSet());
    }

    @Test
    public void testToImSetWithNull()
    {
        TestUtils.assertThrows(() -> ImList.on(1, null).toImSet(), NullPointerException.class);

    }

    @Test
    public void testMapWithIndex()
    {
        ImList<Integer> is = ImRange.oneTo(3);
        ImList<Integer> zeros = is.mapWithIndex((i, j) -> i - j);
        assertTrue(zeros.all(i -> i == 0));
    }

    @Test
    public void testAtTiming()
    {
        int TEST_COUNT = 10;
        ImList<Integer> lst = ImRange.inclusive(-12, TEST_COUNT);

        say("lst", lst);
        Integer[] array = lst.toArray(Integer.class);

        say("array", array);
        ImList<Integer> is = ImList.on(array).drop(13);

        System.out.println(is.getClass());

        assertEquals(TEST_COUNT, is.size());
        assertEquals(Integer.valueOf(6), is.at(6));

        var time = Timer.time(() ->
        {
            for (int i = 0; i < TEST_COUNT; i++)
            {
                is.at(Rando.nextInt(1, is.size()));
            }
        });

        System.out.println(time.getMs());
    }

    @Test
    public void testCharacterToString()
    {
        Character zero = Character.valueOf((char) 0);
        Character one = Character.valueOf((char) 1);
        Character two = Character.valueOf((char) 2);
        Character x7F = Character.valueOf((char) 0x7F);
        Character x90 = Character.valueOf((char) 0x90);
        ImList<Character> l = on(zero, one, two, x7F, x90);

        assertEquals(5, l.size());

        String s = l.toString("");
        assertEquals(5, s.length());

    }

    @Test
    public void testInterleave()
    {
        ImList<Integer> a = on(1, 2);
        ImList<Integer> b = on(3, 4);
        assertEquals("[1, 2, 3, 4], [1, 3, 2, 4], [1, 3, 4, 2], [3, 1, 2, 4], [3, 1, 4, 2], [3, 4, 1, 2]", interleave(a, b).toString(", "));
    }

    @Test
    public void testInterleave2()
    {
        ImList<Integer> a = on(1, 2, 3);
        ImList<Integer> b = on(4, 5);
        assertEquals("[1, 2, 3, 4, 5], [1, 2, 4, 3, 5], [1, 2, 4, 5, 3], [1, 4, 2, 3, 5], [1, 4, 2, 5, 3], [1, 4, 5, 2, 3], [4, 1, 2, 3, 5], [4, 1, 2, 5, 3], [4, 1, 5, 2, 3], [4, 5, 1, 2, 3]",
                interleave(a, b).toString(", "));
    }

    @Test
    public void testInterleaveCommutes()
    {
        ImList<Integer> a = on(1, 2, 3);
        ImList<Integer> b = on(4, 5);
        assertEquals(interleave(a, b).toSet(), interleave(b, a).toSet());
    }

    private void checkCanBeSerialized(ImList<Integer> list) throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream s1 = new ByteArrayOutputStream();
        ObjectOutputStream s4 = new ObjectOutputStream(s1);

        s4.writeObject(list);

        s4.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(s1.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);

        Object readObject = ois.readObject();
        ois.close();

        assertEquals("" + list, "" + readObject);
    }

    @Test
    public void testOn()
    {
        ImList<Integer> a = on();

        assertSame(ImList.empty(), a);

    }

    @Test
    public void testToPairs()
    {
        ImList<Integer> four = ImList.on(1, 2, 3, 4);

        assertEquals(ImList.on(ImPair.on(1, 2), ImPair.on(3, 4)), four.toPairs());
        assertEquals(ImList.on(ImPair.on(1, 2)), four.take(2).toPairs());
    }

    @Test
    public void testToPairsThrowsWhenSizeIsOdd()
    {
        ImList<Integer> three = ImList.on(1, 2, 3);

        TestUtils.assertThrows((() -> three.toPairs()), IllegalState.class);
    }

    @Test
    public void testReplace()
    {
        ImList<Integer> three = ImList.on(1, 2, 3);

        assertEquals(ImList.on(1, 4, 3), three.replace(i -> i == 2, 4));
        assertEquals(ImList.on(5, 5, 5), three.replace(i -> true, 5));
        assertEquals(three, three.replace(i -> i == 7, 100));
    }

    @Test
    public void testAnd()
    {
        ImList<Boolean> four = on(true, true, false, false);

        assertFalse(ImList.and(four));

        four.powerSet().foreach(l -> assertEquals(ImList.and(l), !l.find(i -> !i).isPresent()));
    }

    @Test
    public void testOr()
    {
        ImList<Boolean> four = on(true, true, false, false);

        assertTrue(ImList.or(four));

        four.powerSet().foreach(l -> assertEquals(ImList.or(l), l.find(i -> i).isPresent()));
    }

    @Test
    public void testRevs()
    {

        ImRange.inclusive(0, 6).foreach(n ->
                {
                    ImList bs = n == 0 ? ImList.on() : ImList.on(ImRange.oneTo(n).toArray(Integer.class));
                    ImList r = reverseOld(bs);

                    assertEquals(r, bs.reverse());

                    assertEquals(bs, bs.reverse().reverse());

                    assertEquals(r.drop(2), bs.reverse().drop(2));
                    assertEquals(r.take(3), bs.reverse().take(3));
                }
        );

    }

    @Test
    public void testReverseTimings()
    {
        int m = 8;

        //        say("ImRange");
        ImRange.oneTo(m).foreach(n ->
                {
                    int count = 4096 << (n);
                    //                    say();
                    //                    say(count);
                    testRev2(count, (l -> l.reverse()));

                    //                    say("old", count, testRev2(count, (l -> reverseOld(l))));
                    //                    say("new", count, testRev2(count, (l -> l.reverse())));
                    //                    say("ReversLst", count, testRev2(count, (l -> ImReverseList.on(l.toArray(Object.class)))));

                }
        );

        //        say("");
        //
        //        say("LstOnArray");
        ImRange.oneTo(m).foreach(n ->
                {
                    int count = 4096 << (n);
                    //                    say();
                    //                    say(count);
                    testRevArray(count, (l -> reverseOld(l)));
                    testRevArray(count, (l -> l.reverse()));

                    //                    say("old", count, testRevArray(count, (l -> reverseOld(l))));
                    //                    say("new", count, testRevArray(count, (l -> l.reverse())));

                }
        );
    }

    @Test
    public void testReverseTimingForSum()
    {

        int m = 8;

        ImRange.oneTo(m).foreach(n ->
                {
                    int count = 4096 << (n);
                    ImList as = ImRange.oneTo(count);
                    ImList is = ImList.on(as.toArray(Object.class));
                    //                    say();
                    //                    say(count);
                    timeSum(is);
                    timeSum(is.reverse());

                    //                    say("reverse ", timeSum(is.reverse()));
                    //                    say("original", timeSum(is));

                }
        );

    }

    long testRev2(int count, Fn<ImList, ImList> fn)
    {
        ImList bs = ImRange.oneTo(count);

        ImPair<Timer, ImList> res = Timer.time(() -> {
            ImList<Object> r = fn.of(bs).flush();
            r.head();
            return r;
        });

        return res.fst.getMillis();
    }

    long timeSum(ImList<Integer> is)
    {
        ImPair<Timer, Integer> res = Timer.time(() -> Util.sumInt(is));

        return res.fst.getMillis();
    }

    long testRevArray(int count, Fn<ImList, ImList> fn)
    {
        ImList as = ImRange.oneTo(count).flush();
        ImList bs = ImList.on(as.toArray(Object.class));

        ImPair<Timer, ImList> res = Timer.time(() -> {
            ImList<Object> r = fn.of(bs).flush();
            r.head();
            return r;
        });

        return res.fst.getMillis();
    }

    /**
     * The original implementation
     */
    static <A> ImList<A> reverseOld(ImList<A> orig)
    {
        ImList<A> reverse = empty();
        ImList<A> old = orig;

        while (!old.isEmpty())
        {
            reverse = cons(old.head(), reverse);
            old = old.tail();
        }

        return reverse;
    }

    @Test
    public void testCutIntoTwo()
    {
        var is = ImList.on(1, 2, 3, 4, 5);

        var pair = is.cutIntoTwo(i -> i <= 4);

        assertEquals(pair, ImPair.on(ImList.on(1, 2, 3, 4), ImList.on(5)));
    }

    @Test
    public void testCutIntoTwoWhenFisrtIsEmpty()
    {
        var is = ImList.on(1, 2, 3, 4, 5);

        var pair = is.cutIntoTwo(i -> i > 4);

        assertEquals(pair, ImPair.on(ImList.on(), is));
    }

    @Test
    public void testCutIntoTwoWhenSecondIsEmpty()
    {
        var is = ImList.on(1, 2, 3, 4, 5);

        var pair = is.cutIntoTwo(i -> i > 0);

        assertEquals(pair, ImPair.on(is, ImList.on()));
    }

    @Test
    public void testCutIntoTwoWhenBothEmpty()
    {
        var is = ImList.on();

        var pair = is.cutIntoTwo(i -> true);

        assertEquals(pair, ImPair.on(ImList.on(), ImList.on()));
    }

    @Test
    public void testCutIntoThree()
    {
        var is = ImRange.oneTo(5);

        is.foreach(n -> {

            // Set up a predicate and its negation
            Fn<Integer, Boolean> fn = i -> i > n && i <= n + 1;
            Fn<Integer, Boolean> fnNot = i -> !fn.of(i);

            // Do this for every subset of is
            is.powerSet().foreach(ns -> {

                // say(ns);

                var t = ns.cutIntoThree(fn);

                // say(t);

                // Should all be subsequences
                assertEquals(ns, ImList.join(t.e1, t.e2, t.e3));

                // The obvious assertions
                assertTrue(t.e1.all(fnNot));
                assertTrue(t.e2.all(fn));
                assertTrue(t.e3.all(fnNot));
            });
        });
    }

    @Test
    public void testShuffleIsVaguelyRandom()
    {
        /**
         * A very crude test to assert that if we shuffle a list of 6 integers 24 times, then almost all of these shuffles will not have any repeats in them
         */
        ImList<Integer> range = ImRange.oneTo(6);

        int runCount = 40;
        ImList<Integer> all = unfold(getUniqueListSize(range), i -> getUniqueListSize(range)).take(runCount);

        double average = Util.sumInt(all) / (double) runCount;
        say("average", average);
        assertTrue(average > 23);

    }

    private static int getUniqueListSize(ImList<Integer> range)
    {
        ImList<ImList<Integer>> subs = ImList.repeat(range, 24).map(r -> r.shuffle());

        return subs.foldl(ImSet.empty(), (z, i) -> z.add(i)).size();
    }

}