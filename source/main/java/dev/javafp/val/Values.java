/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.val;

import dev.javafp.box.HasTextBox;
import dev.javafp.eq.Equals;
import dev.javafp.lst.ImList;

/**
 * <p> An interface to help with "autogenerating" hashCode, equals, toString
 * <p> The basic idea is that an object returns its components as a list and then, this list is used to
 * implement hashCode, equals, toString
 * <p> A class that implements Values must:
 * <ol>
 * <li>
 * <p> declare hashCode as a field
 * </li>
 * <li>
 * <p> return hashCode in hashCode()
 * </li>
 * <li>
 * <p> invoke calculateHash() in its constructor after all its bits have been set up
 * </li>
 * <li>
 * <p> delegate to defaultEquals in equals()
 * </li>
 * <li>
 * <p> implement getValues()
 * </li>
 * <li>
 * <p> implement getNames()
 * </li>
 * </ol>
 * <p> I have also got a ImValuesImpl to help with all this
 * <p> For classes that are descendants of ImValuesImpl all they have to do is:
 * <ol>
 * <li>
 * <p> implement getNames
 * </li>
 * <li>
 * <p> implement getValues
 * </li>
 * </ol>
 * <p> see Link for a simple class that does this
 * <p> The ImValuesImpl::toString implementation basically gets the object as a ImList of its values and displays it indented.
 *
 * <p> This is {@link dev.javafp.net.ApiRequest}:
 *
 * <pre>{@code
 * GetRequest: url:             DrumUrl: scheme:         http
 *                                       port:           53031
 *                                       host:           localhost
 *                                       pathComponents: [hello]
 *                                       queryElements:  []
 *                                       fragment:
 *             method:          GET
 *             followRedirects: true
 *             requestHeaders:  []
 *             queryParameters: [(bish, bash), (foo, bar)]
 * }</pre>
 *
 *
 */
public interface Values extends HasTextBox
{
    // Return the field values for this object including fields from superclasses
    ImList<Object> getValues();

    ImList<String> getNames();

    default boolean defaultEquals(Object other)
    {
        if (other == null)
            return false;
        else if (other == this)
            return true;
        else if (other.getClass() == this.getClass())
            return Equals.isEqual(getValues(), ((Values) other).getValues());
        else
            return false;
    }

}