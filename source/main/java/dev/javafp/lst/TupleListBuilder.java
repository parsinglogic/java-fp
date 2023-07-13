package dev.javafp.lst;

import dev.javafp.tuple.ImQuartet;
import dev.javafp.tuple.ImTriple;

public class TupleListBuilder
{
    public static <C, D, A, B> ImList<ImQuartet<A, B, C, D>> on4(ImList<A> as, ImList<B> bs, ImList<C> cs, ImList<D> ds)
    {
        return ImQuartetList.on(as, bs, cs, ds);
    }

    public static <B, A, C> ImList<ImTriple<A, B, C>> on3(ImList<A> as, ImList<B> bs, ImList<C> cs)
    {
        return ImTripleList.on(as, bs, cs);
    }
}
