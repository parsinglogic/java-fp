package dev.javafp.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.javafp.ex.InvalidState;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.util.ImMaybe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A class to parse the web platform tests from https://github.com/web-platform-tests/wpt.git
 *
 *
 * <pre>{@code
 *
 * [
 *   "See ../README.md for a description of the format.",
 *   {
 *     "input": "http://example\t.\norg",
 *     "base": "http://example.org/foo/bar",
 *     "href": "http://example.org/",
 *     "origin": "http://example.org",
 *     "protocol": "http:",
 *     "username": "",
 *     "password": "",
 *     "host": "example.org",
 *     "hostname": "example.org",
 *     "port": "",
 *     "pathname": "/",
 *     "search": "",
 *     "hash": ""
 *   },
 *   ...
 *   {
 *     "input": "mailto:/../",
 *     "base": null,
 *     "href": "mailto:/",
 *     "origin": "null",
 *     "protocol": "mailto:",
 *     "username": "",
 *     "password": "",
 *     "host": "",
 *     "hostname": "",
 *     "port": "",
 *     "pathname": "/",
 *     "search": "",
 *     "hash": ""
 *   },
 *   "# unknown schemes and their hosts",
 *   {
 *     "input": "sc://ñ.test/",
 *     "base": null,
 *     "href": "sc://%C3%B1.test/",
 *     "origin": "null",
 *     "protocol": "sc:",
 *     "username": "",
 *     "password": "",
 *     "host": "%C3%B1.test",
 *     "hostname": "%C3%B1.test",
 *     "port": "",
 *     "pathname": "/",
 *     "search": "",
 *     "hash": ""
 *   },
 *   {
 *     "input": "sc://%/",
 *     "base": null,
 *     "href": "sc://%/",
 *     "protocol": "sc:",
 *     "username": "",
 *     "password": "",
 *     "host": "%",
 *     "hostname": "%",
 *     "port": "",
 *     "pathname": "/",
 *     "search": "",
 *     "hash": ""
 *   },
 *   {
 *     "input": "sc://@/",
 *     "base": null,
 *     "failure": true
 *   },
 *   {
 *     "input": "sc://te@s:t@/",
 *     "base": null,
 *     "failure": true
 *   },
 *   {
 *     "input": "sc://:/",
 *     "base": null,
 *     "failure": true
 *   },
 *   {
 *     "input": "sc://:12/",
 *     "base": null,
 *     "failure": true
 *   }
 * ]
 * }</pre>
 *
 *
 */
public class WptJsonDecoder
{

    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Get a Json object from `element`
     */
    public static JsonObject getAsObject(JsonElement element)
    {
        try
        {
            return element.getAsJsonObject();
        } catch (InvalidState e)
        {
            throw new WptJsonParseError("expected element to be an object");
        }
    }

    /**
     * Get the String at field `fieldName` from `jsonObject` and throw if it is not a Json String
     */
    public static String getAsString(String fieldName, JsonObject jsonObject)
    {
        JsonElement element = getElementFromField(fieldName, jsonObject);

        if (element == null)
            return null;

        try
        {
            return element.getAsString();
        } catch (InvalidState e)
        {
            throw new WptJsonParseError("expected field to be a string");
        }
    }

    /**
     * Get the JsonElement at field `fieldName` from `jsonObject` and throw if it is missing or is null
     */
    private static JsonElement getElementFromField(String fieldName, JsonObject jsonObject)
    {
        JsonElement value = jsonObject.get(fieldName);

        if (value == null)
            return null;
        else if (value.isJsonNull())
            return null;
        else
            return value;
    }

    /**
     * Get the array at field `fieldName` from `jsonObject` and throw if it is not a Json Array
     */
    public static JsonArray getAsArray(JsonElement element)
    {

        try
        {
            return element.getAsJsonArray();
        } catch (InvalidState e)
        {
            throw new WptJsonParseError("expected element to be an array");
        }
    }

    public static ImMaybe<JsonElement> getResourceAsJsonElement(String name)
    {

        InputStream is = WptJsonDecoder.class.getResourceAsStream(name);

        if (is == null)
            return ImMaybe.nothing;
        else
        {
            try (Reader reader = new InputStreamReader(is))
            {
                return ImMaybe.just(JsonParser.parseReader(reader));
            } catch (IOException e)
            {
                throw new UnexpectedChecked(e);
            }
        }

    }

}