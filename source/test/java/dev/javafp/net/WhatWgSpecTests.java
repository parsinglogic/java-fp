package dev.javafp.net;

import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImUtils;
import dev.javafp.util.TextUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests taken from the examples in the WhatWg spec
 *
 * https://url.spec.whatwg.org/
 *
 * So - some of these are "errors" but not fatal ones. Others are "real errors"
 *
 * The jsdom live viewer is here:
 *
 * https://jsdom.github.io/whatwg-url/
 *
 *
 *
 */
public class WhatWgSpecTests
{

    ImMap<String, String> empty = ImMap.empty();

    ImList<ImMap<String, String>> urlFixtures = ImList.on(

            // 1.1. Writing

            // domain-invalid-code-point
            empty
                    .put("input", "https://exa%23mple.org")
                    .put("fail", ""),

            // host-invalid-code-point
            empty
                    .put("input", "foo://exa[mple.org")
                    .put("fail", ""),

            //  NOT FATAL - IPv4-empty-part An IPv4 address ends with a U+002E (.)
            empty
                    .put("input", "https://127.0.0.1./")
                    .put("scheme", "https")
                    .put("host", "127.0.0.1")
                    .put("path", "/"),

            // IPv4-too-many-parts An IPv4 address does not consist of exactly 4 parts.
            empty
                    .put("input", "https://1.2.3.4.5/")
                    .put("fail", ""),

            // IPv4-non-numeric-part An IPv4 address part is not numeric.
            empty
                    .put("input", "https://test.42")
                    .put("fail", ""),

            //  NOT FATAL - IPv4-non-decimal-part The IPv4 address contains numbers expressed using hexadecimal or octal digits.
            empty
                    .put("input", "https://127.0.0x0.1")
                    .put("scheme", "https")
                    .put("host", "127.0.0.1")
                    .put("path", "/"),

            // IPv4-out-of-range-part An IPv4 address part exceeds 255.
            empty
                    .put("input", "https://255.255.4000.1")
                    .put("fail", ""),

            // IPv6-unclosed An IPv6 address is missing the closing U+005D (]).
            empty
                    .put("input", "https://[::1")
                    .put("fail", ""),

            // IPv6-invalid-compression An IPv6 address begins with improper compression.
            empty
                    .put("input", "https://[:1]")
                    .put("fail", ""),

            //  IPv6-too-many-pieces An IPv6 address contains more than 8 pieces.
            empty
                    .put("input", "https://[1:2:3:4:5:6:7:8:9]")
                    .put("fail", ""),

            //  IPv6-multiple-compression An IPv6 address is compressed in more than one spot.
            empty
                    .put("input", "https://[1::1::1]")
                    .put("fail", ""),

            // IPv6-invalid-code-point An IPv6 address contains a code point that is neither an ASCII hex digit nor a U+003A (:). Or it unexpectedly ends.
            empty
                    .put("input", "https://[1:2:3!:4]")
                    .put("fail", ""),

            // IPv6-invalid-code-point An IPv6 address contains a code point that is neither an ASCII hex digit nor a U+003A (:). Or it unexpectedly ends.
            empty
                    .put("input", "https://[1:2:3:]")
                    .put("fail", ""),

            // IPv6-too-few-pieces An uncompressed IPv6 address contains fewer than 8 pieces.
            empty
                    .put("input", "https://[1:2:3]")
                    .put("fail", ""),

            // IPv4-in-IPv6-too-many-pieces An IPv6 address with IPv4 address syntax: the IPv6 address has more than 6 pieces.
            empty
                    .put("input", "https://[1:1:1:1:1:1:1:127.0.0.1]")
                    .put("fail", ""),

            //  IPv4-in-IPv6-invalid-code-point An IPv6 address with IPv4 address syntax:
            //
            //   An IPv4 part is empty or contains a non-ASCII digit.
            empty
                    .put("input", "https://[ffff::.0.0.1]")
                    .put("fail", ""),

            //  IPv4-in-IPv6-invalid-code-point An IPv6 address with IPv4 address syntax:
            //
            //   An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0.
            //   There are too many IPv4 parts.
            empty
                    .put("input", "https://[ffff::127.0.xyz.1]")
                    .put("fail", ""),

            //  IPv4-in-IPv6-invalid-code-point An IPv6 address with IPv4 address syntax:
            //
            //   An IPv4 part is empty or contains a non-ASCII digit.
            empty
                    .put("input", "https://[ffff::127.0xyz]")
                    .put("fail", ""),

            //  IPv4-in-IPv6-invalid-code-point An IPv6 address with IPv4 address syntax:
            //
            //   An IPv4 part contains a leading 0.
            empty
                    .put("input", "https://[ffff::127.00.0.1]")
                    .put("fail", ""),

            //  IPv4-in-IPv6-invalid-code-point An IPv6 address with IPv4 address syntax:
            //
            //   An IPv4 part is empty or contains a non-ASCII digit. An IPv4 part contains a leading 0.
            //   There are too many IPv4 parts.
            empty
                    .put("input", "https://[ffff::127.0.0.1.2]")
                    .put("fail", ""),

            // IPv4-in-IPv6-out-of-range-part An IPv6 address with IPv4 address syntax: an IPv4 part exceeds 255.
            empty
                    .put("input", "https://[ffff::127.0.0.4000]")
                    .put("fail", ""),

            // IPv4-in-IPv6-too-few-parts An IPv6 address with IPv4 address syntax: an IPv4 address contains too few parts.
            empty
                    .put("input", "https://[ffff::127.0.0]")
                    .put("fail", ""),

            // IPv4 address inside IPv6
            empty
                    .put("input", "http://[2001:db8:122::192.179.123.45]")
                    .put("scheme", "http")
                    .put("host", "[2001:db8:122::c0b3:7b2d]")
                    .put("path", "/"),

            //  NOT FATAL - invalid-URL-unit A code point is found that is not a URL unit.
            empty
                    .put("input", "https://example.org/>")
                    .put("scheme", "https")
                    .put("host", "example.org")
                    .put("path", "/%3E"),

            //  NOT FATAL - invalid-URL-unit A code point is found that is not a URL unit.
            empty
                    .put("input", " https://example.org ")
                    .put("fail", ""),

            //  NOT FATAL - invalid-URL-unit A code point is found that is not a URL unit.
            empty
                    .put("input", "ht\ntps://example.org")
                    .put("scheme", "https")
                    .put("host", "example.org")
                    .put("path", "/"),

            // NOT FATAL - invalid-URL-unit A code point is found that is not a URL unit.
            empty
                    .put("input", "https://example.org/%s")
                    .put("scheme", "https")
                    .put("host", "example.org")
                    .put("path", "/%s"),

            // NOT FATAL - special-scheme-missing-following-solidus The input’s scheme is not followed by "//".
            empty
                    .put("input", "file:c:/my-secret-folder")
                    .put("scheme", "file")
                    .put("path", "/c:/my-secret-folder"),

            // NOT FATAL - special-scheme-missing-following-solidus The input’s scheme is not followed by "//".
            empty
                    .put("input", "https:example.org")
                    .put("scheme", "https")
                    .put("host", "example.org")
                    .put("path", "/"),

            // missing-scheme-non-relative-URL The input is missing a scheme, because it does not begin with an ASCII alpha, and either no base URL was provided or the base URL cannot be used as a base URL because it has an opaque path.
            empty
                    .put("input", "💩")
                    .put("fail", ""),

            // NOT FATAL - invalid-reverse-solidus The URL has a special scheme and it uses U+005C (\) instead of U+002F (/).
            empty
                    .put("input", "https://example.org\\path\\to\\file")
                    .put("scheme", "https")
                    .put("host", "example.org")
                    .put("path", "/path/to/file"),

            // NOT FATAL - invalid-credentials The input includes credentials.
            empty
                    .put("input", "https://user@example.org")
                    .put("scheme", "https")
                    .put("user", "user")
                    .put("host", "example.org")
                    .put("path", "/"),

            // host-missing The input has a special scheme, but does not contain a host.
            empty
                    .put("input", "https://#fragment")
                    .put("fail", ""),

            // host-missing The input has a special scheme, but does not contain a host. I added this test
            empty
                    .put("input", "https://?fragment")
                    .put("fail", ""),

            // host-missing The input has a special scheme, but does not contain a host.
            empty
                    .put("input", "https://:443")
                    .put("fail", ""),

            // port-out-of-range The input’s port is too big.
            empty
                    .put("input", "https://example.org:70000")
                    .put("fail", ""),

            // port-invalid The input’s port is invalid.
            empty
                    .put("input", "https://user:pass@")
                    .put("fail", ""),

            // NOT FATAL - file-invalid-Windows-drive-letter The input is a relative-URL string that starts with a Windows drive letter and the base URL’s scheme is "file".
            empty
                    .put("input", "\"file://c:\"")
                    .put("fail", "")

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

}