package dev.javafp.map;

import dev.javafp.func.Fn;
import dev.javafp.util.Caster;

public interface TestCollection<A> extends Monad<A>
{

    //    default <B> TestCollection<B> map(Fn<A, B> fn)
    //    {
    //        return null;
    //    }

    default <B> TestCollection<B> map(Fn<A, B> fn)
    {
        return null;
    }

    @Override
    default TestCollection<A> join(Monad<? extends Monad<?>> m)
    {
        return null;
    }

    default void foo()
    {
        TestCollection<TestCollection<Integer>> tt = null;
        TestCollection<?> qq = null;

        Monad<Monad<?>> mm = Caster.cast(tt);

        join(Caster.cast(tt));
        join(mm);
        join(tt);
        //        join(qq);
    }

    default TestCollection<A> jj(TestCollection<TestCollection<Integer>> m)
    {
        return join(Caster.cast(m));
    }

    <C> void bar();

    default <D> void bing()
    {
        D foo = null;

        if (foo == null)
        {

        }
    }

}
