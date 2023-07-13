/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.tree;

import dev.javafp.util.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> A mutable tree whose only purpose is to be used in testing Rose Tree Zippers
 */
public class MutableTree<T>
{
    T element;
    List<MutableTree<T>> children;

    public MutableTree(T element, List<MutableTree<T>> children)
    {
        this.element = element;
        this.children = children;
    }

    public static <A> MutableTree<A> from(ImRoseTree<A> roseTree)
    {
        List<MutableTree<A>> kids = new ArrayList<MutableTree<A>>();
        for (ImRoseTree<A> a : roseTree.getSubTrees())
        {
            kids.add(from(a));
        }

        return new MutableTree<A>(roseTree.getElement(), kids);
    }

    @Override
    public String toString()
    {
        if (element == null)
        {
            return "Empty Rose Tree";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(element);

        if (children.size() > 0)
        {
            sb.append(" (");
            sb.append(TextUtils.join(children, " "));
            sb.append(")");

        }
        return sb.toString();
    }

    public boolean removeNodeWithElement(T elementToRemove)
    {
        if (elementToRemove.equals(element))
        {
            // Make myself the empty tree
            element = null;
            return true;
        }

        int index = getIndexOf(elementToRemove);
        if (index >= 0)
        {
            children.remove(index);
            return true;
        }
        else
        {
            for (MutableTree<T> t : children)
            {
                if (t.removeNodeWithElement(elementToRemove))
                    return true;
            }
        }

        return false;

    }

    private int getIndexOf(T elementToRemove)
    {
        for (int i = 0; i < children.size(); i++)
        {
            if (children.get(i).element.equals(elementToRemove))
            {
                return i;
            }
        }
        return -1;
    }

    public void insertBeforeNodeWithElement(T elementToFind, MutableTree<T> treeToInsert)
    {
        int index = getIndexOf(elementToFind);
        if (index >= 0)
        {
            children.add(index, treeToInsert);
        }
        else
        {
            for (MutableTree<T> t : children)
            {
                t.insertBeforeNodeWithElement(elementToFind, treeToInsert);
            }
        }
    }

    public void insertAfterNodeWithElement(T elementToFind, MutableTree<T> treeToInsert)
    {
        int index = getIndexOf(elementToFind);
        if (index >= 0)
        {
            children.add(index + 1, treeToInsert);
        }
        else
        {
            for (MutableTree<T> t : children)
            {
                t.insertAfterNodeWithElement(elementToFind, treeToInsert);
            }
        }

    }

}