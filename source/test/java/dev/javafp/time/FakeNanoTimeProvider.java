package dev.javafp.time;

import dev.javafp.lst.ImList;

public class FakeNanoTimeProvider extends NanoTimeProvider
{

    private ImList<Long> timestamps;

    public FakeNanoTimeProvider(ImList<Long> timestamps)
    {
        this.timestamps = timestamps;
    }

    public long nanoTime()
    {
        long n = timestamps.head();

        timestamps = timestamps.tail();

        return n;
    }
}
