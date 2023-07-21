package dev.javafp.lst;

/**
 * Read lines from a Reader
 * TODO
 * Set up a string "abcd\r\r\n\n"
 * for each permutation, assert that our lines are the same as Reader:readLine
 *
 * Hmm I can't do this. The semantics are different. I consider CR without NL as part of the line
 * I guess I could use readLine myself
 */
public class ImListOnReader2Test
{

    //    @Test
    //    public void testSimple()
    //    {
    //        ImList<Integer> lines = ImRange.oneTo(5);
    //
    //        StringReader reader = new StringReader(lines.toString(""));
    //
    //        ImList<Character> list = ImList.onReader2(new BufferedReader(reader));
    //
    //        assertEquals("" + lines, "" + list);
    //    }
    //
    //    @Test
    //    public void testOnEmpty()
    //    {
    //
    //        StringReader reader = new StringReader("");
    //
    //        ImList<Character> list = ImList.onReader2(new BufferedReader(reader));
    //
    //        assertEquals(ImList.on(), list);
    //    }

}