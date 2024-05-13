/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.shelf;

import dev.javafp.ex.ZipperHasNoFocusException;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImListZipper;
import dev.javafp.set.ImTree;
import dev.javafp.set.ImTreeZipper;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.TextUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p> A zipper on an {@link ImShelf} lets you navigate back and forth in the shelf and "mutate" it as you go.
 * <p> {@code ImShelfZipper}
 *  is to
 * {@code ImShelf}
 *  what
 * {@code java.util.ListIterator}
 *  is to
 * {@code java.util.List}
 * . It is a cursor that lets you
 * navigate back and forward within a shelf and to add/replace/delete elements relative to the current position.
 * <p> Of course, since
 * {@code Im}
 *  collections
 * are immutable, you cannot
 * <em>actually</em>
 *  mutate the underlying shelf.
 * Instead, methods that "mutate" the zipper return new zippers.
 * For example {@link #next()} returns a
 * <em>new</em>
 *  zipper pointing to the next element in the shelf
 * leaving the original zipper unchanged. After "mutating" the zipper, you have to use {@link #close()}
 * which returns the new shelf with the changes made to it.
 * <h2>next() and prev() and the "pair of lists" analogy</h2>
 * <p> A good way to think of a shelf zipper is as a pair of lists formed out of the original shelf.
 * The
 * <em>focus</em>
 *  element is the last element of the first list (if there is one).
 * <p> If we consider the shelf
 * {@code [1, 2, 3]}
 * , when we get a zipper on it we get:
 *
 * <pre>{@code
 * [] [1, 2, 3]
 * }</pre>
 * <p> The first list is empty and the second list is just the list of the elements in the original shelf. In this state, there is no focus element.
 * <p> If we use
 * {@code next()}
 *  we get a new zipper:
 *
 * <pre>{@code
 * [1] [2, 3]
 * }</pre>
 * <p> To "mutate" the zipper you can only operate on the focus - the last element of the first list. You can remove/replace it
 * and add elements after it.
 * <p> You can get these lists from a zipper - see {@link #getFirst()} and {@link #getSecond()}.
 * <p> This class is very similar in functionality to {@link ImListZipper}. The only significant
 * differences are in the performance -
 * closing a list zipper is
 * <strong>{@code O}</strong>
 * {@code (n)}
 *  where
 * {@code n}
 * is the size of the first list whereas closing a shelf zipper is
 * <strong>{@code O}</strong>
 * {@code (log p)}
 *  where
 * {@code p}
 *  is the maximum of the sizes of the two lists.
 * <p> Inserting a list of size
 * {@code n}
 *  into a list zipper is
 * <strong>{@code O}</strong>
 * {@code (n)}
 *  but inserting the same sized shelf into a
 * shelf zipper of size
 * {@code m}
 *  is
 * <strong>{@code O}</strong>
 * {@code (log max(m,n))}
 * .
 * <p> See {@link ImListZipper} for more examples and description.
 * <h2>An Example</h2>
 * <p> We start by creating a shelf:
 *
 * <pre>{@code
 * ImShelf<Integer> shelf = ImShelf.onArray(2, 3);
 * }</pre>
 * <p> Create a zipper on it:
 *
 * <pre>{@code
 * ImShelfZipper<Integer> start = shelf.getZipper();
 * }</pre>
 * <p> It is in the initial state - the first list is empty:
 *
 * <pre>{@code
 * start  =>  [] [2, 3]
 * }</pre>
 * <p> Go to the next element:
 *
 * <pre>{@code
 * ImShelfZipper<Integer> z = start.next();
 *
 * z  =>  [2] [3]
 * }</pre>
 * <p> Replace an element:
 *
 * <pre>{@code
 * z = z.setFocus(8);
 *
 * z  =>  [8] [3]
 * }</pre>
 * <p> Go back:
 *
 * <pre>{@code
 * z = z.prev();
 *
 * z  =>  [] [8, 3]
 * }</pre>
 * <p> Push a new element element onto the first list:
 *
 * <pre>{@code
 * z = z.push(0);
 *
 * z  =>  [0] [8, 3]
 * }</pre>
 * <p> Push a shelf of elements onto the first list:
 *
 * <pre>{@code
 * z = z.pushAll(ImShelf.onArray(5, 13));
 *
 * z  =>  [0, 5, 13] [8, 3]
 * }</pre>
 * <p> Move back:
 *
 * <pre>{@code
 * z = z.prev();
 *
 * z  =>  [0, 5] [13, 8, 3]
 * }</pre>
 * <p> Pop an element off the first list:
 *
 * <pre>{@code
 * z = z.pop();
 *
 * z  =>  [0] [13, 8, 3]
 * }</pre>
 * <p> Closing the zipper gives us the new "modified" shelf:
 *
 * <pre>{@code
 * z.close()  =>  [0, 13, 8, 3]
 * }</pre>
 * <p> Of course, the original shelf has not changed:
 *
 * <pre>{@code
 * shelf  =>  [0, 13, 8, 3]
 * }</pre>
 * <p> And indeed the original zipper has not changed. If you close a zipper without making any changes then
 * you just get back the original shelf:
 *
 * <pre>{@code
 * start.close() == shelf         =>  true
 * start.next().close() == shelf  =>  true
 * }</pre>
 * <p> See the
 * <a href="{@docRoot}/im/package-summary.html">
 * package summary
 * </a>
 * for more details of zippers.
 * @see ImShelf
 * @see ImShelf.ImShelfIterator
 * @see ImShelfZipperIterator
 * @see ImListZipper
 *
 */
public class ImShelfZipper<T> implements Iterable<ImShelfZipper<T>>
{
    final private ImTreeZipper<T> treeZipper;

    final private ImShelf<T> shelf;

    ImShelfZipper(ImShelf<T> shelf, ImTreeZipper<T> treeZipper)
    {
        this.shelf = shelf;
        this.treeZipper = treeZipper;
    }

    /**
     * <p> The zipper whose focus is the next element in the shelf.
     * <p> The first element of the second list is moved to the end of the first list:
     *
     * <pre>{@code
     * [1] [2, 3] next()  => [1, 2] [3]
     * }</pre>
     * <p> @throws NoSuchElementException if no such element exists
     *
     */
    public ImShelfZipper<T> next()
    {
        ImMaybe<ImTreeZipper<T>> next = treeZipper.next();

        if (next.isPresent())
            return new ImShelfZipper<T>(shelf, next.get());
        else
            throw new NoSuchElementException();
    }

    /**
     * <p> The zipper whose focus is the previous element in the first list (if there is one).
     * <p> The last element of the first list (if there is one) is moved to the front of the second list:
     *
     * <pre>{@code
     * [1, 2] [3] prev()    => [1] [2, 3]
     * [1] [2, 3] prev()    => [] [1, 2, 3]
     * }</pre>
     * <p> If the first list is empty then
     * {@code this}
     *  is returned.
     *
     * <pre>{@code
     * [] [1, 2, 3] prev()  => [] [1, 2, 3]
     * }</pre>
     * <p> If the first list is now empty then you won't be able to set or get the focus or pop it.
     * <p> Note that this means that you can always use
     * {@code prev()}
     *  on a zipper - even if {@link #hasPrev()} is
     * {@code false}
     *
     */
    public ImShelfZipper<T> prev()
    {
        ImMaybe<ImTreeZipper<T>> prev = treeZipper.previous();

        return prev.isPresent()
               ? new ImShelfZipper<T>(shelf, prev.get())
               : this;
    }

    /**
     * <p> Return the shelf that the zipper represents, with all modifications made.
     * <p> The lists are joined together:
     *
     * <pre>{@code
     * [1, 2] [3] close()  => [1, 2, 3]
     * }</pre>
     *
     */
    public ImShelf<T> close()
    {
        ImTree<T> newTree = treeZipper.close();

        return newTree == shelf.tree
               // If the tree is the same then the shelf must be the same
               ? shelf
               : new ImShelf<T>(newTree);
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  except that the focus element is
     * {@code newElement}
     * .
     *
     * <pre>{@code
     * [1] [2, 3] setFocus(5)    => [5] [2, 3]
     * [] [1, 2, 3] setFocus(5)  => throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws NoSuchElementException if the first list is empty
     *
     */
    public ImShelfZipper<T> setFocus(T newElement)
    {
        return new ImShelfZipper<T>(shelf, treeZipper.replaceElement(newElement));
    }

    /**
     * <p> Return the focus.
     *
     * <pre>{@code
     * [1] [2, 3] getFocus()    =>  1
     * [] [1, 2, 3] getFocus()  =>  throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws ZipperHasNoFocusException if the first list is empty
     *
     */
    public T getFocus()
    {
        if (treeZipper.getElement() == null)
            throw new ZipperHasNoFocusException();
        else
            return treeZipper.getElement();
    }

    /**
     * <p> The zipper that is the same as
     * {@code this}
     *  but with
     * {@code newElement}
     *  pushed onto the first list.
     *
     * <pre>{@code
     * [1] [2, 3] push(5)    =>  [1, 5] [2, 3]
     * [] [1, 2, 3] push(5)  =>  [5] [1, 2, 3]
     * }</pre>
     * <p> The new focus is
     * {@code newElement}
     * .
     *
     */
    public ImShelfZipper<T> push(T newElement)
    {
        return new ImShelfZipper<T>(shelf, treeZipper.insertAfter(ImTree.on(newElement)));
    }

    /**
     * <p> The zipper that is
     * {@code this}
     *  but with
     * {@code shelfToInsert}
     *  pushed onto the first list.
     * <p> The new focus is the last element of
     * {@code elementsToInsert}
     * .
     *
     * <pre>{@code
     * [1] [2, 3] push([5, 8, 13])  =>  [1, 5, 8, 13] [2, 3]
     * [] [] push([5, 8, 13])       =>  [5, 8, 13] []
     * }</pre>
     *
     */
    public ImShelfZipper<T> pushAll(ImShelf<T> shelfToInsert)
    {
        return new ImShelfZipper<T>(shelf, treeZipper.insertAfter(shelfToInsert.tree));
    }

    /**
     * <p> The zipper that is
     * {@code this}
     *  but with the last element popped from the first list.
     *
     * <pre>{@code
     * [1] [2, 3] pop()  =>  [1] [2, 3]
     * [] [1] pop()      =>  throws ZipperHasNoFocusException
     * }</pre>
     * <p> @throws NoSuchElementException if the first list is empty
     *
     */
    public ImShelfZipper<T> pop()
    {
        if (getIndex() == 0)
            throw new ZipperHasNoFocusException();
        else
            return new ImShelfZipper<T>(shelf, treeZipper.removeNode());
    }

    /**
     * <p> The index of the focus - the size of the first list.
     *
     * <pre>{@code
     * [1, 5, 8, 13] [2, 3] getIndex()  =>  4
     * }</pre>
     *
     */
    public int getIndex()
    {
        return treeZipper.getRank();
    }

    /**
     * <p> A string representation of this zipper.
     *
     * <pre>{@code
     * ImShelf.empty().getZipper().toString()  =>  "[] []"
     *
     * ImShelfZipper<Integer> z = ImShelf.onArray(1, 2, 3).getZipper();
     *
     * z.toString()                      =>  "[] [1, 2, 3]"
     * z.next().toString()               =>  "[1] [2, 3]"
     * z.next().next().toString()        =>  "[1, 2] [3]"
     * z.next().next().next().toString() =>  "[1, 2, 3] []"
     * }</pre>
     *
     */
    @Override
    public String toString()
    {
        ImList<T> first = getFirst();

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
        ImList<String> s = getSecond().toStringList();

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
    private ImList<T> getFirst()
    {
        return ImList.onAll(close()).take(getIndex()).reverse();
    }

    /**
     * <p> The second list.
     *
     * <pre>{@code
     * [1, 5, 8, 13] [2, 3] getSecond()  =>  [2, 3]
     * }</pre>
     *
     */
    private ImList<T> getSecond()
    {
        return ImList.onAll(close()).drop(getIndex());
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
        return treeZipper.getAfterSize() > 0;
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
        return getIndex() > 1;
    }

    /**
     * <p> A zipper iterator on
     * {@code this}
     * .
     *
     */
    @Override
    public Iterator<ImShelfZipper<T>> iterator()
    {
        return new ImShelfZipperIterator<T>(this);
    }

}