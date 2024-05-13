/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.net;

import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.file.ReaderUtil;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.val.ImValuesImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p> An API request. An object that can sent a GET or a POST and get the result
 * <p> Difficult to know how to structure this class
 * <p> We want to protect the client as much as possible from making mistakes but maintain as much
 * flexibility as possible.
 * <p> If we have a GET and a POST subclass then I think that the error handling might be easier. Let's do that.
 * <p> we have:
 * <p> request
 * url - the base url - not including query parameters
 * followRedirects flag
 * headers
 * query parameters
 * <p> For a POST we also have
 * <p> request
 * body
 * <p> The query parameters have to be escaped
 * <p> https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
 *
 */
public class ApiRequest extends ImValuesImpl
{

    enum Method
    {
        GET,
        POST
    }

    /**
     * The URL this request will use
     */
    public final ImUrl url;

    /**
     * The method to use - GET or POST
     */
    public final Method method;

    /**
     * <p> Does this request follow redirects?
     * <p> Always set to
     * {@code true}
     *  for now.
     *
     */
    public final boolean followRedirects;

    /**
     * The request headers
     */
    public final ImList<ImPair<String, String>> requestHeaders;

    /**
     * The query parameters
     */
    public final ImList<ImPair<String, String>> queryParameters;

    private static final Charset utf8 = StandardCharsets.UTF_8;

    protected ApiRequest(ImUrl url, Method method, boolean followRedirects, ImList<ImPair<String, String>> requestHeaders, ImList<ImPair<String, String>> queryParameters)
    {
        this.url = url.withNoQueriesOrFragments();

        this.method = method;
        this.followRedirects = followRedirects;
        this.requestHeaders = requestHeaders;

        // Get the query elements from the url and add the ones passed as an argument
        this.queryParameters = url.queryElements.append(queryParameters);
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
        return ImList.on(url, method, followRedirects, requestHeaders, queryParameters);
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
        return ImList.on("url", "method", "followRedirects", "requestHeaders", "queryParameters");
    }

    protected String getStatusMessage(HttpURLConnection conn)
    {
        try
        {
            return conn.getResponseMessage();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    protected HttpURLConnection createConnection()
    {
        HttpURLConnection conn = createConnection(createUrl(getUrlIncludingQueries()));

        // Add the headers
        requestHeaders.foreach(p -> addRequestHeader(conn, p));

        setRequestTo("" + method, conn);

        conn.setInstanceFollowRedirects(followRedirects);

        return conn;
    }

    /**
     * <p> The url for this request - including any queries
     */
    public String getUrlIncludingQueries()
    {
        return "" + url.withQueryElements(queryParameters);
    }

    protected ApiResponse getApiResponse(HttpURLConnection conn)
    {
        try
        {
            return ApiResponse.on(getStatus(conn), getStatusMessage(conn), conn.getHeaderFields(), getResponseBody(conn));
        } finally
        {
            conn.disconnect();
        }
    }

    protected void addRequestHeader(HttpURLConnection conn, ImPair<String, String> p)
    {

        conn.setRequestProperty(p.fst, p.snd);
    }

    protected static String urlEncode(String s)
    {
        return URLEncoder.encode(s, utf8);
    }

    protected int getStatus(HttpURLConnection urlConnection)
    {
        try
        {
            return urlConnection.getResponseCode();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    protected void setRequestTo(String verb, HttpURLConnection urlConnection)
    {
        try
        {
            urlConnection.setRequestMethod(verb);
        } catch (ProtocolException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    protected HttpURLConnection createConnection(URL url)
    {
        try
        {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    protected String getResponseBody(HttpURLConnection con)
    {
        return getStatus(con) >= 200 && getStatus(con) < 300
               ? ReaderUtil.read(getInputStream(con))
               : "";
    }

    private InputStream getInputStream(HttpURLConnection con)
    {
        try
        {
            return con.getInputStream();
        } catch (IOException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    protected URL createUrl(String urlString)
    {
        try
        {
            return new URL(urlString);

        } catch (MalformedURLException e)
        {
            throw new UnexpectedChecked(e);
        }

    }

}