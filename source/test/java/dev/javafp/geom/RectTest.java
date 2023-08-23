package dev.javafp.geom;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.util.Random;

import static dev.javafp.geom.Point.pt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RectTest
{

    @Test
    public void testGetCentre()
    {
        Rect r = new Rect(Point.zero, Point.on(4, 3)).moveTo(Point.on(1, 2));

        assertEquals(5, r.corner.minus(r.origin).length(), SMALL);

        assertEquals(r.origin.midPoint(r.corner), r.getCentre());
    }

    @Test
    public void testInset()
    {
        int in = 1;
        Rect r = new Rect(Point.zero, Point.on(4, 3)).moveTo(Point.on(1, 2));

        Rect inset = r.inset(in);

        assertEquals(r, inset.outset(in));
        Point pIn = Point.on(in, in);
        assertEquals(r.size.minus(pIn.times(2)), inset.size);

        assertEquals(r.origin.plus(pIn), inset.origin);
        assertEquals(r.corner.minus(pIn), inset.corner);
    }

    @Test
    public void testCentreOn()
    {
        Rect r = new Rect(Point.zero, Point.on(6, 4));

        Point centre = Point.on(7, 8);
        Rect centred = r.centreOn(centre);

        assertEquals(centre, centred.centre());

        assertEquals(Rect.on(4, 6, 6, 4), centred);
    }

    @Test
    public void testCentreIn()
    {
        Rect r = new Rect(Point.zero, Point.on(4, 3));

        Rect container = new Rect(100, 100, 80, 60);
        Rect centred = r.centreIn(container);

        assertEquals(container.getCentre(), centred.getCentre());
        assertEquals(r.size, centred.size);
    }

    @Test
    public void testLayoutVertically()
    {
        ImList<Rect> rects = Rect.layoutVertically(1, Point.on(3, 2), Point.on(4, 3), Point.on(5, 4));

        assertEquals(Rect.on(0, 0, 3, 2), rects.at(1));
        assertEquals(Rect.on(0, 3, 4, 3), rects.at(2));
        assertEquals(Rect.on(0, 7, 5, 4), rects.at(3));
    }

    @Test
    public void testLayoutHorizontally()
    {
        ImList<Rect> rects = Rect.layoutHorizontally(1, Point.on(3, 2), Point.on(4, 3), Point.on(5, 4));

        assertEquals(Rect.on(0, 0, 3, 2), rects.at(1));
        assertEquals(Rect.on(4, 0, 4, 3), rects.at(2));
        assertEquals(Rect.on(9, 0, 5, 4), rects.at(3));
    }

    @Test
    public void testPositions()
    {
        Rect one = new Rect(Point.zero, Point.on(6, 4));
        Rect two = new Rect(Point.on(10, 20), Point.on(30, 40));

        assertEquals(one.north(two.south()).north(), two.south());
        assertEquals(one.south(two.north()).south(), two.north());
        assertEquals(one.east(two.west()).east(), two.west());
        assertEquals(one.west(two.east()).west(), two.east());

        assertEquals(one.northEast(two.southWest()).northEast(), two.southWest());
        assertEquals(one.northWest(two.southEast()).northWest(), two.southEast());
        assertEquals(one.southEast(two.northWest()).southEast(), two.northWest());
        assertEquals(one.southWest(two.northEast()).southWest(), two.northEast());

    }

    @Test
    public void testContains()
    {
        Rect r = Rect.on(0, 0, 9, 7);

        assertTrue(r.contains(r.origin));
        assertTrue(r.contains(r.corner));
        assertTrue(r.contains(r.centre()));

        Random rand = new Random(12345);

        ImList<Integer> xs = ImList.randomInts(rand, -2, 9);
        ImList<Integer> ys = ImList.randomInts(rand, -3, 9);

        xs.zip(ys).take(10000).foreach(p -> assertEquals(r.contains(Point.on(p.fst, p.snd)), contains(r, p)));

    }

    private boolean contains(Rect r, ImPair<Integer, Integer> p)
    {
        return p.fst >= r.origin.x && p.fst <= r.corner.x && p.snd >= r.origin.y && p.snd <= r.corner.y;
    }

    @Test
    public void testExtent()
    {
        Random rnd = new Random(12345);

        ImList<Double> ds = ImList.randomDoubles(rnd);

        ImList<Rect> rects = ds.take(1000 * 4).group(4).map(i -> Rect.on(i.at(1), i.at(2), i.at(3), i.at(4)));

        Rect extent = Rect.extent(rects);

        // All the origins and corners of rects will be inside extent
        rects.foreach(r -> assertTrue(extent.contains(r.origin) && extent.contains(r.corner)));

        // Each edge of extent will have at least one point on it from origin or corner of the rects
        assertTrue(rects.filter(r -> near(extent.origin.x, r.origin.x)).size() > 0);
        assertTrue(rects.filter(r -> near(extent.origin.y, r.origin.y)).size() > 0);
        assertTrue(rects.filter(r -> near(extent.corner.x, r.corner.x)).size() > 0);
        assertTrue(rects.filter(r -> near(extent.corner.y, r.corner.y)).size() > 0);

    }

    @Test
    public void testJustify()
    {

        Point pt = pt(1, 1);
        Rect container = Rect.on(1, 1, 3, 3);
        //        assertEquals(Rect.on(1, 1, 1, 1), pt.justifyIn(container, Orient2.LeftTop));
        //        assertEquals(Rect.on(2, 2, 1, 1), pt.justifyIn(container, Orient2.CentreCentre));
        //        assertEquals(Rect.on(3, 3, 1, 1), pt.justifyIn(container, Orient2.RightBottom));
        //
        //        assertEquals(Rect.on(1, 2, 1, 1), pt.justifyIn(container, Orient2.LeftCentre));

        assertEquals(Rect.on(1, 1, 1, 1), container.justifyIn(Orient2.LeftTop, pt));
        assertEquals(Rect.on(2, 2, 1, 1), container.justifyIn(Orient2.CentreCentre, pt));
        assertEquals(Rect.on(3, 3, 1, 1), container.justifyIn(Orient2.RightBottom, pt));
        assertEquals(Rect.on(1, 2, 1, 1), container.justifyIn(Orient2.LeftCentre, pt));

    }

    private static final double SMALL = 0.00000000000001;

    private boolean near(double x, double y)
    {
        return Math.abs(x - y) < SMALL;
    }
}