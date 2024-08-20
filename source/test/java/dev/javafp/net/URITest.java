package dev.javafp.net;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImTriple;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

@Ignore
public class URITest
{

    ImList<String> baddies = ImList.on(

            "https://exa%23mple.org", "The input’s host contains a forbidden domain code point.",
            "foo://exa[mple.org", "An opaque host (in a URL that is not special) contains a forbidden host code point.",
            "https://127.0.0.1./", "An IPv4 address ends with a U+002E (.).",
            "https://1.2.3.4.5/", "An IPv4 address does not consist of exactly 4 parts.",
            "https://test.42", "An IPv4 address part is not numeric.",
            "https://127.0.0x0.1", "The IPv4 address contains numbers expressed using hexadecimal or octal digits.",
            "https://255.255.4000.1", "An IPv4 address part exceeds 255.",
            "https://[::1", "An IPv6 address is missing the closing U+005D (]).",
            "https://[:1]", "An IPv6 address begins with improper compression.",
            "https://[1:2:3:4:5:6:7:8:9]", "An IPv6 address contains more than 8 pieces.",
            "https://[1::1::1]", "An IPv6 address is compressed in more than one spot.",
            "https://[1:2:3!:4]", "An IPv6 address contains a code point that is neither an ASCII hex digit nor a U+003A (:). Or it unexpectedly ends.",
            "https://[1:2:3:]", "An IPv6 address contains a code point that is neither an ASCII hex digit nor a U+003A (:). Or it unexpectedly ends.",
            "https://[1:2:3]", "An uncompressed IPv6 address contains fewer than 8 pieces.",
            "https://[1:1:1:1:1:1:1:127.0.0.1]", "An IPv6 address with IPv4 address syntax: the IPv6 address has more than 6 pieces.",
            "https://[ffff::.0.0.1]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://[ffff::127.0.xyz.1]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://[ffff::127.0xyz]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://[ffff::127.00.0.1]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://[ffff::127.0.0.1.2]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://[ffff::127.0.0]",
            "An IPv6 address with IPv4 address syntax:  An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0. There are too many IPv4 parts.",
            "https://example.org/>", "An IPv6 address with IPv4 address syntax: an IPv4 address contains too few parts.",
            " https://example.org ", "A code point is found that is not a URL unit.",
            "ht\ntps://example.org", "A code point is found that is not a URL unit.",
            "https://example.org/%s", "A code point is found that is not a URL unit.",
            "file:c:/my-secret-folder", "The input’s scheme is not followed by \"//\".",
            "https:example.org", "The input’s scheme is not followed by \"//\".",
            "💩",
            "The input is missing a scheme, because it does not begin with an ASCII alpha, and either no base URL was provided or the base URL cannot be used as a base URL because it has an opaque path.",

            "https://example.org\\path\\to\\file", "The URL has a special scheme and it uses U+005C (\\) instead of U+002F (/).",
            "https://#fragment", "The input has a special scheme, but does not contain a host.",

            "https://:443", "The input has a special scheme, but does not contain a host.",
            "https://user:pass@", "The input has a special scheme, but does not contain a host.",
            "https://example.org:70000", "The input’s port is too big.",
            "https://example.org:7z", "The input’s port is invalid.",
            "file://c:", "A file: URL’s host is a Windows drive letter."
    );

    @Test
    public void testBaddiesWithURI()
    {
        ImList<ImList<String>> pairs = baddies.group(2);

        ImList<ImTriple<String, String, String>> exes = pairs.map(i -> tryCreatingURIon(i));

        say(exes.filter(i -> !i.e3.startsWith("host: ")));
        say(exes.filter(i -> i.e3.startsWith("host: ")));
    }

    //    @Test
    //    public void testBaddiesWithImUrl()
    //    {
    //        ImList<ImList<String>> pairs = baddies.group(2);
    //
    //        ImList<ImTriple<String, String, String>> exes = pairs.map(i -> tryCreatingImURLon(i));
    //        //        say(exes);
    //
    //        say(exes.filter(i -> !i.e3.startsWith("host: ")));
    //        say(exes.filter(i -> i.e3.startsWith("host: ")));
    //    }

    @Test
    public void testBaddiesWithJavaURL()
    {
        ImList<ImList<String>> pairs = baddies.group(2);

        ImList<ImTriple<String, String, String>> exes = pairs.map(i -> tryCreatingJavaURLon(i));
        //        say(exes);

        say(exes.filter(i -> i.e3.startsWith("host: ")));
    }
    //
    //    private ImTriple<String, String, String> tryCreatingImURLon(ImList<String> pair)
    //    {
    //        ImEither<String, OldUrl> e = OldUrl.on(pair.at(1));
    //
    //        String error = e.isLeft ? e.left : "host: " + e.right.host;
    //
    //        return ImTriple.on(pair.at(2), pair.at(1), error);
    //    }

    private ImTriple<String, String, String> tryCreatingURIon(ImList<String> pair)
    {
        try
        {
            URI uri = new URI(pair.at(1));

            String host = "host: " + (uri.getHost() == null ? "null" : uri.getHost());
            String hostAndAscii = host + " ascii: " + uri.toASCIIString();
            return ImTriple.on(pair.at(2), pair.at(1), hostAndAscii);

        } catch (Exception e)
        {

            String error = e.getMessage() == null ? "" : e.getMessage();
            return ImTriple.on(pair.at(2), pair.at(1), error);
        }
    }

    protected ImTriple<String, String, String> tryCreatingJavaURLon(ImList<String> pair)
    {

        try
        {
            URL url = new URL(pair.at(1));

            String host = url.getHost() == null ? "null" : url.getHost();
            return ImTriple.on(pair.at(2), pair.at(1), "host: " + host);

        } catch (MalformedURLException e)
        {
            String error = e.getMessage() == null ? "" : e.getMessage();
            return ImTriple.on(pair.at(2), pair.at(1), error);
        }

    }

    @Test
    public void testLocalhostInFileIsNotEmptyButShouldBe() throws URISyntaxException
    {
        URI uri = new URI("file://localhost/a/b/c");
        assertEquals("localhost", uri.getHost());
    }

    @Test
    public void testIp4ShouldBeExpandedButIsnt() throws URISyntaxException
    {
        URI uri = new URI("http://1");
        assertEquals("1", uri.getHost());
    }

}