package dev.javafp.util;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuadraticSolverTest
{

    double SMALL = 0.000000000000001;

    @Test
    public void testNeg()
    {
        assertEquals(ImList.on(-1.0), QuadraticSolver.solve(-1, -2, -1));
    }

    @Test
    public void testZero()
    {
        assertEquals(ImList.on(Double.NaN), QuadraticSolver.solve(0, 0, 0));
    }

    @Test
    public void testSmall()
    {
        ImList<Double> results = QuadraticSolver.solve(0.00001, 1, -2);
        assertEquals(-100001.9999600016, results.at(1), SMALL);
        assertEquals(1.9999600015974115, results.at(2), SMALL);
    }

    @Test
    public void testOneRoot()
    {
        ImList<Double> results = QuadraticSolver.solve(1, -6, 9);
        assertEquals(3, results.at(1), SMALL);
        assertEquals(1, results.size());
    }

    @Test
    public void testOneRoot2()
    {
        ImList<Double> results = QuadraticSolver.solve(1, -6, 8.9999999999999999);
        assertEquals(3, results.at(1), SMALL);
        assertEquals(1, results.size());
    }

    @Test
    public void testOneRoot3()
    {
        ImList<Double> results = QuadraticSolver.solve(1, -6, 8.999999999999999);
        assertEquals(2.9999999578531513, results.at(1), SMALL);
        assertEquals(3.0000000421468487, results.at(2), SMALL);
    }

}