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

    public final String bodyString;

    private PostRequest(DrumUrl url, boolean followRedirects, ImList<ImPair<String, String>> requestHeaders, ImList<ImPair<String, String>> queryParameters, String bodyString)
    {
        super(url, Method.POST, followRedirects, requestHeaders, queryParameters);

        this.bodyString = bodyString;

    }

    public static PostRequest on(DrumUrl url)
    {
        return new PostRequest(url, true, ImList.on(), ImList.on(), "");
    }

    public ApiResponse send()
    {
        HttpURLConnection conn = createConnection();

        conn.setDoOutput(true);

        write(bodyString, conn);

        return getApiResponse(conn);
    }

    /**
     * <p> A common case is that we need to set the body to "form parameters"
     * <p> Set the body to the specified parameters in application/x-www-form-urlencoded styley
     * and set the content type appropriately
     *
     */
    public PostRequest setBodyFormParameters(ImList<ImPair<String, String>> formParameters)
    {
        return this
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .setBody(TextUtils.join(formParameters.map(p -> urlEncode(p.fst) + "=" + urlEncode(p.snd) + "&"), ""));
    }

    public PostRequest setBody(String bodyString)
    {
        return new PostRequest(url, followRedirects, requestHeaders, queryParameters, bodyString);
    }

    private void write(String s, HttpURLConnection connection)
    {
        //        Say.say("request body", s);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream()))
        {
            writer.write(s);

        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public PostRequest addHeader(String key, String value)
    {
        return new PostRequest(url, followRedirects, requestHeaders.push(ImPair.on(key, value)), queryParameters, bodyString);
    }

    public PostRequest addQueryParameter(String key, String value)
    {

        return new PostRequest(url, followRedirects, requestHeaders, queryParameters.push(ImPair.on(urlEncode(key), urlEncode(value))), bodyString);
    }

    @Override
    public ImList<Object> getValues()
    {
        return super.getValues().appendElement(bodyString);
    }

    @Override
    public ImList<String> getNames()
    {
        return super.getNames().appendElement("bodyString");
    }

}