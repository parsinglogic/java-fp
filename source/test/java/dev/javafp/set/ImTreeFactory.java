/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import dev.javafp.lst.ImList;

public class ImTreeFactory
{
    private ImList<String> list;

    public ImTreeFactory(final String string)
    {
        list = string.length() == 0
               ? ImList.<String>empty()
               : ImList.on(string.split(" +"));
    }

    private Character getLabel(final String top)
    {
        return top.charAt(0);
    }

    private ImTree<Character> makeNode(final int expectedLevel)
    {
        if (list.isEmpty())
        {
            return ImTree.Nil();
        }

        if (getLevel(list.head()) != expectedLevel)
        {
            return ImTree.Nil();
        }

        final Character label = getLabel(list.head());

        list = list.tail(); // A bit clunky

        if (label.equals('-'))
        {
            return ImTree.Nil();
        }

        final ImTree<Character> left = makeNode(expectedLevel + 1);
        final ImTree<Character> right = makeNode(expectedLevel + 1);

        return new ImTree<Character>(label, left, right);
    }

    private int getLevel(final String top)
    {
        return Integer.parseInt(top.substring(1));
    }

    public ImTree<Character> create()
    {
        return makeNode(1);
    }

}