package dev.javafp.geom;

import dev.javafp.tuple.ImTriple;
import dev.javafp.util.Say;
import org.junit.Test;

public class LineEquationTest
{

    @Test
    public void testIntersectParallel()
    {

        LineEquation eq = LineEquation.on(1, 2, 3);
        LineEquation eq2 = LineEquation.on(100, 200.000000001, 1000);

        ImTriple<Double, Double, Double> p = eq.intersection(eq2);

        Say.say(p);

        //
        //        assertClose(0, tv1.lineEquation().applyTo(in));
        //        assertClose(0, tv2.lineEquation().applyTo(in));
    }
}