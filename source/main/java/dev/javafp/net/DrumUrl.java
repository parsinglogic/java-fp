/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImValuesImpl;
import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * <p> This class parses a URL string into a URL ... er ... object, and can convert it back to a string.
 * <p> It also allows clients to create new URL objects by setting various parts of the URL.
 * <p> The rules for parsing URLs are pretty damn peculiar.
 * <p> I started off using the URL class - but that uses an old RFC:
 *
 * <pre>{@code
 * The syntax of URL is defined by RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax, amended by
 * RFC 2732: Format for Literal IPv6 Addresses in URLs. The Literal IPv6 address format also supports scope_ids.
 * }</pre>
 * <p> It seems to have bugs and is, of course not immutable.
 * <p> I then tried using URI - which is based on a later RFC -
 * but it also has problems
 * <p> Represents a Uniform Resource Identifier (URI) reference.
 * Aside from some minor deviations noted below, an instance of this class represents a URI reference as
 * defined by RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax, amended by RFC 2732: Format for Literal
 * IPv6 Addresses in URLs.
 *
 * <pre>{@code
 * 2396 is 1998
 * 2732 is 1999
 * 3986 is 2005
 * }</pre>
 * <p> This documents Java's pain with this issue
 * <p> <a href="https://cr.openjdk.java.net/~dfuchs/writeups/updating-uri/"  ></a>
 * <p> I need to parse the query string as well - this is not defined in any URL parsing standard.
 * <p> The rules for what is and is not a valid URL are pretty damn complicated with loads of special cases.
 * <p> According to Wikipedia https://en.wikipedia.org/wiki/URL
 * <p> Uniform Resource Locators were defined in RFC 1738 in 1994 by Tim Berners-Lee
 * <h2>Latest spec</h2>
 * <p> There appears to be an effort to standardise 3986 (and 3987 - Internationalized Resource Identifiers (IRIs))
 * <p> https://url.spec.whatwg.org/
 * <p> (Although this is very difficult to read)
 * <p> from that document:
 *
 * <pre>{@code
 * Goals
 *
 * The URL standard takes the following approach towards making URLs fully interoperable:
 *
 * Align RFC 3986 and RFC 3987 with contemporary implementations and obsolete them in the process. (E.g., spaces, other "illegal"
 * code points, query encoding, equality, canonicalization, are all concepts not entirely shared, or defined.) URL parsing needs
 * to become as solid as HTML parsing. [RFC3986] [RFC3987]
 *
 * Standardize on the term URL. URI and IRI are just confusing. In practice a single algorithm is used for both so keeping them
 * distinct is not helping anyone. URL also easily wins the search result popularity contest.
 * }</pre>
 * <h2>The Live Viewer</h2>
 * <p> There is a github repos associated withe the spec
 * <p> https://github.com/whatwg/url
 * <p> It says this:
 * <blockquote>
 * <p> Tests can be found in the url/ directory of web-platform-tests/wpt. A dashboard showing the tests running against major browsers can be seen at wpt.fyi.
 * <p> A complete JavaScript implementation of the standard can be found at jsdom/whatwg-url. This implementation is kept synchronized with the standard and tests.
 * <p> The Live URL Viewer lets you manually test-parse any URL, comparing your browser's URL parser to that of jsdom/whatwg-url.
 * <p> https://jsdom.github.io/whatwg-url
 * </blockquote>
 * <p> However, this viewer appears to show some differences with the spec
 * <h2>So where are we now?</h2>
 * <p> I am using a library to help me do the parsing - I have decided to keep this class as a wrapper
 * <p> I don't fully understand the whatwg spec
 * I don't believe the galimatias library is bug free
 * <h2>So what is a URL exactly?</h2>
 * <p> A URL string has this form (roughly)
 *
 * <pre>{@code
 * scheme '://' host ':' port '/' path '?' query '#' fragment
 * }</pre>
 * <p> So it has six parts
 * <p> In fact, I am only talking about the main URL types that we care about - the ones with scheme http or https
 * <p> For other schemes, different rules apply
 * <p> There are some strange rules about what characters are allowed in the URL sting - and what characters are allowed in the URL object
 * <p> This is the Whatwg spec - section 4.1:
 *
 * <pre>{@code
 * A URL is a universal identifier. To disambiguate from a valid URL string it can also be referred to as a URL record.
 *
 * A URL’s scheme is an ASCII string that identifies the type of URL and can be used to dispatch a URL for further processing after parsing. It is initially the empty string.
 *
 * A URL’s username is an ASCII string identifying a username. It is initially the empty string.
 *
 * A URL’s password is an ASCII string identifying a password. It is initially the empty string.
 *
 * A URL’s host is null or a host. It is initially null.
 *
 * ...
 *
 * A URL’s port is either null or a 16-bit unsigned integer that identifies a networking port. It is initially null.
 *
 * A URL’s path is a list of zero or more ASCII strings, usually identifying a location in hierarchical form. It is initially empty.
 * }</pre>
 * <p> Hmm
 * <p> The problem with this is that it is very misleading.
 * <p> The first thing to note is that these rules apply to the URL record - not the URL sting.
 * <p> While it is literally true that, apart from the port, the URL's parts are ASCII strings, what this doesn't mention is that these
 * ASCII strings are, for some parts, intended to represent UNICODE strings.
 * <p> They could have specified an encoding or even left the storage format up to the implementation - but they didn't.
 * <p> They could also have simply specified that the UNICODE strings should be some encoding or other - but they didn't
 * <p> Instead they have specified that the characters are stored in UTF8 - which is then "percent encoded"
 * <h3>Questions on the whatwg spec</h3>
 * <h4>How can fragments contain many {@code #} characters</h4>
 * <p> a://example.co#######
 * <p> seems to be ok on the
 * <a href="https://jsdom.github.io/whatwg-url"  >live URL Viewer</a>
 * <p> 4.3 says
 * <blockquote>
 * <p> A URL-fragment string must be zero or more URL units.
 * <p> The URL units are URL code points and percent-encoded bytes.
 * <p> Percent-encoded bytes can be used to encode code points that are not URL code points or are excluded from being written.
 * </blockquote>
 * <h4>Why do they specify the forbidden host code points?</h4>
 * <p> There are a set of forbidden characters - but there are many others that are noted as invalid on the live URL viewer
 * <p> eg FORM FEED
 * <p> This is OK:
 *
 * <pre>{@code
 * https://fo+Ńńo:8080//foŃo//b(ar/%3F/?wibble%32#fr(ag►
 * }</pre>
 * <p> This is not: (
 * {@code %0C}
 *  is
 * {@code formfeed}
 * )
 *
 * <pre>{@code
 * https://fo+%0CŃńo:8080//foŃo//b(ar/%3F/?wibble%32#fr(ag►
 * }</pre>
 * <h2>Why not just use the java class URI?</h2>
 * <p> The URI class in Java seems to only refers to 2396:
 * <blockquote>
 * <p> Aside from some minor deviations noted below, an instance of this class represents
 * a URI reference as defined by RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax,
 * amended by RFC 2732: Format for Literal IPv6 Addresses in URLs.
 * </blockquote>
 * <p> I tried to use what Jetty gives us - but it is a bit peculiar. It does not tell you what the fragment is for example. I guess that is because fragments
 * are meant to be browser-side things?
 * <p> I tried to use URI - but it chokes on URLs that actually get sent from browsers. I don't want to parse a url only to get an exception
 * <p> The URI class does not let us access the parts of a query as a map
 * <p> So we need to process the URLs that we get in Drum
 * <p> So to reach Drum on the server the url must have been processed by (eg)
 * <ul>
 * <li>
 * <p> curl
 * </li>
 * <li>
 * <p> wget
 * </li>
 * <li>
 * <p> a browser
 * </li>
 * <li>
 * <p> some javascript code
 * </li>
 * <li>
 * <p> Whatever processes it on the public internet
 * </li>
 * <li>
 * <p> Firewalls, routers, load balancers, reverse proxies, API gateways
 * </li>
 * <li>
 * <p> Some server side code - Jetty, Undertow, Lambda?
 * </li>
 * </ul>
 * <p> These elements will apply their rules about what is a valid URL
 * <p> I am passing the url - as it is perceived on the browser - to the server
 * <p> I think this is the best plan as it accurately reflects what we will need to do to do a redirect
 * <p> If we use the URL on the server - it is possible that it will have been mangled
 * <p> The URI has this form (roughly)
 *
 * <pre>{@code
 * scheme '://' host ':' port '/' path '?' query '#' fragment
 * }</pre>
 * <p> Actually, after the scheme comes the authority
 *
 * <pre>{@code
 * authority   = [ userinfo "@" ] host [ ":" port ]
 * }</pre>
 * <p> I am ignoring the userinfo part for now
 * <p> So we have the concept of the URL string - which is parsed into the URL in-memory representation (object)
 * <p> So you take the URL string and parse it to get the URL object and we can then write it to get the URL string
 * <p> It seems that each part of the URL has
 * <ol>
 * <li>
 * <p> a different allowed character set.
 * </li>
 * <li>
 * <p> a different set of characters that need to be encoded to percent escaped form
 * </li>
 * </ol>
 * <p> I don't think it matters if we encode a code point that did not have to be encoded
 * <p> So, obviously, if we want to have a = character in a query then we must encode it so that it doesn't act as a delimeter
 * <h2>Encoding and decoding</h2>
 * <p> There are two java classes
 * URLEncoder and URLDecoder
 * <p> These are based on application/x-www-form-urlencoded MIME format
 * <p> So an encoded string has a character set that is:
 *
 * <pre>{@code
 * [a-z]
 * [A-Z]
 * [0-9]
 * *
 * .
 * -
 * _
 * }</pre>
 * <p> (and % - but this is the character that introduces two octets). An octet is a character [0-9A-F]
 * <p> If we want to have a % character anywhere we must encode it
 * encoding and decoding make reference to a character encoding - the idea is tha we encode a unicode code point into a percent
 * escaped list of characters that you would see if the code point was encoded using that character encoding.
 * <p> The default is UTF-8
 * <h2>Where can escaped octets appear ?</h2>
 * <p> RFC 2396 allows escaped octets to only appear in:
 * <ul>
 * <li>
 * <p> user-info
 * </li>
 * <li>
 * <p> path
 * </li>
 * <li>
 * <p> query
 * </li>
 * <li>
 * <p> fragment
 * </li>
 * </ul>
 * <p> ie not:
 * <ul>
 * <li>
 * <p> scheme
 * </li>
 * <li>
 * <p> host
 * </li>
 * <li>
 * <p> port
 * </li>
 * </ul>
 * <p> I am not sure if this is the case in the whatwg spec. I suppose it is.
 * <h2>A rough attempt</h2>
 * <h2>Parsing</h2>
 * <p> Roughly I think what we need to do is to parse the URL into its different parts
 * store each part in its
 * <em>decoded</em>
 *  state
 * <p> This means that we can't guarantee that the round trip ends up back where we started
 * <p> In other words
 * <p> Let s be a valid URL
 * <p> then
 * <p> write(parse(s)) != s for some s
 * <h2>Writing</h2>
 * <p> To access a part
 * <p> encode each part (not scheme, host or port)
 * <p> To write a URL
 * <p> Extract each part in order asn encode it and display it with the necessary delimiters
 * <p> There are complications/questions:
 * <p> What is optional and mandatory?
 * <p> Should I tidy the URL up as I convert it to a string?
 * eg
 * www.example.com/ to www.example.com
 * www.example.com////// to www.example.com
 * www.example.com? to www.example.com/?
 * <h2>The allowed characters for the different parts</h2>
 * <p> Let's start by defining URL code points:
 * <p> From the Whatwg spec:
 * <p> The URL code points are:
 *
 * <pre>{@code
 * ASCII alphanumeric,
 * U+0021 (!),
 * U+0024 ($),
 * U+0026 (&amp;),
 *
 * U+0027 ('),
 * U+0028 LEFT PARENTHESIS,
 * U+0029 RIGHT PARENTHESIS,
 * U+002A (*),
 * U+002B (+),
 * U+002C (,),
 * U+002D (-),
 * U+002E (.),
 * U+002F (/),
 *
 * U+003A (:),
 * U+003B (;),
 *
 * U+003D (=),
 * U+003F (?),
 *
 * U+0040 (@),
 * U+005F (_),
 * U+007E (~),
 * and code points in the range U+00A0 to U+10FFFD, inclusive, excluding surrogates and noncharacters.
 * }</pre>
 * <p> percent-encoded byte is (as a regex)
 *
 * <pre>{@code
 * [%][A-Z0-9]{2,2}
 * }</pre>
 * <p> A URL unit is some URL codePoints and percent-encoded bytes
 * <p> Each part of the URL string is specified in terms of URL Units - and some parts have further restrictions
 * <p> For example the port part can only be numeric
 * <p> However - you may notice that
 * {@code #}
 *  is not a code point
 * <h2>Scheme - No encoding</h2>
 * <p> Case insensitive - canonical form is lowercase
 *
 * <pre>{@code
 * [a-z][a-z0-9+.-]
 * }</pre>
 * <h2>Host - encoding</h2>
 * <p> according to https://url.spec.whatwg.org
 * <blockquote>
 * <p> A forbidden host code point is U+0000 NULL, U+0009 TAB, U+000A LF, U+000D CR, U+0020 SPACE, U+0023 (#), U+0025 (%), U+002F (/),
 * U+003A (:), U+003F (?), U+0040 (@), U+005B ([), U+005C (), or U+005D (]).
 * </blockquote>
 * <p> So - not
 * {@code null \t \n \r space # % / : ? @ [ \ ]}
 * <h2>Port - no encoding</h2>
 * <p> unsigned 16 bit integer (I guess there is an assumption that it can also be null)
 * <p> If the port is present and the same as the default port then it is meant to be stored as null (4.4 page 24)
 * <p> The spec does not say anything about leading zeros. The Live URL viewer allows them
 * <h2>Path - encoding</h2>
 * <h2>Query - encoding</h2>
 * <h2>Fragment - encoding</h2>
 * <p> You could argue that fragments should not be sent to the server at all. They are meant to indicate to the browser what part of the
 * resulting page to view
 * <h2>Browsers</h2>
 * <p> FF kind of takes this view. If you have a request that differs only in the fragment, FF will only send the first one. I think this
 * is a little unreasonable since it will send urls without a
 * {@code #}
 * that don't differ.
 * <p> This will upset Drum. If the user adds a
 * {@code #}
 *  to a url and we don't tell her then, if she hits return in the address bar, expecting it to refresh,
 * it won't.
 * <ul>
 * <li>
 * <p> FF 57.0.4 (64-bit) sends a request involving a fragment only once in succession
 * </li>
 * <li>
 * <p> Chrome  63.0.3239.132 (Official Build) (64-bit) sends the request each time
 * </li>
 * <li>
 * <p> Safari  11.0.2 (12604.4.7.1.4) sends the request each time
 * </li>
 * </ul>
 * <h2>Percent encoding</h2>
 * <h3>{@code %XX} where each {@code X} is a hex digit</h3>
 * <p> Jetty prevents invalid encodings being sent when they appear in the path
 *
 * <pre>{@code
 * http://%6Cocalhost:8080/ is corrected by both FF and Chrome before sending
 *
 *
 * http://localhost:8080/:?://%XX
 * }</pre>
 * <p> is sent through by both
 * <p> I think that, if a browser notices a % encoding in the scheme, host, port or path then it will correct it before sending
 * <p> It appears that percent encoding is done at the byte level where the bytes are assumed to be UTF-8. So the bytes are not necessarily
 * correct UTF-8. Hmm
 * <p> These are some urls that FF accepts and causes a GET to be sent to the server
 *
 * <pre>{@code
 * http://localhost:8080//////
 *
 * http://localhost:8080/:
 *
 * http://localhost:8080/:?###
 * http://localhost:8080/:?://###
 * http://localhost:8080/:?:/
 * http://localhost:8080/:?://
 * }</pre>
 * <p> chrome sends them
 *
 * <pre>{@code
 * http://localhost:8080/:?://#
 * http://localhost:8080/T?foo%%######
 *
 * http://localhost:8080/?#####
 * http://localhost:8080/:?://%
 * http://localhost:8080/:?://%XX
 *
 * localhost:8080/:?://%30
 *
 * http://localhost:8080/%%?foo
 * }</pre>
 * <p> gives Bad Request 400 - handled by Jetty
 *
 * <pre>{@code
 * http://localhost:8080/T?foo%%
 * }</pre>
 * <p> localhost:8080/ab%30/d
 * <p> FF sends this as is but changes the address bar to
 * {@code http://localhost:8080/ab0/d}
 *  so a subsequent
 * {@code GET}
 * will not be exactly the same as before
 * Chrome adjusts it
 * <em>before</em>
 *  it sends it
 * <p> http://localhost:8080/ab%23/d    (%23 is #)
 * This gets sent as is and is never corrected by Chrome.
 * <p> I guess the browsers only correct the % escapes that are not necessary
 * <p> So why have this class?
 * <p> I am sending the URL to the server from the browser. I want to be able to parse this and process it to a certain extent. For example
 * I want to replace the path components to create a link but I don't want to affect any of the other parts
 * <p> I wanted to have
 * <em>control</em>
 *  (as usual) in this messy and confusing area.
 * <h2>Query</h2>
 * <p> The content of the query is not part of any URL spec
 * <p> https://www.w3.org/TR/html401/interact/forms.html
 * <p> Hmm - this suggests tha all unicode code points > U+007E (~) should be encoded
 * <p> Crucially, it does not say how to decode them
 * <p> Let's assume that a query maps to [(String, String)]
 * <p> with the usual separators etc
 * <p> We store the pairs decoded.
 * <p> the first and second part of each pair can be "" and there can be duplicates - Ie we will not regard duplicates as a parsing error
 * <p> You can 'look up' a query element using
 * <p> getQueryStringValueMaybe(String key)
 * <p> this will only return a Just if exactly one of the pairs has key as the first element.
 * <p> It returns the value.
 * <p> When we display the Url, we will go through the query list, decoding them
 * This means that the original string will not necessarily be preserved. I can't
 *
 */
public class DrumUrl extends ImValuesImpl
{

    private static final Pattern compile = Pattern.compile("^[a-zA-Z]\\w+:\\/\\/");
    public final String scheme;
    public final String port;
    public final String host;
    public final ImList<String> pathComponents; // This will never be null
    public final String path;
    //    public final String query;
    public final ImList<ImPair<String, String>> queryElements;
    public final String fragment;

    private static final Charset utf8 = StandardCharsets.UTF_8;

    /**
     * <p> If a parameter is null it means that the character that introduced it was not present in the url string
     * If a parameter is the empty string it mean that the component is introduced by its character but has no characters
     *
     */
    private DrumUrl(String scheme, String host, String port, ImList<String> pathComponents, ImList<ImPair<String, String>> queryElements, String fragment)
    {

        Throw.Exception.ifNull("scheme", scheme);
        Throw.Exception.ifNull("host", host);
        Throw.Exception.ifNull("port", port);
        Throw.Exception.ifNull("queryStringMap", queryElements);
        Throw.Exception.ifNull("fragment", fragment);

        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.pathComponents = pathComponents;

        this.fragment = fragment;
        this.queryElements = queryElements;

        this.path = pathComponents.toString("/");

    }

    /**
     * <p> The urlString has this form (roughly)
     * <p> scheme '://' host ':' port '/' path '?' query '#' fragment
     * <p> We can think of this as
     * <p> scheme ( separator field ) *
     * <p> If a separator exists then the value of the field following it will be non null - it could be ""
     * if a separator does not exist then the field that is meant to follow it will be null.
     * <p> This rule applies to the scheme - but taking into account the '://' that follows it rather than precedes it
     * <p> This class regards all strings as valid.
     * <p> If there are no separators then we assume that the string represents the host
     * <p> The empty string will mean that all the fields are null
     *
     */
    public static DrumUrl on(String urlString)
    {

        // If the string starts with a / then it must be a file scheme

        if (urlString.startsWith("/"))
        {
            ImList<String> parts = ImList.on(urlString.split("/+"));
            return on$("file:///" + parts.toString("/"));
        }

        // If there is no scheme, assume http - URI doesn't seem to work in some cases
        else if (!compile.matcher(urlString).find())
        {
            return on$("http://" + urlString);
        }
        else
        {
            return on$(urlString);
        }

    }

    private static DrumUrl on$(String urlString)
    {

        try
        {
            String scheme;
            String port;
            String host;
            String path;
            String query;
            String fragment;

            URL url = URL.parse(urlString);

            scheme = url.scheme();

            host = url.host() == null ? "" : url.host().toHumanString();

            port = isPortDefault(urlString)
                   ? ""
                   : "" + url.port();

            path = nullToEmptyString(url.path());

            query = nullToEmptyString(url.query());

            fragment = nullToEmptyString(url.fragment());

            ImList<String> pathComponents = getPathComponents(path);

            return new DrumUrl(scheme, host, port, pathComponents, getQueryStringValues(query), fragment);
        } catch (GalimatiasParseException e)
        {
            throw new DrumUrlParseException(e.getMessage());
        }

    }

    //    private static DrumUrl on$(String urlString)
    //    {
    //
    //        try
    //        {
    //            String scheme;
    //            String port;
    //            String host;
    //            String path;
    //            String query;
    //            String fragment;
    //
    //            URI uri = new URI(urlString);
    //
    //            scheme = uri.getScheme().toLowerCase();
    //
    //            host = nullToEmptyString(uri.getHost());
    //
    //            port = getPort(uri);
    //
    //            path = nullToEmptyString(uri.getPath());
    //
    //            query = nullToEmptyString(uri.getRawQuery());
    //
    //            fragment = nullToEmptyString(uri.getFragment());
    //
    //            ImList<String> pathComponents = getPathComponents(path);
    //
    //            return new DrumUrl(scheme, host, port, pathComponents, getQueryStringValues(query), fragment);
    //        } catch (URISyntaxException e)
    //        {
    //            throw new UnexpectedChecked(e);
    //        }
    //
    //    }

    public static boolean isPortDefault(String s)
    {
        String ss = "/?#:";

        ImPair<String, String> pair = ParseUtils.splitAt("://", s);
        ImList<Character> rest = ImList.onString(pair.snd).dropWhile(c -> !ss.contains("" + c));

        if (rest.isEmpty())
            return true;
        else if (rest.head() != ':')
            return true;
        else if (rest.tail().isEmpty())
            return true;
        else
            return !Character.isDigit(rest.tail().head());
    }

    private static String nullToEmptyString(String s)
    {
        return s == null ? "" : s;
    }

    public static ImList<ImPair<String, String>> getQueryStringValues(String query)
    {
        return parseQueryString(query);
    }

    /**
     *
     * <pre>{@code
     * "a=b"       a->b
     * "a==b"      a->=b
     * "a==b="     a->=b=
     * "a=b&c=d    a->b, c->d
     * ""          ignored
     * "a"         ignored
     * "="         ignored
     * "a="        ignored
     * "=a"        ignored
     * }</pre>
     *
     */
    private static ImList<ImPair<String, String>> parseQueryString(String query)
    {
        ImList<String> expressions = ParseUtils.split('&', query);

        // Filter out the empty elements and the invalid elements - ie "=bar"
        // A valid element must have no = or else x=y or x= - so not starting with =
        ImList<String> es = expressions.filter(e -> !e.isBlank()).filter(e -> !e.startsWith("="));

        // "a=b" => a -> b
        // "a="  => a -> ""
        // "a"   => a -> ""
        // "a==" => a -> =

        // Make pairs and convert nulls to empty strings and store as is
        return es
                .map(s -> ParseUtils.splitAt('=', s))
                .map(p -> ImPair.on(p.fst, nullToEmptyString(p.snd)));

        //                .filter(s -> s.contains("=") && !s.startsWith("=") && !(StringUtils.countMatches(s, '=') == 1 && s.endsWith("=")));
    }

    /**
     * <p> This is tricky
     * <p> URL        path
     * a.b        ""
     * a.b/       ""
     * a.b//      ""
     * a.b/c/d    "c/d"
     * a.b//c/d/  "c/d"
     *
     */
    private static ImList<String> getPathComponents(String path)
    {
        ImList<String> p = path == null
                           ? ImList.on()
                           : ImList.on(path.split("/+"));

        return p.isNotEmpty() && p.head().isEmpty()
               ? p.tail()
               : p;
    }

    public static DrumUrl on(Path path)
    {
        return DrumUrl.on("file://" + path.toFile().toString());
    }

    public static boolean sameOrigin(DrumUrl urlOne, DrumUrl urlTwo)
    {
        return Eq.uals(urlOne.scheme, urlTwo.scheme) &&
                Eq.uals(urlOne.host, urlTwo.host) &&
                Eq.uals(urlOne.port, urlTwo.port);
    }

    public DrumUrl withPath(ImList<String> pathComponents)
    {
        return new DrumUrl(scheme, host, port, pathComponents, queryElements, fragment);
    }

    public DrumUrl withPath(String pathString)
    {
        Throw.Exception.ifTrue(pathString.contains("?"), "paths can't contain ?");
        Throw.Exception.ifTrue(pathString.contains("#"), "paths can't contain #");
        return withPath(ParseUtils.split('/', pathString));
    }

    public DrumUrl withPort(int port)
    {
        return new DrumUrl(scheme, host, "" + port, pathComponents, queryElements, fragment);
    }

    public DrumUrl withNoQueriesOrFragments()
    {
        return new DrumUrl(scheme, host, port, pathComponents, ImList.on(), "");
    }

    public DrumUrl withScheme(String scheme)
    {
        return new DrumUrl(scheme, host, port, pathComponents, queryElements, fragment);
    }

    public DrumUrl addQueryElement(String key, String value)
    {
        return new DrumUrl(scheme, host, port, pathComponents, queryElements.appendElement(ImPair.on(encode(key), encode(value))), fragment);
    }

    public DrumUrl withQueryElements(ImList<ImPair<String, String>> queryElements)
    {
        return new DrumUrl(scheme, host, port, pathComponents, queryElements, fragment);
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(scheme, port, host, pathComponents, queryElements, fragment);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("scheme", "port", "host", "pathComponents", "queryElements", "fragment");
    }

    @Override
    public String toString()
    {
        return ImList.on(
                scheme,
                "://",
                host,
                showPort(port),
                TextUtils.joinMin(pathComponents, "/", "/", ""),
                withSep('?', getQueryString()),
                withSep('#', fragment)
        ).toString("");
    }

    private String showPort(String port)
    {
        return port.isEmpty()
               ? ""
               : ":" + port;
    }

    private String withSep(char sep, String part)
    {
        return part.isEmpty() ? "" : sep + part;
    }

    public String getQueryString()
    {
        ImList<String> qs = queryElements.map(p -> p.fst + "=" + p.snd);

        return TextUtils.joinMin(qs, "", "&", "");
    }

    public ImEither<String, String> getQueryStringValueDecoded(String keyEncoded)
    {
        ImList<ImPair<String, String>> pairs = queryElements.filter(p -> Eq.uals(p.fst, keyEncoded));

        return pairs.size() == 1
               ? ImEither.Right(pairs.head().snd)
               : ImEither.Left("Could not find query string value for key: " + keyEncoded);
    }

    /**
     * <p> Get the decoded value of a query element given its encoded key.
     * <p> Only returns a Just if there is exactly one such value
     *
     */
    public ImMaybe<String> getQueryStringValueDecodedMaybe(String keyEncoded)
    {
        ImList<ImPair<String, String>> pairs = queryElements.filter(p -> Eq.uals(p.fst, keyEncoded));

        return pairs.size() == 1
               ? ImMaybe.just(decode(pairs.head().snd))
               : ImMaybe.nothing();
    }

    protected static String decode(String s)
    {
        return URLDecoder.decode(s, utf8);
    }

    protected static String encode(String s)
    {
        return URLEncoder.encode(s, utf8);
    }

}