/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.lst;

import dev.javafp.eq.Eq;
import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.func.Fn;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> A zipper on an {@link ImList} lets you navigate back and forth in the list and "mutate" it as you go.
 * <h2>Introduction</h2>
 * <p> {@code ImListZipper}
 *  is to
 * {@code ImList}
 *  what
 * {@code java.util.ListIterator}
 *  is to
 * {@code java.util.List}
 * . It is a cursor that lets you
 * navigate back and forward within a list and to add/replace/delete elements relative to the current position.
 * <p> Of course, since
 * {@code Im}
 *  collections
 * are immutable, you cannot
 * <em>actually</em>
 *  mutate the underlying list.
 * Instead, methods that "mutate" the zipper return new zippers.
 * For example {@link #next()} returns a
 * <em>new</em>
 *  zipper pointing to the next element in the list
 * leaving the original zipper unchanged. After "mutating" the zipper, you have to use {@link #close()}
 * which returns the new list with the changes made to it.
 * <h3>next() and prev() and the "pair of lists" analogy</h3>
 * <p> A good way to think of a list zipper is as a pair of lists formed out of the original list.
 * The
 * <em>focus</em>
 *  element is the last element of the first list (if there is one).
 * <p> If we consider the list
 * {@code [1, 2, 3]}
 * , when we get a zipper on it we get:
 * *
 * 1  2 3
 * <p> The first list is empty and the second list is just the original list. In this state, there is no focus element.
 * <p> If we use
 * {@code next()}
 *  we get a new zipper:
 *
 * <pre>{@code
 *  *
 *  1  2  3
 * }</pre>
 * <p> The focus element on this zipper is
 * {@code 1}
 * .
 * <p> If we use
 * {@code next()}
 *  on this new zipper we get this:
 *
 * <pre>{@code
 *    *
 * 1  2  3
 * }</pre>
 * <p> The focus element on this zipper is
 * {@code 2}
 * .
 * <p> If we use
 * {@code next()}
 *  on this new zipper we get this:
 *
 * <pre>{@code
 *       *
 * 1  2  3
 * }</pre>
 * <p> The focus element on this zipper is
 * {@code 3}
 * .
 * <p> If we use
 * {@code prev()}
 *  on this, we get:
 *
 * <pre>{@code
 *    *
 * 1  2  3
 * }</pre>
 * <p> So you can think of
 * {@code next()}
 *  as moving the first element of the second list to the end of the first
 * list.
 * {@code prev()}
 *  moves the last element of the first list to the start of the second list.
 * <p> In fact the current implementation stores the lists as
 * {@code Lsts}
 *  with the first list reversed so
 * {@code next()}
 *  and
 * {@code prev()}
 *  are
 * <strong>{@code O}</strong>
 * {@code (1)}
 *  operations.
 * <p> To "mutate" the zipper you can only operate on the focus - the last element of the first list. You can remove/replace it
 * and add elements after it.
 * <p> The {@link #close()} method just joins the two lists together. It is
 * <strong>{@code O}</strong>
 * {@code (n)}
 *  where
 * {@code n}
 *  is the size of the list.
 * <p> {@link #getIndex()} returns the size of the first list.
 * <h2>The initial zipper state</h2>
 * <p> As we stated above, when you get a zipper on a list, it will initially be in "before the start of the list" state
 * Let's call this the
 * <em>initial state</em>
 * .
 *
 * <pre>{@code
 * If
 *
 *     z.getIndex() <= 1
 *
 * then
 *
 *     z.prev().prev() = z.prev()
 * }</pre>
 * <p> This means that, for zippers on non-empty lists, this is always true:
 *
 * <pre>{@code
 * z.prev().next() == z.getElement()
 * }</pre>
 * <p> The ability to move to the initial state means that to insert an element before the focus,
 * you can use this:
 *
 * <pre>{@code
 * z.prev().push(x)
 * }</pre>
 * <p> and it will work on any zipper on any position on a list of any size.
 * <p> When a zipper is in the initial state, three methods will throw {@link NoSuchElementException}:
 * <ul>
 * <li>
 * <p> {@link #getFocus()}
 * </li>
 * <li>
 * <p> {@link #setFocus(Object)}
 * </li>
 * <li>
 * <p> {@link #pop()}
 * </li>
 * </ul>
 * <p> A zipper on the empty list looks like this:
 *
 * <pre>{@code
 * *
 * }</pre>
 * <p> It be in the initial state. Additionally {@link #hasNext()} will be
 * {@code false}
 *  and {@link #next()}
 * will throw {@link NoSuchElementException}.
 * <h2>Inserting and removing elements</h2>
 * <p> To insert an element, use
 * {@code push()}
 * . To remove the focus element use
 * {@code pop()}
 * :
 * <p> push(5) on this:
 *
 * <pre>{@code
 *       *
 * 1  2  3  8
 * }</pre>
 * <p> gives:
 *
 * <pre>{@code
 *          *
 * 1  2  3  5  8
 * }</pre>
 * <p> and pop() on this zipper will give:
 *
 * <pre>{@code
 *       *
 * 1  2  3  8
 * }</pre>
 * <p> The methods are so called because, if you imagine the first list as a stack with the focus element at the top
 * of the stack then you can think of
 * {@code push()}
 *  and
 * {@code pop()}
 *  operating on this stack and they work as you would expect.
 * <p> If the focus was the first element, the zipper returned by pop()
 * will be in the initial state.
 *
 * <pre>{@code
 *  *
 *  1  2  3
 *
 *  *
 *     2  3
 * }</pre>
 * <p> This means that, for non-empty zipper
 * {@code z}
 *  and any object
 * {@code e}
 * , pop() and push() are inverse operations:
 *
 * <pre>{@code
 *  z.push(e).pop()
 *  z.pop().push(z.getElement())
 * }</pre>
 * <p> and code like this (for example):
 *
 * <pre>{@code
 *  pop().insertAllAfter(newList)
 * }</pre>
 * <p> will replace the focus with
 * {@code newList}
 *  for any zipper on any position on any non-empty list.
 * <p> For empty lists, you can't pop() - but push(e) followed by pop() will return a zipper on the empty list.
 * <p> {@code push()}
 *  and
 * {@code pop()}
 *  are
 * <strong>{@code O}</strong>
 * {@code (1)}
 *  operations.
 * <h2>Changing the focus</h2>
 * <p> You can set the focus using setFocus()
 *
 * <pre>{@code
 *    *
 * 1  2  3   setfocus(5)     =>  1  5  3
 *
 *       *
 * 1  2  3   setfocus(5)     =>  1  2  5
 *
 * *
 *   1  2    setfocus(5)  =>  throws ZipperHasNoFocusException
 * }</pre>
 * <h2>Using while loops or foreach</h2>
 * <p> When you get a zipper on a list, it will initially be "before the start of the list". In order to get the first
 * element you have to use {@link #next()}. This is similar to
 * {@code java.util.ListIterator}
 *  and allows you to write code like this:
 *
 * <pre>{@code
 * ImListZipper<Integer> z = ImList.onArray(1, 2, 3).getZipper();
 *
 * while (z.hasNext())
 * {
 *     z = z.next();
 *     System.err.println(z.pop().close());
 * }
 * }</pre>
 * <p> which will display:
 *
 * <pre>{@code
 * [2, 3]
 * [1, 3]
 * [1, 2]
 * }</pre>
 * <p> Zippers are also
 * {@code iterable}
 * . This code:
 *
 * <pre>{@code
 * for (ImListZipper<Integer> z : ImList.onArray(1, 2, 3).getZipper())
 * {
 *     System.err.println(z.pop().close());
 * }
 * }</pre>
 * <p> will display the same output as the while loop example above.
 * <p> The iterator will work on each element of the original list so, if you need to "mutate" it as you go then the
 * while loop is more useful. Here is an example that removes all elements except the first and last from a list.
 *
 * <pre>{@code
 * ImListZipper<Integer> z = ImList.onArray(1, 2, 3, 5).getZipper();
 *
 * while (z.hasNext())
 * {
 *     z = z.next();
 *     if (z.hasPrev() && z.hasNext())
 *         z = z.pop();
 * }
 *
 * z.close()  =>  [1, 5]
 * }</pre>
 * <h2>Example - permutations of a list</h2>
 * <p> To generate the permutations of a list, one algorithm is to take each element
 * {@code e}
 *  in turn from the list and cons
 * {@code e}
 *  with each list generated by the permutations of the list with element
 * {@code e}
 *  removed. In pseudo-code:
 *
 * <pre>{@code
 * perms([1, 2, 3])  =  1 consed with all from perms([2, 3]) +
 *                      2 consed with all from perms([1, 3]) +
 *                      3 consed with all from perms([1, 2]) +
 * }</pre>
 * <p> with
 *
 * <pre>{@code
 * perms([])  =  [[]]
 * }</pre>
 * <p> Using a list zipper, this algorithm can be implemented fairly directly:
 *
 * <pre>{@code
 * public <T> ImList<ImList<T>> perms(ImList<T> list)
 * {
 *     if (list.isEmpty())
 *         return ImList.on(ImList.<T> empty());
 *
 *     ImList<ImList<T>> p = ImList.empty();
 *
 *     for (ImListZipper<T> z : list.getZipper())
 *     {
 *         for (ImList<T> t : perms(z.pop().close()))
 *         {
 *             p = p.cons(t.cons(z.getElement()));
 *         }
 *     }
 *
 *     return p;
 * }
 * }</pre>
 * <p> For a more functional style, we could observe that consing an element
 * {@code e}
 *  in turn with a list of lists, is the
 * same as mapping the function
 * {@code f}
 *  over the list where:
 *
 * <pre>{@code
 *  f(list) = list.cons(e)
 * }</pre>
 * <p> If we are prepared to get the cons function directly we could write this:
 *
 * <pre>{@code
 * public <T> ImList<ImList<T>> perms2(ImList<T> list)
 * {
 *     if (list.isEmpty())
 *         return ImList.on(ImList.<T> empty());
 *
 *     Function2<ImList<T>> consFn = FnFactory.on(ImList.class).getFnStatic( //
 *             ImList.class, "cons", Object.class, ImList.class);
 *
 *     ImList<ImList<T>> p = ImList.empty();
 *
 *     for (ImListZipper<T> z : list.getZipper())
 *     {
 *         p = p.addingAll(perms2(z.pop().close()).map(consFn.invoke(z.getElement())));
 *     }
 *
 *     return p;
 * }
 * }</pre>
 * <p> although here we saved one line of code but had to add another to do so.
 * <p> If we wanted to have the cons function declared statically then we have a certain amount of ... finagling to do:
 *
 * <pre>{@code
 * static Function2<ImList<Object>> consFn1 = FnFactory.on(ImList.class).getFnStatic( //
 *         ImList.class, "cons", Object.class, ImList.class);
 *
 * [@]SuppressWarnings({ "unchecked" })
 * private static <T> Function2<T> consFn()
 * {
 *     return (Function2<T>) consFn1;
 * }
 *
 * public <T> ImList<ImList<T>> perms3(ImList<T> list)
 * {
 *     if (list.isEmpty())
 *         return ImList.on(ImList.<T> empty());
 *
 *     ImList<ImList<T>> p = ImList.empty();
 *
 *     for (ImListZipper<T> z : list.getZipper())
 *     {
 *         p = p.addingAll(perms3(z.pop().close()).map(ImListZipperTest.<ImList<T>> consFn().invoke(z.getElement())));
 *     }
 *
 *     return p;
 * }
 * }</pre>
 * <h2>Yet another permutations example</h2>
 * <p> And finally (just when you thought this example had been done to death) there is another algorithm that can be
 * easily implemented using a list zipper.
 * <p> In this algorithm
 * we take, the head
 * {@code h}
 * , of the list, calculates the permutions
 * {@code p}
 * , on the tail and then, for each list
 * {@code l}
 * , in
 * {@code p}
 * ,
 * creates lists with
 * {@code h}
 *  after each
 * {@code n-th}
 *  element of
 * {@code l}
 *  (where
 * {@code n}
 *  goes from
 * {@code 0}
 *  to
 * {@code l.size()}
 * ).
 * <p> So, as an example, starting with:
 *
 * <pre>{@code
 * [1, 2, 3]
 * }</pre>
 * <p> we calculate:
 *
 * <pre>{@code
 * perms([2, 3])  => [[2, 3], [3, 2]]
 * }</pre>
 * <p> so we now need to insert the head
 * {@code 1}
 *  in every possible position in
 * {@code [2, 3]}
 *  to form three lists:
 *
 * <pre>{@code
 * [1, 2, 3]  [2, 1, 3] [2, 3, 1]
 * }</pre>
 * <p> and then do the same with
 * {@code [3, 2]}
 *  to give another three:
 *
 * <pre>{@code
 * [1, 3, 2]  [3, 1, 2] [3, 2, 1]
 * }</pre>
 * <p> Here is the code:
 *
 * <pre>{@code
 * public <T> ImList<ImList<T>> perms4(ImList<T> list)
 * {
 *     if (list.isEmpty())
 *         return ImList.on(ImList.<T> empty());
 *
 *     ImList<ImList<T>> p = ImList.empty();
 *
 *     for (ImList<T> l : perms4(list.tail()))
 *     {
 *         p = p.cons(l.cons(list.head()));
 *
 *         for (ImListZipper<T> z : l.getZipper())
 *             p = p.cons(z.push(list.head()).close());
 *     }
 *
 *     return p;
 * }
 * }</pre>
 * <p> See the
 * <a href="{@docRoot}/im/package-summary.html">
 * package summary
 * </a>
 * for more details about zippers.
 *
 */
public class ImListZipper<T> implements Iterable<ImListZipper<T>>
{
    private final ImList<T> first;
    private final ImList<T> second;

    private ImListZipper(ImList<T> list)
    {
        this(ImList.<T>empty(), list);
    }

    ImListZipper(ImList<T> before, ImList<T> after)
    {
        this.first = before;
        this.second = after;
    }

    public static <TT> ImListZipper<TT> on(ImList<TT> list)
    {
        return new ImListZipper<TT>(list);
    }

    /**
     * <p> The zipper whose focus is the next element in the list.
     * <p> The first element of the second list is moved to the end of the first list:
     *
     * <pre>{@code
     *    *                      *
     * 1  2  3  next()  => 1  2  3
     * }</pre>
     * <p> @throws NoSuchElementException if no such element exists
     *
     */
    public ImListZipper<T> next()
    {
        if (second.isEmpty())
        {
            if (first.isEmpty())
                throw new NoSuchElementException("The list is empty");
            else
                throw new NoSuchElementException("Already at the end of the list");
        }
        else
            return new ImListZipper<T>(first.push(second.head()), second.tail());
    }

    /**
     * <p> The zipper whose focus is the previous element in the first list (if there is one).
     * <p> The last element of the first list (if there is one) is moved to the front of the second list:
     *
     * <pre>{@code
     *    *                    *
     * 1  2  3    prev()    => 1  2  3
     *
     * *                       *
     * 1  2  3    prev()    =>    1  2  3
     * }</pre>
     * <p> If the first list is empty then
     * {@code this}
     *  is returned.
     * <ul>
     * <li>
     *
     * <pre>{@code
     *                 *
     * }</pre>
     * <p> 1  2  3  prev()  =>   1  2  3
     * </li>
     * </ul>
     * <p> If the first list is now empty then you won't be able to set or get the focus or pop it.
     * <p> Note that this means that you can always use
     * {@code prev()}
     *  on a zipper - even if {@link #hasPrev()} is
     * {@code false}
     *
     */
    public ImListZipper<T> prev()
    {
        return first.isEmpty()
               ? this
               : new ImListZipper<T>(first.tail(), second.push(first.head()));
    }

    /**
     * <p> {@code true}
     *  if there are more elements before the focus.
     *
     * <pre>{@code
     * [1, 2] [3] hasPrev()  =>  true
     * [1] [2, 3] hasPrev()  =>  false
     * [] [1]     hasPrev()  =>  false
     * }</pre>
     * <p> Note that you can use {@link #prev()} even if
     * {@code hasPrev()}
     *  is
     * {@code false}
     * .
     *
     */
    public boolean hasPrev()
    {
        return first.size() > 1;
    }

    /**
     * <p> The list that the zipper represents, with all modifications made.
     * <p> The lists are joined together:
     *
     * <pre>{@code
     *    *
     * 1  2  3 close()  =>  [1, 2, 3]
     * }</pre>
     *
     */
    public ImList<T> close()
    {
        if (first.isEmpty())
            return second;

        ImList<T> close = second;

        for (T a : first)
        {
            close = ImList.cons(a, close);
        }

        return close;
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  except that the focus element is
     * {@code newElement}
     * .
     *
     * <pre>{@code
     *    *                                     *
     * 1  2  3        setFocus(5)    =>      1  5  3
     *
     * *
     *    1  2  3     setFocus(5)    => throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws ZipperHasNoFocusException if the first list is empty
     *
     */
    public ImListZipper<T> setFocus(T newElement)
    {
        if (first.isEmpty())
            throw new ZipperHasNoFocusException();
        else
            return new ImListZipper<T>(first.tail().push(newElement), second);
    }

    /**
     * <p> The focus of the zipper.
     *
     * <pre>{@code
     * *
     * 1  2  3      getFocus()    =>    1
     *
     * *
     *   1  2  3      getFocus()  =>  throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws NoSuchElementException if the first list is empty
     *
     */
    public T getFocus()
    {
        if (first.isEmpty())
            throw new ZipperHasNoFocusException();
        else
            return first.head();
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  but with
     * {@code newElement}
     *  pushed onto the first list.
     *
     * <pre>{@code
     *    *                               *
     * 1  2  3       push(5)    =>  1  2  5  3
     *
     *
     * *                            *
     *    1  2  3    push(5)    =>  5  1  2  5  3
     * }</pre>
     * <p> The new focus is
     * {@code newElement}
     * .
     *
     */
    public ImListZipper<T> push(T newElement)
    {
        return new ImListZipper<T>(first.push(newElement), second);
    }

    /**
     * <p> The zipper that is
     * {@code this}
     *  but with
     * {@code elementsToInsert}
     *  pushed onto the first list.
     * <p> The new focus is the last element of
     * {@code elementsToInsert}
     * .
     *
     * <pre>{@code
     * (1)  2  3     push([5, 8, 13])       =>  1  5  8  (13)  2  3
     *
     * ()            push([5, 8, 13])       =>  5  8  (13)
     * }</pre>
     *
     */
    public ImListZipper<T> pushAll(ImList<T> elementsToInsert)
    {
        // TODOIM make this faster and immutable
        ImListZipper<T> insertAllAfter = this;

        for (T a : elementsToInsert)
        {
            insertAllAfter = insertAllAfter.push(a);
        }

        return insertAllAfter;
    }

    /**
     * <p> The zipper that is
     * {@code this}
     *  but with the last element popped from the first list.
     *
     * <pre>{@code
     *    *                        *
     * 1  2  3     pop()    =>     1  2  3
     *
     * *
     *    1        pop()    =>  throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws ZipperHasNoFocusException if the first list is empty
     *
     */
    public ImListZipper<T> pop()
    {
        if (first.isEmpty())
            throw new ZipperHasNoFocusException();
        else
            return new ImListZipper<T>(first.tail(), second);
    }

    /**
     * <p> The index of the focus - the size of the first list.
     *
     * <pre>{@code
     *          **
     * 1  5  8  13  2  3 getIndex()   =>   4
     * }</pre>
     *
     */
    public int getIndex()
    {
        return first.size();
    }

    /**
     * <p> A string representation of this zipper.
     *
     * <pre>{@code
     * ImList.empty().getZipper().toString()  =>  "[] []"
     *
     * ImListZipper<Integer> z = ImList.onArray(1, 2, 3).getZipper();
     *
     * z.toString()                      =>  *
     *                                         1  2  3
     *
     * z.next().toString()               =>    *
     *                                         1  2  3
     *
     * z.next().next().toString()        =>       *
     *                                         1  2  3
     *
     * z.next().next().next().toString() =>          *
     *                                         1  2  3
     * }</pre>
     *
     */
    @Override
    public String toString()
    {
        // Get the focus object as a string with { } round it
        String f = first.isEmpty()
                   ? ""
                   : first.head().toString();
        String fs = "{" + f + "}";

        // Get the rest of first as a list of strings
        ImList<String> f3 = first.isEmpty()
                            ? ImList.on()
                            : first.tail().reverse().toStringList();

        // Get second as a list of strings
        ImList<String> s = second.toStringList();

        // Put them together
        return TextUtils.join(f3.append(s.push(fs)), "  ");
    }

    /**
     * <p> The first list.
     *
     * <pre>{@code
     * [1, 5, 8, 13] [2, 3] getFirst()  =>  [1, 5, 8, 13]
     * }</pre>
     *
     */
    public ImList<T> getFirst()
    {
        return first.reverse();
    }

    /**
     * <p> The second list.
     *
     * <pre>{@code
     * [1, 5, 8, 13] [2, 3] getSecond()  =>  [2, 3]
     * }</pre>
     *
     */
    public ImList<T> getSecond()
    {
        return second;
    }

    /**
     * <p> {@code true}
     *  if there are more elements after the focus.
     *
     * <pre>{@code
     * [1] [2, 3] hasNext()  =>  true
     * [] []      hasNext()  =>  false
     * }</pre>
     *
     */
    public boolean hasNext()
    {
        return !second.isEmpty();
    }

    /**
     * <p> A zipper iterator on
     * {@code this}
     * .
     *
     */
    @Override
    public Iterator<ImListZipper<T>> iterator()
    {
        return new ImListZipperIterator<T>(this);
    }

    /**
     * <p> Compares
     * {@code other}
     *  with
     * {@code this}
     *  for equality.  Returns
     * {@code true}
     *  if and only if the specified object is also a
     * {@code list zipper}
     * , both
     * zippers have the same size, and all corresponding pairs of elements in
     * the two zippers are
     * <em>equal</em>
     * .
     * <p> {@code e1}
     *  and
     * {@code e2}
     *  being
     * <em>equal</em>
     *  means
     *
     * <pre>{@code
     * e1.equals(e2) == true
     * }</pre>
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object other)
    {
        return this == other
               ? true
               : other == null
                 ? false
                 : other instanceof ImListZipper
                   ? safeEquals((ImListZipper<T>) other)
                   : false;
    }

    public ImMaybe<ImListZipper<T>> find(T thingToFind)
    {
        return find(i -> Eq.uals(thingToFind, i));
    }

    public ImMaybe<ImListZipper<T>> find(Fn<T, Boolean> pred)
    {
        ImListZipper<T> z = this;

        while (z.hasNext())
        {
            z = z.next();

            if (pred.of(z.getFocus()))
                return ImMaybe.just(z);
        }

        return ImMaybe.nothing();
    }

    private boolean safeEquals(ImListZipper<T> two)
    {
        return first.equalsList(two.first) && second.equalsList(two.second);
    }
}