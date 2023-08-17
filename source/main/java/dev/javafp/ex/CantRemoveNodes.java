/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import dev.javafp.set.ImSet;

/**
 * Thrown when we can't remove a set of Nodes because the neighbours of `nodes`, `neighbours` is such that the following is not true:
 *
 *     neighbours ⊆ nodes
 */
public class CantRemoveNodes extends ImException
{
    /**
     * The exception that indicates that `badNeighbours` are some or all of the nodes that are neighbours to
     * `nodes` that prevent them from being removed.
     */
    public CantRemoveNodes(ImSet<?> nodes, ImSet<?> badNeighbours)
    {
        super("Can't remove nodes " + nodes + " because " + badNeighbours + " are connected to some of them");
    }
}