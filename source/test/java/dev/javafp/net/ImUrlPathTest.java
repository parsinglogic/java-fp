package dev.javafp.net;

import dev.javafp.lst.ImList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImUrlPathTest
{

    @Test
    public void testPathsWithSingleDot()
    {
        assertEquals("/ab", ImUrl.removeRelatives(ImList.onString("/ab")));
        assertEquals("/ab/", ImUrl.removeRelatives(ImList.onString("/ab/")));
        assertEquals("/ab/", ImUrl.removeRelatives(ImList.onString("/ab/.")));
        assertEquals("/ab/", ImUrl.removeRelatives(ImList.onString("/ab/./")));
        assertEquals("/ab//", ImUrl.removeRelatives(ImList.onString("/ab/.//")));
        assertEquals("/ab///", ImUrl.removeRelatives(ImList.onString("/ab/.///")));
        assertEquals("/ab/", ImUrl.removeRelatives(ImList.onString("/ab/./././.")));
        assertEquals("/ab/", ImUrl.removeRelatives(ImList.onString("/ab/./././.")));
    }

    public void testPathsWithOther()
    {
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString(".")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("..")));

    }

    @Test
    public void testPathsWithDoubleDot()
    {
        assertEquals("/a/b", ImUrl.removeRelatives(ImList.onString("/a/b")));
        assertEquals("/a/b/", ImUrl.removeRelatives(ImList.onString("/a/b/")));
        assertEquals("/a/", ImUrl.removeRelatives(ImList.onString("/a/b/..")));
        assertEquals("/a/", ImUrl.removeRelatives(ImList.onString("/a/b/../")));
        assertEquals("/a//", ImUrl.removeRelatives(ImList.onString("/a/b/..//")));
        assertEquals("/a/", ImUrl.removeRelatives(ImList.onString("/a//..")));
        assertEquals("/a/", ImUrl.removeRelatives(ImList.onString("/a//../")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("/a//../..")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("/a//../../")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("/a//../../..")));
        assertEquals("/", ImUrl.removeRelatives(ImList.onString("/a//../../../..")));
    }

    @Test
    public void testPathsWithPercentEncodedDots()
    {
        assertEquals("/four/bing", ImUrl.removeRelatives(ImList.onString("/four/%2E/bing")));
    }

}