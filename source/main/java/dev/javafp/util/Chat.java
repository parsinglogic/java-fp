/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.util;

import dev.javafp.box.TopDownBox;
import dev.javafp.ex.ChatFailed;
import dev.javafp.ex.FlatMapFunctionReturnedNull;
import dev.javafp.func.Fn;
import dev.javafp.func.FnProducer;
import dev.javafp.lst.ImList;
import dev.javafp.val.ImValuesImpl;

/**
 * <p> A class that represents the result of parsing/reading some object from some file/files/whatever
 * <p> There is some chat that we want to give the user and if we get lucky there might be the object as well.
 * <p> If the parse of the object succeeded then the chat will be about that - otherwise the chat will be about
 * why it failed.
 * <p> I think this is a writer monad and ImEither. Hmm
 *
 */
public class Chat<T> extends ImValuesImpl
{

    public final ImList<String> left;
    public final T right;
    public final boolean isOk;

    protected Chat(ImList<String> left, T right, boolean isOk)
    {
        this.left = left;
        this.right = right;
        this.isOk = isOk;

    }

    protected Chat(ImList<String> left, T right)
    {
        this(left, right, true);
    }

    protected Chat(ImList<String> left)
    {
        this(left, null, false);
    }

    public static <T> Chat<T> Right(T thing)
    {
        return Right(ImList.on(), thing);
    }

    public static <T> Chat<T> Right(String chatLine, T thing)
    {
        return Right(ImList.on(chatLine), thing);
    }

    public static <T> Chat<T> Right(ImList<String> chatLines, T thing)
    {
        return new Chat<>(chatLines, thing);
    }

    public static <T> Chat<T> Left(String chatLine)
    {
        return Left(ImList.on(chatLine));
    }

    public static <T> Chat<T> Left(ImList<String> chatLines)
    {
        return new Chat<>(chatLines);
    }

    public static <T> Chat<T> Left(ImList<String> chatLines, T thing)
    {
        return new Chat<>(chatLines, thing, false);
    }

    /**
     * <p> Try running
     * {@code fn}
     * . If this throws, return Chat.Left(
     * {@code errorMessage}
     * ) else Chat.Right("")
     *
     */
    public static Chat<String> tryCatch(FnProducer fn, String errorMessage)
    {
        try
        {
            fn.doit();
            return Chat.Right("");
        } catch (Exception e)
        {
            return Chat.Left(errorMessage);
        }
    }

    public <U> Chat<U> flatMap(Fn<T, Chat<U>> fn)
    {

        if (isOk())
        {
            Chat<U> result = fn.of(right);

            if (result == null)
                throw new FlatMapFunctionReturnedNull(this);
            else
                return result.isOk()
                       ? Chat.Right(left.append(result.left), result.right)
                       : Chat.Left(left.append(result.left));
        }
        else
        {
            return Chat.Left(left);
        }
    }

    /**
     * <p> This function does the same ae flatMap except that the function (
     * {@code fn}
     * ) it invokes is not one that
     * has a parameter. This function still returns a
     * {@code Chat<U>}
     *  but it doesn't need any arguments
     *
     */
    public <U> Chat<U> andThen(FnProducer<Chat<U>> fn)
    {
        if (isOk())
        {
            Chat<U> result = fn.doit();

            if (result == null)
                throw new FlatMapFunctionReturnedNull(this);
            else
                return result.isOk()
                       ? Chat.Right(left.append(result.left), result.right)
                       : Chat.Left(left.append(result.left));
        }
        else
        {
            return Chat.Left(left);
        }
    }

    public boolean isOk()
    {
        return isOk;
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(left, right, isOk);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("left", "right", "isOk");
    }

    public Chat<T> ifFail(String errorMessage)
    {
        return isOk()
               ? this
               : Chat.Left(left.push(errorMessage));
    }

    public Chat<T> prepend(ImList<String> lines)
    {
        return new Chat<T>(lines.append(left), right, isOk);
    }

    @SuppressWarnings("unchecked")
    public Chat<T> prependToFirstLine(String prefix)
    {
        if (left.isEmpty())
            return new Chat(ImList.on(prefix), right, isOk);
        else
            return new Chat(ImList.cons(prefix + left.head(), left.tail()), right, isOk);
    }

    /**
     * <p> Combine a list of chats into a single chat
     */
    @SafeVarargs
    public static <A> Chat<ImList<A>> combine(Chat<A>... chats)
    {
        return combine(ImList.on(chats));
    }

    //    public static <A, B> Chat<A> do_(Chat<A> start, ImList<B> bs, Fn<B, Chat<A>> fun)
    //    {
    //        return bs.foldl(start, (z,e ) -> z.andThen(() -> fun.of(e)));
    //    }

    public Chat<T> doMany(ImList<T> bs, Fn<T, Chat<T>> fn)
    {
        return bs.foldl(this, (z, e) -> z.andThen(() -> fn.of(e)));
    }

    /**
     * <p> Combine a list of chats into a single chat
     * <p> If all the chats are ok then return an ok chat with a list of T - else return a failed chat
     * In either case we concatenate the lists of Strings in left
     *
     */
    public static <A> Chat<ImList<A>> combine(ImList<Chat<A>> chats)
    {
        ImList<Chat<A>> okChats = chats.filter(c -> c.isOk());

        ImList<String> ss = chats.flatMap(c -> c.left);

        return okChats.size() == chats.size()
               ? Chat.Right(ss, okChats.map(c -> c.right))
               : Chat.Left(ss, okChats.map(c -> c.right));
    }

    public String getChatString()
    {
        return TopDownBox.withAll(left).toString();
    }

    public T rightOrThrow()
    {
        if (isOk())
            return right;
        else
            throw new ChatFailed(left);
    }

    /**
     * <p> A Chat with the same
     * {@code isOk}
     *  values as
     * {@code this}
     *  but with
     * {@code okValue}
     *  as the right value
     *
     */
    @SuppressWarnings("unchecked")
    public <U> Chat<U> cast(U okValue)
    {
        return new Chat(left, okValue, isOk);

    }
}