package dev.javafp.util;

import dev.javafp.geom.Point;
import dev.javafp.lst.ImList;
import org.junit.Test;

import java.util.List;

import static dev.javafp.util.SvgTransform.identity;
import static dev.javafp.util.SvgTransform.move;
import static dev.javafp.util.SvgTransform.scale;
import static org.junit.Assert.assertEquals;

public class SvgTransformTest
{

    @Test
    public void testIdMultIsid() throws Exception
    {
        assertEquals(identity(), identity().preMultiplyBy(identity()));
        Say.println("\n" + identity());
    }

    @Test
    public void testTwo() throws Exception
    {
        assertEquals(identity(), scale(2).preMultiplyBy(scale(0.5)));
    }

    @Test
    public void testTran() throws Exception
    {
        assertEquals(identity(), move(3, 4).preMultiplyBy(move(-3, -4)));
    }

    @Test
    public void testToString() throws Exception
    {
        assertEquals("1,0,0,1,0,0", "" + identity());
    }

    @Test
    public void testFromSvgString() throws Exception
    {
        SvgTransform t = new SvgTransform(1.2, 2.3, 3.4, 4.5, 5.6, 6.7);
        assertEquals(t, SvgTransform.fromSvgString(t.getSvgString()));
    }

    @Test
    public void testTranAndScale() throws Exception
    {
        assertEquals(Point.on(8, 16), Point.on(1, 1).preMultiply(scale(2).postMultiplyBy(move(3, 7))));
        Say.println("\n" + scale(2).postMultiplyBy(move(3, 7)));
    }

    @Test
    public void testInverse() throws Exception
    {
        assertEquals(identity(), identity().inverse());

        ImList<ImList<Integer>> perms = ImList.on(1, 2, 3, 4).permutations();
        Say.println("done");

        for (ImList<Integer> p : perms)
        {
            SvgTransform t1 = new SvgTransform(p.at(1), 0, 0, p.at(2), p.at(3), p.at(4));

            System.out.println("tran " + t1);
            System.out.println("inverse " + t1.inverse());

            assertEquals(identity(), t1.preMultiplyBy(t1.inverse()));
        }
    }

    @Test
    public void testTranAndScale2() throws Exception
    {
        ImList<ImList<Integer>> perms = ImList.on(1, 2, 3, 4, 5, 6).permutations();
        Say.println("done");

        for (ImList<Integer> p : perms)
        {

            List<Integer> l = p.toList();
            SvgTransform t1 = new SvgTransform(l.get(0), l.get(1), l.get(2), l.get(3), l.get(4), l.get(5));
            SvgTransform t2 = new SvgTransform(l.get(5), l.get(4), l.get(3), l.get(2), l.get(1), l.get(0));
            Point a1 = Point.on(1, 1).preMultiply(t2.postMultiplyBy(t1));
            //Point a2 = t2.apply(t1.apply(Point.on(1, 1)));

            Point a2 = Point.on(1, 1).preMultiply(t1).preMultiply(t2);
            assertEquals(a2, a1);
        }
    }
}