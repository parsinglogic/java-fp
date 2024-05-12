/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.net;

import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.TextUtils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

/**
 * <p> A POST request
 */
public class PostRequest extends ApiRequest
{

    /**
     * The body of the POST
     */
    public final String bodyString;

    private PostRequest(ImUrl url, boolean followRedirects, ImList<ImPair<String, String>> requestHeaders, ImList<ImPair<String, String>> queryParameters, String bodyString)
    {
        super(url, Method.POST, followRedirects, requestHeaders, queryParameters);

        this.bodyString = bodyString;
    }

    /**
     * <p> A POST request on
     * {@code url}
     *
     */
    public static PostRequest on(ImUrl url)
    {
        return new PostRequest(url, true, ImList.on(), ImList.on(), "");
    }

    /**
     * <p> Send this request and return an
     *
     * {@link ApiResponse}
     *
     */
    public ApiResponse send()
    {
        HttpURLConnection conn = createConnection();

        conn.setDoOutput(true);

        write(bodyString, conn);

        return getApiResponse(conn);
    }

    /**
     * <p> A common case is that we need to set the body to "form parameters"
     * <p> A new POST request with the body set to the specified parameters in application/x-www-form-urlencoded styley
     * and with the content type set appropriately
     *
     */
    public PostRequest setBodyFormParameters(ImList<ImPair<String, String>> formParameters)
    {
        return this
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .setBody(TextUtils.join(formParameters.map(p -> urlEncode(p.fst) + "=" + urlEncode(p.snd) + "&"), ""));
    }

    /**
     * <p> A new GET request that is the same as this but with an added BODY of
     * {@code bodyString}
     *
     */
    public PostRequest setBody(String bodyString)
    {
        return new PostRequest(url, followRedirects, requestHeaders, queryParameters, bodyString);
    }

    /**
     * <p> A new POST request that is the same as this but with an added header with
     * key
     * {@code key}
     *  and value
     * {@code value}
     *
     */
    public PostRequest addHeader(String key, String value)
    {
        return new PostRequest(url, followRedirects, requestHeaders.push(ImPair.on(key, value)), queryParameters, bodyString);
    }

    /**
     * <p> A new POST request that is the same as this but with an added query with
     * key
     * {@code key}
     *  and value
     * {@code value}
     *
     */
    public PostRequest addQuery(String key, String value)
    {
        return new PostRequest(url, followRedirects, requestHeaders, queryParameters.push(ImPair.on(urlEncode(key), urlEncode(value))), bodyString);
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
        return super.getValues().appendElement(bodyString);
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
        return super.getNames().appendElement("bodyString");
    }

    private void write(String s, HttpURLConnection connection)
    {
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream()))
        {
            writer.write(s);

        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }
}