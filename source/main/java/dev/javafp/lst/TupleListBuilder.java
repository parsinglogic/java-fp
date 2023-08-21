package dev.javafp.lst;

import dev.javafp.tuple.ImQuartet;
import dev.javafp.tuple.ImTriple;

/**
 * <p> Utility to build lists of tuples from lists of single elements.
 *
 * <p> Note tha this class is not intended for use by clients. Currently, our package structure does not allow
 * us to make this class non-public.
 */
public class TupleListBuilder
{
    /**
     * @deprecated This method is intended for internal use and should not be called by clients.
     */
    @Deprecated
    public static <C, D, A, B> ImList<ImQuartet<A, B, C, D>> on4(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        return ImQuartetList.on(as, bs, cs, ds);
    }

    /**
     * @deprecated This method is intended for internal use and should not be called by clients.
     */
    @Deprecated
    public static <B, A, C> ImList<ImTriple<A, B, C>> on3(ImList<A> as, ImList<B> bs, ImList<C> cs)
    {
        return ImTripleList.on(as, bs, cs);
    }
}
