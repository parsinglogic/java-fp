package dev.javafp.net;

import dev.javafp.ex.ImException;

public class PercentEncodeException extends ImException
{
    public PercentEncodeException(Character c)
    {
        super("Can't encode character because it is too large:" + c);
    }
}
