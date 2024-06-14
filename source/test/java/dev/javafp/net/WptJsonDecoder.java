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

    //    public static ImMaybe<ImList<String>> getResource(String name)
    //    {
    //
    //        InputStream is = WptJsonDecoder.class.getResourceAsStream(name);
    //
    //        if (is == null)
    //            return ImMaybe.nothing;
    //        else
    //        {
    //            try (Reader reader = new InputStreamReader(is))
    //            {
    //                // We need to flush so that when the reader is closed, we don't have a problem
    //                return ImMaybe.just(ImList.onReader(reader).flush());
    //            } catch (IOException e)
    //            {
    //                throw new UnexpectedChecked(e);
    //            }
    //        }
    //
    //    }

    public static ImMaybe<JsonElement> getResourceAsJsonElement(String name)
    {

        InputStream is = WptJsonDecoder.class.getResourceAsStream(name);

        if (is == null)
            return ImMaybe.nothing;
        else
        {
            try (Reader reader = new InputStreamReader(is))
            {
                JsonParser jsonParser = new JsonParser();

                return ImMaybe.just(jsonParser.parse(reader));
            } catch (IOException e)
            {
                throw new UnexpectedChecked(e);
            }
        }

    }

    //    /**
    //     * Get the history state from jsonObject. It can be null. In this case the getAsString function will fail - and we return the empty string
    //     */
    //    private static String getHistoryState(JsonObject jsonObject)
    //    {
    //        try
    //        {
    //            return getAsString("historyState", jsonObject);
    //        } catch (BatchDecoderException e)
    //        {
    //            return "";
    //        }
    //    }
    //
    //    /**
    //     * Get the pseudo event from the events or the command
    //     */
    //    private static PseudoEvent getPseudoEvent(JsonObject jsonObject)
    //    {
    //        return jsonObject.get("events") != null
    //               ? PseudoEvent.on(ImList.onAll(getAsArray("events", jsonObject)).map(e -> getEvent(e)))
    //               : PseudoEvent.on(getAsString("command", jsonObject));
    //    }
    //
    //    public static DeviceLayout getAsDeviceLayout(String fieldName, JsonObject js)
    //    {
    //        return getDeviceLayout(getElementFromField(fieldName, js));
    //    }
    //
    //    /**
    //     * { 'values': [ { 'visualViewport': { 'values': [ 1283, 667 ], 'tag': 'Point' },
    //     *                 'scrollPoint': { 'values': [ 0, 0 ], 'tag': 'Point' },
    //     *                 'screenSize': { 'values': [ 1536, 960 ], 'tag': 'Point' },
    //     *                 'layoutViewport': { 'values': [ 1283, 667 ], 'tag': 'Point' },
    //     *                 'htmlSize': { 'values': [ 1283, 0 ], 'tag': 'Point' },
    //     *                 'devicePixelRatio': 2 } ],
    //     *                 'tag': 'DeviceLayout' }
    //     *
    //     */
    //    public static DeviceLayout getDeviceLayout(JsonElement element)
    //    {
    //        try
    //        {
    //            JsonObject js = element.getAsJsonObject();
    //
    //            checkTag("DeviceLayout", js);
    //
    //            JsonObject jsonObject = getValuesObject(js);
    //
    //            Point screenSize = getAsPoint("screenSize", jsonObject);
    //            Point layoutViewport = getAsPoint("layoutViewport", jsonObject);
    //            Point visualViewport = getAsPoint("visualViewport", jsonObject);
    //            Point htmlSize = getAsPoint("htmlSize", jsonObject);
    //            Point scrollPoint = getAsPoint("scrollPoint", jsonObject);
    //            double devicePixelRatio = getAsDouble("devicePixelRatio", jsonObject);
    //
    //            return new DeviceLayout(screenSize, layoutViewport, visualViewport, htmlSize, scrollPoint, devicePixelRatio);
    //
    //        } catch (BatchDecoderException e)
    //        {
    //            throw new BatchDecoderFailed("DeviceLayout", element, e);
    //        }
    //    }
    //
    //    public static FontMetrics getAsFontMetrics(String fieldName, JsonObject js)
    //    {
    //        return getFontMetrics(getElementFromField(fieldName, js));
    //    }
    //
    //    /**
    //     *
    //     * Get the font metrics object:
    //     *
    //     * { 'values': [ { 'widths': [ 0.123, ...  , 0.567 ], 'size': '16px', 'height': 19 } ], 'tag': 'FontMetrics' }
    //     *
    //     */
    //    public static FontMetrics getFontMetrics(JsonElement element)
    //    {
    //        try
    //        {
    //            JsonObject js = element.getAsJsonObject();
    //            checkTag("FontMetrics", js);
    //
    //            JsonObject jsonObject = getValuesObject(js);
    //
    //            String sizeString = getAsString("size", jsonObject).replace("px", "");
    //            double size = Double.valueOf(sizeString);
    //            double height = getAsDouble("height", jsonObject);
    //
    //            ImList<Double> widths = ImList.onAll(getAsArray("widths", jsonObject)).map(d -> d.getAsDouble());
    //
    //            return FontMetrics.on(size, height, widths);
    //
    //        } catch (BatchDecoderException e)
    //        {
    //            throw new BatchDecoderFailed("FontMetrics", element, e);
    //        }
    //    }
    //
    //    /**
    //     * Get a point that we assume will be in the field `fieldName` from `jsonObject`
    //     */
    //    public static Point getAsPoint(String fieldName, JsonObject jsonObject)
    //    {
    //        return getPoint(getElementFromField(fieldName, jsonObject));
    //    }
    //
    //    static Point getPoint(JsonElement element)
    //    {
    //        try
    //        {
    //            JsonObject js = getAsObject(element);
    //
    //            checkTag("Point", js);
    //
    //            JsonArray c = getValuesArray(js, 2);
    //
    //            return Point.on(getAsDouble(0, c), getAsDouble(1, c));
    //
    //        } catch (BatchDecoderException e)
    //        {
    //            throw new BatchDecoderFailed("Point", element, e);
    //        }
    //    }
    //

    //    /**
    //     * Get the first element of the array at field `values` object from `jsonObject`
    //     */
    //    public static JsonObject getValuesObject(JsonObject jsonObject)
    //    {
    //        JsonArray values = getValuesArray(jsonObject, 1);
    //
    //        try
    //        {
    //            return values.get(0).getAsJsonObject();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType("values", "JSON Object", values);
    //        }
    //    }
    //
    //    /**
    //     * Get the array at field `values` from `jsonObject` and throw if it is not of size `expectedSize`
    //     */
    //    private static JsonArray getValuesArray(JsonObject jsonObject, int expectedSize)
    //    {
    //        JsonArray valuesArray = getValuesArray(jsonObject);
    //
    //        return valuesArray.size() == expectedSize
    //               ? valuesArray
    //               : Throw.wrap(new BatchDecoderWrongArraySize("values", expectedSize, valuesArray.size()));
    //    }
    //
    //    /**
    //     * Get the array at field `values` from `jsonObject` and throw if it is not a Json Array
    //     */
    //    private static JsonArray getValuesArray(JsonObject jsonObject)
    //    {
    //        return getAsArray("values", jsonObject);
    //    }
    //
    //    /**
    //     * Get the array at field `fieldName` from `jsonObject` and throw if it is not a Json Array
    //     */
    //    private static JsonArray getAsArray(String fieldName, JsonObject jsonObject)
    //    {
    //        JsonElement element = getElementFromField(fieldName, jsonObject);
    //
    //        try
    //        {
    //            return element.getAsJsonArray();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(fieldName, "JSON Array", element);
    //        }
    //    }
    //

    //
    //    /**
    //     * Get the int at field `fieldName` from `jsonObject` and throw if it is not a Json Int
    //     */
    //    public static int getAsInt(String fieldName, JsonObject jsonObject)
    //    {
    //        JsonElement element = getElementFromField(fieldName, jsonObject);
    //
    //        try
    //        {
    //            return element.getAsInt();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(fieldName, "JSON Int", element);
    //        }
    //    }
    //
    //    /**
    //     * Get the String at index `indexFromZero` from `jsonArray` and throw if it is not a Json String or the index is out of range
    //     */
    //    public static String getAsString(int indexFromZero, JsonArray jsonArray)
    //    {
    //        JsonElement element = jsonArray.get(indexFromZero);
    //
    //        try
    //        {
    //            return element.getAsString();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(indexFromZero, "JSON String", element);
    //        }
    //    }
    //
    //    /**
    //     * Get the int at index `indexFromZero` from `jsonArray` and throw if it is not a Json Int or the index is out of range
    //     */
    //    public static int getAsInt(int indexFromZero, JsonArray jsonArray)
    //    {
    //        JsonElement element = jsonArray.get(indexFromZero);
    //
    //        try
    //        {
    //            return element.getAsInt();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(indexFromZero, "JSON Int", element);
    //        }
    //    }
    //
    //    /**
    //     * Get the double at field `fieldName` from `jsonObject` and throw if it is not a Json double
    //     */
    //    private static double getAsDouble(String fieldName, JsonObject jsonObject)
    //    {
    //        JsonElement element = getElementFromField(fieldName, jsonObject);
    //
    //        try
    //        {
    //            return element.getAsDouble();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(fieldName, "JSON Double", element);
    //        }
    //    }
    //
    //    /**
    //     * Get the double at index `indexFromZero` from `jsonArray` and throw if it is not a Json Double or the index is out of range
    //     */
    //    private static double getAsDouble(int indexFromZero, JsonArray jsonArray)
    //    {
    //        JsonElement element = jsonArray.get(indexFromZero);
    //
    //        try
    //        {
    //            return element.getAsDouble();
    //        } catch (InvalidState e)
    //        {
    //            throw new BatchDecoderBadType(indexFromZero, "JSON Double", element);
    //        }
    //    }
    //

    //
    //    /**
    //     * Get the DrumEvent from `element`, throw if we can't recognise the tag
    //     */
    //    static DrumEvent getEvent(JsonElement element)
    //    {
    //        try
    //        {
    //            JsonObject js = getAsObject(element);
    //
    //            String type = getAsString("tag", js);
    //
    //            switch (type)
    //            {
    //            case "HistoryEvent":
    //                return new HistoryEvent(getAsString(0, getValuesArray(js, 1)));
    //
    //            case "ButtonEvent":
    //                return new ButtonEvent(getAsString(0, getValuesArray(js, 1)));
    //
    //            case "FocusEvent":
    //                return new FocusEvent(getAsString(0, getValuesArray(js, 1)));
    //
    //            case "TextEvent":
    //                JsonArray valuesArray = getValuesArray(js, 2);
    //                return new TextEvent(getAsString(0, valuesArray), getAsString(1, valuesArray));
    //
    //            case "SetTabIndexEvent":
    //                JsonArray va = getValuesArray(js, 2);
    //                return new SetTabIndex(getAsString(0, va), getAsInt(1, va));
    //
    //            case "ResizeEvent":
    //                return new ResizeEvent();
    //
    //            case "PopEvent":
    //                return new PopEvent();
    //
    //            case "UnhideEvent":
    //                return new UnhideEvent();
    //
    //            case "LoadEvent":
    //                return new LoadEvent();
    //
    //            case "UnloadEvent":
    //                return new UnloadEvent();
    //
    //            default:
    //                throw new BatchDecoderBadTag("", type);
    //            }
    //        } catch (BatchDecoderException e)
    //        {
    //            throw new BatchDecoderFailed("get events", element, e);
    //        }
    //
    //    }

}