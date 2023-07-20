/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.ex;

import java.util.Collection;
import java.util.List;

public class Throw
{
    public static class Exception
    {
        /**
         * <p> Throws an exception if
         * {@code value}
         *  is
         * {@code null}
         *
         */
        public static void ifNull(String name, Object value)
        {
            if (value == null)
            {
                throw new NullArgument(name);
            }
        }

        /**
         * <p> Throws an exception if
         * {@code value}
         *  is not
         * {@code null}
         *
         */
        public static void ifNotNull(String name, Object value)
        {
            if (value != null)
            {
                throw new NotNullArgument(name);
            }
        }

        /**
         * <p> Throws an exception if
         * {@code value}
         *  is the empty string.
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
         * <p> Throws an exception if
         * {@code value}
         *  is empty.
         *
         */
        public static void ifEmpty(String name, Collection<?> collectionToCheck)
        {
            if (collectionToCheck.isEmpty())
            {
                throw new EmptyCollection(name);
            }
        }

        /**
         * <p> Thows an exception if
         * {@code value}
         *  is null or empty
         *
         */
        public static void ifNullOrEmpty(String name, Collection<?> value)
        {
            Exception.ifNull(name, value);
            Exception.ifEmpty(name, value);
        }

        /**
         * <p> Thows an exception if
         * {@code value}
         *  is null or empty
         *
         */
        public static void ifNullOrEmpty(String name, String value)
        {
            Exception.ifNull(name, value);
            Exception.ifEmpty(name, value);
        }

        public static void ifTrue(boolean condition, String message)
        {
            if (condition)
            {
                throw new IllegalState(message);
            }
        }

        public static void ifFalse(boolean condition, String message)
        {
            if (!condition)
            {
                throw new IllegalState(message);
            }
        }

        public static <A> A ifYouGetHere()
        {
            throw new IllegalState("Internal Error");
        }

        // TODO should not be an argument exception - Van 13-sep-06
        public static void ifInvoked(String message)
        {
            throw new IllegalState(message);
        }

        //        public static void ifNotType(Object thing, Class<?> clazz)
        //        {
        //            if (!clazz.isAssignableFrom(thing.getClass()))
        //            {
        //                throw new IllegalArgumentClass(thing, clazz);
        //            }
        //        }

        /**
         * <p> Checks if the specified index is within the range of the size
         * of the specified collection. Ie do this check:
         *
         * <pre>{@code
         * (0 <= i) && (i < things.size())
         * }</pre>
         * <p> and throw a runtime exception if this is false.
         *
         */
        public static void ifOutOfRange(String name, int index, Collection<?> things)
        {
            ifOutOfRange(name, index, 0, things.size() - 1);
        }

        /**
         * <p> Checks if the specified index is within the range of the size
         * of the specified collection. Ie do this check:
         *
         * <pre>{@code
         * (!things.isEmpty()) && (0 <= i) && (i < things.size())
         * }</pre>
         * <p> and throw a runtime exception if this is false.
         *
         */
        public static void ifIndexNotInCollection(int index, Collection<?> things, String collectionName)
        {
            if (things.isEmpty() || index < 0 || index >= things.size())
            {
                throw new InvalidCollectionIndex(index, things.size(), collectionName);
            }
        }

        /**
         * <p> Checks if the specified index is within the range of the size
         * of the specified collection including -1. Ie do this check:
         *
         * <pre>{@code
         * (-1 <= i) && (i < things.size())
         * }</pre>
         * <p> and throw a runtime exception if this is false.
         *
         */
        public static void ifOutOfRangeIncludingMinusOne(String name, int index, List<?> things)
        {
            if (things.isEmpty() && index != 1)
            {
                throw new ArgumentOutOfRange(name, index);
            }
            ifOutOfRange(name, index, -1, things.size() - 1);
        }

        /**
         * <p> Checks if the specified index is within a range
         * Ie do this check:
         *
         * <pre>{@code
         * (min <= i) && (i <= max)
         * }</pre>
         * <p> and throw a runtime exception if this is false.
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
         * (min <= i) && (i < max)
         * }</pre>
         * <p> and throw a runtime exception if this is false.
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
         * <p> Checks if the specified index is within a range
         * <p> We do this check:
         *
         * <pre>{@code
         * (min <= i) && (i < max)
         * }</pre>
         * <p> and throw a runtime exception if this is false.
         *
         */
        public static void ifLessThanOrEqualTo(String name, int index, int min)
        {
            if (index <= min)
            {
                throw new ArgumentShouldBeGreaterThan(name, index, min);
            }
        }

        public static <A> A ifYouGetHere(String message)
        {
            throw new IllegalState(message);
        }
    }

    /**
     * <p> A method to let us have an exception throw as one of the branches of a ?: invocation
     *
     * <pre>{@code
     *    return Eq.uals(parent, rootId)
     *    ? p.snd
     *    : Throw.wrap(new DrumException("blah"));
     * }</pre>
     * <p> I can't just throw an exception because ... java
     *
     */
    public static <A> A wrap(DrumException ex)
    {
        throw ex;
    }

}