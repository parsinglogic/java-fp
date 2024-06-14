package dev.javafp.tuple;

/**
 * A pair with elements of the same type
 */
public class ImDuo<A> extends ImPair<A, A>
{
    public ImDuo(A fst, A snd)
    {
        super(fst, snd);
    }

    public ImDuo<A> swap()
    {
        return new ImDuo<>(snd, fst);
    }
}
