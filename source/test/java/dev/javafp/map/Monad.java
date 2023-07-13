package dev.javafp.map;

import dev.javafp.func.Fn;

public interface Monad<A> extends Functor<A>
{

    Monad<A> join(Monad<? extends Monad<?>> m);

    default <B> Monad<B> flatMap(Fn<A, ? extends Monad<B>> fn)
    {
        return join((Monad) map(fn));
    }
}
