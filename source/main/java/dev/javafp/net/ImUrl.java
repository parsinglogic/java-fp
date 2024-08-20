/*
 * Copyright (c) 2012 Adrian Van Emmenis
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dev.javafp.net;

import com.ibm.icu.text.IDNA;
import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.func.Fn;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImTriple;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ImOneOfThree;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.Sums;
import dev.javafp.util.TextUtils;
import dev.javafp.val.ImCodePoint;
import dev.javafp.val.ImValuesImpl;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static dev.javafp.val.ImCodePoint.COLON;
import static dev.javafp.val.ImCodePoint.alpha;

/**
 * <p>
 * This class <strong><em>parses</em></strong> a URL string into an ImUrl object, and can convert it back to a string. You can access the various parts of the URL using fields and it is an immutable object.
 * </p>
 * <p>
 * It also allows clients to create new URL objects by "setting" various parts of an existing URL.
 * </p>
 * <p>
 * Once we have an ImUrl object - the idea is that we can convert it to a Java URL object and then send an HTTP style query to some website using the built-in Java features  - see APIRequest.
 * </p>
 * <h2>Why this class?</h2>
 *
 *
 * <p>
 * We originally had a URL class that was a thin immutable wrapper around the Java URL class - ie just use the Java URL class to do the parsing of the URL string - but that class only supports a very old version of the relevant standard so we felt we should do more in this area.
 * </p>
 * <p>
 * In particular, it is not able to handle URLs like this:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     https://räksmörgås.josefsson.org
 * </pre>
 *
 *
 * <p>
 * What is meant to happen here is that the URL parser converts this using the IDNA name mapping standard:
 * </p>
 * <p>
 * <a href="https://datatracker.ietf.org/doc/html/rfc5891#section-4.2.3">https://datatracker.ietf.org/doc/html/rfc5891#section-4.2.3</a>
 * </p>
 * <p>
 * to get this:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     https://xn--rksmrgs-5wao1o.josefsson.org
 * </pre>
 *
 *
 * <p>
 * This mapping is specified in
 * </p>
 * <p>
 * <a href="https://datatracker.ietf.org/doc/html/rfc3490">https://datatracker.ietf.org/doc/html/rfc3490</a>
 * </p>
 * <p>
 * I am not sure why this particular mapping was chosen.
 * </p>
 * <p>
 * So we are using the IBM icu4j classes to do the necessary IDNA name mappings.
 * </p>
 * <p>
 * If we supply the above IDNA-mapped  URL to the Java URL class then it will work correctly - it just doesn't know how to do the IDNA mapping on the original unicode string.
 * </p>
 * <h2>Why is parsing URLs so... messy?</h2>
 *
 *
 * <p>
 * If you read the WhatWG standard document you might be surprised to find that, apart from defining a few sets of codepoints and explaining some broad concepts, the main meat of the standard is a pseudocode description of how to do the parsing. I suppose you could either regard this as a refreshingly practical way to explain how to implement the standard or an admission that it's so damn complicated that this is the only way to get through it. It certainly makes it difficult to get an overview of what is expected by having to plough through about eight pages of pseudocode.
 * </p>
 * <p>
 * In "Parsing millions of URLs per second" (<a href="https://onlinelibrary.wiley.com/doi/10.1002/spe.3296?af=R">https://onlinelibrary.wiley.com/doi/10.1002/spe.3296?af=R</a>) which discusses a particular implementation of the WhatWG standard (now integrated with Node.js version 20), the authors provide a helpful state-transition diagram to illustrate the parser.
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image1.png" width="600" >
 *
 * </p>
 * <p>
 * <strong><em>The URL parser state machine.</em></strong>
 * </p>
 * <p>
 * Obviously, our old friend "history" has a part to play in why things are so complicated. As we have struggled with our implementation we would identify a few main reasons
 * </p>
 * <ol>
 *
 * <li>The host part is treated very differently from all the other parts
 *
 * <li>The file scheme is different from all the other special schemes
 *
 * <li>The little-used authority parts (user and password) are very peculiar indeed
 *
 * <li>The parsing of "opaque" (ie non-standard) schemes imposes many subtle issues
 *
 * <li>The parsing of relative URLs is tricky
 * </li>
 * </ol>
 * <p>
 * We have simplified our task by not attempting to implement 4 and 5 above.
 * </p>
 * <h2>The WhatWG standard</h2>
 *
 *
 * <p>
 * The relevant standard for parsing URLs and their internal representation  is the WhatWg standard:
 * </p>
 * <p>
 * <a href="https://url.spec.whatwg.org">https://url.spec.whatwg.org</a>
 * </p>
 * <p>
 * This describes itself as a "living standard" - we are using the version that was updated 25-mar-2024.
 * </p>
 * <p>
 * We would like to support the WhatWg standard completely but that is too much effort for us to do at the moment.
 * </p>
 * <p>
 * This has a good overview:
 * </p>
 * <p>
 * <a href="https://onlinelibrary.wiley.com/doi/10.1002/spe.3296?af=R">https://onlinelibrary.wiley.com/doi/10.1002/spe.3296?af=R</a>
 * </p>
 * <p>
 * So there are some restrictions in this implementation:
 * </p>
 * <ul>
 *
 * <li>It only supports four schemes - http, https, file, ftp  because these are the only ones supported by Java "out of the box"
 *
 * <li>It doesn't support the jar scheme yet - even though Java does support it "out of the box"
 *
 * <li>It only supports <strong><em>absolute</em></strong> URLs
 * </li>
 * </ul>
 * <p>
 * With these restrictions, it does support the WhatWg standard fairly closely.
 * </p>
 * <p>
 * It passes all of the 296 applicable tests from
 * </p>
 * <p>
 * <a href="https://github.com/web-platform-tests">https://github.com/web-platform-tests</a>
 * </p>
 * <h2>Does this mean that this class parses URLs in exactly the same way as browsers do?</h2>
 *
 *
 * <p>
 * Alas no. The WhatWg standard is not followed exactly by all browsers. I have tested on Chrome and found many cases where Chrome does something different to the standard. Hey ho.
 * </p>
 * <p>
 * The standard has many cases where it states that some input causes a validation error:
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image2.png" width="600" >
 *
 * </p>
 * <p>
 * For the validation errors that don't mean that the parser should terminate, our parser also does not terminate and we currently don't provide any indication that there has been a validation error.
 * </p>
 * <p>
 * WhatWg's pseudocode has used an approach that parses each part in one or two functions with all the variations noted above being handled by flags being passed to each function with plentiful if tests to do different things in different cases. This does not lead to very comprehensible pseudocode.
 * </p>
 * <p>
 * In addition, and we don't want you to think we are just complaining for the sake of it here, the extract below is from the spec - with the pseudocode for the scheme state, it is not obvious how we would easily implement section 2.1.3. It requires us to make a decision based on the credentials (user/password) and the port - but these three items have not been parsed yet - if we are following the pseudocode for the state machine. Tricky.
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image5.png" width="600" >
 *
 * </p>
 * <p>
 * From "Parsing millions of URLs per second":
 * </p>
 * <p>
 *
 *     <em>To illustrate the complexity, our C++ software library implementing the WHATWG URL standard—and little else—has approximately 20,000 lines of code.</em>
 * </p>
 * <p>
 * We can't help wondering - if the standard authors have decided that code is the easiest way to describe the parsing, would it not have been more helpful to use an actual compilable language so that it would be easier to read and test for implementers.
 * </p>
 * <h2>How widely implemented is the WhatWG standard in browsers?</h2>
 *
 *
 * <p>
 * While we were using the (very useful) website
 * </p>
 * <p>
 * <a href="https://jsdom.github.io/whatwg-url">https://jsdom.github.io/whatwg-url</a>
 * </p>
 * <p>
 * to better understand the standard (on Chrome on Macos Version 126.0.6478.127) we noticed about 20 different cases where the standard and Chrome disagreed. This is just an ad-hoc observation. We have not tested these cases on other browsers.
 * </p>
 * <h2>The "quick" summary</h2>
 *
 *
 * <p>
 * The basic idea of a WhatWG URL parser is this:
 * </p>
 * <ol>
 *
 * <li>It takes an input in the form of a string in UTF-16 encoding
 *
 * <li>It parses it and converts this to an internal format. If there are "fatal" errors then it should refuse to parse it. For non-fatal errors it should still work.
 *
 * <li>It can serialise this internal format to a string that contains only ASCII printable characters.
 * </li>
 * </ol>
 * <p>
 * The spec makes it clear that the string is processed one unicode codepoint at a time. It also usually talks about codepoints rather than characters.
 * </p>
 * <p>
 * We will also talk about codepoints rather than characters.
 * </p>
 * <h2>The parts of a URL</h2>
 *
 *
 * <p>
 * So if we think about a URL input string as being composed of <strong><em>parts</em></strong>, our first idea might be that it is this (well roughly):
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image7.png" width="600" >
 *
 * </p>
 * <p>
 * For clarity, I have highlighted the delimiters.
 * </p>
 * <p>
 * In fact there are a few more parts that we normally forget (or never knew about in the first place):
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image6.png" width="600" >
 *
 * </p>
 * <p>
 * This means that there are eight parts to a URL. Most of the parts are optional.
 * </p>
 * <p>
 * Just to be difficult, I am going to say that there are actually <strong><em>nine</em></strong> parts to a URL.
 * </p>
 * <p>
 * After the scheme part, delimited by ':' there can be a run of slashes, '/' '\'. For the file scheme the number of slashes is significant - so I am going to promote what most people will tell you is a humble delimiter into a full part. So now, (again - roughly) we have this:
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image4.png" width="600" >
 *
 * </p>
 * <p>
 * The scheme-part is required and the host part is required - except for some cases in the file scheme - this depends on the slashes-part in this case.
 * </p>
 * <p>
 * The other parts are optional - the file scheme does not allow a port.
 * </p>
 * <p>
 * If an optional part does not appear in the URL then its default value in the parsed form is considered to be the empty string (sigh - except for the path part, where the default is '/'.
 * </p>
 * <p>
 * If it does appear but is empty because its start delimiter appears but one of its terminating delimiters follows it immediately, then it is also considered to be the empty string. When the URL is serialised it is represented as if it didn't appear.
 * </p>
 * <p>
 * We can split the parts into three distinct types based on how the parser handles non-printable ASCII codepoints and percent encoded triplets
 * </p>
 * <h3>Strict</h3>
 *
 *
 * <p>
 * Non-printable ASCII codepoints or percent encoded triplets are not allowed. For these parts we don't talk about the disallowed codepoints in the input - because each part specifies what codepoints are allowed and that is a small set in each case - so considering disallowed codepoints is unnecessary.
 * </p>
 * <p>
 * These parts are:
 * </p>
 * <ol>
 *
 * <li>scheme
 *
 * <li>slashes
 *
 * <li>port
 * </li>
 * </ol>
 * <h3>Non-strict</h3>
 *
 *
 * <ol>
 *
 * <li>Non-printable ASCII codepoints are allowed - they will be converted to percent encoded triplets.
 *
 * <li>Percent encoded triplets are allowed - they will be passed through unchanged.
 *
 * <li>The '%' codepoint is allowed - even if not part of a valid percent encoded triplet - it is passed through unchanged. This behavior is described in the standard as being a non-fatal validation error (except in the authority part) so ... we regard it as allowed behaviour.
 * </li>
 * </ol>
 * <p>
 * So although the <strong><em>spirit</em></strong> of having percent encoded triplets is that they should be decodable to unicode codepoints,  <strong><em>there is no validation that this is the case</em></strong>. Any percent encoded triplets are simply passed through unchanged - valid or invalid.
 * </p>
 * <p>
 * For these parts we do talk about there being disallowed codepoints in the input. These are normally disallowed because it would confuse the parsing. For example, any codepoint that acts as an end delimiter is normally disallowed (but see passwords!).
 * </p>
 * <p>
 * In order to smuggle them into a part, you can represent them as percent encoded triplets - remember that the parser just passes these through unchanged - and hope that these will be decoded at the server when the URL is used.
 * </p>
 * <p>
 * These parts are:
 * </p>
 * <ol>
 *
 * <li>user
 *
 * <li>password
 *
 * <li>path
 *
 * <li>query
 *
 * <li>fragment
 * </li>
 * </ol>
 * <h3>Host</h3>
 *
 *
 * <p>
 * In the host part, non-printable ASCII codepoints <strong><em>are</em></strong> allowed - although not all will be valid. Some codepoints are identified by the standard as being "forbidden" and others will be rejected by the IDNA name mapping/Punycode algorithm.
 * </p>
 * <p>
 * Any percent encoded triplets <strong><em>are</em></strong> decoded to unicode characters - if these triplets represent invalid UTF-8 sequences then this will cause a fatal error.
 * </p>
 * <p>
 * To decode a run of percent encoded triples to unicode characters, the parser converts the triples to octets - each triple defines an octet - and then it treats the run of octets that it has just created as utf-8 encoded bytes and it decodes them to unicode codepoints.
 * </p>
 * <p>
 * So - there might be code points in the input that are non-printable ASCII codepoints and there might be some that were not in the input but were generated by the process of decoding the percent encoded triplets. These are mapped to printable ASCII using the IDNA mapping (AKA punycode).
 * </p>
 * <p>
 * Note that it only decodes the runs of triplets themselves - it does not convert the triplets to bytes and then combine them with neighbouring code points and try to decode these.
 * </p>
 * <p>
 * The only part that this applies to is:
 * </p>
 * <ol>
 *
 * <li>host
 * </li>
 * </ol>
 * <p>
 * The idea is that once an input has been parsed, the serialised form of it will be parsable into a URL that will have the same serialised form.
 * </p>
 * <p>
 * If the parsing process detects an error, the standard says that the implementation is encouraged to make the error available somewhere. The standard identifies two types of errors
 * </p>
 * <ol>
 *
 * <li>fatal errors that stop the URL being parsed
 *
 * <li>non-fatal errors - warnings really - although it does not specify how to make the text of the warning available to the user.
 * </li>
 * </ol>
 * <p>
 * We are all used to typing URLs into browser address bars - although this is a little misleading since, the browser does its best to take what you have typed and give you some sort of result - even if it is not a strictly valid URL.
 * </p>
 * <p>
 * for example you can type
 * </p>
 * <p>
 * google.com
 * </p>
 * <p>
 * into most browsers and you will see the google search page.
 * </p>
 * <p>
 * if you look at what the browser has done you will notice that the url it is actually using is
 * </p>
 * <p>
 * <a href="https://www.google.com/">https://www.google.com/</a>
 * </p>
 * <p>
 * The browser has added the scheme "https" (or maybe "http") and inserted "://" between that and "google.com" and the web site has redirected you to the actual page.
 * </p>
 * <p>
 * In Java, if you try this:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     new URL("google.com");
 * </pre>
 *
 *
 * <p>
 * You will get an exception:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     java.net.MalformedURLException: no protocol: google.com
 * </pre>
 *
 *
 * <p>
 * You can try going through the URI class - but you will get a similar exception.
 * </p>
 * <p>
 * So, despite what browsers allow, you need to specify the protocol (AKA scheme) at the start of the URL.
 * </p>
 * <p>
 * For the host there are a number of codepoints that are "forbidden". For all the other parts there are codepoints that are not allowed in the input because they would interfere with the parsing
 * </p>
 *
 * <table class="table">
 *   <tr>
 *    <td>
 *    </td>
 *    <td>optional?
 *    </td>
 *    <td>type
 *    </td>
 *    <td>start delimiters
 *    </td>
 *    <td>end delimiters
 *    </td>
 *    <td>disallowed input codepoints
 *    </td>
 *    <td>percent encode sets
 *    </td>
 *    <td>notes
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>scheme
 *    </td>
 *    <td>no
 *    </td>
 *    <td>strict
 *    </td>
 *    <td>start of input
 *    </td>
 *    <td>':'
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>No percent encoding allowed
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>slashes
 *    </td>
 *    <td>yes
 *    </td>
 *    <td>strict
 *    </td>
 *    <td>':'
 *    </td>
 *    <td>Not really an end delimiter - ends before a character that is not a '/' or '\'
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>No percent encoding allowed. Must be composed of 0 or more slashes
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>user
 *    </td>
 *    <td>yes - except for the file scheme where it is not allowed
 *    </td>
 *    <td>non-strict
 *    </td>
 *    <td>'/' '\' ':'
 *    </td>
 *    <td>':'
 *    </td>
 *    <td>'/' '\' '?' '#'
 *    </td>
 *    <td>'/' ':' ';' '=' '@' '[' '\' '\' ']' '^' '|' (and the path encode set)
 *    </td>
 *    <td>can't contain '/' '\' '?' or '#' so why they appear in the percent encode set - we don't know
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>password
 *    </td>
 *    <td>yes - except for the file scheme where it is not allowed
 *    </td>
 *    <td>non-strict
 *    </td>
 *    <td>':'
 *    </td>
 *    <td>'@'
 *    </td>
 *    <td>'/' '\' '?' '#'
 *    </td>
 *    <td>as above
 *    </td>
 *    <td>as above
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>host
 *    </td>
 *    <td>no - except for the file scheme
 *    </td>
 *    <td>host
 *    </td>
 *    <td>'/' '\' ':' '@'
 *    </td>
 *    <td>'/' '\' ':'  '?' '#' end-of-input
 *    </td>
 *    <td>'/' '\' '?' '#' ':' '&lt;' '>' '@' '[' ']' '^' '|' (and the c0 controls) and '%' unless it is part of a valid percent encoded triple and that triple is part of a run of triples that can be converted to a valid codepoint ... that is not 'forbidden'
 *    </td>
 *    <td>n/a - the host part uses IDNA mapping
 *    </td>
 *    <td>The disallowed input codepoints can't be smuggled into a host by percent encoding them. The standard calls them 'forbidden' codepoints.
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>port
 *    </td>
 *    <td>yes - except for the file scheme where it is not allowed
 *    </td>
 *    <td>strict
 *    </td>
 *    <td>':'
 *    </td>
 *    <td>'/' '\'  '?' '#' end-of-input
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>n/a
 *    </td>
 *    <td>must be decimal digits
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>path
 *    </td>
 *    <td>yes
 *    </td>
 *    <td>non-strict
 *    </td>
 *    <td>'/' '\' ':'
 *    </td>
 *    <td> '?' '#' end-of-input
 *    </td>
 *    <td>'?' '#'
 *    </td>
 *    <td>'?' '`' '{' '}' (and the query encode set)
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>query
 *    </td>
 *    <td>yes
 *    </td>
 *    <td>non-strict
 *    </td>
 *    <td>'?'
 *    </td>
 *    <td>'#' end-of-input
 *    </td>
 *    <td>'#'
 *    </td>
 *    <td> ' ' '"' '&lt;' '>' '#'
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>fragment
 *    </td>
 *    <td>yes
 *    </td>
 *    <td>non-strict
 *    </td>
 *    <td>'#'
 *    </td>
 *    <td>end-of-input
 *    </td>
 *    <td>none
 *    </td>
 *    <td> ' ' '"' '&lt;' '>' '`'
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 * </table>
 *
 *
 * <p>
 * So each of the parts is parsed in its own idiosyncratic way. Some of them are <strong><em>very</em></strong> idiosyncratic - to the point of being slightly bonkers (IMHO).
 * </p>
 * <p>
 * For example, these are valid URLs:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     http:::@@{♥~?%#
 *     http:4294967295
 *     http:/\/\/\/\:::::@@@@
 * </pre>
 *
 *
 * <p>
 * We will describe the parts in more detail::
 * </p>
 * <h2>The scheme part (AKA protocol)</h2>
 *
 *
 * <p>
 * Because we only allow four schemes, this is fairly simple.
 * </p>
 * <p>
 * You can have http, https, file, ftp - and that's it.
 * </p>
 * <p>
 * You can use upper, lower, mixed case.
 * </p>
 * <p>
 * The parser will convert the scheme to lowercase before serialising.
 * </p>
 * <p>
 * The scheme is right-delimited by ':'.
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     http:                                scheme = "http"
 *     HTTPS:                               scheme = "https"
 *     FiLE:                                scheme = "file"
 *     fTp:                                 scheme = "ftp"
 * </pre>
 *
 *
 * <h2>The slashes part</h2>
 *
 *
 * <p>
 * After the ':' that is the end delimiter for the scheme, there can be 0 or more slashes - back or forward:  '\'  '/'
 * </p>
 * <p>
 * Any slashes after the ':' are ignored (except for the file scheme where they <strong><em>are</em></strong> significant - see below). You could almost consider them a decorative feature!
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     //
 *     /
 *     \
 *     \\
 *     ///////
 *     \\\\\\\\\\\
 *     \\\///\\\///\\\\\\\///////
 * </pre>
 *
 *
 * <h2>The authority part (user-name and password)</h2>
 *
 *
 * <p>
 * Both parts are optional. For the file scheme they are not allowed.
 * </p>
 * <p>
 * The user-name is terminated by ':' and the password is terminated by '@'.
 * </p>
 * <p>
 * Curiously, the password can contain '@' codepoints even though it is also the end-delimiter.  Normally, if you want to smuggle in a codepoint that is an end delimiter you have to percent encode it - but not with the password.
 * </p>
 * <p>
 * This makes it a little tricky to parse correctly, it's true.
 * </p>
 * <p>
 * The authority can't contain '/' '\' '?' '#'.
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     foo:bar@                   user = "foo", password = "bar"
 *     :bar@                      user = "", password = "bar"
 *     :@                         user = "", password = ""
 *     :::@@@                     user = "", password = "%3A%3A%40%40"
 * </pre>
 *
 *
 * <h2>The host part</h2>
 *
 *
 * <p>
 * With schemes http, https and ftp you must specify a host. For the file scheme this is optional.
 * </p>
 * <p>
 * With the file scheme, this depends on how many slashes there are after 'file:'. If the number of slashes is two then you can either have:
 * </p>
 * <ol>
 *
 * <li>a host (which can then be followed by a path)
 *
 * <li>a windows path (so no host can be specified)
 * </li>
 * </ol>
 * <p>
 * otherwise (not two slashes) you cannot specify a host
 * </p>
 * <p>
 * The host is right-delimited by one of '/' '\' '?' '#' or the end of the input.
 * </p>
 * <p>
 * If you use "localhost" with the file scheme, the standard says that you must record the host as being the empty string - as if you didn't refer to a host at all.
 * </p>
 * <p>
 * Hosts can have three forms:
 * </p>
 * <ol>
 *
 * <li>IPv4 address - eg  <strong><code>192.168.0.17</code></strong>
 *
 * <li>IPv6 address - eg  <strong><code>[::1:56:42]</code></strong>
 *
 * <li>host and domain name - eg <strong><code>foo.bing.bar</code></strong>
 * </li>
 * </ol>
 * <p>
 * Conceptually, the parser tries to parse the IPv4 address and the IPv6 address options first. If these forms are not recognised then it tries host and domain name. This is significant because, when parsing IPv4 or IPv6 addresses, the '%' codepoint is not allowed but with option 3 above, the '%' codepoint is allowed in the input - but see below for the details.
 * </p>
 * <h3>host and domain name</h3>
 *
 *
 * <p>
 * In the host part, when parsing a host name and a domain name, codepoints are treated very differently to all the other parts. If there are non printable ASCII codepoints in the host part, they will be mapped to printable ASCII codepoints using IDNA name mapping (AKA Punycode encoding).
 * </p>
 * <p>
 * The parser looks at the code points that comprise the host and it regards any runs of percent encoded triplets as representing a UTF-8 encoding of unicode codepoints. It converts the triplets to bytes and decodes them - which will result in new codepoints.
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     percent encoded triplets    -->   UTF-8 byte sequences  -->  codepoints  -->  IDNA/Punycode codepoints
 * </pre>
 *
 *
 * <p>
 * This means that any percent encoded triplets must map to a valid UTF-8 byte sequence - and you can't have a '%' on its own - a '%' is a forbidden codepoint.
 * </p>
 * <h3>Dots!</h3>
 *
 *
 * <p>
 * So, the parsing algorithm (as described below) splits the host into a list of labels delimited by dots where the dots could be this character '｡' which is the codepoint <code>0xFF61</code>
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     '｡' (hex codepoint 0xFF61) UTF-8 encodes to bytes 0xEF 0xBD 0xA1 so the percent encoding is %EF%BD%A1
 * </pre>
 *
 *
 * <p>
 * So
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     a%EF%BD%A1com
 * </pre>
 *
 *
 * <p>
 * This will parse as:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     a.com
 * </pre>
 *
 *
 * <p>
 * A domain name is a list of "labels", separated by "dots". Each label is a list of codepoints that has no dots in it.
 * </p>
 * <p>
 * A dot is one of the following codepoints:
 * </p>
 * <ol>
 *
 * <li><strong><code>U+002E ( . ) FULL STOP</code></strong>
 *
 * <li><strong><code>U+FF0E ( ．) FULLWIDTH FULL STOP</code></strong>
 *
 * <li><strong><code>U+3002 ( 。) IDEOGRAPHIC FULL STOP</code></strong>
 *
 * <li><strong><code>U+FF61 ( ｡ ) HALFWIDTH IDEOGRAPHIC FULL STOP</code></strong>
 * </li>
 * </ol>
 * <p>
 * Although the parser accepts these dots as input, it converts each to the printable ASCII (FULL STOP) dot before serialising.
 * </p>
 * <p>
 * As we mentioned before, the host part is the only part where non printable ASCII characters are not percent encoded before serialising. Instead, the parser uses IDNA name mapping. Each label is mapped individually.
 * </p>
 * <h3>IDNA name mapping (Punycode encoding)</h3>
 *
 *
 * <p>
 * <a href="https://www.rfc-editor.org/info/rfc5894">https://www.rfc-editor.org/info/rfc5894</a>
 * </p>
 * <p>
 * <a href="https://www.unicode.org/reports/tr46/tr46-31.html">https://www.unicode.org/reports/tr46/tr46-31.html</a>
 * </p>
 * <p>
 * It uses the Punycode algorithm and generates something called an A-label.
 * </p>
 * <p>
 * from <a href="https://www.rfc-editor.org/info/rfc5894">https://www.rfc-editor.org/info/rfc5894</a>
 * </p>
 * <p>
 *
 *
 *
 *
 * <img src="{@docRoot}/dev/doc-files/url-images-image3.png" width="600" >
 *
 * </p>
 * <p>
 * The WhatWG standard has a basic list of forbidden domain codepoints but it is not the case that any other codepoints are valid.
 * </p>
 * <p>
 * The full set of rules about what codepoints are valid in IDNA mapping is beyond the scope of this document.
 * </p>
 * <p>
 * Some are considered invalid and will result in a fatal error. Some are simply removed from the input. This can result in the printable ASCII being empty. This is a fatal error.
 * </p>
 * <p>
 * If a label the input is already Puny encoded - ie it looks like these:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     xn--blah-blah
 *     xn--blah
 * </pre>
 *
 *
 * <p>
 * then, this is checked for validity and simply passed through. If it is not valid then this is a fatal error.
 * </p>
 * <p>
 * An example that is a real site - albeit created to help explain Punycode encoding is:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     räksmörgås.josefsson.org          host = "xn--rksmrgs-5wao1o.josefsson.org"
 * </pre>
 *
 *
 * <p>
 * If you enter either of the above into your web browser address bar, you will go to the same site.
 * </p>
 * <p>
 * Web sites can have names that are emojis but with some restrictions. Some emojis are combinations of two simpler emojis - such as bald man, which is the man emoji combined with the bald emoji. In these cases the emojis will be combined using a zero width joiner codepoint between them. This zero width joiner is not accepted by the IDNA mapping algorithm.
 * </p>
 * <p>
 * Note that composite emojis <strong><em>are</em></strong> allowed in other parts
 * </p>
 * <p>
 * Example of a single emoji for the host part and a composite emoji for the path part:
 * </p>
 * <p>
 *
 *     😫/👩🏼‍🦰<code>               <strong>host = "<a href="http://xn--qq8h/%F0%9F%98%AB/%F0%9F%91%A9%F0%9F%8F%BC%E2%80%8D%F0%9F%A6%B0">xn--qq8h</a>", path = "<a href="http://xn--qq8h/%F0%9F%98%AB/%F0%9F%91%A9%F0%9F%8F%BC%E2%80%8D%F0%9F%A6%B0">%F0%9F%98%AB/%F0%9F%91%A9%F0%9F%8F%BC%E2%80%8D%F0%9F%A6%B</a>"</strong></code>
 *
 * <p>
 * For details of the emojis in this example:
 * </p>
 * <p>
 * <a href="https://emojipedia.org/man">https://emojipedia.org/man</a>
 * </p>
 * <p>
 * <a href="https://emojipedia.org/tired-face">https://emojipedia.org/tired-face</a>
 * </p>
 * <p>
 * <a href="https://emojipedia.org/woman-medium-light-skin-tone-red-hair">https://emojipedia.org/woman-medium-light-skin-tone-red-hair</a>
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 * <table class="table">
 *   <tr>
 *    <td>Input
 *    </td>
 *    <td>Host
 *    </td>
 *    <td>Notes
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>аррӏе.com</code></strong>
 *    </td>
 *    <td><strong><code>/xn--80ak6aa92e.com</code></strong>
 *    </td>
 *    <td>This is the famous "apple in cyrillic" homograph attack - see
 * <p>
 * <a href="https://www.theregister.com/2017/04/18/homograph_attack_again/">https://www.theregister.com/2017/04/18/homograph_attack_again/</a>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>xn--rksmrgs-5wao1o.com</code></strong>
 *    </td>
 *    <td><strong><code>xn--rksmrgs-5wao1o.com</code></strong>
 *    </td>
 *    <td>If the input is already encoded then there is nothing to do
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>xn--rksmrgs-5wao1ox.com</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - puny encoding is invalid
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>abc.com </code></strong>
 *    </td>
 *    <td><strong><code>abc.com</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>a。b｡c    </code></strong>
 *    </td>
 *    <td><strong><code>a.b.c</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>a..b...c...   </code></strong>
 *    </td>
 *    <td><strong><code>a..b...c...</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>a.1  </code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - last component is numeric
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>a.1.    </code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - effective last component is numeric
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>a.1..  </code></strong>
 *    </td>
 *    <td><strong><code>a.1..</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 * </table>
 *
 *
 *
 *
 *
 * <pre class="prettyprint">
 *
 * </pre>
 *
 *
 * <h3>IPv4 addresses</h3>
 *
 *
 * <p>
 * These are typically groups(segments) of four decimal numbers, each number &lt;= 255.
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     1.2.3.4
 *     192.168.1.0
 * </pre>
 *
 *
 * <p>
 * but you can also use numbers in octal and hex format
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     1.010.0xF.0
 * </pre>
 *
 *
 * <p>
 * And those dots? Just like with domain names, they can be any one of four unicode codepoints - see above.
 * </p>
 * <h4>They are just digits</h4>
 *
 *
 * <p>
 * One way to think about an IPv4 address is that it represents a single integer and that it is expressed as 4 "digits" using base 256.
 * </p>
 * <p>
 * Each "digit" is represented by an integer from 0 to 255
 * </p>
 * <p>
 * So for integers a,b,c,d where each is between 0 and 255, the final number is
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     256^3 * a + 256^2 * b + 256 * c + d
 * </pre>
 *
 *
 * <p>
 * If the last number, d is greater than 255 then it will try to split it into "digits" and replace d with these new digits.
 * </p>
 * <p>
 * So
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     0.0.256 -> 0.0.1.1
 *     http://4294967295
 * </pre>
 *
 *
 * <p>
 * is a valid IPv4 address and is serialised as:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     http://255.255.255.255
 * </pre>
 *
 *
 * <p>
 * If there are fewer than 4 numbers, even after any last number processing, then zeros are added after the previous numbers to pad to 4 numbers
 * </p>
 * <p>
 * If this results in an address with more than 4 segments then this is a fatal error.
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 * <table class="table">
 *   <tr>
 *    <td>Input
 *    </td>
 *    <td>Host
 *    </td>
 *    <td>
 * Notes
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>1.2.3.4</code></strong>
 *    </td>
 *    <td><strong><code>1.2.3.4</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>10。20｡30．40</code></strong>
 *    </td>
 *    <td><strong><code>10.20.30.40</code></strong>
 *    </td>
 *    <td>There are no spaces. It's just that some dots are wide.
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>1.2.0x10.010</code></strong>
 *    </td>
 *    <td><strong><code>1.2.16.8</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>1.10.0x11.000000010</code></strong>
 *    </td>
 *    <td><strong><code>1.10.17.8</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>0x0001.0xAb.0xff.0xFF</code></strong>
 *    </td>
 *    <td><strong><code>1.171.255.255</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>1.2.00x10.000000010</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - hex numbers can't have leading zeros before the 0x
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>0.0.257</code></strong>
 *    </td>
 *    <td><strong><code>0.0.1.2</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td>
 * <strong><code>1.0.0.257</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - would expand to 5 segments
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>258</code></strong>
 *    </td>
 *    <td><strong><code>0.0.1.3</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>0</code></strong>
 *    </td>
 *    <td><strong><code>0.0.0.0</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>4294967295</code></strong>
 *    </td>
 *    <td>
 * <strong><code>255.255.255</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *   </tr>
 *   <tr>
 *    <td><strong><code>4294967296</code></strong>
 *    </td>
 *    <td>
 *    </td>
 *    <td>invalid - address too big
 *    </td>
 *   </tr>
 * </table>
 *
 *
 * <h3>IPv6 address</h3>
 *
 *
 * <p>
 * If the host part starts with '[' then this signals that you are supplying an IPv6 address. The IPv6 address is terminated with a ']' but it also needs a ':', '/' '\' '?' or '#'
 * </p>
 * <p>
 * Eg
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     http://[1:2:3:4:5:6:7:8]
 *     http://[1:0::FFFF:0:0:0:01]
 * </pre>
 *
 *
 * <p>
 * An IPv6 address has 8 segments - there is no strange expanding of digits as in IPv4 - but they make up for that oversight by having a peculiar compression system and an even more peculiar system for embedding IPv4 addresses inside it. Phew!
 * </p>
 * <p>
 * Each segment is a hex number between 0 and FFFF inclusive (with an exception for the last part - see below)
 * </p>
 * <p>
 * You can compress" to remove sequences of 0's by replacing the 0's with an empty part.
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     0:0:0:4:5:6:7:8  -> ::4:5:6:7:8
 * </pre>
 *
 *
 * <p>
 * An empty part means "fill with enough 0 parts to get up to 8 parts".
 * </p>
 * <p>
 * You can only have one such empty part in the address.
 * </p>
 * <p>
 * In the input, the compression rules are quite permissive. You can replace even a single 0 with an empty part. You don't have to compress the whole run of 0's.
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     1:2:3:0:5:6:7:8  -> 1:2:3::5:6:7:8
 *     1:2:3:0:0:0:7:8  -> 1:2:3::0:7:8
 * </pre>
 *
 *
 * <p>
 * When serialising, the parser is required to convert the address to a "canonical form". It should first expand any compressed parts and then choose the <strong><em>first</em></strong> maximally sized run of 0's and compress it.
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     [1:2:3:4:5:6:7:8]                 host = "[1:2:3:4:5:6:7:8]"
 *     [1:0:0:4:5:0:0:8]                 host = "[1::4:5:0:0:8]"
 *     [1:0:0:0:5::8]                    host = "[1::5:0:0:8]"
 *     [0:0:0:0:0:0:0:0]                 host = "[::]"
 *     [A:B:C:D:E:F::0]                  host = "[a:b:c:d:e:f::]"
 *     [FFFF::]                          host = "[ffff::]"
 *     [00FF::]                          host = "[ff::]"
 *     [0000F::]                         invalid - too many digits
 *     [:2::]                            invalid - too many digits
 * </pre>
 *
 *
 * <h4>Embedded IPv4 addresses</h4>
 *
 *
 * <p>
 * In the same way that Ipv4 addresses can be thought of as four "digits" base 0x100 (256), IPv6 addresses can be thought of as eight "digits" base 0x10000(<strong>65536)</strong>. You are not allowed to supply digits that are larger than 0xFFFF and have them adjusted but you are allowed to have the last part of the address be an IPv4 address and the parser is required to convert those <strong><em>four</em></strong> digits base 256 to <strong><em>two</em></strong> digits base 0x10000 and replace the IPv4 address with these two digits.
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     [::1.0.0.2] -> [::100:2]
 *     [1:2:3:4:5:6:2.0.0.0] -> [1:2:3:4:5:6:200:0]
 * </pre>
 *
 *
 * <p>
 * If you have more than six components before the embedded IPv4 address then this is a fatal error.
 * </p>
 * <p>
 * And just for grins, there are special rules about the embedded IPv4 address that must be obeyed. You can't just throw in any old address.
 * </p>
 * <ol>
 *
 * <li>All the numbers must be base 10.
 *
 * <li>No leading zeros.
 *
 * <li>Exactly four components - so all must be &lt;=255
 * </li>
 * </ol>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     [ffff::127.0.0.1]                 host = "[ffff::7f00:1]"
 *     [::255.255.255.255]               host = "[::ffff:ffff]"
 *     [ffff::127.0.1]                   invalid - ipv4 parsing is strict - must be 4 segments
 *     [::0.0.0.256]                     invalid - ipv4 parsing is strict - must be 4 segments &lt;= 255
 *     [ffff::127.0.1.02]                invalid - ipv4 parsing is strict - must be decimal numbers - no leading zeros
 *     [ffff::127.0.1.FFFF]              invalid - ipv4 parsing is strict - must be decimal numbers
 * </pre>
 *
 *
 * <h2>The port part</h2>
 *
 *
 * <p>
 * The port is left-delimited by a ':' and right-delimited by one of  '/' '\' '?' '#' or the end of the input.
 * </p>
 * <p>
 * It can be the empty string and, if there is no ':', it defaults to the empty string.
 * </p>
 * <p>
 * This has to be in the range 0 to 65535 and is only decimal (no hex or octal) but you can go nuts with leading 0's.
 * </p>
 * <p>
 * Each scheme has a default port number to be used if the port is omitted. If the default port number is specified in the input then it should not appear in the serialised form
 * </p>
 * <p>
 * Examples:
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     10                                        port = "10"
 *     0000000000000000000000000000012345        port = "12345"
 *     65536                                     invalid - number too big
 * </pre>
 *
 *
 * <h2>The path part</h2>
 *
 *
 * <p>
 * A path is right-delimited by one of '?' '#' or the end of the input.
 * </p>
 * <p>
 * All paths are considered to start with a '/'.  This is the default, if no path is specified.
 * </p>
 * <p>
 * Paths are considered to be a sequence of path segments, separated by a slash  ('/' or '\'). When a path is serialised, any '\'s are mapped to  '/'s.
 * </p>
 * <p>
 * Each segment is a list of codepoints.
 * </p>
 * <p>
 * In some ways (but not all), a path looks like a path in a Unix file system.
 * </p>
 * <p>
 * A segment can be empty. In a Unix path, empty segments are tolerated in the command line syntax but disregarded because files in a Unix file system cannot have local names that are "" - but in URLs they are considered to be valid.
 * </p>
 * <p>
 * Parsers are required to normalise a path before serialisation.
 * </p>
 * <h3>Normalisation</h3>
 *
 *
 * <p>
 * The parser considers segments from left to right.
 * </p>
 * <p>
 * A segment that is "." is removed from the path.
 * </p>
 * <p>
 * A segment that is ".." means that this segment and the previous segment is removed (if there is one). This removal will apply to segments that are empty as well as ones that are not empty.
 * </p>
 * <p>
 * Now, we must separate the discussion of the path into two cases - file scheme and the others.
 * </p>
 * <p>
 * Let's start with the other schemes.
 * </p>
 * <h3>Paths in http, https, ftp schemes</h3>
 *
 *
 * <p>
 * The path follows the port (or the host if there is no port specified) and its start delimiter is a slash ( '/' or '\'). It continues until a '?' or a '#' or the end of input.
 * </p>
 * <h3>Paths in the file scheme</h3>
 *
 *
 * <p>
 * First let's describe how the parser attempts to parse a windows drive path:
 * </p>
 * <ol>
 *
 * <li>The first codepoint must be an ASCIIi alpha codepoint, upper or lower case
 *
 * <li>If there is another codepoint, it must be either ':' or  '|'
 *
 * <li> If there is another codepoint, it must be a slash  ('/' or '\').
 *
 * <li>If there are further codepoints then these are considered to be part of the path - up to '?' '#' or the end of the input.
 * </li>
 * </ol>
 * <p>
 * No port is allowed in the file scheme.
 * </p>
 * <p>
 * If the path has been parsed as a windows drive path, if there was a '|' after the drive letter, it is mapped to ':'.
 * </p>
 * <p>
 * When parsing a standard path, there are no particular parsing rules. It is allowed to have ':' '|' codepoints but they are not treated as being special and they are not transformed before serialisation.
 * </p>
 * <p>
 * The parser looks at the slashes part and does something different depending on how many slashes there are:
 * </p>
 * <h3>file scheme - zero or one slash</h3>
 *
 *
 * <p>
 * The parser assumes that <strong><em>there is no host</em></strong> and tries to parse the rest of the input as starting with a windows drive path. This might succeed or fail. If it fails then it will attempt to parse it as a standard path.
 * </p>
 * <p>
 * Examples
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     file:a                         path = "/a"
 *     file:a:                        path = "/a:"
 *     file:                          path = "/"
 *     file::                         path = "/::"
 *     file:a:/                       path = "/a:/"
 *     file:Z:\                       path = "/Z:/"
 *     file:a:/b                      path = "/a:/b"
 *     file:a|/b                      path = "/a:/b"
 *     file:a|b                       path = "/a|b"     No mapping of | because it is not a windows drive path
 *     file:a/b                       path = "/a/b"
 *     file:/a                        path = "/a"
 * </pre>
 *
 *
 * <h3>file scheme - two slashes</h3>
 *
 *
 * <p>
 * The parser assumes that <strong><em>there might be a host</em></strong>.
 * </p>
 * <p>
 * The parser tries to parse the rest of the input as starting with a windows drive path. If this succeeds then there is no host. If it fails then there must be a host.
 * </p>
 * <p>
 *  If this fails, it will try to parse the rest of the input as starting with a host. In this case, after a host is parsed, it will attempt to parse the rest of the input as starting with a standard path.
 * </p>
 * <p>
 * Examples
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     file://a                       host = "a" path = "/'
 *     file://                        host = "" path = '/'
 *     file://a/b                     host = "a" path = "/b"
 *     file://a|/b                    host = "" path = "/a:/b"
 *     file://a/b|                    host = "a" path = "/b|"
 * </pre>
 *
 *
 * <h3>file scheme - three or more slashes</h3>
 *
 *
 * <p>
 * The parser assumes that <strong><em>there is no host</em></strong> and tries to parse the rest of the input as starting with a standard path.
 * </p>
 * <p>
 * If there are <strong><code>n</code></strong> slashes then there will be <strong><code>n - 2</code></strong> slashes at the start of the path.
 *
 * <p>
 * Examples
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     file:///a                       host = "" path = "/'
 *     file:///                        host = "" path = '/'
 *     file:////                       host = "" path = "//"
 *     file://///a|/b                  host = "" path = "///a|/b"
 * </pre>
 *
 *
 * <h2>The query part (AKA search)</h2>
 *
 *
 * <p>
 * A query is right-delimited by '#' or the end of the input.
 * </p>
 * <p>
 * Any codepoints that are not printable ASCII are UTF-8 encoded and the resulting bytes are percent encoded.
 * </p>
 * <p>
 * There is an expected syntax for items in a query - essentially this syntax represents them as key-value pairs - but the URL parser does not do any validation on this part.
 * </p>
 * <p>
 * Examples
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     a=b&amp;c=🤪                       query = "a=b&amp;c=%F0%9F%A4%AA"
 *     👋😕                           query = "%F0%9F%91%8B%F0%9F%98%95"
 * </pre>
 *
 *
 * <h2>The fragment part (AKA hash)</h2>
 *
 *
 * <p>
 * A fragment is right-delimited by the end of the input.
 * </p>
 * <p>
 * In URLs that represent HTML pages, there is an expectation that the fragment contains a string that represents an anchor element on a HTML page - but the URL parser does not do any validation of this.
 * </p>
 * <p>
 * Examples
 * </p>
 *
 *
 *
 * <pre class="prettyprint">     infrastructure                fragment = "infrastructure"
 *     👩🏼‍🦰                            fragment = "%F0%9F%91%A9%F0%9F%8F%BC%E2%80%8D%F0%9F%A6%B0"
 */
public class ImUrl extends ImValuesImpl
{

    private static final ImCodePoint atCodePoint = ImCodePoint.valueOf('@');
    /**
     *
     * <p> 3.2. Host miscellaneous
     *
     * <pre>{@code
     * A forbidden host code point is
     * U+0000 NULL, U+0009 TAB, U+000A LF, U+000D CR, U+0020 SPACE, U+0023 (#), U+002F (/), U+003A (:), U+003C (<), U+003E (>), U+003F (?),
     * U+0040 (@), U+005B ([), U+005C (\), U+005D (]), U+005E (^), or U+007C (|).
     * }}</pre>
     *
     * <p> A forbidden domain code point is a forbidden host code point, a C0 control, U+0025 (%), or U+007F DELETE.
     *
     *
     *
     * <p>So forbiddenHostCodePoints are not used directly in this code since they are  only used for non-special schemes
     */

    private static final ImSet<ImCodePoint> forbiddenHostCodePoints =
            ImCodePoint.setOn(" #/:<>?@[\\]^|").union(ImCodePoint.setOn(0x0, 0x9, 0xA, 0xD));

    private static final ImSet<ImCodePoint> c0ControlChars = makeC0ControlChars();

    private static final ImSet<ImCodePoint> forbiddenDomainCodePoints = forbiddenHostCodePoints.union(c0ControlChars.union(ImCodePoint.setOn(0x7F, 0x25)));

    private static final ImSet<ImCodePoint> forbiddenAuthCodePoints = ImList.onString("/\\?#").toImSet();

    private static final ImSet<ImCodePoint> colonPipe = ImList.onString(":|").toImSet();
    private static final ImSet<ImCodePoint> slashes = ImList.onString("/\\").toImSet();

    private static final ImSet<ImCodePoint> hostTerminators = ImList.onString("/\\?#:").toImSet();
    private static final ImSet<ImCodePoint> hostTerminators2 = ImList.onString("/\\?#:|").toImSet();
    private static final ImSet<ImCodePoint> portTerminators = ImList.onString("?#/").toImSet();
    private static final ImSet<ImCodePoint> pathTerminators = ImList.onString("?#").toImSet();
    private static final ImSet<ImCodePoint> queryTerminators = ImList.onString("#").toImSet();
    private static final ImSet<ImCodePoint> removeTheseFromPath = ImList.onString("\n\r\t").toImSet();

    // The various percent encode set "extras". Any octet that is <= 0x1F or >=0x7F (when considered as an unsigned number)
    // will be percent encoded
    // Not that these use Bytes because we will only percent-encode strings after being UTF-8 encoded

    private static final ImSet<Byte> fragmentPercentEncodeSet = asciiToByteSet(" \"<>`");
    private static final ImSet<Byte> queryPercentEncodeSet = asciiToByteSet(" \"<>#");
    private static final ImSet<Byte> specialQueryPercentEncodeSet = asciiToByteSet(" \"<>#'");
    private static final ImSet<Byte> pathPercentEncodeSet = queryPercentEncodeSet.union(asciiToByteSet("?`{}"));

    // We use this for the user-info and the password - the raw user-info can't contain a : so we don't need to have it in this set - but
    // we use it for the password encoding as well so let's just use one set
    private static final ImSet<Byte> authPercentEncodeSet = pathPercentEncodeSet.union(asciiToByteSet("/:;=@[\\]^|"));

    private static final BigInteger bigInt255 = BigInteger.valueOf(255);

    // A list of predicates to let us split a list into runs of no zeros then all zeros
    static ImList<Fn<String, Boolean>> nonZeroThenZeroPreds = ImList.join(ImList.repeat(ImList.on(i1 -> !Eq.uals(i1, "0"), i2 -> Eq.uals(i2, "0")), 5));

    // A hex number as it appears in an IPv6 address.
    private static final Pattern hexUpToFourDigitsPattern = Pattern.compile("[0-9a-fA-F]{1,4}");

    // Numerics and dots
    private static final Pattern digitsAndDots = Pattern.compile("[0-9.]+");

    /**
     * The scheme of this URL
     */
    public final String scheme;

    /**
     * The user of this URL - default is ""
     */
    public final String user;

    /**
     * The password of this URL - default is ""
     */
    public final String password;

    /**
     * The host of this URL - default is ""
     */
    public final String host;

    /**
     * The port of this URL - default is ""
     */
    public final String port;

    /**
     * The path of this URL - default is "/"
     */
    public final String path;

    /**
     * The query of this URL after parsing into key-value pairs - default is the empty list
     */
    public final ImList<ImPair<String, String>> queryPairs;

    /**
     * The query of this URL - default is ""
     */
    public final String query;

    /**
     * The fragment of this URL - default is ""
     */
    public final String fragment;

    /**
     * Create a Java URL from `this` by serialising each part and using
     *
     * <p>
     * {@link java.net.URI#URI(String, String, String, int, String, String, String)}  }
     *
     * <p>and then
     *
     * <p>
     * {@link URI#toURL()}
     *
     *
     */
    public URL toJavaUrl()
    {
        try
        {
            return new URI(scheme, user + ":" + password, host, getPortAsInteger(), path, "", "").toURL();

        } catch (URISyntaxException | MalformedURLException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    /**
     * Serialise the URL as a string
     */
    public String asString()
    {

        String authString0 = user + (password.isEmpty() ? "" : ":" + password);
        String authString = authString0.isEmpty() ? "" : authString0 + "@";

        String portString = port.isEmpty() ? "" : ":" + port;

        String queryString = query.isEmpty() ? "" : "?" + query;

        String fragmentString = fragment.isEmpty() ? "" : "#" + fragment;

        return scheme + "://" + authString + host + portString + path + queryString + fragmentString;
    }

    static enum Scheme
    {
        ftp, //   21
        file, //
        http, //  80
        https, // 443

    }

    private ImUrl(String scheme, String user, String password, String host, String port, String path, String query, ImList<ImPair<String, String>> queryPairs, String fragment)
    {
        this.scheme = scheme;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.queryPairs = queryPairs;
        this.fragment = fragment;
    }

    /**
     * Create a url object with the specified parts.
     *
     * Note that we are storing the query twice - once as a string and also as a list of key-value pairs
     */
    public static ImUrl on(String scheme, String user, String password, String host, String port, String path, String query, String fragment)
    {
        return new ImUrl(scheme, user, password, host, port, path, query, getQueryStringValues(query), fragment);
    }

    /**
     * A file URL representing `path`
     */
    public static ImUrl on(Path path)
    {
        return on("file:" + path.toAbsolutePath());
    }

    /**
     * <p> Parse
     * {@code urlString}
     *  as a URL assuming that
     * {@code urlString}
     * is valid
     * <p> Throw {@link UrlParseFailed}  if it is not valid
     */
    public static ImUrl on(String urlString)
    {
        ImEither<String, ImUrl> result = parse(urlString);

        return result.isLeft
               ? Throw.wrap(new UrlParseFailed(result.left))
               : result.right;
    }

    int getPortAsInteger()
    {
        if (!port.isEmpty())
            return Integer.valueOf(port);
        else

            switch (Scheme.valueOf(scheme))
            {
            case ftp:
                return 21;

            case http:
                return 80;

            case https:
                return 443;

            }

        return Throw.Exception.ifYouGetHere("scheme " + scheme + " does not have a default");
    }

    private static String fixPort(Scheme scheme, String port)
    {
        if (port.isEmpty())
            return port;
        else

            switch (scheme)
            {
            case ftp:
                return Eq.uals(port, "21") ? "" : port;

            case http:
                return Eq.uals(port, "80") ? "" : port;

            case https:
                return Eq.uals(port, "443") ? "" : port;

            }

        return Throw.Exception.ifYouGetHere("scheme " + scheme + " does not have a default");
    }

    /**
     * The url that is the same as this except that the query and the fragment part are both set to ""
     */
    public ImUrl withNoQueriesOrFragments()
    {
        return new ImUrl(scheme, user, password, host, port, path, "", ImList.on(), "");
    }

    /**
     * <p> The url that is the same as this except that the query is set to the serialised form of
     * {@code queryPairs}
     */
    public ImUrl withQueryPairs(ImList<ImPair<String, String>> queryPairs)
    {
        return new ImUrl(scheme, user, password, host, port, path, queryPairs.toString(""), queryPairs, "");
    }

    private static String nullToEmptyString(String s)
    {
        return s == null ? "" : s;
    }

    static ImList<ImPair<String, String>> getQueryStringValues(String query)
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
     * }}</pre>
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

    static ImSet<Byte> asciiToByteSet(String s)
    {
        ImSet<Byte> byteSet = ImSet.on();

        for (int i = 0; i < s.length(); i++)
            byteSet = byteSet.add((byte) (s.charAt(i) & 0xFF));

        return byteSet;
    }

    private static ImSet<ImCodePoint> makeC0ControlChars()
    {
        return ImRange.zeroTo(32 + 1).map(i -> ImCodePoint.on(i)).toImSet();
    }

    static ImTriple<String, String, ImList<ImCodePoint>> parseAuthority(ImList<ImCodePoint> input)
    {
        ImTriple<ImList<ImCodePoint>, ImList<ImCodePoint>, ImList<ImCodePoint>> result = parseAuthority$(input);

        return ImTriple.on(authEncode(result.e1), authEncode(result.e2), result.e3);

    }

    private static ImTriple<ImList<ImCodePoint>, ImList<ImCodePoint>, ImList<ImCodePoint>> parseAuthority$(ImList<ImCodePoint> input)
    {

        /**
         * user-names can contain any code points, suitably percent-encoded
         * They can't contain five *characters* / \ ? # :
         * They are terminated by the first : or @ - but not the first @ - the last one
         *
         * passwords can contain any code points, suitably percent-encoded
         * They can't contain four *characters* / \ ? #
         * They are terminated by @ - but not the first @ - the last one
         *
         * u@host:123/a?@  user = "u", no password
         *
         * u@host:123@/a?  user = "u@host", password = "123"
         *
         *
         *
         * Cut at the first forbidden code point
         *
         * If there is an authority, it must be in the first part. All the following searches are in the first part.
         *
         *  find the first colon
         *
         *  if there is one
         *     if there is an @ after it
         *         there is a user and password
         *         eg a@b@@:x@@@a@host/a/b/c#
         *     else
         *         find the last @ before the first colon
         *         if it exists
         *             there is a user but no password
         *             eg a@user@host:1234\a\b\d?
         *         else
         *             no user, no password
         *             eg host:1234?
         *  else
         *      find the last @
         *      if there is one
         *          user, no password
         *          eg user@blah@blah@host:1234/a/b/d/e
         *      else
         *          no user, no password
         *          eg host/a/b/c
         *
         *
         */

        // We want to split just before the first forbidden code point
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> parts = input.cutIntoTwo(c -> !forbiddenAuthCodePoints.contains(c));

        ImList<ImCodePoint> in = parts.fst;

        // Find the first colon
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> colonParts = in.cutIntoTwo(c -> !Eq.uals(c, ImCodePoint.valueOf(':')));

        // Find the last @
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> atParts = cutIntoTwoAtTheLastAtChar(in);

        if (colonParts.snd.isNotEmpty())
        {
            // There is a colon

            // Is there an @?

            if (atParts.snd.isNotEmpty())
            {

                if (atParts.fst.size() > colonParts.fst.size())
                {
                    // @ is after the colon
                    ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> atParts2 = cutIntoTwoAtTheLastAtChar(colonParts.snd.tail());
                    return ImTriple.on(colonParts.fst, atParts2.fst, atParts2.snd.tail().append(parts.snd));
                }
                else
                {
                    // @ is before the colon
                    return ImTriple.on(atParts.fst, ImList.on(), atParts.snd.tail().append(parts.snd));
                }
            }
            else
            {
                // No @
                return ImTriple.on(ImList.on(), ImList.on(), input);
            }
        }
        else
        {
            // No colon
            if (atParts.snd.isNotEmpty())
            {
                // There is an @
                return ImTriple.on(atParts.fst, ImList.on(), atParts.snd.tail().append(parts.snd));
            }
            else
            {
                // No @
                return ImTriple.on(ImList.on(), ImList.on(), input);
            }
        }

    }

    private static String authEncode(ImList<ImCodePoint> part)
    {
        return utf8EncodeAndThenPercentEncode(authPercentEncodeSet, part.toString(""));
    }

    /**
     * It all depend on whether we have any @ code points in the first part because this can be the delimiter.
     * If we do then there *is* a password/username. We end it *before* the *last* @ in the first part.
     *
     * If there are no @ code points, we return the pair on the empty list and parts concatenated
     *
     * In this case, we need to add the second part of password parts and the second element of parts
     *
     * abc@de@example.com?a=b         start with this
     *
     * abc@de@example.com   ?a=b      split before the forbidden ? - so parts = ("abc@de@example.com", "?a=b")
     *
     * abc@de   example.com   ?a=b    we find that there is a @ and split at the last one
     *
     * abc@de   example.com?a=b       join the  last part of the first part snd the second part
     *
     *
     */
    private static ImMaybe<ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>>> getPairUsingLastAt(ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> parts)
    {
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = cutIntoTwoAtTheLastAtChar(parts.fst);

        return parts.fst.contains(atCodePoint)
               ? ImMaybe.just(ImPair.on(pair.fst, pair.snd.append(parts.snd)))
               : ImMaybe.nothing;
    }

    /**
     *  Split at the **last** @ char - if there is one - otherwise return input and the empty list
     *  If there is an @ character, the second
     *
     *     [ a b @ c @ d e f ] => [ a b @ c ], [ @ d e f ]
     *     [ a b c d e ] => [ a b c d e ] []
     */
    private static ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> cutIntoTwoAtTheLastAtChar(ImList<ImCodePoint> input)
    {
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> ps = input.reverse().cutIntoTwo(c -> !Eq.uals(atCodePoint, c));

        return ps.snd.isEmpty()
               ? ImPair.on(input, ImList.on())
               : ImPair.on(ps.snd.tail().reverse(), ps.fst.reverse().push(atCodePoint));
    }

    /**
     * <p> Parse
     * {@code input}
     *  as a URL or return an error message if the input is invalid.
     * <p> See the class comments for a very long description of how the parsing works
     */
    public static ImEither<String, ImUrl> parse(String input)
    {
        /**
         *
         *
         * At any of these parse stages we could get
         *
         * A valid result (and the rest of the input that we need to parse next )
         * An error - which means that we stop processing
         */

        ImList<ImCodePoint> chars = ImList.onString(input);

        // remove tab, nl, cr
        ImList<ImCodePoint> chars2 = chars.filter(c -> !removeTheseFromPath.contains(c));

        // Ok - we are using the "poor man's do syntax" here.
        // Each parse needs to return one or two things and possibly cause the whole parse to fail
        // Having a deeply nested series of flatmaps is actually the simplest way of doing this (I think)

        /**
         * Special  Default
         * scheme	port
         * ======   =======
         * "ftp"	21
         * "file"	null
         * "http"	80
         * "https"	443
         *
         */

        // Parse the scheme
        return parseScheme(chars2).flatMap(i -> i.fst == Scheme.file
                                                ? parseFileScheme(i.snd)
                                                : parseNonFileScheme(i.snd, i.fst));
    }

    static ImEither<String, ImUrl> parsePath(Path path)
    {
        return parse("file:" + path);
    }

    /**
     * Parse `input` as a URL or return an error message
     */
    private static ImEither<String, ImUrl> parseNonFileScheme(ImList<ImCodePoint> chars, Scheme scheme)
    {
        /**
         * At any of these parse stages we could get
         *
         * A valid result (and the rest of the input that we need to parse next )
         * An error - which means that we stop processing
         */

        // Ok - we are using the "poor man's do syntax" here.
        // Each parse needs to return one or two things and possibly cause the whole parse to fail
        // Having a nested series of flatmaps is actually the simplest way of doing this (I think)

        // Get the /'s or \'s at the start of `chars` -  we just discard them
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> parts = chars.cutIntoTwo(c -> slashes.contains(c));

        // Parse the authority
        ImTriple<String, String, ImList<ImCodePoint>> authority = parseAuthority(parts.snd);

        // Parse the host
        return parseHost(authority.e3).flatMap(host -> {

            if (host.fst.isEmpty())
                return ImEither.Left("empty host");

            // Parse the port
            return parsePort(host.snd).flatMap(port -> {

                // parse the path
                return parsePath(port.snd).flatMap(m -> {

                    // parse the query
                    return parseQuery(m.snd).flatMap(query -> {

                        // When we parse the fragment - It is either empty or it isn't - no error is possible
                        // If we get to here we have parsed every part that we need and can create the URL object

                        return ImEither.Right(ImUrl.on(scheme.toString(), authority.e1, authority.e2, host.fst, fixPort(scheme, port.fst),
                                fixPath(m.fst), query.fst, parseFragment(query.snd)));
                    });
                });
            });
        });

    }

    /**
     *
     */
    private static ImEither<String, ImUrl> parseFileScheme(ImList<ImCodePoint> chars)
    {

        /**
         * Holy Fafferoni, Batman!
         *
         * For file schemes we have this weirdness:
         *
         * if there are exactly two slashes (/ or\) after the : of the scheme we assume that there might be a host
         * otherwise we assume no host.
         *
         * Essentially there are three cases. What we do next is different with some backtracking sometimes.
         *
         * We do something different here because of the backtracking that we are required to do
         *
         * We do different things based on how many slashes there are in a run after the "file:" - call this n
         *
         * if n = 0, 1, 3  eg file:A or file:/A  file:///A
         *     No host parsing
         *     remove n / chars
         *     get the path based on the path terminators - ?#
         *     parse as a path (with special set to true)
         *     if this fails
         *         parse the path (not special)
         *
         * else if n = 2   eg file://A
         *     Could be a host
         *
         *     remove n chars
         *     get the host based on the *host* terminators - `hostDelimiters2`
         *     if host terminated by a : or |
         *         if the host could be a drive letter then it is not parsed as a host
         *             get the path based on the path terminators - ?#
         *             reparse as a path (with special set to true)
         *         else
         *             error - don't bother to reparse it as a path
         * else
         *     n >=4 eg file:////A file://///A
         *
         *     remove 2 chars
         *     No host parsing
         *     get the path based on the path terminators - ?#
         *     parse as a path (with special set to true)
         *     if this fails
         *         parse the path (not special)
         */

        return parseHostAndPath(chars).flatMap(k -> {

            String host = k.fst.fst;
            String path = k.fst.snd;

            // parse the query
            return parseQuery(k.snd).flatMap(n -> {

                // When we parse the fragment - It is either empty or it isn't - no error is possible
                // If we get to here we have parsed every part that we need and can create the URL object
                return ImEither.Right(ImUrl.on("file", "", "", fixLocalHost(host), "", fixPath(path), n.fst, parseFragment(n.snd)));

            });
        });

    }

    static String fixPath(String path)
    {
        //        i -> Pattern.compile("%2e").matcher(this).replaceAll(replacement)
        String p1 = path.replace('\\', '/');

        // deal with . and ..
        String p3 = removeRelatives(ImList.onString(p1));

        return utf8EncodeAndThenPercentEncode(pathPercentEncodeSet, p3);
    }

    protected static String removeRelatives(ImList<ImCodePoint> input)
    {
        // Split at "/"
        ImList<String> r0 = ParseUtils.split('/', input.toString(""));

        ImList<String> r1 = r0.map(i -> {
            switch (i.toLowerCase().replaceAll("%2e", "."))
            {
            case ".":
                return ".";
            case "..":
                return "..";
            default:
                return i;

            }
        });

        if (r1.isEmpty())
        {
            return "/";
        }
        else
        {
            // Remove the "/" at the start, if there is one
            ImList<String> r2 = r1.head().isEmpty()
                                ? r1.tail()
                                : r1;

            if (r2.isEmpty())
            {
                return "/";
            }
            else
            {

                // Frig city
                // If the last item is ".." or "." then we add "" at the end
                // This is to allow for these required mappings
                //
                // /p    -> /p
                // /p/   -> /p/
                // /p/.  -> /p/
                // /p/./  -> /p/
                ImList<String> rest = Eq.uals(".", r2.last()) || Eq.uals("..", r2.last())
                                      ? r2.appendElement("")
                                      : r2;

                return TextUtils.join(removeRelatives(ImList.empty(), rest), "/", "/", "");
            }
        }
    }

    private static ImList<String> removeRelatives(ImList<String> first, ImList<String> rest)
    {
        if (rest.isEmpty())
            return first.reverse();
        else if (Eq.uals(".", rest.head()))
            return removeRelatives(first, rest.tail());
        else if (Eq.uals("..", rest.head()))
            return removeRelatives(safeTail(first), rest.tail());
        else
            return removeRelatives(first.push(rest.head()), rest.tail());
    }

    private static ImList<String> safeTail(ImList<String> xs)
    {
        return xs.isEmpty()
               ? xs
               : xs.tail();
    }

    /**
     *
     * for example
     *
     *     a:/b
     *     //a.b.c/def?abc
     */
    private static ImEither<String, ImPair<ImPair<String, String>, ImList<ImCodePoint>>> parseHostAndPath(ImList<ImCodePoint> input)
    {
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> p2 = input.cutIntoTwo(c -> slashes.contains(c));

        int slashCount = p2.fst.size();

        // ////a:\b

        if (slashCount == 2)
        {
            // file://a:\

            ImList<ImCodePoint> inputWithoutSlashes = input.drop(2);

            // So we either have:
            // 1. no host and a windows drive
            // 2. a host and then a simple path

            if (couldBeWindowsDrive(inputWithoutSlashes))
            {
                ImMaybe<String> pathE = parseWindowsDrivePath(inputWithoutSlashes);

                return pathE.isPresent()
                       ? ImEither.Right(ImPair.on(ImPair.on("", pathE.get()), inputWithoutSlashes.drop(pathE.get().length())))
                       : ImEither.Left(inputWithoutSlashes.toString(""));
            }
            else
            {
                // Parse host, then simple path
                ImEither<String, ImPair<String, ImList<ImCodePoint>>> hostE = parseHost(inputWithoutSlashes);

                if (hostE.isLeft)
                {
                    return ImEither.Left(inputWithoutSlashes.toString(""));
                }
                else
                {
                    ImPair<String, ImList<ImCodePoint>> hostPair = hostE.right;

                    // COLON is normally a valid terminator, so we might have parsed a host with ':' as the terminator - but that would be wrong
                    if (hostPair.snd.startsWithElement(COLON))
                        return ImEither.Left(inputWithoutSlashes.toString(""));
                    else
                    {
                        // Parse the path
                        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pathPair = hostPair.snd.cutIntoTwo(c -> !pathTerminators.contains(c));

                        return ImEither.Right(ImPair.on(ImPair.on(hostPair.fst, pathPair.fst.toString("")), pathPair.snd));
                    }
                }
            }

        }
        else if (slashCount <= 3)
        {
            // slashCount = 0, 1, 3  eg file:A or file:/A  file:///A

            /**
             *     No host parsing
             *     get the path based on the path terminators - ?#
             *
             *
             */

            ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pathPair = input.drop(slashCount).cutIntoTwo(c -> !pathTerminators.contains(c));

            // Try parsing as a windows drive (this involves mapping | to : )
            String path = parseWindowsDrivePath(pathPair.fst).ifPresentElse(
                    right -> right,
                    pathPair.fst.toString("")
            );

            return ImEither.Right(ImPair.on(ImPair.on("", path), pathPair.snd));

        }
        else
        {
            // if slashCount >= 4  eg file:A or file:/A  file:///A

            /**
             *     No host parsing
             *     get the path based on the path terminators - ?#
             *         parse the path (not special)
             */

            ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pathPair = input.drop(slashCount).cutIntoTwo(c -> !pathTerminators.contains(c));

            // We need to add some slashes back in

            String addTheseSlashes = "/".repeat(slashCount - 2);

            return ImEither.Right(ImPair.on(ImPair.on("", addTheseSlashes + pathPair.fst.toString("")), pathPair.snd));

        }

    }

    private static boolean couldBeWindowsDrive(ImList<ImCodePoint> input)
    {
        return input.size() >= 2 && startsWithOneOf(alpha, input) && startsWithOneOf(colonPipe, input.tail());
    }

    private static boolean startsWithWindowsColonOrPipe(ImList<ImCodePoint> input)
    {
        return input.isNotEmpty() && colonPipe.contains(input.head());
    }

    private static String fixLocalHost(String host)
    {
        return Eq.uals(host, "localhost")
               ? ""
               : host;
    }

    private static boolean startsWithOneOf(ImSet<ImCodePoint> set, ImList<ImCodePoint> list)
    {
        return list.isNotEmpty() && set.contains(list.head());
    }

    /**
     *
     * for example
     *
     * file://////ab.c   -> "file", "//////ab.c"
     * file:a:           -> "file", "a:"
     */
    private static ImEither<String, ImPair<Scheme, ImList<ImCodePoint>>> parseScheme(ImList<ImCodePoint> input)
    {
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = input.cutIntoTwo(c -> !Eq.uals(ImCodePoint.valueOf(':'), c));

        try
        {
            return pair.snd.isEmpty()
                   ? ImEither.Left(pair.fst.toString(""))
                   : ImEither.Right(ImPair.on(Scheme.valueOf(pair.fst.toString("").toLowerCase()), pair.snd.tail()));
        } catch (IllegalArgumentException e)
        {
            return ImEither.Left(pair.fst.toString(""));
        }
    }

    private static ImEither<String, ImPair<String, ImList<ImCodePoint>>> parsePath(ImList<ImCodePoint> input)
    {
        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = input.cutIntoTwo(c -> !pathTerminators.contains(c));
        return ImEither.Right(ImPair.on(pair.fst.toString(""), pair.snd));
    }

    /**
     *
     * for example
     *
     *     a|\/\/   -> a:////
     */
    private static ImMaybe<String> parseWindowsDrivePath(ImList<ImCodePoint> input)
    {
        if (couldBeWindowsDrive((input)))
        {
            ImList<ImCodePoint> rest = input.drop(2);

            // We have (eg)
            // A:
            // A:/lkdjflkjl
            // A:////////
            // A|\/\/
            // A:b      ERROR
            // A|b      ERROR
            return startsWithOneOf(slashes, rest)
                   ? ImMaybe.just(rest.push(ImCodePoint.valueOf(':')).push(input.head()).toString(""))
                   : ImMaybe.nothing;
        }
        else
        {
            return ImMaybe.nothing;
        }
    }

    //    private static boolean isWindowsDriveLetter(ImList<ImCodePt> input)
    //    {
    //        return input.size() == 1 && ImCodePt.alpha().contains(input.head());
    //    }

    private static ImEither<String, ImPair<String, ImList<ImCodePoint>>> parsePort(ImList<ImCodePoint> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), ImCodePoint.valueOf(':')))
        {
            // eg "http://a.b?a=b"
            return ImEither.Right(ImPair.on("", input));
        }
        else
        {
            // The first char is :
            // we consider the tail of input
            ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = input.tail().cutIntoTwo(c -> !portTerminators.contains(c));

            if (pair.fst.isEmpty())
            {
                // There is no port
                // eg "http://a:" or "http://a:?aaa" or "http://a:#aaa"
                return ImEither.Right(ImPair.on("", pair.snd));
            }
            else
            {
                String s = pair.fst.toString("");

                try
                {
                    // Check we have just digits
                    if (IPv4Segments.numericPattern.matcher(s).matches())
                    {
                        // The number must not be too large
                        int p = Integer.parseInt(s);

                        return p <= 0xFFFF // 65535
                               ? ImEither.Right(ImPair.on("" + p, pair.snd))
                               : ImEither.Left(s);
                    }
                    else
                    {
                        return ImEither.Left(s);
                    }
                } catch (NumberFormatException e)
                {
                    return ImEither.Left(s);
                }
            }
        }
    }

    private static ImEither<String, ImPair<String, ImList<ImCodePoint>>> parseQuery(ImList<ImCodePoint> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), ImCodePoint.valueOf('?')))
        {
            // No query
            // eg "http://a.b:123/#aaa"
            return ImEither.Right(ImPair.on("", input));
        }
        else
        {
            // first char is ?
            ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = input.tail().cutIntoTwo(c -> !queryTerminators.contains(c));

            String p = utf8EncodeAndThenPercentEncode(specialQueryPercentEncodeSet, pair.fst.toString(""));

            return ImEither.Right(ImPair.on(p, pair.snd));
        }
    }

    private static String parseFragment(ImList<ImCodePoint> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), ImCodePoint.valueOf('#')))
        {
            // No fragment
            // eg "http://a.b:123/?aaa"
            return "";
        }
        else
        {
            // first char is #
            return utf8EncodeAndThenPercentEncode(fragmentPercentEncodeSet, input.tail().toString(""));
        }
    }

    static ImEither<String, ImPair<String, ImList<ImCodePoint>>> parseHost(ImList<ImCodePoint> input)
    {

        // First, just get the host string out of input
        ImMaybe<ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>>> pairMaybe = parseHost$(input);

        if (!pairMaybe.isPresent())
        {
            return ImEither.Left("IPV6 host error - " + input);
        }

        ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> pair = pairMaybe.get();

        ImList<ImCodePoint> hostChars = pair.fst;

        // If it is empty we return it
        if (hostChars.isEmpty())
            return ImEither.Right(ImPair.on("", pair.snd));
        else
        {

            //
            if (Eq.uals(ImCodePoint.valueOf('['), hostChars.head()))
                return validateIpv6Address(hostChars.tail()).match(
                        left -> ImEither.Left("Invalid IPv6 - " + left),
                        right -> ImEither.Right(ImPair.on(right, pair.snd))
                );
            else
            {

                // Try to convert it to a list of numbers if it might be an IPv4 address
                ImOneOfThree<String, String, String> res = convertHost(hostChars);

                // This might succeed, fail because the host is a domain name or fail because it is an invalid IPv4 address and can't be a domain name

                if (res.type == ImOneOfThree.ThreeType.A)
                {
                    return ImEither.Right(ImPair.on(res.a(), pair.snd));
                }
                else if (res.type == ImOneOfThree.ThreeType.C)
                {
                    return ImEither.Left(res.c());
                }
                else
                {
                    return ImEither.Right(ImPair.on(res.b(), pair.snd));
                }

            }
        }

    }

    private static ImEither<String, String> validateIpv6Address(ImList<ImCodePoint> input)
    {
        return validateIpv6Address(input.toString(""));
    }

    private static ImEither<String, String> validateIpv6Address(String s)
    {
        ImList<String> ns = ParseUtils.split(':', s);

        int emptyCount = ParseUtils.split(':', "X" + s + "X").filter(i -> i.isEmpty()).size();

        if (ns.isEmpty())
        {
            return ImEither.Left(s + " - empty IPv6 address");
        }
        else if (ns.size() <= 2)
        {
            return ImEither.Left(s + " - size problem");
        }
        else if (ns.at(1).isEmpty() && !ns.at(2).isEmpty())
        {
            return ImEither.Left(s + " - single : at start of IPv6 address");
        }
        else if (ns.last().isEmpty() && !ns.at(ns.size() - 1).isEmpty())
        {
            return ImEither.Left(s + " - single : at end of IPv6 address");
        }
        else if (Eq.uals("", ns.last()))
        {
            return ImEither.Left(s + " - : at end of IPv6 address");
        }
        else if (digitsAndDots.matcher(ns.last()).matches() && ParseUtils.split('.', ns.last()).size() == 4)
        {
            // So the last part is just dots and decimal digits with 4 parts if we consider the dots as separators.
            // So each part is a string of digits. The separator is '.'
            // It might be an IPV4 address - one that might be allowed inside an ipv6 address
            //
            // But it might be this: which is not allowed
            // 12345.00000077..999999

            ImList<String> split = ParseUtils.split('.', ns.last());

            // empty parts are not allowed

            if (split.any(i -> i.isEmpty()))
                return ImEither.Left(ns.last() + " - empty part");

            // leading zeros are not allowed
            if (split.any(i -> !Eq.uals(i, TextUtils.stripLeadingZeros(i))))
                return ImEither.Left(ns.last() + " - leading zeros");

            // Convert each part to BigInteger
            ImList<BigInteger> ints = split.map(i -> new BigInteger(i));

            // Check the magnitude of each number
            if (ints.any(i -> i.compareTo(bigInt255) > 0))
                return ImEither.Left(ns.last() + " - numbers > 255");

            // If we get here, then we have a valid Ipv4 address

            return validateIpv6Address(combineV6AndV4(ns, ints));

            //            return result.match(
            //                    a -> validateIpv6Address(combineV6AndV4(ns, a)), // it was a valid IPv4 address
            //                    b -> ImEither.Left(b + " - invalid hex number"), // It was not an ipv4 address
            //                    c -> ImEither.Left(c + " - invalid hex number") // It was not an ipv4 address
            //            );

        }
        else if (emptyCount > 1 || (emptyCount == 1 && ns.size() > 8) || (emptyCount == 0 && ns.size() != 8))
        {
            return ImEither.Left(s + " - size problem");
        }
        else if (!ns.all(i -> i.isEmpty() || hexUpToFourDigitsPattern.matcher(i).matches()))
        {
            return ImEither.Left(s + " - more than 4 digits or invalid hex number");
        }
        else
        {
            ImList<String> ns2 = ns.map(i -> TextUtils.stripLeadingZeros(i));

            // expand, then compress
            String ss = compressV6(expandV6(ns2)).toString(":");

            return ImEither.Right(TextUtils.quote("[", ss.toLowerCase(), "]"));
        }
    }

    private static String combineV6AndV4(ImList<String> sixes, ImList<BigInteger> fours)
    {
        return sixes.reverse().tail()
                .push(Integer.toHexString(fours.at(1).intValue() * 256 + fours.at(2).intValue()))
                .push(Integer.toHexString(fours.at(3).intValue() * 256 + fours.at(4).intValue())).reverse().toString(":");
    }

    static ImOneOfThree<String, String, String> convertHost(ImList<ImCodePoint> input)
    {
        return convertHost$(input).match(
                v4 -> ImOneOfThree.a(v4.toString('.')),
                name -> ImOneOfThree.b(name),
                error -> ImOneOfThree.c(error)
        );
    }

    /**
     *
     * return IPv4 address as a list of Big Integer | domain name | error message
     */
    private static ImOneOfThree<ImList<BigInteger>, String, String> convertHost$(ImList<ImCodePoint> input)
    {
        // Convert to a string
        String s = input.toString("");

        // Convert all percent encoded strings to UnicodeChars by treating any non ascii percent encoded chars as being UTF-8 encoding
        ImList<ImCodePoint> unicode = ImCodePoint.decodePercents(input);

        // Now we have actual Unicode ( albeit in a Java String - which is UTF16 )

        ImOneOfThree<ImList<BigInteger>, String, String> result = parseAsIPv4(unicode.toString(""));

        return result.match(
                v4Address -> result,
                name -> idnaToASCII(name),
                error -> result
        );

    }

    private static ImOneOfThree<ImList<BigInteger>, String, String> idnaToASCII(String name)
    {
        // This is 3.5 Host Processing in the WhatWG spec

        // We are using icu4j because this seems to be the most authoratative library
        // 3.5 says this:
        //    "Let asciiDomain be the result of running domain to ASCII with domain and false."
        //
        // Weeell - the terms in section 3.5 of the spec don't quite match what IDNA provides, so we are guessing about the options
        StringBuilder sb = new StringBuilder();

        IDNA uts46Instance = IDNA.getUTS46Instance(IDNA.CHECK_BIDI | IDNA.CHECK_CONTEXTJ | IDNA.NONTRANSITIONAL_TO_ASCII | IDNA.NONTRANSITIONAL_TO_UNICODE);

        IDNA.Info info = new IDNA.Info();

        // Run in non strict mode - don't set IDNA.USE_STD3_RULES
        uts46Instance.nameToASCII(name, sb, info);

        String asciiDomain = sb.toString();

        // Sometimes, because nameToASCII automatically removes some characters, the result can be empty
        if (asciiDomain.isEmpty())
            return ImOneOfThree.c(name);
        else
        {
            String errorString = String.valueOf(info.getErrors());

            // We ignore the label too long and empty label errors since it appears that What WG does not enforce these
            if (info.getErrors().isEmpty() || errorString.equals("[LABEL_TOO_LONG]") || errorString.equals("[EMPTY_LABEL]"))
            {
                // The spec does not want any forbidden host characters to appear in the processed domain
                // Even if a forbidden code point in the raw host string was percent encoded, because we have decoded the string before calling this they will be exposed now
                // If there are no errors then we need to check for forbidden *domain* code points
                ImMaybe<ImCodePoint> forbiddenMaybe = ImList.onString(asciiDomain).find(i -> forbiddenDomainCodePoints.contains(i));

                // If we find any then this is an error
                return forbiddenMaybe.isPresent()
                       ? ImOneOfThree.c(forbiddenMaybe.get().toString())
                       : ImOneOfThree.b(asciiDomain);

            }
            else
                return ImOneOfThree.c(errorString);
        }
    }

    private static ImMaybe<ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>>> parseHost$(ImList<ImCodePoint> input)
    {
        ImList<ImCodePoint> square = ImList.onString("[");

        // So, : is a host delimiter but it appears in an IPV6 address so we must do something special if we find a [ at the start
        if (input.startsWith(square))
        {
            ImPair<ImList<ImCodePoint>, ImList<ImCodePoint>> parts = input.cutIntoTwo(c -> !Eq.uals(c, ImCodePoint.valueOf(']')));

            // If no ] is present then this is an error
            return parts.snd.isEmpty()
                   ? ImMaybe.nothing
                   : ImMaybe.just(ImPair.on(parts.fst, parts.snd.tail()));

        }
        else
        {
            // Cut input into two parts based on the first host delimiter that we find
            return ImMaybe.just(input.cutIntoTwo(c -> !hostTerminators.contains(c)));
        }

    }

    static String percentEncodeByte(byte i)
    {
        return (String.format("%%%02X", i));
    }

    /**
     * We assume that we are dealing with an input that has already been UTF-8 encoded.
     *
     * We assume that the C0 controls - ie [0, 1F] and the UnicodeChars  in [7F, FF] are already in every set to be encoded
     *
     * This is what is stated in the WhatWg spec in 1.3. Percent-encoded bytes
     *
     */
    private static String percentEncodeBytes(ImSet<Byte> encodeSet, byte[] utf8EncodedInput)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < utf8EncodedInput.length; i++)
        {
            byte b = utf8EncodedInput[i];

            int bi = b & 0xFF;

            // If the byte is a C0 control or has the first bit set
            if (bi <= 0x1F || bi >= 0x7F || encodeSet.contains(b))
                sb.append(percentEncodeByte(b));
            else
                sb.append((char) bi);
        }

        return sb.toString();
    }

    static String utf8EncodeAndThenPercentEncode(ImSet<Byte> encodeSet, String input)
    {
        return percentEncodeBytes(encodeSet, input.getBytes());
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(scheme, user, password, host, port, path, queryPairs, fragment);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("scheme", "user", "password", "host", "port", "path", "queryPairs", "fragment");
    }

    static class IPv4Segments
    {
        private static final Pattern octalPattern = Pattern.compile("0[0-7]*");
        private static final Pattern decimalPattern = Pattern.compile("[1-9][0-9]*");
        private static final Pattern hexPattern = Pattern.compile("0[xX][0-9a-fA-F]*");
        private static final Pattern numericPattern = Pattern.compile("[0-9]+");

        private final ImList<String> parts;
        private final ImList<ImEither<String, BigInteger>> partsAsType;
        private final ImList<ImEither<String, BigInteger>> effectivePartsAsTypes;

        IPv4Segments(ImList<String> parts, ImList<ImEither<String, BigInteger>> partsAsType, ImList<ImEither<String, BigInteger>> effectivePartsAsTypes)
        {
            this.parts = parts;
            this.partsAsType = partsAsType;
            this.effectivePartsAsTypes = effectivePartsAsTypes;
        }

        boolean effectiveLastIsNumeric()
        {
            return effectivePartsAsTypes.isNotEmpty() && isNumeric(effectivePartsAsTypes.last());
        }

        private boolean isNumeric(ImEither<String, BigInteger> part)
        {
            return !part.isLeft || numericPattern.matcher(part.left).matches();
        }

        static IPv4Segments on(String input)
        {

            String input2 = input.replaceAll("[｡。．]", ".");

            ImList<String> ps = ParseUtils.split('.', input2);

            ImList<ImEither<String, BigInteger>> partsAsTypes = ps.map(i -> getNumberOrString(i));

            return new IPv4Segments(ps, partsAsTypes, calculateEffectiveParts(partsAsTypes));

        }

        private static ImList<ImEither<String, BigInteger>> calculateEffectiveParts(ImList<ImEither<String, BigInteger>> partsAsTypes)
        {
            if (partsAsTypes.isEmpty())
                return partsAsTypes;
            else
            {
                ImList<ImEither<String, BigInteger>> reverse = partsAsTypes.reverse();

                ImEither<String, BigInteger> last = reverse.at(1);

                return last.isLeft && last.left.isEmpty()
                       ? reverse.tail().reverse()
                       : partsAsTypes;
            }
        }

        /**
         * Either a String - or an Integer.
         * It will be an Integer iff it can be interpreted as a positive integer base 8, 10, or 16
         */
        static ImEither<String, BigInteger> getNumberOrString(String part)
        {
            try
            {
                if (octalPattern.matcher(part).matches())
                {
                    return ImEither.Right(new BigInteger(part, 8));
                }
                else if (decimalPattern.matcher(part).matches())
                {
                    return ImEither.Right(new BigInteger(part, 10));
                }
                else if (hexPattern.matcher(part).matches())
                {
                    // Special case for 0x or 0X
                    String ss2 = part.substring(2);

                    return ss2.isEmpty()
                           ? ImEither.Right(BigInteger.ZERO)
                           : ImEither.Right(new BigInteger(ss2, 16));
                }
                else
                    return ImEither.Left(part);

            } catch (NumberFormatException e)
            {
                return ImEither.Left(part);
            }
        }
    }

    /**
     * Try to parse `input` as an IPv4 address
     * and return a `ImOneOfThree` that represents
     *
     *      IPv4 | domain-name | error
     */
    static ImOneOfThree<ImList<BigInteger>, String, String> parseAsIPv4(String input)
    {
        // Split the input into segments at the four types of dot
        IPv4Segments segments = IPv4Segments.on(input);

        ImList<ImEither<String, BigInteger>> eff = segments.effectivePartsAsTypes;

        // If size is >= 1 and <= 4 and all the parts are numbers then it might be IPv4
        if (eff.size() >= 1 && eff.size() <= 4 && eff.all(i -> !i.isLeft))
        {
            // All the segments are numbers
            ImList<BigInteger> numbers = eff.map(i -> i.right);

            // If the last number is > 255 then we get the "digits" (0-255) from it using radix 256 and use these to expand the address
            // to 4 parts. This could go wrong of course - in which case the address is invalid.

            // We need all the numbers before the last one to be <= 255

            ImList<BigInteger> previousNumbers = numbers.removeAt(numbers.size());
            boolean previousNumbersAreOk = previousNumbers.all(n -> n.compareTo(BigInteger.valueOf(255)) <= 0);

            if (!previousNumbersAreOk)
            {
                return ImOneOfThree.c(input);
            }
            else
            {
                // get the digits modulo 256 from the last number
                ImList<BigInteger> digits = Sums.convertToDigitsUsingRadix(BigInteger.valueOf(256), numbers.at(numbers.size()));

                // if size - 1 + digits.size > 4 then error
                if (previousNumbers.size() + digits.size() > 4)
                {
                    return ImOneOfThree.c(input);
                }
                else
                {
                    // otherwise get  (4 - (size - 1) - digits.size) zeros and add digits
                    ImList<BigInteger> zeros = ImList.repeat(BigInteger.ZERO, 4 - previousNumbers.size() - digits.size());
                    return ImOneOfThree.a(previousNumbers.append(zeros).append(digits));
                }
            }
        }
        else
        {
            // Some of the segments are not numbers or there are more than 4 segments

            // If the effective last part is a number then this is an error
            return segments.effectiveLastIsNumeric()
                   ? ImOneOfThree.c("last segment is numeric - " + input)
                   : ImOneOfThree.b(input);
        }
    }

    static ImList<String> compressV6(ImList<String> ns)
    {
        ImList<ImList<String>> parts = getRunsOfZeros(ns);

        //        say("parts", parts);

        // extract the lengths of all the zero runs that are >= 2

        ImList<Integer> sizes = parts.filter(i -> Eq.uals(i.head(), "0") && i.size() >= 2).map(i -> i.size());

        //        say("sizes", sizes);

        if (sizes.isEmpty())
            return ns;
        else
        {
            // Sort descending to find the maximum length

            ImList<Integer> sortedSizes = sizes.sort((i, j) -> j - i);
            //            say("sizes sorted", sortedSizes);

            int max = sortedSizes.head();

            // Go through the parts concatenating them - when we find a maximally sized zero run, compress it and then
            // concatenate the rest
            ImPair<ImList<ImList<String>>, ImList<ImList<String>>> ps = parts.cutIntoTwo(i -> !(Eq.uals(i.head(), "0") && i.size() == max));

            //            say("ps", ps);

            return ImList.join(ImList.join(ps.fst), ImList.on(""), ImList.join(ps.snd.tail()));
        }

    }

    private static ImList<ImList<String>> getRunsOfZeros(ImList<String> ns)
    {
        // split the list into runs of zeros and runs of non-zeros - with no empty lists
        return ns.cutIntoParts(nonZeroThenZeroPreds).filter(i -> i.isNotEmpty());
    }

    private static ImList<String> expandV6(ImList<String> compressed)
    {
        ImPair<ImList<String>, ImList<String>> parts = compressed.cutIntoTwo(i -> !Eq.uals(i, ""));

        return parts.snd.isEmpty()
               ? compressed
               : ImList.join(parts.fst, ImList.repeat("0", 8 - (compressed.size() - 1)), parts.snd.tail());
    }

    private static boolean isARunOfZeroes(ImList<String> i)
    {
        return i.size() >= 2 && Eq.uals(i.head(), "0");
    }

}
