package dev.javafp.util;

import java.util.Map;

public class Args
{

    private Map<String, String> args;

    public Args(Map<String, String> args)
    {
        this.args = args;

    }

    public boolean hasArg(String key)
    {
        return args.containsKey(key);
    }

}