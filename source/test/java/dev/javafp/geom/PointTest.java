package dev.javafp.geom;

import dev.javafp.eq.Equals;
import dev.javafp.lst.ImList;
import dev.javafp.rand.PseudoRandom;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import static dev.javafp.geom.Point.pt;
import static dev.javafp.geom.Point.zero;
import static dev.javafp.lst.ImList.on;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PointTest
{

    @Test
    public void testEqualsStandard()
    {
        // A list of things that are all different
        ImList<Object> thingOnes = on(
                pt(1, 2),
                pt(3, 4),
                "a string");

        // The same lists as in thingOnes
        ImList<Object> thingTwos = on(
                pt(1, 2),
                pt(3, 4),
                "a string");

        TestUtils.checkThings(thingOnes, thingTwos);
    }

    @Test
    public void testEquals()
    {
        Point p1 = Point.on(1, 2);
        Point p2 = Point.on(1, 2);

        assertEquals(p1, p2);
        assertTrue(Equals.isEqual(p1, p1));
        assertTrue(Equals.isEqual(p1, p2));
        assertFalse(Equals.isEqual(p1, null));
        assertFalse(Equals.isEqual(null, p2));
        assertFalse(Equals.isEqual(p1, zero));
        assertFalse(Equals.isEqual(zero, p1));
        assertFalse(Equals.isEqual("", p1));
        assertFalse(Equals.isEqual(p1, ""));
    }

    @Test
    public void testSeveral()
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
        TestUtils.assertToStringEquals("(14, 6)", pt(2, 3).times(7, 2));

        // mid, midPoint, centre
        TestUtils.assertToStringEquals("(3, 4.5)", pt(2, 3).midPoint(pt(4, 6)));
        TestUtils.assertToStringEquals("(3, 4.5)", Point.mid(pt(4, 6), pt(2, 3)));
        TestUtils.assertToStringEquals("(2, 3)", pt(4, 6).centre());

        // x, y
        TestUtils.assertToStringEquals("(2, 3)", pt(2, 8).y(3));
        TestUtils.assertToStringEquals("(9, 3)", pt(2, 3).x(9));

        // north, south, east, west
        TestUtils.assertToStringEquals("(1, -2)", pt(1, 1).north(3));
        TestUtils.assertToStringEquals("(1, 4)", pt(1, 1).south(3));
        TestUtils.assertToStringEquals("(4, 1)", pt(1, 1).east(3));
        TestUtils.assertToStringEquals("(-2, 1)", pt(1, 1).west(3));

        // dot(Point other)
        TestUtils.assertToStringEquals("1.0", pt(1, 0).dot(pt(1, 0)));
        TestUtils.assertToStringEquals("0.0", pt(1, 0).dot(pt(0, 1)));
        TestUtils.assertToStringEquals("1.0", pt(1, 1).dot(pt(0, 1)));

        // corner
        TestUtils.assertToStringEquals("|(0, 0) (1, 2)|", pt(1, 2).corner(zero));

        // toRect()
        TestUtils.assertToStringEquals("|(0, 0) (1, 2)|", pt(1, 2).toRect());
        TestUtils.assertToStringEquals("|(1, 2) (3, 4)|", pt(1, 2).NW(pt(3, 4)));
        TestUtils.assertToStringEquals("|(-2, 2) (3, 4)|", pt(1, 2).NE(pt(3, 4)));
        TestUtils.assertToStringEquals("|(1, -2) (3, 4)|", pt(1, 2).SW(pt(3, 4)));
        TestUtils.assertToStringEquals("|(-2, -2) (3, 4)|", pt(1, 2).SE(pt(3, 4)));

        // Double> toPair()
        TestUtils.assertToStringEquals("(5.0, 6.0)", pt(5, 6).toPair());

        // plusX(double xOff)
        // plusY(double yOff)
        TestUtils.assertToStringEquals("(5, 1)", pt(1, 1).plusX(4));
        TestUtils.assertToStringEquals("(1, 5)", pt(1, 1).plusY(4));

        // getValues()
        TestUtils.assertToStringEquals("[2.1, 3.2]", pt(2.1, 3.2).getValues());

        // getNames()
        TestUtils.assertToStringEquals("[x, y]", pt(2.1, 3.2).getNames());

    }

    @Test
    public void testGeAndLe()
    {
        int count = 100;

        // A list of random numbers
        ImList<Double> ds = ImList.unfold(PseudoRandom.nextDouble(0, 10), i -> PseudoRandom.nextDouble(0, 10)).take(4 * count);

        ImList<ImList<Double>> fours = ds.group(4);

        fours.foreach(d -> checkGe(pt(d.at(1), d.at(2)), pt(d.at(3), d.at(4))));

        // test with the two points being equal
        fours.foreach(d -> checkGe(pt(d.at(1), d.at(2)), pt(d.at(1), d.at(2))));

    }

    private void checkGe(Point p1, Point p2)
    {
        //        say(p1, p2);
        boolean ge = p1.ge(p2);
        assertEquals(p1.x >= p2.x && p1.y >= p2.y, ge);

        boolean le = p1.le(p2);
        assertEquals(p2.x >= p1.x && p2.y >= p1.y, le);

        boolean lt = p1.lt(p2);
        assertEquals(p2.x > p1.x && p2.y > p1.y, lt);
    }

    @Test
    public void testNormalise()
    {
        Point p1 = Point.on(1.345e-6, 3.98989e-8);
        Point p2 = Point.on(400, 3);
        Point p3 = Point.on(500000, -878687686);

        assertEquals(1.0, p1.normalise().length(), 1e-10);
        assertEquals(1.0, p2.normalise().length(), 1e-10);
        assertEquals(1.0, p3.normalise().length(), 1e-10);
    }

}