package dev.javafp.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointTest
{

    @Test
    public void testEquals() throws Exception
    {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);

        assertEquals(p1, p2);
    }

    @Test
    public void testPlus() throws Exception
    {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(4, 3);
        Point p3 = new Point(5, 5);

        assertEquals(p3, p1.plus(p2));
    }

    @Test
    public void testNormalise() throws Exception
    {
        Point p1 = new Point(1.345e-6, 3.98989e-8);
        Point p2 = new Point(400, 3);
        Point p3 = new Point(500000, -878687686);

        assertEquals(1.0, p1.normalise().length(), 1e-10);
        assertEquals(1.0, p2.normalise().length(), 1e-10);
        assertEquals(1.0, p3.normalise().length(), 1e-10);
    }

}