package dev.javafp.net;

import dev.javafp.ex.ImException;

public class UrlTestFail extends ImException
{

    public UrlTestFail(String input)
    {
        super("Fail on " + input);
    }

    public UrlTestFail(String input, Exception e)
    {
        super("Fail on " + input + "\n" + e);
    }

    public UrlTestFail(String input, String error)
    {
        super("Fail on " + input + "\n" + "Error: " + error);
    }

}
