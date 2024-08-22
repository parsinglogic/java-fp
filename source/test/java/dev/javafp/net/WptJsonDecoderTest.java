package dev.javafp.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ImUtils;
import dev.javafp.util.TextUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

/**
 *
 * Test from
 *
 * https://github.com/web-platform-tests/wpt.git
 *
 *
 *
 *
 *
 */
public class WptJsonDecoderTest
{
    ImList<String> supportedSchemes = ImList.on("http:", "https:", "ftp:", "file:", "jar:");

    ImList<String> fieldNames = ImList.on("input",
            "base",
            "host",
            "failure",
            "href",
            "origin",
            "protocol",
            "username",
            "password",
            "hostname",
            "port",
            "pathname",
            "search",
            "hash");

    @Test
    @Ignore
    public void testAll()
    {
        ImMaybe<JsonElement> resource = WptJsonDecoder.getResourceAsJsonElement("urltestdata.json");

        JsonElement e = resource.get();

        ImList<JsonElement> jsonObjects = ImList.onAll(WptJsonDecoder.getAsArray(e));

        // Get a list of maps - some of these will be comments
        ImList<ImMap<String, String>> testsAndComments = jsonObjects.map(i -> makeMap(i));

        // Filter out the comments by filtering in the actual tests
        ImList<ImMap<String, String>> allTests = testsAndComments.filter(i -> i.keysSet().contains("input"));

        // Filter to only be the schemes that we support
        ImList<ImMap<String, String>> supportedProtocolTests = allTests.filter(i -> ImList.or(supportedSchemes.map(j -> i.get("input").startsWith(j))));

        // filter to only be the absolute url tests
        ImList<ImMap<String, String>> nonRelativeTests = supportedProtocolTests.filter(i -> !i.keysSet().contains("base"));

        // Get the failures
        ImList<ImMap<String, String>> failureTests = nonRelativeTests.filter(i -> i.keysSet().contains("failure"));

        //        say(failureTests.toString("\n"));

        // Get the successes
        ImList<ImMap<String, String>> successTests = nonRelativeTests.filter(i -> !i.keysSet().contains("failure")).filter(i -> !i.get("input").contains("/."));

        say("total test count", allTests.size());
        say("supportedProtocolTests count", supportedProtocolTests.size());
        say("nonRelativeTests count", nonRelativeTests.size());
        say("failureTests count", failureTests.size());
        say("successTests count", successTests.size());

        ImList<ImMap<String, String>> successesTidied = successTests.map(i -> tidyMap(i));

        successesTidied.foreach(i -> testFixture(i));

        ImList<ImMap<String, String>> failTidied = failureTests.map(i -> tidyMap(i));

        failTidied.foreach(i -> testFixture(i));

    }

    private ImMap<String, String> tidyMap(ImMap<String, String> m)
    {
        //        say("tidy", m);
        ImList<ImPair<String, String>> ps = m.keyValuePairs();

        ImList<ImPair<String, String>> ps2 = ps.map(p ->
        {
            switch (p.fst)
            {
            // Removes
            case "href":
            case "origin":
            case "host":
                return null;

            // Simple renames
            case "pathname":
                return ImPair.on("path", p.snd);
            case "username":
                return ImPair.on("user", p.snd);
            case "hostname":
                return ImPair.on("host", p.snd);

            // Renames and processing
            case "search":
                return ImPair.on("query", p.snd.substring(1));
            case "protocol":
                return ImPair.on("scheme", p.snd.replaceFirst(":", ""));
            case "hash":
                return ImPair.on("fragment", p.snd.substring(1));
            case "failure":
                return ImPair.on("fail", "");
            default:
                return p;
            }
        });

        return ImMap.fromPairs(ps2.filter(i -> i != null));

    }

    public void testFixture(ImMap<String, String> fx)
    {
        ImEither<String, ImUrl> urlEither = null;

        try
        {
            say("trying", fx.get("input"));
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

    ImMap<String, String> empty = ImMap.empty();

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

    private ImMap<String, String> makeMap(JsonElement element)
    {
        if (!element.isJsonPrimitive())
        {
            JsonObject jso = WptJsonDecoder.getAsObject(element);

            // There will always be an input and a base

            // get fail
            JsonElement value = jso.get("failure");
            boolean fail = value != null && value.isJsonPrimitive() && value.getAsBoolean();

            // get other fields

            /**
             *
             *     "input": "turn://test/a/../b",
             *     "base": null,
             *     "href": "turn://test/b",
             *     "origin": "null",
             *     "protocol": "turn:",
             *     "username": "",
             *     "password": "",
             *     "host": "test",
             *     "hostname": "test",
             *     "port": "",
             *     "pathname": "/b",
             *     "search": "",
             *     "hash": ""
             *
             */

                 /*

        Expected :[origin->https://test, protocol->https:, search->, hostname->test, username->test, hash->, host->test, href->https://test@test/, port->, password->, pathname->/]
Actual   :[scheme->https, host->test, path->/, user->test]
         */
            return fieldNames.foldl(empty, (z, i) -> addToMap(z, i, jso));

        }
        else
            return ImMap.on("comment", element.getAsString());
    }

    private ImMap<String, String> addToMap(ImMap<String, String> map, String name, JsonObject jso)
    {
        String value = WptJsonDecoder.getAsString(name, jso);

        return putValue(map, name, value);
    }

    private static ImMap<String, String> putValue(ImMap<String, String> map, String name, String value)
    {
        return value == null || Eq.uals("", value)
               ? map
               : map.put(name, value);
    }

    //    private ImList<String> testSuccess(ImMap<String, String> testMap)
    //    {
    //        String host = testMap.get("host");
    //
    //        String input = testMap.get("input");
    //
    //        ImEither<String, OldUrl> urlEither = OldUrl.on(input);
    //
    //        if (urlEither.isLeft)
    //            return ImList.on("PARSE FAIL", input, host);
    //        else if (Eq.uals(host, urlEither.right.host))
    //            return ImList.on("SUCCESS", input, host);
    //        else
    //            return ImList.on("BAD HOST", input, "EXPECTED", host, "ACTUAL", urlEither.right.host);
    //
    //    }

    //    private ImList<String> testSuccessWithURI(ImMap<String, String> testMap)
    //    {
    //        String host = testMap.get("host");
    //
    //        String input = testMap.get("input");
    //
    //        ImEither<String, URI> urlEither = tryURI(input);
    //
    //        if (urlEither.isLeft)
    //            return ImList.on("PARSE FAIL", input, host);
    //        else if (Eq.uals(host, urlEither.right.getHost()))
    //            return ImList.on("SUCCESS", input, host);
    //        else
    //            return ImList.on("BAD HOST", input, "EXPECTED", host, "ACTUAL", urlEither.right.getHost());
    //
    //    }

    private ImEither<String, URI> tryURI(String input)
    {
        try
        {
            return ImEither.Right(new URI(input));

        } catch (Exception e)
        {
            return ImEither.Left("");
        }
    }

    private ImList<String> testSuccessWithURL(ImMap<String, String> testMap)
    {
        String host = testMap.get("host");

        String input = testMap.get("input");

        ImEither<String, URL> urlEither = tryURL(input);

        if (urlEither.isLeft)
            return ImList.on("PARSE FAIL", input, host);
        else if (Eq.uals(host, urlEither.right.getHost()))
            return ImList.on("SUCCESS", input, host);
        else
            return ImList.on("BAD HOST", input, "EXPECTED", host, "ACTUAL", urlEither.right.getHost());

    }

    private ImEither<String, URL> tryURL(String input)
    {
        try
        {
            return ImEither.Right(new URL(input));

        } catch (Exception e)
        {
            return ImEither.Left("");
        }
    }

}