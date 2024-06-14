package dev.javafp.tuple;

import org.junit.Test;

import static dev.javafp.util.Say.say;

public class ImDuoTest
{

    @Test
    public void testCreate()
    {
        ImDuo<Integer> p1 = ImDuo$.on(1, 2);

        say(p1);

    }

}