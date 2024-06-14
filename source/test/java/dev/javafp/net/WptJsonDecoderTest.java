package dev.javafp.net;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImMaybe;
import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static dev.javafp.util.Say.say;

public class WptJsonDecoderTest
{
    ImList<String> supportedSchemes = ImList.on("http", "https", "ftp", "file", "jar");

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
    public void testOne()
    {
        ImMaybe<JsonElement> resource = WptJsonDecoder.getResourceAsJsonElement("urltestdata.json");

        say(resource.isPresent());

        JsonElement e = resource.get();

        ImList<JsonElement> jsonObjects = ImList.onAll(WptJsonDecoder.getAsArray(e));

        ImList<ImMap<String, String>> testsAndComments = jsonObjects.map(i -> makeMap(i));

        ImList<ImMap<String, String>> allTests = testsAndComments.filter(i -> i.keysSet().contains("input"));

        ImList<ImMap<String, String>> supportedProtocolTests = allTests.filter(i -> ImList.or(supportedSchemes.map(j -> i.get("input").startsWith(j))));

        ImList<ImMap<String, String>> nonRelativeTests = supportedProtocolTests.filter(i -> !i.keysSet().contains("base"));

        ImList<ImMap<String, String>> failureTests = nonRelativeTests.filter(i -> i.keysSet().contains("failure"));
        ImList<ImMap<String, String>> successTests = nonRelativeTests.filter(i -> !i.keysSet().contains("failure"));

        say("total test count", allTests.size());
        say("supportedProtocolTests count", supportedProtocolTests.size());
        say("nonRelativeTests count", nonRelativeTests.size());
        say("failureTests count", failureTests.size());
        say("successTests count", successTests.size());

        ImList<ImList<String>> successResults = successTests.map(i -> testSuccessWithURL(i));

        ImList<ImList<String>> bad = successResults.filter(i -> i.head().startsWith("BAD"));
        say("bad count", bad.size());
        say(bad.toString("\n"));

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

            ImMap<String, String> startMap = ImMap.empty();

            return fieldNames.foldl(startMap, (z, i) -> addToMap(z, i, jso));

            //
            //
            //            boolean schemeIsSupported = ImList.or(supportedSchemes.map(i -> input.startsWith(i)));
            //
            //            if (!fail && base == null && schemeIsSupported)
            //            {
            //
            //                testUrlExpectOk(input, host);
            //            }

        }
        else
            return ImMap.on("comment", element.getAsString());
    }

    private ImMap<String, String> addToMap(ImMap<String, String> map, String name, JsonObject jso)
    {
        String value = WptJsonDecoder.getAsString(name, jso);

        return value == null
               ? map
               : map.put(name, value);
    }

    private ImList<String> testSuccess(ImMap<String, String> testMap)
    {
        String host = testMap.get("host");

        String input = testMap.get("input");

        ImEither<String, ImUrl> urlEither = ImUrl.on(input);

        if (urlEither.isLeft)
            return ImList.on("PARSE FAIL", input, host);
        else if (Eq.uals(host, urlEither.right.host))
            return ImList.on("SUCCESS", input, host);
        else
            return ImList.on("BAD HOST", input, "EXPECTED", host, "ACTUAL", urlEither.right.host);

    }

    private ImList<String> testSuccessWithURI(ImMap<String, String> testMap)
    {
        String host = testMap.get("host");

        String input = testMap.get("input");

        ImEither<String, URI> urlEither = tryURI(input);

        if (urlEither.isLeft)
            return ImList.on("PARSE FAIL", input, host);
        else if (Eq.uals(host, urlEither.right.getHost()))
            return ImList.on("SUCCESS", input, host);
        else
            return ImList.on("BAD HOST", input, "EXPECTED", host, "ACTUAL", urlEither.right.getHost());

    }

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