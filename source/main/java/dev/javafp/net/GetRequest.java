/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.net;

import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;

/**
 * <p> A GET request
 */
public class GetRequest extends ApiRequest
{

    private GetRequest(ImUrl url, boolean followRedirects, ImList<ImPair<String, String>> requestHeaders, ImList<ImPair<String, String>> queryParameters)
    {
        super(url, Method.GET, followRedirects, requestHeaders, queryParameters);
    }

    /**
     * <p> A GET request on
     * {@code url}
     *
     */
    public static GetRequest on(ImUrl url)
    {
        return new GetRequest(url, true, ImList.on(), ImList.on());
    }

    /**
     * <p> Send this request and return an
     *
     * {@link ApiResponse}
     *
     */
    public ApiResponse send()
    {
        return getApiResponse(createConnection());
    }

    /**
     * <p> A new GET request that is the same as this but with an added header with
     * key
     * {@code key}
     *  and value
     * {@code value}
     *
     */
    public GetRequest addHeader(String key, String value)
    {
        return new GetRequest(url, followRedirects, requestHeaders.push(ImPair.on(key, value)), queryParameters);
    }

    /**
     * <p> A new GET request that is the same as this but with an added query with
     * key
     * {@code key}
     *  and value
     * {@code value}
     *
     */
    public GetRequest addQuery(String key, String value)
    {
        return new GetRequest(url, followRedirects, requestHeaders, queryParameters.push(ImPair.on(urlEncode(key), urlEncode(value))));
    }

}