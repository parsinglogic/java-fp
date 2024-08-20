package dev.javafp.net;

import dev.javafp.ex.ImException;

/**
 * <p> Thrown when {@link ImUrl#on(String)} fails to parse a String into a URL.
 *
 * <p> The message is the text that was in {@link dev.javafp.util.ImEither#left}
 * returned by
 *
 * <p>
 * {@link ImUrl#parse(String)}
 * <p>
 * (which is what  {@link ImUrl#on(String)} calls)
 *
 */
public class UrlParseFailed extends ImException
{
    public UrlParseFailed(String message)
    {
        super(message);
    }
}
