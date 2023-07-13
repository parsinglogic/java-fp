package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImTriple;
import dev.javafp.util.ImEither;
import dev.javafp.util.TestUtils;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DrumUrlTest
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

        DrumUrl url = DrumUrl.on(s);

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
        DrumUrl u1 = DrumUrl.on(s);
        DrumUrl u2 = DrumUrl.on("" + u1);

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
        assertEquals(expected, "" + DrumUrl.on(urlString));
    }

    private String getParts(String uriString)
    {
        DrumUrl uri = DrumUrl.on(uriString);

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
        assertEquals(ImList.on("a", "b").toPairs(), DrumUrl.on("foo.com?a=b").queryElements);

        //""          ignored
        assertEquals(ImList.on(), DrumUrl.on("foo.com").queryElements);

        assertEquals(ImList.on(), DrumUrl.on("foo.com?").queryElements);

        assertEquals(ImList.on("a", "b").toPairs(), DrumUrl.on("foo.com?a=b&").queryElements);

        //"a"         ignored
        assertEquals(ImList.on("a", "b", "c", "").toPairs(), DrumUrl.on("foo.com?a=b&c").queryElements);

        //"="         ignored
        assertEquals(ImList.on("a", "b").toPairs(), DrumUrl.on("foo.com?a=b&=").queryElements);

        //"a="        ignored
        assertEquals(ImList.on("a", "b", "c", "").toPairs(), DrumUrl.on("foo.com?a=b&c=").queryElements);

        //"=a"        ignored
        assertEquals(ImList.on("a", "b").toPairs(), DrumUrl.on("foo.com?a=b&=c").queryElements);

        //"a=b&c=d    a->b, c->d
        assertEquals(ImList.on("a", "b", "c", "d").toPairs(), DrumUrl.on("foo.com?a=b&c=d").queryElements);

        //"a==b"      a->=b
        assertEquals(ImList.on("a", "=b").toPairs(), DrumUrl.on("foo.com?a==b").queryElements);
    }

    @Test
    public void getQueryValue()
    {
        DrumUrl url = DrumUrl.on("foo.com?a=b&c=d");

        assertEquals(ImEither.Right("b"), url.getQueryStringValueDecoded("a"));
        assertEquals(ImEither.Right("d"), url.getQueryStringValueDecoded("c"));
        String expectedMsg = "Could not find query string value for key: foo";
        assertEquals(ImEither.Left(expectedMsg), url.getQueryStringValueDecoded("foo"));
    }

    @Test
    public void withNoQueries()
    {
        DrumUrl url = DrumUrl.on("http://foo.com?a=b&c=d");

        assertEquals("http://foo.com?a=b&c=d", "" + url);
        //        String expected = "foo.com";
        //
        //        assertEquals(expected, "" + url.withNoQueriesOrFragments());

    }

    @Test
    public void withPath()
    {
        DrumUrl url = DrumUrl.on("foo.com?a=b&c=d");

        DrumUrl urlWithPath = url.withPath(ImList.on("bish", "bash"));
        assertEquals("http://foo.com/bish/bash?a=b&c=d", "" + urlWithPath);
    }

    @Test
    public void errorThrows()
    {
        TestUtils.assertThrows(() -> DrumUrl.on("^%**^%*^*^%"), DrumUrlParseException.class, "Invalid host: Domain contains invalid character: %");
    }

    @Test
    public void withScheme()
    {
        DrumUrl url = DrumUrl.on("https://foo.com?a=b&c=d");
        String expected = "http://foo.com?a=b&c=d";

        assertEquals(expected, "" + url.withScheme("http"));
        assertEquals(url, url.withScheme("https"));

        assertEquals("http://foo:345", "" + DrumUrl.on("https://foo:345").withScheme("http"));
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

                    DrumUrl u1 = DrumUrl.on(p.fst.toString(""));
                    DrumUrl u2 = DrumUrl.on(p.snd.toString(""));

                    ImTriple<String, String, String> t1 = ImTriple.on(u1.scheme, u1.host, u1.port);
                    ImTriple<String, String, String> t2 = ImTriple.on(u2.scheme, u2.host, u2.port);

                    // The origins are equal if the scheme, host and port are the same
                    assertEquals(DrumUrl.sameOrigin(u1, u2), Eq.uals(t1, t2));
                }

        );

    }
}