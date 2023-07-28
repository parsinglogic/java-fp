package dev.javafp.net;

import dev.javafp.box.AbstractTextBox;
import dev.javafp.box.TopDownBox;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.Say;
import dev.javafp.util.TextUtils;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import org.junit.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * A class to show how the java URI class works
 */
public class GalimatiasURLTest
{

    @Test
    public void testURIDoesNotThrow() throws GalimatiasParseException
    {
        String s = "http://localhost:8080/mock-cognito/%26logout"
                + "?client_id=%23dummy%26-client-id"
                + "&response_type=code"
                + "&scope=aws.cognito.signin.user.admin+email+openid+profile"
                + "&redirect_uri=http://localhost:8080"
                + "&state=a397e4f6-369e-4131-928d-e2ee5147117d_2"
                + "#%23foo";

        URL url = URL.parse(s);

        assertEquals(8080, url.port());

    }

    @Test
    public void testLeadingZerosInPort() throws Exception
    {
        URL url = URL.parse("http://example.com:0000000000000000000080");

        assertEquals(80, url.port());
    }

    @Test
    public void testPort() throws Exception
    {
        URL url = URL.parse("http://example.com");

        assertEquals(80, url.port());
    }

    /**
     * Hmm https://jsdom.github.io/whatwg-url/#url=YjovLyU2NXhhbXBsZS5jbw==&base=YWJvdXQ6Ymxhbms=
     * disagrees with this
     * @throws Exception
     */
    @Test
    public void testEncodeInHost() throws Exception
    {
        URL url = URL.parse("http://%65xample.com");

        assertEquals("example.com", url.host().toString());
        assertEquals("example.com", url.host().toHumanString());
    }

    @Test
    public void testURIDoesThrowWhenSchemeStartsWithADigit()
    {
        try
        {
            URL url = URL.parse("1a://example.com");

            fail();
        } catch (GalimatiasParseException e)
        {

        }
    }

    @Test
    public void testURLDoesThrowWhenHostHasInvalidChars()
    {
        ImList<ImPair<String, String>> ps = ImList.onString("\t\n\r %/:?@[\\]").map(c -> tryToParse(c));

        say(ps.toString("\n"));

    }

    @Test
    public void testEncodeDecode()
    {
        String happy = "😀";

        say("encode(happy)", encode(happy));
        assertEquals(happy, decode(encode(happy)));

        say(decode(encode("=")));
    }

    @Test
    public void testMany()
    {

        /**
         *
         * see the live URL viewer
         *
         * https://jsdom.github.io/whatwg-url/
         *
         * to check these tests
         *
         *
         * GURL defaults ports to 80 for http etc - not what the
         */
        ImList<String> ss = ImList.on(
                "http:///a", // No host, no port
                "http://///a///",
                "http:///a?%3D=a", // %3D is =
                "http:///a?%3D='", // ' is meant to be encoded
                "http:///a?a =b", // <space> is meant to be encoded
                "http://example?a=😀",
                "http://foo.com:/bar?a%3Fb#*",
                "http://host:65535/path?query#fragment",
                "http://host:65536/path?query#fragment", // this should fail
                "ftp://////////////////a/b",
                "http://example.co###########",
                "a://example.co###########", // No host is returned - different from the test site
                "http://a?%01=%23",
                "http://example.co#####!$&'()*+,-./:;=?@_~\uD83D\uDE00",
                "http://example.co#####!$&'()*+,-./:;=?@_~😀",
                "http://localhost:8080/mock-cognito/%26logout",
                "http://localhost►:8080/mock-cognito►?►=▼#✔︎",
                "https://uk.finance.yahoo.com/quote/%5EFTSE/chart?p=%5EFTSE#ey",
                ""
        );

        TopDownBox boxes = TopDownBox.withAllBoxes(ss.map(i -> runTest(i)));

        Say.say(boxes);

        ss.foreach(s -> say(s, "default port", ImUrl.isPortDefault(s)));
    }

    private AbstractTextBox runTest(String s)
    {
        ImList<String> labels = ImList.on("Original", "Parts", "toString()", "toHumanString()", "");
        try
        {
            URL url = URL.parse(s);

            ImList<String> actual = ImList.on(
                    url.scheme(),
                    url.host() == null ? "<null>" : url.host().toHumanString(),
                    "" + url.port(),
                    url.path(),
                    url.query(),
                    url.fragment()
            );

            return Say.formatColumns(labels, ImList.on(s, TextUtils.join(actual, ", "), url.toString(), url.toHumanString()));

        } catch (GalimatiasParseException e)
        {
            return Say.formatColumns(labels, ImList.on(s, "ERROR: " + e.getMessage()));
        }
    }

    @Test
    public void testUpperToLower() throws GalimatiasParseException
    {
        URL url = URL.parse("FILE::///ab/c");
        assertEquals("file", url.scheme());
    }

    private ImPair<String, String> tryToParse(Character c)
    {
        try
        {
            URL.parse("a://b" + c);
            return ImPair.on("Fail", encode("" + c));
        } catch (GalimatiasParseException e)
        {
            return ImPair.on("Ok", encode("" + c));
        }
    }

    @Test
    public void testDoesParseSchemesOk() throws Exception
    {
        URI uri = new URI("a.+-://a.b");

        assertEquals("a.+-", uri.getScheme());
    }

    @Test
    public void testDoesNotCanonicaliseScheme() throws Exception
    {
        URI uri = new URI("HTTPS://example.com");

        assertEquals("HTTPS", uri.getScheme());

    }

    @Test
    public void testALongURL() throws Exception
    {

        String s = "http://localhost:8080/mock-cognito/%26logout"
                + "?client_id=%23dummy%26-client-id"
                + "&response_type=code"
                + "&scope=aws.cognito.signin.user.admin+email+openid+profile"
                + "&redirect_uri=http://localhost:8080"
                + "&state=a397e4f6-369e-4131-928d-e2ee5147117d_2"
                + "#%23foo";

        URI uri = new URI(s);

        assertEquals("http", uri.getScheme());
        assertEquals("localhost", uri.getHost());
        assertEquals(8080, uri.getPort());

        assertEquals("/mock-cognito/%26logout", uri.getRawPath());
        assertEquals("/mock-cognito/&logout", uri.getPath());
        assertEquals("#foo", uri.getFragment());
        assertEquals(
                "client_id=%23dummy%26-client-id&response_type=code&scope=aws.cognito.signin.user.admin+email+openid+profile&redirect_uri=http://localhost:8080&state=a397e4f6-369e-4131-928d-e2ee5147117d_2",
                uri.getRawQuery());

    }

    protected static String decode(String s)
    {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    protected static String encode(String s)
    {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

}