/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 import dev.javafp.eq.*;
 import dev.javafp.ex.*;
 import dev.javafp.box.*;
 import dev.javafp.lst.*;
 import dev.javafp.map.*;
 import dev.javafp.net.*;
 import dev.javafp.set.*;
 import dev.javafp.sql.*;
 import dev.javafp.val.*;
 import dev.javafp.graph.*;
 import dev.javafp.queue.*;
 import dev.javafp.shelf.*;
 import dev.javafp.tuple.*;
 import dev.javafpinternal.diff.*;
 import dev.javafpinternal.util.*;
 import dev.javafpinternal.delete.*;
 import dev.javafpinternal.parse.*;
 import dev.javafpinternal.sheet.*;
 import dev.javafpinternal.interval.*;
 import dev.javafpinternal.ex.*;
 import dev.javafp.file.*;
 import dev.javafp.func.*;
 import dev.javafp.geom.*;
 import dev.javafp.rand.*;
 import dev.javafp.time.*;
 import dev.javafp.tree.*;
 import dev.javafp.util.*;
 * <p> The main immutable collection classes.
 * <h2>Introduction</h2>
 * <p> These are Java implementations of data structures that you find in functional programming languages.
 * <p> The main difference from Java collections is that they are
 * <em>persistent</em>
 * . In this context, persistence has nothing
 * to do with disk or database storage but is the property that operations on a collection
 * to add/remove/update elements will not mutate the collection in place but instead will create a new collection that
 * contains the required changes - leaving the old collection unchanged.
 * <p> So, instead of this:
 *
 * <pre>{@code
 * List<Integer> list = new ArrayList<Integer>();
 * list.add(1);
 * list.add(2);
 * }</pre>
 * <p> You might say this:
 *
 * <pre>{@code
 * ImShelf<Integer> shelf = ImShelf.empty();
 * ImShelf<Integer> shelf1 = shelf.adding(1);
 * ImShelf<Integer> shelf2 = shelf1.adding(2);
 * }</pre>
 * <p> Of course you don't
 * <em>have</em>
 *  to save all the old versions if they are not required at that point in the code:
 *
 * <pre>{@code
 * ImShelf<Integer> shelf = ImShelf.empty();
 * shelf = shelf.adding(1);
 * shelf = shelf.adding(2);
 * }</pre>
 * <p> or:
 *
 * <pre>{@code
 * ImShelf<Integer> shelf = ImShelf.<Integer> empty().adding(1).adding(2);
 * }</pre>
 * <p> There can be advantages in code readability too:
 *
 * <pre>{@code
 * public List<Integer> addLists(List<Integer> one, List<Integer> two)
 * {
 *     List<Integer> list = new ArrayList<Integer>(one);
 *     list.addAll(two);
 *     return list;
 * }
 *
 * public ImShelf<Integer> addShelfs(ImShelf<Integer> one, ImShelf<Integer> two)
 * {
 *     return one.addingAll(two);
 * }
 * }</pre>
 * <p> This sounds rather expensive on memory but, in fact, it is possible for the new collections to share most of their
 * elements with the old collections, so that it is perfectly feasible to work in this way.
 * <p> This is not the same as copy-on-write collections which typically
 * <em>do</em>
 *  create the whole collection afresh.
 * <p> This style of working can be used to implement undo features and is also inherently thread safe.
 * <p> All this doesn't come for free of course and, for certain types of operations, the
 * {@code im}
 *  collections are slower and consume
 * more memory (not counting previous versions of objects) than their Java counterparts.
 * <p> We should point out that Java itself already has at least one immutable collection in its standard library.
 * One of the most commonly used classes in Java,
 * {@code java.lang.String}
 * , which is essentially an ordered collection of
 * characters, is designed as a completely immutable object.
 * <p> There are six main public classes:
 * <ul>
 * <li>
 * <p> {@link dev.javafp.lst.ImList}  - more like an unbalanced tree or a stack - maybe slightly similar to the Java
 * {@code LinkedList}
 * </li>
 * <li>
 * <p> {@link dev.javafp.shelf.ImShelf} - the near-ish equivalent of the Java
 * {@code ArrayList}
 * </li>
 * <li>
 * <p> {@link dev.javafp.set.ImSet} - the equivalent of the Java
 * {@code Set}
 * </li>
 * <li>
 * <p> {@link dev.javafp.set.ImSortedSet} - the equivalent of the Java
 * {@code SortedSet}
 * </li>
 * <li>
 * <p> {@link dev.javafp.map.ImMap} - the equivalent of the Java
 * {@code Map}
 * </li>
 * <li>
 * <p> {@link dev.javafp.tree.ImRoseTree} - a tree where each node can have an arbitrary number of sub trees.
 * </li>
 * </ul>
 * <p> The first four classes above are all subclasses of {@link ImCollection} which extends
 * {@code java.util.Collection}
 * .
 * <p> All of these classes (except for
 * {@code ImList}
 * ) use another non-public class to store their data:
 * <ul>
 * <li>
 * <p> {@code ImTree}
 *  - a balanced binary tree
 * </li>
 * </ul>
 * <h2>Play up and play the game</h2>
 * <p> All of these immutable collections require that you use them for storing
 * <strong>immutable objects only</strong>
 * . The objects
 * don't have to be programatically guaranteed to be immutable - we just require them not to change during the
 * lifetime of the collection they are in.
 * <p> This is the same requirement when storing objects in
 * {@code java.util.Set}
 *  - except that we require it of all
 * six immutable collections - including
 * {@code ImList}
 *  and
 * {@code ImShelf}
 * .
 * <p> Just like that
 * {@code java.util.Set}
 * , we cannot enforce that rule and, if you break it, thing will fail in random ways so
 * it is your responsibility to "play the game" in this regard.
 * <h2>Standard methods</h2>
 * <p> All ImCollections have a number of standard methods. Some of them are there as a consequence of
 * the fact that
 * {@code ImCollection}
 *  extends
 * {@code java.lang.Collection}
 * <h2>Methods from java.lang.Collection</h2>
 *
 * <pre>{@code
 * toArray()
 * }</pre>
 * <p> There are no methods to convert from ImCollections to Collection. Since all ImCollections are Collections
 * you can use new CollectionBlah(ImBlah) to create new Collections from ImCollections
 * <h2>Examples:</h2>
 *
 * <pre>{@code
 * new ArrayList(ImList.on(1, 2, 3))
 * new LinkedList(ImList.on(1, 2, 3))
 * new HashSet(ImSet.on(1, 2, 3))
 * new ArrayList(ImSet.on(1, 2, 3))
 * }</pre>
 * <h2>Creation Methods</h2>
 * <p> All ImCollections have the following static "conversion constructor" methods:
 *
 * <pre>{@code
 * public static <T> ImBlah<T> onAll(Collection<T> elements)
 * public static <T> ImBlah<T> onArray(T... elementsArray)
 * public static <T> ImBlah<T> onIterator(T... elementsIterator)
 * public static <T> ImBlah<T> on(T singleElement)
 * }</pre>
 * <p> Let's explain them by using the example of
 * {@code ImShelf}
 *  - the others are similar.
 * <p> The first method creates a new
 * {@code ImShelf}
 *  out of
 * {@code elements}
 *  - except in the case where
 * {@code elements}
 *  is an instance of
 * {@code ImShelf}
 *  - it then will just return
 * {@code elements}
 * .
 * <p> If
 * {@code elements}
 *  is empty then the singleton empty
 * {@code ImShelf}
 *  is returned.
 * <p> The
 * {@code onArray()}
 *  method will create a new instance of
 * {@code ImShelf}
 * by iterating over the elements in
 * {@code elementsArray}
 * .
 * <p> {@code onIterator()}
 *  is similar.
 * <h2>Empty</h2>
 * <p> All ImCollections have the following static method:
 *
 * <pre>{@code
 * public static <T> ImBlah<T> empty()
 * }</pre>
 * <p> that creates a singleton empty collection of the appropriate type
 * <h2>Index one addressing</h2>
 * <p> For those strange people who, for some inexplicable reason, feel more comfortable with starting to count at one
 * rather than zero,
 * we provide versions of
 * {@code get(), set(), remove(), indexOf()}
 *  in
 * {@code ImList}
 *  and
 * {@code ImShelf}
 *  that feel the same way
 * as they do. The equivalent
 * {@code get()}
 *  method is
 * {@code get1()}
 * , the others are similarly named.
 * <h2>map() and fold()</h2>
 * <p> {@code map}
 *  and
 * {@code fold}
 *  functions are commonly used in functional programming languages.
 * <p> The basic concepts come from deep and profound ideas in mathematics (notably Category theory) but for our purposes
 * we can just describe them like this.
 * <p> {@code map()}
 *  is a method on an
 * {@code ImCollection}
 *  that takes a
 * <em>function</em>
 *  as an argument.
 * <p> So the result of
 *
 * <pre>{@code
 * things.map(fn)
 * }</pre>
 * <p> is a new collection of the same type as
 * {@code things}
 *  that is formed out of the elements obtained by applying
 * {@code fn}
 *  in turn
 * to each element in
 * {@code things}
 * . The function must, therefore, be one that takes one argument.
 * <p> Mapping a function
 * {@code fn}
 *  over a list is
 * the list where the i-th element is the result of evaluating the single argument function
 * {@code fn}
 *  on
 * the i-th element of the original list.
 * <p> Many other Java libraries provides similar features - although most represent the function as an instance of
 * an interface that has an
 * {@code apply()}
 *  method.
 * <p> In order to use the map method in this library or any other method that has a function as an argument, you will
 * need to obtain a function from a method in an existing class using the
 * {@code FnFactory}
 *  class.
 * <p> See the
 * <a href="{@docRoot}/im/functions/package-summary.html">
 * functions package summary
 * </a>
 * for more details of how functions work.
 * <h2>Example:</h2>
 * <p> To convert a list of objects to a list of their string representations:
 *
 * <pre>{@code
 * Function1<String> toStringFn = FnFactory.on(Object.class).getFn(String.class, "toString");
 * ImList.onArray(1, 2).map(toStringFn)  => "[1, 2]"
 * }</pre>
 * <p> {@code fold()}
 *  is a method that has two arguments - a function (itself of two arguments) and an "accumulator". It
 * applies the function in turn to the accumulator
 * and each element in the collection to return a new value of the accumulator - which it feeds into the next function
 * call on the next element.
 * <p> {@code ImCollection}
 *  implements fold() and more description and examples can be found in its documentation.
 * <p> See the
 * <a href="{@docRoot}/im/functions/package-summary.html">
 * functions package summary
 * </a>
 * for more details of how
 * {@code fold()}
 *  works.
 * <p> In fact the concept of mapping and folding is (with extensions)
 * the basis of database map-reduce systems.
 * <h2>Example:</h2>
 * <p> To calculate the maximum of a list of integers:
 *
 * <pre>{@code
 * Function2<Integer> maxFn = FnFactory.on(Math.class).getFnStatic(int.class, "max", int.class, int.class);
 *
 * ImList.onArray(1, 2, 3).fold(0, maxFn)  =>  3
 * }</pre>
 * <h2>Equals and hashCode</h2>
 * <p> All the collections that are subclasses of ImCollection implement equals() and hashCode(). They work in the same
 * way as the Java collections.
 * <p> Because the collections are immutable (and also because you will be "playing the game" by not storing objects
 * whose hash codes will be changing during the lifetime of a particular collection) they can calculate their
 * hash code when they are created very efficiently and cache it for efficient access thereafter.
 * <p> Where possible, each collection's hash code will be the same as the hash code of the equivalent Java collection
 * that contains equal elements. See the documentation of each
 * {@code hashCode()}
 *  method for details.
 * <h2>joinArray(), joinIterator(), join()</h2>
 * <p> All ImCollections have the following static method:
 *
 * <pre>{@code
 * public static <T> ImBlah<T> joinAll(Collection<T>...)
 * }</pre>
 * <p> that creates a singleton empty collection of the appropriate type
 * <p> Unfortunately, this method will generate a warning like this (eg):
 *
 * <pre>{@code
 * Type safety: A generic array of ImBlah<? extends Thing> is created for a varargs parameter
 * }</pre>
 * <p> Hey ho...
 * <h2>Should I use joinAll() or addingAll()?</h2>
 * <p> It might be more familiar to Java programmers to use addingAll() as it is an analogue of addAll()
 * <p> However, there is an issue with generic types that causes a problem here.
 * <p> If the collections that you are joining are declared as having elements of the same type then there is
 * no problem. The resulting collection has the same type
 *
 * <pre>{@code
 * ImList<Integer> one = ...
 * ImShelf<Integer> two = ...
 * List<Integer> three = ...
 *
 * one.addingAll(two).addingAll(three)
 * }</pre>
 * <p> but if one of the collections is declared slightly differently then there could be a problem:
 *
 * <pre>{@code
 * ImList<Integer> one = ...
 * ImShelf<Number> two = ...
 * List<Integer> three = ...
 * }</pre>
 * <p> If we try this:
 *
 * <pre>{@code
 * ImShelf<Number> r1 =  one.addingAll(two).addingAll(three)
 * }</pre>
 * <p> We get a compile error:
 *
 * <pre>{@code
 * The method addingAll(Collection<? extends Integer>) in the type ImShelf<Integer> is not applicable for the arguments (ImShelf<Number>)
 * }</pre>
 * <p> although this would be ok:
 *
 * <pre>{@code
 * ImShelf<Number> r1 = two.addingAll(one).addingAll(three)
 * }</pre>
 * <p> Casting our way out of trouble is not easy. Trying this:
 *
 * <pre>{@code
 * ImShelf<Number> r3 = ((ImShelf<Number>) one).addingAll(two).addingAll(three);
 * }</pre>
 * <p> gives this error:
 *
 * <pre>{@code
 *   Cannot cast from ImShelf<Integer> to ImShelf<Number>
 * }</pre>
 * <p> However, with the appropriate diplomatic skills, the Java compiler can be talked into allowing it:
 *
 * <pre>{@code
 *   ImShelf<Number> r4 = ((ImShelf<Number>) (ImShelf<? extends Number>) one).addingAll(two).addingAll(three);
 * }</pre>
 * <p> This now compiles - albeit with a certain loss of ... perspicuity ... and
 * we are still left with a warning:
 *
 * <pre>{@code
 *   Type safety: Unchecked cast from ImShelf<capture#1-of ? extends Number> to ImShelf<Number>
 * }</pre>
 * <p> In fact, since
 * {@code ImShelf}
 *  is immutable, it is perfectly safe to do these sorts of casts and we provide a
 * static method in
 * {@code ImShelf}
 *  for this purpose called
 * {@code upCast()}
 * :
 *
 * <pre>{@code
 *  ImShelf<Number> r5 = ImShelf.<Number> upCast(one).addingAll(two).addingAll(three);
 * }</pre>
 * <p> which does not generate a warning and is a little neater than the first solution.
 * <p> Using
 * {@code joinArray()}
 *  looks even neater:
 *
 * <pre>{@code
 * ImShelf<Number> r6 = ImShelf.concat(one, two, three);
 * }</pre>
 * <p> but here you get that pesky varargs warning:
 *
 * <pre>{@code
 * Type safety: A generic array of Collection<? extends Number> is created for a varargs parameter
 * }</pre>
 * <p> Choose your poison!
 * <p> In fact, the
 * {@code upCast()}
 *  method is not strictly necessary, since
 * {@code on()}
 *  will do the same job:
 *
 * <pre>{@code
 * ImShelf<Number> r7 = ImShelf.<Number> on(one).addingAll(two).addingAll(three);
 * }</pre>
 * <p> We did consider removing
 * {@code upCast()}
 *  but decided to leave it in since it might be easier to remember.
 * <h2>Should I use empty() or on()?</h2>
 * <p> Another good question.
 * <p> They both produce the same result
 *
 * <pre>{@code
 * ImShelf.on() == ImShelf.empty()  =>  true
 * }</pre>
 * <p> but, depending on the declaration of the component type there may be a problem.
 * <p> this is ok:
 *
 * <pre>{@code
 * ImShelf<Integer> s1 = ImShelf.on();
 * }</pre>
 * <p> but this:
 *
 * <pre>{@code
 * ImShelf<List<Integer>> s2 = ImShelf.on();
 * }</pre>
 * <p> produces the varargs warning:
 *
 * <pre>{@code
 * Type safety: A generic array of List<Integer> is created for a varargs parameter
 * }</pre>
 * <p> If you use
 * {@code empty()}
 * , you are warning free:
 *
 * <pre>{@code
 * ImShelf<List<Integer>> s3 = ImShelf.empty();
 * }</pre>
 * <h2>Zippers</h2>
 * <blockquote>
 * <p> The wonderful thing about Tiggers
 * Is Tiggers are wonderful things
 * Their tops are made out of rubber
 * Their bottoms are made out of springs
 * They're bouncy, trouncy, flouncy, pouncy fun, fun, fun, fun, fun
 * </blockquote>
 * <p> <a href="https://en.wikipedia.org/wiki/Sherman_Brothers"  >The Sherman Bothers</a>
 * <p> Zippers may not quite be Tiggers but they are indeed rather wonderful things that let us operate on immutable functional
 * data structures in a very convenient way.
 * <p> Zippers were first mentioned in a paper by Gerard Huet in 1997
 * <a href="http://www.st.cs.uni-saarland.de/edu/seminare/2005/advanced-fp/docs/huet-zipper.pdf"  >"Functional Pearl: The Zipper"</a>
 * and have since become an important concept in functional programming.
 * <p> One way to think about zippers in ths library is that they are the
 * {@code im}
 *  equivalent of
 * {@code java.util.ListIterator}
 *  -
 * they allow bi-directional navigation and "mutation" of the underlying collection.
 * <p> The wonderful thing about zippers (apart from the fact that zippers are wonderful things)
 * is that they allow you to make repeated changes to a collection efficiently.
 * While the zipper is open it only makes the minimum changes to the local part of the underlying tree. It only
 * constructs the new tree when it is closed
 * <p> To be clear, even without using zippers, a new collection still shares almost all of its elements with the old
 * collection it was created from, zippers just make it so that even fewer new objects have to be created each time.
 * <h2>Zippers on balanced binary trees - implementation notes</h2>
 * <p> These notes refer to the underlying tree zipper implementation which is currently not a public class.
 * <p> As we note above, the underlying implementation for all the
 * {@code im}
 *  collection classes is a balanced binary tree.
 * <p> The {@link dev.javafp.shelf.ImShelfZipper} and the {@link dev.javafp.tree.ImRoseTreeZipper} are based on a tree zipper on the underlying tree.
 * This tree zipper
 * is "safe" even though the trees that it points to are rebalanced from time to time.
 * <p> The key observation is that balancing only occurs when the zipper "moves up" and then only locally and the rank
 * of the elements in the original zipper focus changes in a predicable way after the zipper has moved up.
 * <p> The re-balancing means that, in a zipper that has side left, the result of next() is not necessarily the focus of the parent
 * zipper.
 * <p> Essentially, the tree can change shape when the zipper goes up.
 * <h2>Definitions</h2>
 * <p> The
 * <em>rank</em>
 *  of an element in a tree is the position it appears in during a depth first scan of the tree.
 * <p> The
 * <em>local rank</em>
 *  of a tree is the rank of the root of the tree in itself. The local rank of a tree
 * {@code t}
 *  is:
 *
 * <pre>{@code
 *  t.left.size + 1
 * }</pre>
 * <p> where
 * {@code t.left}
 *  is the left child of
 * {@code t}
 * <h3>Example:</h3>
 * <p> If we do an insertBefore() of tree 1 into tree 2 at index 1
 *
 * <pre>{@code
 *    -> d          b
 *      ...        ...
 *   -> c -        a -
 *
 *   tree 1        tree 2
 * }</pre>
 * <p> then we get this (showing the zipper):
 *
 * <pre>{@code
 * |->   d            |->  d             |-> b
 * |     ...          |    ...              ....
 * |->  c -           |->  b                a  d
 * |   ...                ...                 ...
 * |-> b -                a c                 c -
 *    ...
 *    a -
 *
 * diagram 1         diagram 2          diagram 3
 * }</pre>
 * <p> The zipper starts as in diagram 2 abaove.
 * When the zipper goes up from
 * {@code b}
 * , it re-balances to get diagram 2 above.
 * And when it goes up again (also from
 * {@code b}
 * ) it rebalances again to get) diagram 3 (and it still seems stuck with
 * {@code b}
 * !)
 * <p> So we have a problem with implementing
 * {@code next()}
 *  and
 * {@code previous()}
 * . The naive approach (that assumes that
 * the structure of the tree does not change as you go up) won't work.
 * <p> So a naive approach is: to do
 * {@code next()}
 *  on the zipper on
 * {@code b}
 * , look at the right sibling of
 * {@code b}
 *  and see that it
 * is
 * {@code nil}
 *  and therefore go up. That logic fails when re-balancing happens when you go up.
 * <p> There is an "invariant" however and that is that the local rank of
 * {@code b}
 *  can be predicted relative to the
 * focus of the new zipper after going up.
 * <p> In fact, if the old zipper has side
 * {@code left}
 * , then after going up, the rank of a node in the focus of
 * the old zipper does not change in the focus of the new zipper.
 * <p> The local rank of
 * {@code b}
 *  is
 * {@code 2}
 *  in (1). After going up, the local rank of
 * {@code b}
 *  in the new focus node is still
 * {@code 2}
 * . And, after going
 * up again it still is
 * {@code 2}
 * <p> The situation is a little more complicated when the side is
 * {@code right}
 *  - but still predictable:
 * <p> The rank increases by the local rank of the parent focus (the parent
 * <em>before</em>
 *  going up)
 *
 * <pre>{@code
 *      b       <-              b       <-                    f      <-
 *    ......                 .......                      .........
 *    a    d    <-           a     f    <-                b       g
 *      ......                 ........               ........  .....
 *      c    f  <-             d      g               a      d      h
 *       ........            .....   ...                  .......
 *       e      g            c   e     h                  c     e
 *             ...
 *               h
 * }</pre>
 * <p> Consider
 * {@code g}
 *  in the diagrams above
 * <p> It starts at rank
 * {@code 3}
 *  in
 * {@code f}
 * then rank
 *
 * <pre>{@code
 * 5 = 2 (rank of d) + 3
 * }</pre>
 * <p> then
 *
 * <pre>{@code
 * rank 7 =  2 (rank of b) + 5
 * }</pre>
 * <p> Another way of thinking about it is to observe that when you go up and re-balance the parent node you will not
 * change the order of any of the nodes. If we go up to the right then we just get some extra nodes to our right
 * and going up to the left means that we will get some new nodes to our left -
 * {@code z.parent.left.size + 1}
 *  of them.
 * <p> This rule applies for all nodes of the tree - even if they are not descendants of the zipper focus to start with.
 * <p> Consider node a in the diagram above. It has rank in the tree as a whole and rank -4 relative to f - and that does
 * not change as the zipper goes up.
 * <p> So, we recast
 * {@code z.next()}
 *  as meaning:
 *
 * <pre>{@code
 * let r = the local rank of z and then go to the node that has rank r + 1
 * }</pre>
 * <p> If the node with the appropriate rank is not available in the tree that is currently the zipper's focus, then
 * we can go up, and work out the new rank that our target object should occupy in the new tree. This continues
 * recursively until the node is found or we reach the root zipper.
 * <p> A similar argument applies to
 * {@code previous()}
 * .
 * <h2>Implementation Notes - Immutable Balanced Binary Trees</h2>
 * <h2>Introduction</h2>
 * <p> Under the covers, all the immutable collection classes are based on immutable balanced binary trees.
 * <p> For example, the shelf containing:
 *
 * <pre>{@code
 * [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
 * }</pre>
 * <p> looks like this:
 *
 * <pre>{@code
 *             8
 *    ..............
 *    4            16
 *  .....       ..........
 *  2   6      12       18
 * ... ...   ......    ....
 * 1 3 5 7  10   14   17  19
 *         ...   ...     ...
 *         9 11 13 15    - 20
 * }</pre>
 * <p> This shelf:
 *
 * <pre>{@code
 * [21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
 * }</pre>
 * <p> Looks like this:
 *
 * <pre>{@code
 *        24
 *   .........
 *  22       28
 *  ...    ......
 * 21 23  26    29
 *        ...  ...
 *       25 27 - 30
 * }</pre>
 * <p> And when you add the second onto the end of the first you get this:
 *
 * <pre>{@code
 * [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
 * }</pre>
 * <p> which looks like this:
 *
 * <pre>{@code
 *                    16
 *          .....................
 *         8                    24
 *    ..........           ............
 *    4        12         20          28
 *  .....    ......     .......     ......
 *  2   6   10   14    18    22    26    29
 * ... ... ...   ...   ...   ...   ...  ...
 * 1 3 5 7 9 11 13 15 17 19 21 23 25 27 - 30
 * }</pre>
 * <h2>Don't go changing to try to please me...</h2>
 * <p> These trees are
 * <em>immutable</em>
 *  so the
 * {@code insert}
 *  and
 * {@code remove}
 *  methods don't actually change existing trees.
 * Instead they create a new tree with a node added or deleted as appropriate, reusing as many of
 * the old nodes as possible.
 * <p> If you add 31 at index 16:
 *
 * <pre>{@code
 * [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 31, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30]
 * }</pre>
 * <p> The underlying tree will be:
 *
 * <pre>{@code
 *                     16
 *           ......................
 *          8                     24
 *    ...........            ............
 *    4         12          20          28
 *  .....    .......      .......     ......
 *  2   6   10    14     18    22    26    29
 * ... ... ...   ....    ...   ...   ...  ...
 * 1 3 5 7 9 11 13  15  17 19 21 23 25 27 - 30
 *                 ...
 *                 - 31
 * }</pre>
 * <p> In order to create this new tree, six new objects had to be created:
 *
 * <pre>{@code
 *                    (16)
 *           ......................
 *         (8)                    24
 *    ...........            ............
 *    4        (12)         20          28
 *  .....    .......      .......     ......
 *  2   6   10   (14)    18    22    26    29
 * ... ... ...   ....    ...   ...   ...  ...
 * 1 3 5 7 9 11 13 (15) 17 19 21 23 25 27 - 30
 *                 ...
 *                 -(31)
 * }</pre>
 * <p> All the other nodes in the tree are shared with the previous tree.
 * <h2>ImTree</h2>
 * <p> The {@link dev.javafp.tree.ImTree} class is the heart of the Immutable collections library.
 * It is intended to be used only as a component of the other classes so it is not a public class.
 * <p> An
 * {@code ImTree}
 *  is an AVL tree (a balanced binary tree) where each node stores some arbitrary data.
 * <p> Note that, in this class, there is no concept of the data that is being stored being instances of
 * {@code java.lang.Comparable}
 * .
 * This functionality is added by {@link dev.javafp.set.ImSortedSet}.
 * <p> A node in an
 * {@code ImTree}
 *  is either a leaf node
 * {@code Nil}
 *  or a
 * {@code Node}
 *  that has two children that
 * are themselves
 * {@code ImTrees}
 * .
 * <p> Each
 * {@code Node}
 *  can contain a value of an arbitrary type and two Integers
 * representing the height and size of the tree rooted at that node. A
 * {@code Nil}
 *  has no data.
 * <p> Note that this definition does not, of itself, specify that the tree is balanced. We enforce that
 * invariant in each method that adds/removes nodes.
 * <p> Consider an example tree with six non nil nodes:
 * <p> <img src="doc-files/tree-abcdef.png" alt="tree-abcdef"  style="width: 20%"/>
 * </p>
 * <p> If we show the nil nodes then it looks like this:
 * <p> <img src="doc-files/tree-abcdef-with-nulls.png" alt="tree-abcdef-with-nulls"  style="width: 20%"/>
 * </p>
 * <p> Each
 * {@code Node}
 *  also has a
 * {@code size}
 *  value defined as the sum of the sizes of its children plus one.
 * nil Nodes are considered to have a size of zero.
 * The size represents how many non nil nodes there are in the tree rooted at n.
 * <p> <img src="doc-files/tree-abcdef-with-sizes.png" alt="tree-abcdef-with-sizes"  style="width: 20%"/>
 * </p>
 * <p> Each node also has a height value representing the size of the longest path from that node to a leaf
 * node.
 * <p> The height of a node is the maximum of the heights of its children. Nil nodes are considered to
 * have a height of zero.
 * <p> Let's annotate our example with the heights:
 * <p> <img src="doc-files/tree-abcdef-with-heights.png" alt="tree-abcdef-with-heights"  style="width: 20%"/>
 * </p>
 * <p> Because the tree is balanced, this means that the heights of the children of a node will differ
 * by at most one.
 * <p> Each node is considered to have a
 * <em>rank</em>
 *  that represents its position in the tree in a pre-order scan.
 * <p> Ranks start at one (exactly as Nature intended!)
 * <p> Let's annotate our example with the ranks:
 * <p> <img src="doc-files/tree-abcdef-with-ranks.png" alt="tree-abcdef-with-ranks"  style="width: 20%"/>
 * </p>
 * <p> We don't store the ranks. To calculate the rank of a node or to find a node at a particular rank we can use
 * the size of child nodes to derive the answer.
 * <p> A number of the algorithms used in manipulating trees are taken from
 * <a href="http://groups.csail.mit.edu/mac/users/adams/BB/"  >Implementing Sets Efficiently in a Functional Language</a>
 *  - although we should note that this paper is
 * concerned with binary search trees where the elements have a total ordering. Our basic implementation does not
 * rely on this property but we note that most of the techniques mentioned in this paper do not rely on it either.
 * <h2>Release Notes</h2>
 * <p> The first release improves on the pre-relaese in a number of ways
 * <p> The names of the methods have been tidied up and made more consistent.
 * <p> Classes have been refactored to packages.
 * <p> Zero based addressing versions of various methods have been provided.
 * <p> There is a new ImListZipper class.
 * <p> Zippers on shelves and trees now allow you to make any type of mutation to the underlying collection without
 * closing it automatically for some types as they used to.
 * <p> Closing a shelf zipper or tree zipper is now very fast if no mutations have been made. The same applies to
 * {@code up()}
 * {@code next()}
 *  and
 * {@code prev()}
 *  in tree zippers.
 * <p> Functions have been extensively refactored and enhanced.
 * <p> The
 * {@code Maybe}
 *  type is no longer used in the public API.
 * <h2>References</h2>
 * <ul>
 * <li>
 * <p> <a href="http://www.st.cs.uni-saarland.de/edu/seminare/2005/advanced-fp/docs/huet-zipper.pdf"  >"Functional Pearl: The Zipper"</a>
 * </li>
 * <li>
 * <p> <a href="http://groups.csail.mit.edu/mac/users/adams/BB/"  >Implementing Sets Efficiently in a Functional Language</a>
 * </li>
 * </ul>
 *
 */

package dev.javafp.lst;