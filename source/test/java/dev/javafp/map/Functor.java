package dev.javafp.map;

import dev.javafp.func.Fn;

public interface Functor<A>
{
    <B> Functor<?> map(Fn<A, B> fn);
}
