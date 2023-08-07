/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.Say;
import dev.javafp.val.ImValuesImpl;

import java.util.List;
import java.util.Map;

/**
 * <p> The response from sending a GET or a POST using an ApiClient
 */

public class ApiResponse extends ImValuesImpl
{
    public final int status;
    public final String statusMessage;
    public final ImMap<String, ImList<String>> headers;
    public final String body;

    public ApiResponse(int status, String statusMessage, ImMap<String, ImList<String>> headers, String body)
    {
        this.status = status;
        this.statusMessage = statusMessage;
        this.headers = headers;
        this.body = body;

        Say.say("body length", body.length());

    }

    public static ApiResponse on(int status, String statusMessage, Map<String, List<String>> headerFields, String body)
    {
        ImList<ImPair<String, ImList<String>>> pairs = ImList.onIterator(headerFields.entrySet().iterator()).map(e -> ImPair.on(e.getKey(), ImList.on(e.getValue())));

        Say.say("null key pairs", pairs.filter(p -> p.fst == null).toString("\n"));

        // we notice that sometimes the key is null, - for the response code
        return new ApiResponse(status, statusMessage, ImMap.fromPairs(pairs.filter(p -> p.fst != null)), body);
    }

    /**
     *
     * The field values for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(status, statusMessage, headers, body);
    }

    /**
     *
     * The field names for this object including fields from superclasses.
     *
     * See {@link dev.javafp.val.Values} and {@link dev.javafp.val.ImValuesImpl}
     */
    @Override
    public ImList<String> getNames()
    {
        return ImList.on("status", "statusMessage", "headers", "body");
    }

    /**
     * <p> Get a list of key/value pairs from the body - assuming form-URL encoding
     */
    public ImList<ImPair<String, String>> keyValuePairs()
    {
        return ParseUtils.split('&', body).filter(i -> !Eq.uals("", i)).map(s -> ParseUtils.splitAt('=', s));
    }
}