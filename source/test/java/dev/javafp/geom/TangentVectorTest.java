package dev.javafp.geom;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImTriple;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TangentVectorTest
{
    @Test
    public void testEquals() throws Exception
    {
        TangentVector one = new TangentVector(1, 2, 3, 4);
        TangentVector two = new TangentVector(1, 2, 3, 4);

        assertEquals(one, two);
    }

    @Test
    public void testRepeat() throws Exception
    {
        ImList<TangentVector> actual = new TangentVector(1, 2, 3, 4).repeat(Point.on(3, 4), 3);

        ImList<TangentVector> ex = ImList.on(new TangentVector(1, 2, 3, 4), new TangentVector(4, 6, 3, 4), new TangentVector(7, 10, 3, 4));

        assertEquals(ex, actual);
    }

    @Test
    public void testGrid() throws Exception
    {
        /**
         *
         * Let's draw this:
         *
         *      1   2   3   4
         *    ┌───┬───┬───┬───┐
         *  1 │   │   │   │   │
         *    ├───┼───┼───┼───┤
         *  2 │   │   │   │   │
         *    └───┴───┴───┴───┘
         *
         */

        var expected = "[\n" +
                "  1 TangentVector: start:  (0, 0)\n" +
                "                   offset: (4, 0)\n" +
                "    ────────\n" +
                "  2 TangentVector: start:  (0, 1)\n" +
                "                   offset: (4, 0)\n" +
                "    ────────\n" +
                "  3 TangentVector: start:  (0, 2)\n" +
                "                   offset: (4, 0)\n" +
                "    ────────\n" +
                "  4 TangentVector: start:  (0, 0)\n" +
                "                   offset: (0, 2)\n" +
                "    ────────\n" +
                "  5 TangentVector: start:  (1, 0)\n" +
                "                   offset: (0, 2)\n" +
                "    ────────\n" +
                "  6 TangentVector: start:  (2, 0)\n" +
                "                   offset: (0, 2)\n" +
                "    ────────\n" +
                "  7 TangentVector: start:  (3, 0)\n" +
                "                   offset: (0, 2)\n" +
                "    ────────\n" +
                "  8 TangentVector: start:  (4, 0)\n" +
                "                   offset: (0, 2)\n" +
                "]\n";

        assertEquals(expected, TangentVector.grid(1, 1, 4, 2).toString());
    }

    @Test
    public void testLineCoords2() throws Exception
    {
        check(TangentVector.fromTo(Point.zero, Point.on(1, 1)));
        check(TangentVector.fromTo(Point.zero, Point.on(0, 1)));
        check(TangentVector.fromTo(Point.zero, Point.on(1, 0)));
    }

    private void check(TangentVector tv)
    {
        LineEquation lc = tv.lineEquation();

        assertClose(0, lc.applyTo(tv.start));
        assertClose(0, lc.applyTo(tv.getCorner()));
    }

    private void assertClose(double expected, double actual)
    {
        assertTrue(Math.abs(expected - actual) < 1e-10);
    }

    @Test
    public void testIntersect()
    {
        TangentVector tv1 = new TangentVector(0, 0, 1, 0);
        TangentVector tv2 = new TangentVector(0, 0, 5, -10);

        ImTriple<Double, Double, Double> intersection = tv1.intersection(tv2);

        Point in = Point.on(intersection.e1 / intersection.e3, intersection.e2 / intersection.e3);
        System.out.println(in);

        assertClose(0, tv1.lineEquation().applyTo(in));
        assertClose(0, tv2.lineEquation().applyTo(in));
    }

    @Test
    public void testIntersectParallel()
    {
        TangentVector tv1 = new TangentVector(0, 0, 1000000000, 1);
        TangentVector tv2 = new TangentVector(1, 1, 1, 0);

        ImTriple<Double, Double, Double> intersection = tv1.intersection(tv2);

        Point in = Point.on(intersection.e1 / intersection.e3, intersection.e2 / intersection.e3);
        System.out.println(in);

        assertClose(0, tv1.lineEquation().applyTo(in));
        assertClose(0, tv2.lineEquation().applyTo(in));
    }
}