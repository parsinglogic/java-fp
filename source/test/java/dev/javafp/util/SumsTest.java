package dev.javafp.util;

import org.junit.Test;

import java.math.BigInteger;

import static junit.framework.TestCase.assertEquals;

public class SumsTest
{

    @Test
    public void testIntegerParsing()
    {
        assertEquals("[0]", "" + Sums.convertToDigitsUsingRadix(bi("10"), bi("0")));
        assertEquals("[9]", "" + Sums.convertToDigitsUsingRadix(bi("10"), bi("9")));
        assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9]", "" + Sums.convertToDigitsUsingRadix(bi("10"), bi("123456789")));

        assertEquals("[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]", "" + Sums.convertToDigitsUsingRadix(bi("2"), bi("1024")));
        assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", "" + Sums.convertToDigitsUsingRadix(bi("2"), bi("1023")));

        assertEquals("[255, 255, 255, 255, 255]", "" + Sums.convertToDigitsUsingRadix(bi("256"), bi("256").pow(5).subtract(bi("1"))));
        assertEquals("[1, 0, 0, 0, 0, 0]", "" + Sums.convertToDigitsUsingRadix(BigInteger.valueOf(255), BigInteger.valueOf(255).pow(5)));

        assertEquals("[5, 245, 222, 224]", "" + Sums.convertToDigitsUsingRadix(BigInteger.valueOf(256), new BigInteger("99999456")));
    }

    private BigInteger bi(String numberString)
    {
        return new BigInteger(numberString);
    }
}