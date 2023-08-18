/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import java.util.Collection;

/**
 * <p>Utility for throwing exceptions - mainly useful when checking arguments to functions.
 */
public class Throw
{
    /**
     * Class that contains the utility functions for checking conditions and throwing exceptions if the conditions are
     * not met.
     */
    public static class Exception
    {
        /**
         * <p> Throw an exception
         * if the argument with name
         * {@code name}
         * and value
         * {@code value}
         *  is
         * {@code null}
         *
         */
        public static void ifNull(String name, Object value)
        {
            if (value == null)
            {
                throw new NullValue(name);
            }
        }

        /**
         * <p> Throw an exception if
         * if the argument with name
         * {@code name}
         * and value
         * {@code value}
         *  is not
         * {@code null}
         *
         */
        public static void ifNotNull(String name, Object value)
        {
            if (value != null)
            {
                throw new NotNullValue(name);
            }
        }

        /**
         * <p> Throw an exception
         * if the argument with name
         * {@code name}
         * and value
         * {@code value}
         * is the empty string.
         *
         */
        public static void ifEmpty(String name, String value)
        {
            if (value.isEmpty())
            {
                throw new EmptyString(name);
            }
        }

        /**
         * <p> Throw an exception
         * if the argument with name
         * {@code name}
         * and value
         * {@code value}
         *  is
         * {@code null}
         *  or empty
         *
         */
        public static void ifNullOrEmpty(String name, Collection<?> value)
        {
            Exception.ifNull(name, value);
            if (value.isEmpty())
                throw new EmptyCollection(name);
        }

        /**
         * <p> Throw an exception
         * if the string argument with name
         * {@code name}
         * and value
         *  is
         * {@code null}
         *  or empty
         *
         */
        public static void ifNullOrEmpty(String name, String value)
        {
            Exception.ifNull(name, value);
            Exception.ifEmpty(name, value);
        }

        /**
         * <p> Throw an exception if
         * {@code condition}
         *  is true - with custom message
         * {@code message}
         *
         */
        public static void ifTrue(boolean condition, String message)
        {
            if (condition)
            {
                throw new InvalidState(message);
            }
        }

        /**
         * <p> Throw an exception if
         * {@code condition}
         *  is false - with custom message
         * {@code message}
         *
         */
        public static void ifFalse(boolean condition, String message)
        {
            if (!condition)
            {
                throw new InvalidState(message);
            }
        }

        /**
         * Throw an
         * {@code InvalidState}
         * exception
         */
        public static <A> A ifYouGetHere()
        {
            throw new InvalidState("Internal Error");
        }

        /**
         * <p> Throw an exception
         * if the 1-based index with name
         * {@code name}
         * and value
         * {@code value}
         * is not valid for the collection
         * {@code collectionName}
         * with size
         * {@code size}
         */

        /**
         * <p> Check if the specified 1-based index is within the range of the size
         * of the specified collection.
         * and throw
         * {@code ImIndexOutOfBounds}
         * if this is false.
         *
         */
        public static void ifIndexOutOfBounds(String name, int index, String collectionName, int size)
        {
            ImIndexOutOfBounds.check(name, index, collectionName, size);
        }

        /**
         * <p> Check if the specified 0-based index is within the range of the size
         * of the specified collection.
         * and throw
         * {@code ImIndexOutOfBounds}
         * if this is false.
         *
         */
        public static void ifIndexOutOfBounds0(String name, int index, String collectionName, int size)
        {
            ImIndexOutOfBounds.check0(name, index, collectionName, size);
        }

        /**
         * <p> Check if the specified index is within a range.
         * <p>So - do this check:
         *
         * <pre>{@code
         * (min <= index) && (index <= max)
         * }</pre>
         * and throw
         * {@code ArgumentOutOfRange}
         * if this is false.
         *
         */
        public static void ifOutOfRange(String name, int index, int min, int max)
        {
            // Validate the args are ok
            ifTrue(min > max, "min > max");

            if (!((min <= index) && (index <= max)))
            {
                throw new ArgumentOutOfRange(name, index, min, max);
            }
        }

        /**
         * <p> Checks if the specified index is within a range
         * <p> We do this check:
         *
         * <pre>{@code
         * index < max
         * }</pre>
         * and throw
         * {@code ArgumentShouldNotBeLessThan}
         * if this is false.
         *
         */
        public static void ifLessThan(String name, int index, int min)
        {
            if (index < min)
            {
                throw new ArgumentShouldNotBeLessThan(name, index, min);
            }
        }

        /**
         * <p> Checks if the specified index
         * {@code <=}
         * a value
         * <p> We do this check:
         *
         * <pre>{@code
         * index > min
         * }</pre>
         * and throw
         * {@code ValueShouldBeGreaterThan}
         * if this is false.
         *
         */
        public static void ifLessThanOrEqualTo(String name, int index, int min)
        {
            if (index <= min)
            {
                throw new ValueShouldBeGreaterThan(name, index, min);
            }
        }

    }

    /**
     * <p> A method to let us have an exception throw as one of the branches of a ?: invocation
     *
     * <pre>{@code
     *    return Eq.uals(parent, rootId)
     *    ? p.snd
     *    : Throw.wrap(new ImException("blah"));
     * }</pre>
     * <p> I can't just throw an exception because ... java
     *
     */
    public static <A> A wrap(ImException ex)
    {
        throw ex;
    }

}