package dev.javafp.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RectGridTest
{

    @Test
    public void test1()
    {
        RectGrid g = RectGrid.x(1, 2.0, 3.0).y(4, 5.0, 6.0);

        assertEquals(Rect.on(0, 0, 2, 5), g.at(1, 1));
        assertEquals(Rect.on(0, 9, 2, 6), g.at(1, 2));
        assertEquals(Rect.on(3, 0, 3, 5), g.at(2, 1));
        assertEquals(Rect.on(3, 9, 3, 6), g.at(2, 2));
    }
}