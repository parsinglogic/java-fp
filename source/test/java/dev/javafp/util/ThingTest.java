package dev.javafp.util;

import dev.javafp.func.FnPairConsumer;
import org.junit.Test;

public class ThingTest
{

    @Test
    public void one()
    {
        FnPairConsumer<Thing, Integer> x = Thing::foo;
    }
}