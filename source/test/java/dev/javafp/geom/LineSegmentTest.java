package dev.javafp.geom;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LineSegmentTest
{
    @Test
    public void testEquals()
    {
        LineSegment one = LineSegment.originOffset(1, 2, 3, 4);
        LineSegment two = LineSegment.originOffset(1, 2, 3, 4);

        assertEquals(one, two);
    }

    @Test
    public void testRepeat()
    {
        ImList<LineSegment> actual = LineSegment.originOffset(1, 2, 3, 4).repeat(Point.on(3, 4), 3);

        ImList<LineSegment> ex = ImList.on(LineSegment.originOffset(1, 2, 3, 4), LineSegment.originOffset(4, 6, 3, 4), LineSegment.originOffset(7, 10, 3, 4));

        assertEquals(ex, actual);
    }

    @Test
    public void testGrid()
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

        var expected = "[\n"
                + "  1 LineSegment: start:  (0, 0)\n"
                + "                 offset: (4, 0)\n"
                + "    ────────\n"
                + "  2 LineSegment: start:  (0, 1)\n"
                + "                 offset: (4, 0)\n"
                + "    ────────\n"
                + "  3 LineSegment: start:  (0, 2)\n"
                + "                 offset: (4, 0)\n"
                + "    ────────\n"
                + "  4 LineSegment: start:  (0, 0)\n"
                + "                 offset: (0, 2)\n"
                + "    ────────\n"
                + "  5 LineSegment: start:  (1, 0)\n"
                + "                 offset: (0, 2)\n"
                + "    ────────\n"
                + "  6 LineSegment: start:  (2, 0)\n"
                + "                 offset: (0, 2)\n"
                + "    ────────\n"
                + "  7 LineSegment: start:  (3, 0)\n"
                + "                 offset: (0, 2)\n"
                + "    ────────\n"
                + "  8 LineSegment: start:  (4, 0)\n"
                + "                 offset: (0, 2)\n"
                + "]\n";

        assertEquals(expected, LineSegment.grid(1, 1, 4, 2).toString());
    }

    @Test
    public void testLineCoords2()
    {
        check(LineSegment.fromTo(Point.zero, Point.on(1, 1)));
        check(LineSegment.fromTo(Point.zero, Point.on(0, 1)));
        check(LineSegment.fromTo(Point.zero, Point.on(1, 0)));
    }

    private void check(LineSegment tv)
    {
        LineEquation lc = tv.lineEquation();

        assertClose(0, lc.applyTo(tv.start));
        assertClose(0, lc.applyTo(tv.getCorner()));
    }

    private void assertClose(double expected, double actual)
    {
        assertTrue(Math.abs(expected - actual) < 1e-10);
    }

}