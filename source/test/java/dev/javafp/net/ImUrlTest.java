package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImUtils;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.TestUtils;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImCodePoint;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Path;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImUrlTest
{

    static ImList<Fn<String, Boolean>> splitPredicates = ImList.join(ImList.repeat(ImList.on(i1 -> !Eq.uals(i1, "0"), i2 -> Eq.uals(i2, "0")), 5));
    ImMap<String, String> empty = ImMap.empty();

    ImList<ImMap<String, String>> authorities2 = ImList.on(
            empty
                    .put("input", "user:password@")
                    .put("user", "user")
                    .put("password", "password")
                    .put("rest", "")

    );

    ImList<String> hostsToConvert = ImList.on(
            "abc", "abc",
            "[1:2]", "[1:2]",
            "[abc]", "[abc]",
            "räksmörgås.josefsson.org", "xn--rksmrgs-5wao1o.josefsson.org",
            "xn--rksmrgs-5wao1o.josefsson.org", "xn--rksmrgs-5wao1o.josefsson.org"
    );

    /**
     * These are ok:
     *
     * http://:@@b:?sdf
     * http://:@       @b:?sdf
     * http://[1::0008]
     * http://a:
     * http://127.0x1
     * http://127.011
     * http://a: <>[]^`{|} b@c
     *
     * all allowed printable (apart from alphanum)
     * http://~}{`_=;.-,+)('$! hostname = ~}{`_=;.-,+)('$!
     * http://1/
     *
     * This is apple.com
     *
     * https://www.xn--80ak6aa92e.com/
     *
     *
     *
     *
     *
     *
     * [1::0008] is ok
     * 127.0.0.0x0000000000000001 is ok - converts to 127.0.0.1
     * 127.1 is ok - converts to 127.0.0.1
     *
     * http://1.2.3.0000000004 is ok
     * http://127.011 ok - converts to 127.0.0.9
     *
     * http://1.2.3.0xA is ok converts to 1.2.3.10
     * [1::00008] fails
     * [1::0x8] fails
     *
     * http://1.2.3.4.5 fails
     * ttp://1.2.3.0000000009 fails
     *
     *
     * from https://www.xudongz.com/blog/2017/idn-phishing/
     *
     * "短.co" is "xn–s7y.co"
     *
     * аррӏе.com   https://www.xn--80ak6aa92e.com/
     *
     * it is all cyrillic
     *
     *  The screenshots show that the browsers did not convert it. They do now.
     *
     *  This has 3 different types of dots
     *
     *  http:///1｡2。3．4
     *
     *  It seems that these are parsed as being dots for the purposes of IPv4 but ok for host names - they are transformed to . rather than UTf8 % encoded
     */

    @Test
    public void testPercentEncodeBytes()
    {
        assertEquals("%0A", ImUrl.percentEncodeByte((byte) '\n'));
        assertEquals("%7E", ImUrl.percentEncodeByte((byte) '~'));
        assertEquals("%7F", ImUrl.percentEncodeByte((byte) '\u007F'));

    }

    @Test
    public void testPercentEncodeString()
    {
        assertEquals("%E2%99%A5", ImUrl.utf8EncodeAndThenPercentEncode(ImSet.empty(), "♥"));

    }

    private Void checkTripleAgainstExpected(ImPair<String, ImList<ImCodePoint>> pair, ImMap<String, String> fx)
    {
        ImMap<String, String> resMap = empty.put("host", pair.fst).put("rest", pair.snd.toString(""));

        assertEquals(fx.remove("input"), resMap);

        return null;
    }

    @Test
    public void testAllHostsConvertOk()
    {

        ImList<ImList<String>> grouped = hostsToConvert.group(2);

        ImList<String> inputs = grouped.map(i -> i.head());
        ImList<String> expected = grouped.map(i -> i.at(2));

        ImList<String> results = inputs.map(i -> tryConvertingHost(i));

        ImList<ImPair<String, String>> r = inputs.zip(results);

        ImList<String> fails = r.map(i -> Eq.uals(i.fst, i.snd) ? "OK" : "FAIL " + i.fst);

        say(r.toString("\n\n"));

        say(fails.toString("\n"));
    }

    private String tryConvertingHost(String input)
    {
        return ImUrl.convertHost(ImList.onString(input)).a();
    }

    @Test
    public void testGetNumberOrString()
    {

        assertEquals(ImEither.Left("abcde"), ImUrl.IPv4Segments.getNumberOrString("abcde"));
        assertEquals(ImEither.Right(bi("255")), ImUrl.IPv4Segments.getNumberOrString("255"));
        assertEquals(ImEither.Right(bi("0")), ImUrl.IPv4Segments.getNumberOrString("0"));
        assertEquals(ImEither.Right(bi("9")), ImUrl.IPv4Segments.getNumberOrString("011"));
        assertEquals(ImEither.Right(bi("255")), ImUrl.IPv4Segments.getNumberOrString("0377"));
        assertEquals(ImEither.Right(bi("256")), ImUrl.IPv4Segments.getNumberOrString("0400"));
        assertEquals(ImEither.Right(bi("8")), ImUrl.IPv4Segments.getNumberOrString("010"));
        assertEquals(ImEither.Right(bi("15")), ImUrl.IPv4Segments.getNumberOrString("0xf"));
        assertEquals(ImEither.Left("09"), ImUrl.IPv4Segments.getNumberOrString("09"));
        assertEquals(ImEither.Left("0xg"), ImUrl.IPv4Segments.getNumberOrString("0xg"));
    }

    @Test
    public void testParsePath()
    {
        say(ImUrl.parsePath(Path.of("a", "b")));
    }

    @Test
    public void testAsString()
    {
        String e = "";
        String u = "http://user:password@host:1234/path?query#fragment";
        assertEquals(u, ImUrl.on(u).asString());

        u = "http://host:1234/path?query#fragment";
        assertEquals(u, ImUrl.on(u).asString());

        u = "http://host:1234/path?query";
        assertEquals(u, ImUrl.on(u).asString());

        u = "http://host:1234/path";
        assertEquals(u, ImUrl.on(u).asString());

        u = "http://host:1234";
        e = "http://host:1234/";
        assertEquals(e, ImUrl.on(u).asString());

        u = "http://host/path";
        assertEquals(u, ImUrl.on(u).asString());

        u = "http://host?q";
        e = "http://host/?q";
        assertEquals(e, ImUrl.on(u).asString());

        u = "http://host/path?q#frag";
        assertEquals(u, ImUrl.on(u).asString());

        u = "file:/p1/p2";
        e = "file:///p1/p2";
        assertEquals(e, ImUrl.on(u).asString());

        u = "file:a:";
        e = "file:///a:";
        assertEquals(e, ImUrl.on(u).asString());

        u = "http://a";
        e = "http://a/";
        assertEquals(e, ImUrl.on(u).asString());

    }

    private BigInteger bi(String numberString)
    {
        return new BigInteger(numberString);
    }

    @Test
    public void testWholeUrl()
    {
        say(BigInteger.valueOf(256).pow(4));
        // The three other dots
        //        say("\uFF61\u3002\uFF0E");
        //
        //        // \u3002 (ideographic full stop), \uFF0E
        //        "аррӏе.com".getBytes();

        say(ImUrl.parse("http://0.256.0"));
        //        say(NewImUrl.parse("http://a.b.9"));
        //        say(NewImUrl.parse("http://0x99999"));
        //        say(NewImUrl.parse("HTTP://u:p@a.b.c:123/a?q#f"));
        //        say(NewImUrl.parse("http://аррӏе.com"));
        //        say(NewImUrl.parse("HTTP://a.b.c:65535"));
        //        say(NewImUrl.parse("http://///a.b.c"));
        //        say(NewImUrl.parse("http://us er:@pass♥word@@♥"));
    }

    @Test
    public void testPath()
    {
        assertEquals("file:///a/b", ImUrl.on(Path.of("/a", "b")).asString());
        assertEquals("file:///a/b", ImUrl.on(Path.of("/a/b")).asString());
        assertEquals("file:///a/", ImUrl.on(Path.of("/a/b/..")).asString());
        assertEquals("file:///", ImUrl.on(Path.of("/")).asString());
    }

    ImList<ImMap<String, String>> urlFixtures = ImList.on(
            empty
                    .put("input", "http://fooo/\uFFFF")
                    .put("scheme", "http")
                    .put("host", "fooo")
                    .put("path", "/%EF%BF%BF"),

            empty
                    .put("input", "file://\u0080/x")
                    .put("fail", ""),

            empty
                    .put("input", "%ED%BF%BF")
                    .put("fail", ""),

            empty
                    .put("input", "http://١b")
                    .put("fail", ""),
            empty
                    .put("input", "http:///1｡2。3．4")
                    .put("scheme", "http")
                    .put("host", "1.2.3.4")
                    .put("path", "/"),
            empty
                    .put("input", "http://[1:0:0:4:5:0:0:8]")
                    .put("scheme", "http")
                    .put("host", "[1::4:5:0:0:8]")
                    .put("path", "/"),

            empty
                    .put("input", "https://1｡2。4")
                    .put("scheme", "https")
                    .put("host", "1.2.0.4")
                    .put("path", "/"),

            empty
                    .put("input", "https://[ffff::127.1]")
                    .put("fail", ""),

            empty
                    .put("input", "h\ttt\rp\ns://x/aa\raa/h\ngj%h")
                    .put("scheme", "https")
                    .put("host", "x")
                    .put("path", "/aaaa/hgj%h"),

            empty
                    .put("input", "http://4294967296")
                    .put("fail", ""),

            empty
                    .put("input", "https://[ffff::127.00.0.1]")
                    .put("fail", ""),

            empty
                    .put("input", "http://[]")
                    .put("fail", ""),
            empty
                    .put("input", "http://[1]")
                    .put("fail", ""),
            empty
                    .put("input", "http://[1:2]")
                    .put("fail", ""),
            empty
                    .put("input", "http://[1:]")
                    .put("fail", ""),
            empty
                    .put("input", "http://[:1]")
                    .put("fail", ""),
            empty
                    .put("input", "http://[::]")
                    .put("fail", ""),

            empty
                    .put("input", "ftp://[::0001]")
                    .put("scheme", "ftp")
                    .put("host", "[::1]")
                    .put("path", "/"),

            empty
                    .put("input", "http://foo.0x")
                    .put("fail", ""),

            empty
                    .put("input", "http:4294967296")
                    .put("fail", ""),

            empty
                    .put("input", "http://:@a")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("path", "/"),

            empty
                    .put("input", "http://:@@a")
                    .put("scheme", "http")
                    .put("password", "%40")
                    .put("host", "a")
                    .put("path", "/"),

            empty
                    .put("input", "http:///a:00000000000000000000000000000000000000000000000000000000000000000000000000000000010")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("port", "10")
                    .put("path", "/"),

            empty
                    .put("input", "http://1.2.3.08")
                    .put("fail", ""),

            empty
                    .put("input", "file://\u00ad/p")
                    .put("fail", ""),

            empty
                    .put("input", "http://a\u0001b/")
                    .put("fail", ""),

            empty
                    .put("input", "file://localhost:")
                    .put("fail", ""),

            empty
                    .put("input", "file://[1::8]/C:/")
                    .put("scheme", "file")
                    .put("host", "[1::8]")
                    .put("path", "/C:/"),

            empty
                    .put("input", "http://%7F")
                    .put("fail", ""),

            empty
                    .put("input", "http://u e:@♥p@@♥")
                    .put("scheme", "http")
                    .put("user", "u%20e")
                    .put("password", "%40%E2%99%A5p%40")
                    .put("host", "xn--g6h")
                    .put("path", "/"),

            empty
                    .put("input", "http://user:password@a")
                    .put("scheme", "http")
                    .put("user", "user")
                    .put("password", "password")
                    .put("host", "a")
                    .put("path", "/"),

            empty
                    .put("input", "http://a.23")
                    .put("fail", ""),

            empty
                    .put("input", "http://a.23.")
                    .put("fail", ""),

            empty
                    .put("input", "https://a/b/c?d#e")
                    .put("scheme", "https")
                    .put("host", "a")
                    .put("path", "/b/c")
                    .put("query", "d")
                    .put("fragment", "e"),

            empty
                    .put("input", "http://1.0")
                    .put("scheme", "http")
                    .put("host", "1.0.0.0")
                    .put("path", "/"),

            empty
                    .put("input", "http://256")
                    .put("scheme", "http")
                    .put("host", "0.0.1.0")
                    .put("path", "/"),

            empty
                    .put("input", "http://A.B.C")
                    .put("scheme", "http")
                    .put("host", "a.b.c")
                    .put("path", "/"),

            empty
                    .put("input", "file://localhost")
                    .put("scheme", "file")
                    .put("path", "/"),

            empty
                    .put("input", "file://LOCALHOST")
                    .put("scheme", "file")
                    .put("path", "/"),

            empty
                    .put("input", "file:a.com")
                    .put("scheme", "file")
                    .put("path", "/a.com"),

            empty
                    .put("input", "file  :a.com")
                    .put("fail", ""),

            empty
                    .put("input", "file:/a.com")
                    .put("scheme", "file")
                    .put("path", "/a.com"),

            empty
                    .put("input", "file://a.com")
                    .put("scheme", "file")
                    .put("host", "a.com")
                    .put("path", "/"),

            empty
                    .put("input", "file:///a.com")
                    .put("scheme", "file")
                    .put("path", "/a.com"),

            empty
                    .put("input", "file:////a.com")
                    .put("scheme", "file")
                    .put("path", "//a.com"),

            empty
                    .put("input", "file:////Z:")
                    .put("scheme", "file")
                    .put("path", "//Z:"),

            empty
                    .put("input", "file:////Z|")
                    .put("scheme", "file")
                    .put("path", "//Z|"),

            empty
                    .put("input", "file://localhost:")
                    .put("fail", ""),

            empty
                    .put("input", "file://localhost|")
                    .put("fail", ""),

            empty
                    .put("input", "file:Z||")
                    .put("scheme", "file")
                    .put("path", "/Z||"),

            empty
                    .put("input", "file://localhost")
                    .put("scheme", "file")
                    .put("path", "/"),

            empty
                    .put("input", "file://LOCALHOST")
                    .put("scheme", "file")
                    .put("path", "/"),

            empty
                    .put("input", "file:a.com:123")
                    .put("scheme", "file")
                    .put("path", "/a.com:123"),

            empty
                    .put("input", "file:A:123")
                    .put("scheme", "file")
                    .put("path", "/A:123"),

            empty
                    .put("input", "file://A|123")
                    .put("fail", ""),

            empty
                    .put("input", "file:B|\\/|")
                    .put("scheme", "file")
                    .put("path", "/B://|"),

            empty
                    .put("input", "file:/B|\\/|")
                    .put("scheme", "file")
                    .put("path", "/B://|"),

            empty
                    .put("input", "file://B|\\/|")
                    .put("scheme", "file")
                    .put("path", "/B://|"),

            empty
                    .put("input", "file:///B|\\/|")
                    .put("scheme", "file")
                    .put("path", "/B://|"),

            empty
                    .put("input", "file:////B|\\/|")
                    .put("scheme", "file")
                    .put("path", "//B|//|"),

            empty
                    .put("input", "file://a.com/a`{} \"<>")
                    .put("scheme", "file")
                    .put("host", "a.com")
                    .put("path", "/a%60%7B%7D%20%22%3C%3E"),

            empty
                    .put("input", "http://[1:0::FFFF:0:0:0:01]")
                    .put("scheme", "http")
                    .put("host", "[1:0:0:ffff::1]")
                    .put("path", "/"),

            empty
                    .put("input", "http://[1:0::FFFF:0:wibble:0:01]")
                    .put("fail", ""),

            empty
                    .put("input", "http://u e:@♥p@@♥")
                    .put("scheme", "http")
                    .put("user", "u%20e")
                    .put("password", "%40%E2%99%A5p%40")
                    .put("host", "xn--g6h")
                    .put("path", "/"),

            empty
                    .put("input", "http://slashes/one/")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/one/"),

            empty
                    .put("input", "http://slashes/zero")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/zero"),

            empty
                    .put("input", "http://slashes/two/.")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/two/"),
            empty
                    .put("input", "http://slashes/three/./")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/three/"),
            empty
                    .put("input", "http://slashes/four/%2E/bing")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/four/bing"),
            empty
                    .put("input", "file:.")
                    .put("scheme", "file")
                    .put("path", "/"),

            empty
                    .put("input", "http://slashes/foo/\t\n\t/.\n\t.")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/foo/"),

            empty
                    .put("input", "http://slashes/foo/.//.\\/\\\\\\../../a/")
                    .put("scheme", "http")
                    .put("host", "slashes")
                    .put("path", "/foo////a/"),

            empty
                    .put("input", "file://example.com/foo/.\\/\\\\/..\\../../../.././x")
                    .put("scheme", "file")
                    .put("host", "example.com")
                    .put("path", "/x"),

            empty
                    .put("input", "http://a/👨") // Man U+1F468      https://emojipedia.org/man
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("path", "/%F0%9F%91%A8"),

            empty
                    .put("input", "file://👨.com")  // // Man U+1F468      https://emojipedia.org/man
                    .put("scheme", "file")
                    .put("host", "xn--qq8h.com")
                    .put("path", "/"),

            empty
                    .put("input", "ftp://👨🦰.com")  // Man and Red hair - but no ZWJ   U+1F468  U+1F9B0   https://emojipedia.org/man-red-hair
                    .put("scheme", "ftp")
                    .put("host", "xn--qq8hq8f.com")
                    .put("path", "/"),

            empty
                    .put("input", "http://👨‍🦰.com")  // Man and Red hair  U+1F468 U+200D U+1F9B0  https://emojipedia.org/man-red-hair - Note the ZWJ
                    .put("fail", ""),

            empty
                    .put("input", "http://%F0%9F%91%A8.com")  // Man
                    .put("scheme", "http")
                    .put("host", "xn--qq8h.com")
                    .put("path", "/"),

            empty
                    .put("input", "http://x\\a\\/b/c\\/")
                    .put("scheme", "http")
                    .put("host", "x")
                    .put("path", "/a//b/c//"),

            empty
                    .put("input", "http:\\\\//x\\a\\/b/c\\/")
                    .put("scheme", "http")
                    .put("host", "x")
                    .put("path", "/a//b/c//"),

            empty
                    .put("input", "http://a:81")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("port", "81")
                    .put("path", "/"),

            empty
                    .put("input", "http:a")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("path", "/"),

            // Path encoding
            empty
                    .put("input", "http://a.b/ \"<>`{}")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/%20%22%3C%3E%60%7B%7D"),

            empty
                    .put("input", "http://a.b/0123456789:;<=>")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/0123456789:;%3C=%3E"),

            empty
                    .put("input", "http://a.b/0123456789:;<=>?")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/0123456789:;%3C=%3E"),

            empty
                    .put("input", "http://a.b/@ABCDEFGHIJKLMNO")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/@ABCDEFGHIJKLMNO"),

            empty
                    .put("input", "http://a.b/PQRSTUVWXYZ[\\]^_")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/PQRSTUVWXYZ[/]^_"),

            empty
                    .put("input", "http://a.b/`abcdefghijklmno")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/%60abcdefghijklmno"),

            empty
                    .put("input", "http://a.b/pqrstuvwxyz{|}~")
                    .put("scheme", "http")
                    .put("host", "a.b")
                    .put("path", "/pqrstuvwxyz%7B|%7D~"),

            // end Path encoding

            empty
                    .put("input", "http://a:80")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("path", "/"),

            empty
                    .put("input", "https://a:443")
                    .put("scheme", "https")
                    .put("host", "a")
                    .put("path", "/"),

            empty
                    .put("input", "http://A.B.C¡")
                    .put("scheme", "http")
                    .put("host", "a.b.xn--c-6ba")
                    .put("path", "/"),

            empty
                    .put("input", "http://192.179.FFFFFFFFFFFFFFFFF256")
                    .put("scheme", "http")
                    .put("host", "192.179.fffffffffffffffff256")
                    .put("path", "/"),

            empty
                    .put("input", "http://192.179.256")
                    .put("scheme", "http")
                    .put("host", "192.179.1.0")
                    .put("path", "/"),

            empty
                    .put("input", "http://192.179.1.256")
                    .put("fail", ""),

            // Three different dots
            empty
                    .put("input", "http:///1｡2。3．4")
                    .put("scheme", "http")
                    .put("host", "1.2.3.4")
                    .put("path", "/"),
            empty
                    .put("input", "http:///1｡2。4")
                    .put("scheme", "http")
                    .put("host", "1.2.0.4")
                    .put("path", "/"),
            empty
                    .put("input", "http://[::1｡2。3．4]")
                    .put("fail", ""),

            empty
                    .put("input", "http://[::1.2.03.4]")
                    .put("fail", ""),

            empty
                    .put("input", "http://[::1.2.256.4]")
                    .put("fail", ""),

            empty
                    .put("input", "http://[::1.2.255]")
                    .put("fail", ""),

            // abbreviated IPv4
            empty
                    .put("input", "http://900000000")
                    .put("scheme", "http")
                    .put("host", "53.164.233.0")
                    .put("path", "/"),

            empty
                    .put("input", "foo:///1｡2。3．4")
                    .put("fail", ""),

            empty
                    .put("input", "http://a:90900")
                    .put("fail", ""),

            empty
                    .put("input", "http://a[")
                    .put("fail", ""),

            empty
                    .put("input", "http://[]")
                    .put("fail", ""),

            empty
                    .put("input", "http://[:]")
                    .put("fail", ""),

            empty
                    .put("input", "http://a%5B") // try to smuggle a forbidden domain character through
                    .put("fail", ""),

            empty
                    .put("input", "http://a:123?@♥p@@♥#👨‍")
                    .put("scheme", "http")
                    .put("host", "a")
                    .put("query", "@%E2%99%A5p@@%E2%99%A5")
                    .put("fragment", "%F0%9F%91%A8%E2%80%8D")
                    .put("path", "/")
                    .put("port", "123")

    );

    @Test
    public void testWholeUrls()
    {
        urlFixtures.foreach(i -> testFixture(i));
    }

    public void testFixture(ImMap<String, String> fx)
    {
        ImEither<String, ImUrl> urlEither = null;

        try
        {
            urlEither = ImUrl.parse(fx.get("input"));
        } catch (Exception e)
        {
            Throw.wrap(new UrlTestFail(fx.get("input"), ImUtils.getStackTrace(e)));
        }

        urlEither.match(
                error -> fx.keys().contains("fail") ? null : Throw.wrap(new UrlTestFail(fx.get("input"), error)),
                url -> checkUrlAgainstExpected(url, fx)
        );

    }

    private Void checkUrlAgainstExpected(ImUrl url, ImMap<String, String> fixture)
    {
        ImMap<String, String> resultMap = empty
                .put("scheme", url.scheme)
                .put("user", url.user)
                .put("password", url.password)
                .put("host", url.host)
                .put("port", String.valueOf(url.port))
                .put("path", url.path)
                .put("query", url.query)
                .put("fragment", url.fragment);

        ImList<ImPair<String, String>> ps = resultMap.keyValuePairs().foldl(ImList.on(), (z, p) -> p.snd.isEmpty() ? z : z.push(p));

        assertEquals("Error parsing " + TextUtils.quote(fixture.get("input")), fixture.remove("input"), ImMap.fromPairs(ps));

        return null;

    }

    /**
     * To test compressV6 expandV6
     *
     * We think of the string as a list of strings, l, where each string is a hex number <= 4 digits with no leading zeros or ""
     *
     * a run of zeros is a sublist of l where each element is "" and the size is >= 2.
     *
     * a maximally sized run of zeros in l is a run of zeros that has not other runs larger than it.
     *
     * When we compress a v6 address, we will convert the first(leftmost) maximally sized run of zeros to ""
     *
     * A compressed list is one that ha been compressed. There might be no difference between the original and the compressed version if.
     *
     * properties of a compressed v6 list, let lc = compress(l)
     *
     *     lc contains only 0 or 1 ""
     *     if 0 then
     *         lc == l
     *
     *     if there is a "" then
     *         lc.size() <= 6
     *         let n = lc.size() - 1 (the number of zeros that have been compressed)
     *         in
     *         there are no zero runs with size >= n before the ""
     *         there are no zero runs with size > n after the ""
     *         l == expand(lc)
     *
     *
     *
     *
     *
     */
    @Test
    public void testCompress()
    {
        TestUtils.assertToStringEquals("[0, 0, 4, 0, 5, ]", ImUrl.compressV6(getList(0, 0, 4, 0, 5, 0, 0, 0)));
        TestUtils.assertToStringEquals("[1, 2, 3, 4, 5, 6, 7, 8]", ImUrl.compressV6(getList(1, 2, 3, 4, 5, 6, 7, 8)));
        TestUtils.assertToStringEquals("[1, 0, 3, 4, 5, 6, 7, 8]", ImUrl.compressV6(getList(1, 0, 3, 4, 5, 6, 7, 8)));
        TestUtils.assertToStringEquals("[1, 0, 3, 4, 5, 6, 7, 0]", ImUrl.compressV6(getList(1, 0, 3, 4, 5, 6, 7, 0)));
        TestUtils.assertToStringEquals("[1, 0, 3, 4, 5, 6, ]", ImUrl.compressV6(getList(1, 0, 3, 4, 5, 6, 0, 0)));
        TestUtils.assertToStringEquals("[1, , 4, 5, 6, 0, 0]", ImUrl.compressV6(getList(1, 0, 0, 4, 5, 6, 0, 0)));
        TestUtils.assertToStringEquals("[1, 0, 3, 4, 5, , 1]", ImUrl.compressV6(getList(1, 0, 3, 4, 5, 0, 0, 1)));
        TestUtils.assertToStringEquals("[, 3, 4, 5, 0, 0, 1]", ImUrl.compressV6(getList(0, 0, 3, 4, 5, 0, 0, 1)));
        assertEquals(ImList.on(""), ImUrl.compressV6(getList(0, 0, 0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void testURL()
    {

    }

    @Test
    public void testCompressWithProperties()
    {

        for (int i = 0; i <= 7; i++)
        {

            ImList<String> base = ImList.repeat("0", i).append(ImList.oneTo(8 - i).map(k -> String.valueOf(k)));

            for (int j = 1; j <= i * 3; j++)
            {
                ImList<String> b = base.shuffle();

                //                say(b);
                checkProperties(b, ImUrl.compressV6(b));
            }
            //            say("");
        }
    }

    @Test
    public void testExpand()
    {

        ImList<String> zeros = makeList("0,0, 0, 0, 0, 0, 0, 0");

        ImList<String> expand = expand(ImList.on(""));
        assertEquals(zeros, expand);

        assertEquals(makeList("1, 0, 0, 0, 0, 0, 0, 0"), expand(makeList("1, ")));
        assertEquals(makeList("1, 0, 0, 0, 0, 0, 0, 2"), expand(makeList("1, , 2")));
        assertEquals(makeList("1, 0, 0, 0, 3, 0, 0, 2"), expand(makeList("1, ,3, 0, 0, 2")));
        assertEquals(makeList("0, 0, 0, 3, 0, 0, 0, 0"), expand(makeList("0, 0, 0, 3,")));
        assertEquals(makeList("1, 2, 3, 4, 5, 6, 7, 8"), expand(makeList("1, 2, 3, 4, 5, 6, 7, 8")));
    }

    private static ImList<String> getList(Integer... xs)
    {
        Throw.Exception.ifTrue(xs.length != 8, "too few integers in IPV6 address");
        return ImList.on(xs).map(i -> "" + i);
    }

    @Test
    public void testCheckProperties()
    {
        assertTrue(checkProperties(makeList("1, 2, 3, 4, 5, 6, 7, 8"), makeList("1, 2, 3, 4, 5, 6, 7, 8")));
        assertTrue(checkProperties(makeList("0, 0, 0, 3, 0, 0, 0, 0"), makeList("0, 0, 0, 3,")));
    }

    private ImList<String> makeList(String s)
    {
        return ParseUtils.split(',', s).map(i -> i.trim());
    }

    private boolean checkProperties(ImList<String> raw, ImList<String> compressed)
    {

        /*
         *
         *     if raw contains a zero run, then compressed.size() < raw.size()
         *     if there is a "" then
         *         lc.size() <= 6
         *         let n = lc.size() - 1 (the number of zeros that have been compressed)
         *         in
         *         there are no zero runs with size >= n before the ""
         *         there are no zero runs with size > n after the ""
         *         l == expand(lc)
         */

        //        say("compressed", compressed);
        ImList<String> empties = compressed.filter(i -> i.isEmpty());

        int emptiesSize = empties.size();
        //        say("empties", empties, "count", emptiesSize);

        if (emptiesSize == 0)
            return Eq.uals(raw, compressed);
        else if (emptiesSize > 1)
            return false;
        else
        {
            if (emptiesSize > 6)
                return false;
            else
            {
                int n = compressed.size() - 1;

                ImPair<ImList<String>, ImList<String>> parts = compressed.cutIntoTwo(i -> !Eq.uals(i, ""));
                //                say("parts", parts);

                ImList<ImList<String>> runsBefore = getRuns(parts.fst).filter(i -> isARunOfZeroes(i));
                ImList<ImList<String>> runsAfter = getRuns(parts.snd.tail()).filter(i -> isARunOfZeroes(i));

                if (runsBefore.find(i -> i.size() >= n).isPresent())
                    return false;
                else if (runsAfter.find(i -> i.size() > n).isPresent())
                {
                    return false;
                }
                else
                    return Eq.uals(raw, expand(compressed));
            }
        }

    }

    private ImList<ImList<String>> getRuns(ImList<String> ns)
    {
        // split the list into runs of zeros and runs of non-zeros

        return ns.cutIntoParts(splitPredicates).filter(i -> i.isNotEmpty());
    }

    private ImList<String> expand(ImList<String> compressed)
    {
        ImPair<ImList<String>, ImList<String>> parts = compressed.cutIntoTwo(i -> !Eq.uals(i, ""));

        return parts.snd.isEmpty()
               ? compressed
               : ImList.join(parts.fst, ImList.repeat("0", 8 - (compressed.size() - 1)), parts.snd.tail());
    }

    private static boolean isARunOfZeroes(ImList<String> i)
    {
        return i.size() >= 2 && Eq.uals(i.head(), "0");
    }

}