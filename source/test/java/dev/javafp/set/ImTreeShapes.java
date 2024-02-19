/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p> Consider the possible tree shapes of size 2:
 *
 * <pre>{@code
 *    *   *
 *   *      *
 * }</pre>
 * <p> Consider the possible tree shapes of size 3:
 *
 * <pre>{@code
 *    *      *     *     *      *
 *   *      *     * *      *      *
 *  *        *            *        *
 * }</pre>
 * <p> Consider the sizes of the root tree and its immediate child nodes:
 *
 * <pre>{@code
 *    3       3     3      3      3
 *   2 0     2 0   1 1    0 2    0 2
 * }</pre>
 * <p> So, to generate shapes of size 3 we start with a root node r
 * <p> and then we generate all trees with r as the root and
 * <ol>
 * <li>
 * <p> a left child of size 2 and a right child of size 0 (nil)
 * </li>
 * <li>
 * <p> a left child of size 1 and a right child of size 1
 * </li>
 * <li>
 * <p> a left child of size 0 and a right child of size 2
 * </li>
 * </ol>
 * <p> We can generalise this and observe that,
 * <br/>
 * to generate all possible tree shapes of size n, we generate tree shapes with sizes like this:
 *
 * <pre>{@code
 *      n        n                  n
 *     / \      / \     ...        / \
 *   n-1  0   n-2  1              0  n-1
 * }</pre>
 * <p> We need to generate all possible tree shapes for the children as well.
 * This can be done recursively of course
 *
 */
public class ImTreeShapes
{

    private static class Relabeler
    {
        private int count = 0;

        public Relabeler(Character startChar)
        {
            count = startChar.charValue();
        }

        /**
         * <p> 'Rename'
         * {@code node}
         *  by creating a tree that is the same shape but where
         * the elements are a,b... in that order when the tree is flattened
         *
         */
        public ImTree<Character> relabel(ImTree<Character> node)
        {
            if (node.size() == 0)
                return ImTree.Nil();

            if (node.size() == 1)
                return ImTree.on(nextChar());

            // We have to be careful to do these next operations in the correct order

            // Rename the left tree
            ImTree<Character> newLeft = relabel(node.getLeft());

            // Get the next char for the element at this node
            Character thisElement = nextChar();

            // Rename the right tree
            ImTree<Character> newRight = relabel(node.getRight());

            // Put them together
            return new ImTree<Character>(thisElement, newLeft, newRight);
        }

        private Character nextChar()
        {
            return Character.valueOf((char) (count++));
        }

    }

    private static ImTree<Character> nil = ImTree.Nil();

    public List<ImTree<Character>> withSize(int n)
    {
        return withSize(n, false, 'a');
    }

    public List<ImTree<Character>> allUpToSize(int n, boolean balanced, Character startChar)
    {
        List<ImTree<Character>> results = new ArrayList<ImTree<Character>>();

        for (int i = 0; i <= n; i++)
        {
            results.addAll(new ImTreeShapes().withSize(i, balanced, startChar));
        }

        return results;
    }

    public List<ImTree<Character>> allNonNilUpToSize(int n, boolean balanced, Character startChar)
    {
        List<ImTree<Character>> results = new ArrayList<ImTree<Character>>();

        for (int i = 1; i <= n; i++)
        {
            results.addAll(new ImTreeShapes().withSize(i, balanced, startChar));
        }

        return results;
    }

    public List<ImTree<Character>> withSize(int n, boolean balanced, Character startChar)
    {
        // Generate all the possible shapes for this size - but each node has the same element of 'z'
        List<ImTree<Character>> rawResults = withSize2(n, balanced);

        // For each tree we create another tree of the same shape - but renamed so that the nodes are in alphabetical
        // order
        List<ImTree<Character>> results = new ArrayList<ImTree<Character>>(rawResults.size());

        for (ImTree<Character> imTree : rawResults)
        {
            results.add(new Relabeler(startChar).relabel(imTree));
        }

        return results;
    }

    public List<ImTree<Character>> withSize2(int n, boolean balanced)
    {
        if (n == 0)
        {
            return Collections.singletonList(nil);
        }

        List<ImTree<Character>> trees = new ArrayList<ImTree<Character>>();

        List<List<ImTree<Character>>> subTrees = new ArrayList<List<ImTree<Character>>>();
        for (int i = 0; i < n; i++)
        {
            subTrees.add(withSize2(i, balanced));
        }

        // subTrees[n] is a list of all subtrees of size n

        for (int i = 0; i < n; i++)
        {
            List<ImTree<Character>> leftTrees = subTrees.get(n - 1 - i);
            List<ImTree<Character>> rightTrees = subTrees.get(i);

            for (ImTree<Character> l : leftTrees)
            {
                for (ImTree<Character> r : rightTrees)
                {
                    ImTree<Character> tree = new ImTree<Character>('z', l, r);
                    if (!balanced || balanced && tree.isBalanced())
                        trees.add(tree);
                }
            }
        }

        return trees;
    }

}