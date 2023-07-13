package dev.javafp.file;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * Created by aove215 on 03/05/2017.
 */
public class LineReaderTest
{
    @Test
    public void readLine()
    {

        StringReader stringReader = new StringReader("abc\ndef");
        BufferedReader abc = new BufferedReader(stringReader);
        LineReader reader = LineReader.on(abc);

        assertEquals("abc", reader.readLine());
        reader.pushLine("xyz");
        assertEquals("xyz", reader.readLine());

        assertEquals("def", reader.readLine());

        assertEquals(null, reader.readLine());
    }

}