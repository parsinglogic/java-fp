package dev.javafp.geom;

import dev.javafp.lst.ImList;
import dev.javafp.rand.PseudoRandom;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static dev.javafp.geom.Point.pt;
import static org.junit.Assert.assertEquals;

public class PointTest
{

    @Test
    public void testEquals() throws Exception
    {
        Point p1 = Point.on(1, 2);
        Point p2 = Point.on(1, 2);

        assertEquals(p1, p2);
    }

    @Test
    public void testSeveral() throws Exception
    {
        TestUtils.assertToStringEquals("(1.2, 3.4)", Point.on(1.2, 3.4));
        TestUtils.assertToStringEquals("(0.123, 0.456)", pt(0.123, 0.456));

        // offsets
        TestUtils.assertToStringEquals("(123, 0)", Point.xOffset(123));
        TestUtils.assertToStringEquals("(0, 456)", Point.yOffset(456));

        // square
        TestUtils.assertToStringEquals("(99, 99)", Point.square(99));

        // plus, minus
        TestUtils.assertToStringEquals("(99, 99)", pt(44, 55).plus(pt(55, 44)));
        TestUtils.assertToStringEquals("(16, 37)", pt(40, 40).minus(pt(24, 3)));

        // times
        TestUtils.assertToStringEquals("(14, 21)", pt(2, 3).times(7));

        // mid, midPoint, centre
        TestUtils.assertToStringEquals("(3, 4.5)", pt(2, 3).midPoint(pt(4, 6)));
        TestUtils.assertToStringEquals("(3, 4.5)", Point.mid(pt(4, 6), pt(2, 3)));
        TestUtils.assertToStringEquals("(2, 3)", pt(4, 6).centre());

        // x, y
        TestUtils.assertToStringEquals("(2, 3)", pt(2, 8).y(3));
        TestUtils.assertToStringEquals("(9, 3)", pt(2, 3).x(9));

    }

    @Test
    public void testGeAndLe() throws Exception
    {
        int count = 100;

        // A list of random numbers
        ImList<Double> ds = ImList.unfold(PseudoRandom.nextDouble(0, 10), i -> PseudoRandom.nextDouble(0, 10)).take(4 * count);

        ImList<ImList<Double>> fours = ds.group(4);

        fours.foreach(d -> checkGe(pt(d.at(1), d.at(2)), pt(d.at(3), d.at(4))));

    }

    private void checkGe(Point p1, Point p2)
    {
        //        say(p1, p2);
        boolean ge = p1.ge(p2);
        assertEquals(p1.x >= p2.x && p1.y >= p2.y, ge);

        boolean le = p1.le(p2);
        assertEquals(p2.x >= p1.x && p2.y >= p1.y, le);
    }

    @Test
    public void testNormalise() throws Exception
    {
        Point p1 = Point.on(1.345e-6, 3.98989e-8);
        Point p2 = Point.on(400, 3);
        Point p3 = Point.on(500000, -878687686);

        assertEquals(1.0, p1.normalise().length(), 1e-10);
        assertEquals(1.0, p2.normalise().length(), 1e-10);
        assertEquals(1.0, p3.normalise().length(), 1e-10);
    }

}