package dev.javafp.util;

public class Foo
{
    public void a()
    {
        Say.say("Foo.a");
    }

    public void c()
    {
        Say.say("Foo.c");
    }

    public void d()
    {
        c();
    }

}