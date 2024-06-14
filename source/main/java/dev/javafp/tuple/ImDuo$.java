package dev.javafp.tuple;

/**
 * A pair with elements of the same type
 */
public class ImDuo$
{

    public static <T> ImDuo<T> on(T fst, T snd)
    {
        return new ImDuo<>(fst, snd);
    }

    public static <T> ImDuo<T> from(ImPair<T, T> pair)
    {
        return new ImDuo<>(pair.snd, pair.fst);
    }

    public static <T> ImDuo<T> byTwo(T e)
    {
        return new ImDuo<T>(e, e);
    }

}
