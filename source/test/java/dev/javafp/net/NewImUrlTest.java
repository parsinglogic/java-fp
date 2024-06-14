package dev.javafp.net;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.LeafTextBox;
import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImTriple;
import dev.javafp.util.ImEither;
import dev.javafp.util.TextUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class NewImUrlTest
{

    ImList<String> authorities = ImList.on(
            "user:password@", "user", "password", "",
            "example.com", "", "", "example.com",
            ":a@b", "", "a", "b",
            "a:::@@@@b.c", "a", "::@@@", "b.c",
            ":@b", "", "", "b",
            "a:?", "", "", "a:?",
            "a", "", "", "a",
            "/", "", "", "/",
            ":@a", "", "", "a"

    );

    ImList<String> hosts = ImList.on(
            "abc", "abc", "",
            "abc/", "abc", "/",
            "abc?", "abc", "?",
            "abc#", "abc", "#",
            "abc:", "abc", ":",
            "[1:2]", "[1:2]", "",
            "[abc]/", "[abc]", "/",
            "räksmörgås.josefsson.org/abc", "räksmörgås.josefsson.org", "/abc",
            "xn--rksmrgs-5wao1o.josefsson.org?foo=💩", "xn--rksmrgs-5wao1o.josefsson.org", "?foo=💩"
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
     * These are not
     *
     * http://a::@
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
    public void testAllAuthorities()
    {

        ImList<ImList<String>> grouped = authorities.group(4);
        ImList<String> inputs = grouped.map(i -> i.head());
        ImList<ImList<String>> expected = grouped.map(i -> i.tail());

        ImList<ImList<String>> results = inputs.map(i -> tryParsingAuthority(i));

        var r = inputs.zip(expected.zip(results));

        ImList<String> fails = r.map(i -> Eq.uals(i.snd.fst, i.snd.snd) ? "OK" : "FAIL " + i.fst);

        say(r.toString("\n\n"));

        say(fails.toString("\n"));
    }

    private ImList<String> tryParsingAuthority(String input)
    {
        ImTriple<String, String, ImList<Character>> res = NewImUrl.parseAuthority(ImList.onString(input));

        return ImList.on(res.e1, res.e2, res.e3.toString(""));
    }

    @Test
    public void testPercentEncodeCharacters()
    {
        for (char c = ' '; c < 127; c++)
        {
            say(c, NewImUrl.percentEncodeByte(c));
        }
    }

    @Test
    public void testPercentEncodeStrings()
    {
        ImSet<Character> encodeSet = ImList.onString("abc").toImSet();

        assertEquals("%61%62%63d", NewImUrl.percentEncodeString(encodeSet, "abcd"));
    }

    @Test
    public void testUtf8EncodeAndThenPercentEncode()
    {
        ImSet<Character> encodeSet = ImList.onString("a").toImSet();

        assertEquals("%61bcd", NewImUrl.utf8EncodeAndThenPercentEncode(encodeSet, "abcd"));
        assertEquals("%E2%99%A5", NewImUrl.utf8EncodeAndThenPercentEncode(encodeSet, "♥"));
        assertEquals("%61%E2%99%A5%c", NewImUrl.utf8EncodeAndThenPercentEncode(encodeSet, "a♥%c"));
    }

    @Test
    public void testURLEncoding() throws URISyntaxException
    {
        Charset utf8 = StandardCharsets.UTF_8;

        ImList<String> chars = ImRange.inclusive(32, 126).map(i -> "" + (char) (i.intValue()));

        ImList<String> converted = chars.map(c -> URLEncoder.encode(c, utf8));

        say(chars.zip(converted).toString("\n"));

    }

    @Test
    public void testGetBytes() throws UnsupportedEncodingException
    {

        say("💩".charAt(0));
        String s = "♥";

        say(s.codePointAt(0));

        assertEquals("💩", Character.toString("💩".codePointAt(0)));

        byte[] bs = s.getBytes();

        say(s.getBytes("utf8").length);

        int number = bs[0] & 0xFF;
        say(NewImUrl.percentEncodeByte(number));

        number = bs[1] & 0xff;
        say(NewImUrl.percentEncodeByte(number));

        number = bs[2] & 0xff;
        say(NewImUrl.percentEncodeByte(number));

        ByteBuffer buffer = ByteBuffer.wrap(s.getBytes());

        CharBuffer charBuffer = buffer.asCharBuffer();
        say(charBuffer.length());
        //
        //        say(u.percentEncode(charBuffer.charAt(0)));
        //        say(u.percentEncode(charBuffer.charAt(1)));
        //        say(u.percentEncode(charBuffer.charAt(2)));

        say(StandardCharsets.UTF_8.decode(buffer).toString());

        String e = "%E2%99%A5";

        say("decoder", URLDecoder.decode(e, StandardCharsets.UTF_8));

        ImList<Character> l = ImList.onString("♥");

        say(l.toS());
        say(l.toString(""));

        assertEquals(126, "~".getBytes()[0]);
        assertEquals(32, " ".getBytes()[0]);
        assertEquals(1, "\u0001".getBytes()[0]);
        assertEquals(127, "\u007F".getBytes()[0]);
    }

    @Test
    public void testAllHosts()
    {

        ImList<ImList<String>> grouped = hosts.group(3);

        ImList<String> inputs = grouped.map(i -> i.head());
        ImList<ImList<String>> expected = grouped.map(i -> i.tail());

        ImList<ImList<String>> results = inputs.map(i -> tryParsingHost(i));

        var r = inputs.zip(expected.zip(results));

        ImList<String> fails = r.map(i -> Eq.uals(i.snd.fst, i.snd.snd) ? "OK" : "FAIL " + i.fst);

        say(r.toString("\n\n"));

        say(fails.toString("\n"));
    }

    private ImList<String> tryParsingHost(String input)
    {
        ImPair<ImList<Character>, ImList<Character>> res = NewImUrl.parseHost$(ImList.onString(input));

        return ImList.on(res.fst.toString(""), res.snd.toString(""));

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
        return NewImUrl.convertHost(ImList.onString(input)).a();
    }

    @Test
    public void testGetNumberOrString()
    {

        assertEquals(ImEither.Left("abcde"), NewImUrl.Parts.getNumberOrString("abcde"));
        assertEquals(ImEither.Right(bi("255")), NewImUrl.Parts.getNumberOrString("255"));
        assertEquals(ImEither.Right(bi("0")), NewImUrl.Parts.getNumberOrString("0"));
        assertEquals(ImEither.Right(bi("9")), NewImUrl.Parts.getNumberOrString("011"));
        assertEquals(ImEither.Right(bi("255")), NewImUrl.Parts.getNumberOrString("0377"));
        assertEquals(ImEither.Right(bi("256")), NewImUrl.Parts.getNumberOrString("0400"));
        assertEquals(ImEither.Right(bi("8")), NewImUrl.Parts.getNumberOrString("010"));
        assertEquals(ImEither.Right(bi("15")), NewImUrl.Parts.getNumberOrString("0xf"));
        assertEquals(ImEither.Left("09"), NewImUrl.Parts.getNumberOrString("09"));
        assertEquals(ImEither.Left("0xg"), NewImUrl.Parts.getNumberOrString("0xg"));
    }

    private BigInteger bi(String numberString)
    {
        return new BigInteger(numberString);
    }

    @Test
    public void testWholeUrl()
    {

        // The three other dots
        //        say("\uFF61\u3002\uFF0E");
        //
        //        // \u3002 (ideographic full stop), \uFF0E
        //        "аррӏе.com".getBytes();

        say(NewImUrl.parse("http://0.256.0"));
        //        say(NewImUrl.parse("http://a.b.9"));
        //        say(NewImUrl.parse("http://0x99999"));
        //        say(NewImUrl.parse("HTTP://u:p@a.b.c:123/a?q#f"));
        //        say(NewImUrl.parse("http://аррӏе.com"));
        //        say(NewImUrl.parse("HTTP://a.b.c:65535"));
        //        say(NewImUrl.parse("http://///a.b.c"));
        //        say(NewImUrl.parse("http://us er:@pass♥word@@♥"));
    }

    // @formatter:off
    ImList<String> urls = ImList.on(

            "url"                                          ,"scheme" ,"user" ,"password"  ,"host"           ,"port"     ,"path"               ,"query"    ,"fragment"      ,

            "http://user:password@a"                       , "http"  ,"user" ,"password"  ,"a"              ,""         ,""                   ,""         ,""              ,
//            "http://1.0"                                   , "http"  , ""    ,""          ,"1.0.0.0"        ,""         ,""                   ,""         ,""              ,
//            "http://1.2.0"                                 , "http"  , ""    ,""          ,"1.2.0.0"        ,""         ,""                   ,""         ,""              ,
//            "http://u e:@♥p@@♥"                            , "http"  , "us%20er"  ,"%40%E2%99%A5p%40"          ,"1.2.0.0"        ,""         ,""                   ,""         ,""              ,

            "url"                                          ,"scheme" ,"user" ,"password"  ,"host"           ,"port"     ,"path"               ,"query"    ,"fragment"

            );
    // @formatter:on

    @Test
    public void testWholeUrls()
    {
        ImList<ImList<String>> items = urls.group(9);

        ImList<ImList<String>> is = items.removeAt(items.size()).tail();

        ImList<AbstractTextBox> results = is.map(i -> runTestOn(i));

        say(results.toString("\n\n"));
    }

    private AbstractTextBox runTestOn(ImList<String> xss)
    {

        ImList<String> xs = xss.map(i -> TextUtils.quote(i, "\""));

        // Get URL
        // parse it
        ImEither<String, NewImUrl> urlEither = NewImUrl.parse(xss.at(1));

        //               url    scheme user pw     host   port   path   query  fragment
        //               1      2     3     4      5      6      7      8      9
        String format = "%-46s ,%-6s ,%-20s ,%-20s ,%-16s ,%-10s ,%-20s ,%-10s ,%-10s";

        String result = urlEither.match(
                error -> String.format("%-46s ,%s", "FAIL", q(error)),
                url -> format(format, "", url.scheme, url.user, url.password, url.host, url.port, url.path, url.query, url.fragment)
        );

        String expected = String.format(format, xs.at(1), xs.at(2), xs.at(3), xs.at(4), xs.at(5), xs.at(6), xs.at(7), xs.at(8), xs.at(9));

        return LeafTextBox.with(expected).above(LeafTextBox.with(result));

        //return String.format("%-46s ,%-6s ,%-6s ,%-11s ,%-16s ,%-10s ,%-20s ,%-10s ,%-10s", xs.at(1), xs.at(2), xs.at(3), xs.at(4), xs.at(5), xs.at(6), xs.at(7), xs.at(8), xs.at(9));
    }

    private String format(String fmt, String a, String b, String c, String d, String e, String f, String g, String h, String i)
    {
        return String.format(fmt, q(a), q(b), q(c), q(d), q(e), q(f), q(g), q(h), q(i));
    }

    private static String q(String a)
    {
        return TextUtils.quote(a);
    }

}