package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImTriple;
import dev.javafp.util.ImEither;
import org.junit.Test;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static dev.javafp.util.Say.say;
import static java.net.IDN.USE_STD3_ASCII_RULES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImUrlTest
{

    @Test
    public void testSimpleWellComplicatedReally()
    {
        String s = "http://localhost:8080/mock-cognito/%26logout"
                + "?foo=%2F%3F%3D%26%23%3A%25" // /?=&#:%
                + "&response_type=code"
                + "&scope=aws.cognito.signin.user.admin+email+openid+profile"
                + "&redirect_uri=http://localhost:8080"
                + "&state=a397e4f6-369e-4131-928d-e2ee5147117d_2"
                + "#%23foo";

        ImUrl url = makeUrl(s);

        assertEquals("http", url.scheme);
        assertEquals("localhost", url.host);
        assertEquals("8080", url.port);
        assertEquals("mock-cognito/%26logout", url.path);

        assertEquals("/?=&#:%", url.getQueryStringValueDecodedMaybe("foo").get());
        assertEquals("http://localhost:8080", url.getQueryStringValueDecodedMaybe("redirect_uri").get());
        assertEquals("aws.cognito.signin.user.admin email openid profile", url.getQueryStringValueDecodedMaybe("scope").get());

        //        assertEquals("http://localhost:8080/mock-cognito/%26logout?scope=aws.cognito.signin.user.admin+email+openid+profilestate=a397e4f6-369e-4131-928d-e2ee5147117d_2%2F%3F%3D%26%23%3A%25=%2F%3F%3D%26%23%3A%25redirect_uri=http%3A%2F%2Flocalhost%3A8080response_type=code#%23foo", "" + url);

        // http://localhost:52622/logout?client_id=dummy-client-idscope%3Daws.cognito.signin.user.admin+email+openid+profilestate%3D945bd813-d2da-4346-85ed-a859080077cf_1redirect_uri%3Dhttp%3A%2F%2Flocalhost%3A52622response_type%3Dcode
        // http://localhost:52622/logout?client_id=dummy-client-idscope%3Daws.cognito.signin.user.admin+email+openid+profilestate%3D945bd813-d2da-4346-85ed-a859080077cf_1redirect_uri%3Dhttp%3A%2F%2Flocalhost%3A52622response_type%3Dcode http://localhost:52622/logout

    }

    @Test
    public void testCreate()
    {
        validate("http://foo.com:/bar?a%3Fb=#?", "scheme 'http' host 'foo.com' port '' path '[bar]' query 'a%3Fb=' fragment '?'");

        validate("HTTPS://foo.com/bar?a%3Fb=#?", "scheme 'https' host 'foo.com' port '' path '[bar]' query 'a%3Fb=' fragment '?'");

        validate("http://foo.com:888/bar?a%3Fb=#?", "scheme 'http' host 'foo.com' port '888' path '[bar]' query 'a%3Fb=' fragment '?'");

        validate("https://foo:8080//foo//bar//?wibble%32#frag%20",
                "scheme 'https' host 'foo' port '8080' path '[foo, bar]' query 'wibble%32=' fragment 'frag%20'");

        validate("https://uk.finance.yahoo.co%6D/quote/%5EFTSE/chart?p=%5EFTSE#ey",
                "scheme 'https' host 'uk.finance.yahoo.com' port '' path '[quote, %5EFTSE, chart]' query 'p=%5EFTSE' fragment 'ey'");

        validate("www.example.com:/?#", "scheme 'http' host 'www.example.com' port '' path '[]' query '' fragment ''");

        validate("file:///a/b", "scheme 'file' host '' port '' path '[a, b]' query '' fragment ''");
    }

    @Test
    public void testToStringMany()
    {
        ImList<String> schemes = ImList.on("http://");
        ImList<String> ports = ImList.on(":1234");
        ImList<String> paths = ImList.on("", "/a", "/a/b");
        ImList<String> queries = ImList.on("", "?%3D=%27%26", "?a=b&c=d");
        ImList<String> fragments = ImList.on("", "#x");

        for (String s : schemes)
        {
            for (String p : ports)
            {
                for (String path : paths)
                {
                    for (String q : queries)
                    {
                        for (String f : fragments)
                        {
                            String s1 = s + "foo.bar" + p + path + q + f;
                            checkRoundTrip(s1);
                        }
                    }

                }
            }
        }
    }

    /**
     * Assert that the canonical string representation of a url generates the same drum url as the original string
     *
     */
    private void checkRoundTrip(String s)
    {
        ImUrl u1 = makeUrl(s);
        ImUrl u2 = makeUrl("" + u1);

        assertEquals(s, "" + u1);
        assertEquals(u1, u2);
    }

    @Test
    public void testToString()
    {
        check("http://foo.com/bar?a%3Fb=#*", "http://foo.com:/bar?a%3Fb#*");

        check("http://foo.com", "foo.com");
        check("https://foo.com", "https://foo.com");
        check("https://foo.com", "https://foo.com?");
        check("https://foo.com", "https://foo.com?&");
        check("https://foo.com?a=b", "https://foo.com?&&a=b&&");
        check("http://foo.com/bar", "http://foo.com/bar?");
        check("http://foo.com/bar", "http://foo.com/bar?#");

        check("file:///a/b", "//a/b");
        check("file:///a/b", "////a///b////");
    }

    private void validate(String uriString, String expected)
    {
        assertEquals(expected, getParts(uriString));
    }

    private void check(String expected, String urlString)
    {
        assertEquals(expected, "" + makeUrl(urlString));
    }

    private String getParts(String uriString)
    {
        ImUrl uri = makeUrl(uriString);

        return String.format("scheme '%s' host '%s' port '%s' path '%s' query '%s' fragment '%s'",
                uri.scheme,
                uri.host,
                uri.port,
                uri.pathComponents,
                uri.getQueryString(),
                uri.fragment);
    }

    @Test
    public void testAdHoc()
    {

        Pattern groupIdPattern = Pattern.compile("[0-9]+/?");

        assertTrue(groupIdPattern.matcher("0/").matches());
    }

    @Test
    public void validateQueryStringValuesMap()
    {
        ImPair<String, String> ab = ImPair.on("a", "b");
        ImPair<String, String> cd = ImPair.on("c", "d");
        ImMap<String, String> abMap = ImMap.on("a", "b");
        ImMap<String, String> abMap2 = ImMap.on("a", "b").put("c", "");

        //"a=b"       a->b
        assertEquals(ImList.on("a", "b").toPairs(), makeUrl("foo.com?a=b").queryElements);

        //""          ignored
        assertEquals(ImList.on(), makeUrl("foo.com").queryElements);

        assertEquals(ImList.on(), makeUrl("foo.com?").queryElements);

        assertEquals(ImList.on("a", "b").toPairs(), makeUrl("foo.com?a=b&").queryElements);

        //"a"         ignored
        assertEquals(ImList.on("a", "b", "c", "").toPairs(), makeUrl("foo.com?a=b&c").queryElements);

        //"="         ignored
        assertEquals(ImList.on("a", "b").toPairs(), makeUrl("foo.com?a=b&=").queryElements);

        //"a="        ignored
        assertEquals(ImList.on("a", "b", "c", "").toPairs(), makeUrl("foo.com?a=b&c=").queryElements);

        //"=a"        ignored
        assertEquals(ImList.on("a", "b").toPairs(), makeUrl("foo.com?a=b&=c").queryElements);

        //"a=b&c=d    a->b, c->d
        assertEquals(ImList.on("a", "b", "c", "d").toPairs(), makeUrl("foo.com?a=b&c=d").queryElements);

        //"a==b"      a->=b
        assertEquals(ImList.on("a", "=b").toPairs(), makeUrl("foo.com?a==b").queryElements);
    }

    private static ImUrl makeUrl(String urlString)
    {
        return ImUrl.on(urlString).right;
    }

    @Test
    public void getQueryValue()
    {
        ImUrl url = makeUrl("foo.com?a=b&c=d");

        assertEquals(ImEither.Right("b"), url.getQueryStringValueDecoded("a"));
        assertEquals(ImEither.Right("d"), url.getQueryStringValueDecoded("c"));
        String expectedMsg = "Could not find query string value for key: foo";
        assertEquals(ImEither.Left(expectedMsg), url.getQueryStringValueDecoded("foo"));
    }

    @Test
    public void withNoQueries()
    {
        ImUrl url = makeUrl("http://foo.com?a=b&c=d");

        assertEquals("http://foo.com?a=b&c=d", "" + url);
        //        String expected = "foo.com";
        //
        //        assertEquals(expected, "" + url.withNoQueriesOrFragments());

    }

    @Test
    public void testInvalid()
    {
        ImEither<String, ImUrl> urlEither = ImUrl.on("&^%^%$ih(*&^*&^%$WGH++");

        assertEquals("Invalid host: Domain contains invalid character: %", urlEither.left);
    }

    @Test
    public void testNonAsciiCharactersInHostArePunyCoded()
    {
        ImUrl url = makeUrl("https://fo+Ńńo:8080//foŃo//b(ar/%3F/?wibble #fr(ag►");

        assertEquals("xn--fo+o-d2aa", url.host);

        say(url.toString());
        say(url);

        say(URLDecoder.decode("fo%C5%83o, b(ar, %3F", StandardCharsets.UTF_8));
    }

    @Test
    public void testPunyCode()
    {
        String s = "abc.hdkhk\u200B\u2060";

        say(s, s.length());
        String puny = IDN.toASCII(s, USE_STD3_ASCII_RULES);
        say(puny, puny.length());
    }

    @Test
    public void withPath()
    {
        ImUrl url = makeUrl("foo.com?a=b&c=d");

        ImUrl urlWithPath = url.withPath(ImList.on("bish", "bash"));
        assertEquals("http://foo.com/bish/bash?a=b&c=d", "" + urlWithPath);
    }

    @Test
    public void errorThrows()
    {
        ImEither<String, ImUrl> urlEither = ImUrl.on("^%**^%*^*^%");
        assertEquals("Invalid host: Domain contains invalid character: %", urlEither.left);
    }

    @Test
    public void testConvertsToPunyCode()
    {
        String urlString = "https://www.\u0430\u0440\u0440\u04cf\u0435.com/";
        ImUrl url = makeUrl(urlString);

        assertEquals("www.xn--80ak6aa92e.com", url.host);
    }

    @Test
    public void testConvertsToPunyCode2()
    {
        String urlString = "https://www.xn--80ak6aa92e\u0430.com";
        ImEither<String, ImUrl> e = ImUrl.on(urlString);

        assertEquals("Invalid host: A label starts with \"xn--\" but does not contain valid Punycode.", e.left);

    }

    @Test
    public void withScheme()
    {
        ImUrl url = makeUrl("https://foo.com?a=b&c=d");
        String expected = "http://foo.com?a=b&c=d";

        assertEquals(expected, "" + url.withScheme("http"));
        assertEquals(url, url.withScheme("https"));

        assertEquals("http://foo:345", "" + makeUrl("https://foo:345").withScheme("http"));
    }

    @Test
    public void testUrlWithPunyCode()
    {
        ImUrl u1 = makeUrl("https://xn--rksmrgs-5wao1o.josefsson.org");

        ImUrl u2 = makeUrl("https://räksmörgås.josefßon.org");

        assertEquals(u1.host, u2.host);
    }

    @Test
    public void testURIWithPunyCode()
    {
        try
        {
            URI uri = new URI("https://räksmörgås.josefßon.org");
            //            URI uri = new URI("https://[1::1::1]");

            say(uri.getHost());
        } catch (URISyntaxException e)
        {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testDotted()
    {
        ImUrl u1 = makeUrl("http://161.58.228.45:80/index.html");

        say(u1);
    }

    @Test
    public void testWithUser()
    {
        ImUrl u1 = makeUrl("mail://van@vanemmenis.com");

        say(u1);
    }

    @Test
    public void testWithFtp()
    {
        ImUrl u1 = makeUrl("ftp://anonymous:my_passwd@ftp.prep.ai.mit.edu/pub/gnu");

        say(u1);
    }

    @Test
    public void testWithSemi()
    {
        ImUrl u1 = makeUrl("http://www.joes-hardware.com/hammers;sale=false/index.html;graphics=true");

        say(u1);
    }

    @Test
    public void testSameOrigin()
    {
        ImList<String> schemes = ImList.on("http://", "https://");
        ImList<String> hosts = ImList.on("boo.com", "bing.org");
        ImList<String> ports = ImList.on(":1234", ":88");
        ImList<String> paths = ImList.on("", "/a");
        ImList<String> queries = ImList.on("?%3D=%27%26", "?a=b&c=d");
        ImList<String> fragments = ImList.on("", "#x");

        ImList<ImList<String>> urls = ImList.cross(ImList.on(schemes, hosts, ports, paths, queries, fragments));

        ImList<ImPair<ImList<String>, ImList<String>>> pairs = ImList.cartesianProduct(urls, urls);

        pairs.foreach(p -> {

                    ImUrl u1 = makeUrl(p.fst.toString(""));
                    ImUrl u2 = makeUrl(p.snd.toString(""));

                    ImTriple<String, String, String> t1 = ImTriple.on(u1.scheme, u1.host, u1.port);
                    ImTriple<String, String, String> t2 = ImTriple.on(u2.scheme, u2.host, u2.port);

                    // The origins are equal if the scheme, host and port are the same
                    assertEquals(ImUrl.sameOrigin(u1, u2), Eq.uals(t1, t2));
                }

        );

    }
}