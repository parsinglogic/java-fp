package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImSet;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.ImTriple;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImMaybe;
import dev.javafp.util.ImOneOfThree;
import dev.javafp.util.ParseUtils;
import dev.javafp.util.Sums;
import dev.javafp.val.ImValuesImpl;

import java.math.BigInteger;
import java.net.IDN;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class NewImUrl extends ImValuesImpl
{

    /**
     * 3.2. Host miscellaneous
     * A forbidden host code point is U+0000 NULL, U+0009 TAB, U+000A LF, U+000D CR, U+0020 SPACE, U+0023 (#), U+002F (/), U+003A (:), U+003C (<), U+003E (>), U+003F (?),
     * U+0040 (@), U+005B ([), U+005C (\), U+005D (]), U+005E (^), or U+007C (|).
     *
     * A forbidden domain code point is a forbidden host code point, a C0 control, U+0025 (%), or U+007F DELETE.
     *
     */
    static ImSet<Character> forbiddenHostChars = ImList.onString(" #/:<>?@[\\]^|").toImSet().add((char) 0).add((char) 9).add((char) 0xA).add((char) 0xD);
    static ImSet<Character> c0ControlChars = makeC0ControlChars();
    static ImSet<Character> extraForbiddenDomainChars = c0ControlChars.add('%').add((char) 127);
    static ImSet<Character> forbiddenDomainChars = forbiddenHostChars.union(c0ControlChars.union(extraForbiddenDomainChars));
    static ImSet<Character> allowedDomainLowUnicodeChars = ImList.onString("~}{`_=;.-,+)('$!").toImSet();
    static ImSet<Character> forbiddenAuthChars = ImList.onString("/\\?#").toImSet();
    static ImSet<Character> forbiddenUserNameChars = ImList.onString("/\\?#:@").toImSet();

    static ImSet<Character> hostDelimiters = ImList.onString("/\\?#:").toImSet();
    static ImSet<Character> portDelimiters = ImList.onString("?#/").toImSet();
    static ImSet<Character> pathDelimiters = ImList.onString("?#").toImSet();
    static ImSet<Character> queryDelimiters = ImList.onString("#").toImSet();

    // The various percent encode sets
    static ImSet<Character> fragmentPercentEncodeSet = ImList.onString(" \"<>`").toImSet();
    static ImSet<Character> queryPercentEncodeSet = ImList.onString(" \"<>#").toImSet();
    static ImSet<Character> specialQueryPercentEncodeSet = queryPercentEncodeSet.add('\'');
    static ImSet<Character> pathPercentEncodeSet = queryPercentEncodeSet.add('?').add('`').add('{').add('}');
    static ImSet<Character> userInfoPercentEncodeSet = pathPercentEncodeSet.union(ImList.onString("/:;=@[\\]^|").toImSet());

    public final String scheme;
    public final String user;
    public final String password;
    public final String host;
    public final String port;
    public final String path;
    public final String query;
    public final String fragment;

    public NewImUrl(String scheme, String user, String password, String host, String port, String path, String query, String fragment)
    {
        this.scheme = scheme;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.fragment = fragment;
    }

    private static ImSet<Character> makeC0ControlChars()
    {
        ImSet<Character> set = ImSet.on();

        for (char c = 0; c < 32; c++)
            set.add(Character.valueOf(c));

        return set;
    }

    //    public static ImEither<String, ImTriple<ImList<Character>, ImList<Character>, ImList<Character>>> parseAuthority(ImList<Character> input)
    //    {
    //        // This parse can't "fail".
    //        return ImEither.Right(parseAuthority$(input));
    //    }

    public static ImTriple<String, String, ImList<Character>> parseAuthority(ImList<Character> input)
    {
        // try to read user

        ImPair<ImList<Character>, ImList<Character>> parts = input.cutIntoTwo(c -> !forbiddenUserNameChars.contains(c));

        // reached the end - There can't be an authority because there is no host - so set both username and password to empty and return the original input

        if (parts.snd.isEmpty())
        {
            return ImTriple.on("", "", input);
        }
        else if (forbiddenAuthChars.contains(parts.snd.head()))
        {
            return ImTriple.on("", "", input);
        }
        else if (Eq.uals(Character.valueOf('@'), parts.snd.head()))
        {
            // No : - so no password, host comes next
            return ImTriple.on(userInfoEncode(parts.fst), "", parts.snd.tail());
        }
        else if (Eq.uals(Character.valueOf(':'), parts.snd.head()))
        {
            // password comes next

            ImMaybe<ImPair<ImList<Character>, ImList<Character>>> pwPairMaybe = parsePassword(parts.snd.tail());

            return pwPairMaybe.ifPresentElse(
                    pwPair -> ImTriple.on(userInfoEncode(parts.fst), userInfoEncode(pwPair.fst), pwPair.snd),
                    ImTriple.on("", "", input));
        }
        else
            return Throw.Exception.ifYouGetHere();

    }

    private static String userInfoEncode(ImList<Character> part)
    {
        return utf8EncodeAndThenPercentEncode(userInfoPercentEncodeSet, part.toString(""));
    }

    private static ImMaybe<ImPair<ImList<Character>, ImList<Character>>> parsePassword(ImList<Character> input)
    {
        // try to read password by cutting at a forbidden char

        ImPair<ImList<Character>, ImList<Character>> parts = input.cutIntoTwo(c -> !forbiddenAuthChars.contains(c));

        /**
         * Now it all depend on whether we have any @ characters in the first part.
         * If we do then there *is* a password. We end it *before* the last @
         *
         * In this case, we need to add the second part of password parts and the second element of parts
         *
         * abc@de@example.com?a=b         start with this
         *
         * abc@de@example.com   ?a=b      split before the forbidden ?
         *
         * abc@de   example.com   ?a=b    we find that there is a @ and split at the last one
         *
         * abc@de   example.com?a=b       join the two last parts
         *
         *
         * Otherwise, there is no password. We assume no authority either since we might have
         *
         *     [1:2:3:4:5:6:7:8]
         *
         * which we will parse first as a user name of
         *
         *     [1
         *
         * and we then won't find a valid password delimited by a @ so we need to backtrack and try parsing it as a host
         */

        ImPair<ImList<Character>, ImList<Character>> passwordParts = cutIntoTwoAtLastAtChar(parts.fst);

        return parts.fst.contains(Character.valueOf('@'))
               ? ImMaybe.just(ImPair.on(passwordParts.fst, passwordParts.snd.append(parts.snd)))
               : ImMaybe.nothing;
    }

    /**
     *  Split at the **last** @ char - if there is one
     *
     *     [ a b @ c @ d e f ] => [ a b @ c ], [ d e f ]
     *     [ a b c d e ] => [], [ a b c d e ]
     */
    private static ImPair<ImList<Character>, ImList<Character>> cutIntoTwoAtLastAtChar(ImList<Character> input)
    {
        ImPair<ImList<Character>, ImList<Character>> ps = input.reverse().cutIntoTwo(c -> !Eq.uals(Character.valueOf('@'), c));

        return ps.snd.isEmpty()
               ? ImPair.on(ImList.on(), ps.fst.reverse())
               : ImPair.on(ps.snd.tail().reverse(), ps.fst.reverse());

    }

    /**
     * Parse `input` as a URL or return an error message
     */
    public static ImEither<String, NewImUrl> parse(String input)
    {
        /**
         * At any of these parse stages we could get
         *
         * A valid result (and the rest of the input that we need to parse next )
         * An error - which means that we stop processing
         */

        ImList<Character> chars = ImList.onString(input);

        // Ok - we are using the "poor man's do syntax" here.
        // Each parse needs to return one or two things and possibly cause the whole parse to fail
        // Having a deeply nested series of flatmaps is actually the simplest way of doing this (I think)

        // @formatter:off
        // Parse the scheme

        return
        parseScheme(input).flatMap(i -> {

        String scheme = i.fst;

        // Parse the authority
        ImTriple<String, String, ImList<Character>> a = parseAuthority(i.snd);
        String user = a.e1;
        String password = a.e2;

        // Parse the host
        return parseHost(a.e3).flatMap(k -> {
        String host = k.fst;

        // Parse the port
        return parsePort(k.snd).flatMap(l -> {
        String port = l.fst;

        // parse the path
        return parsePath(l.snd).flatMap(m -> {

        String path = m.fst;

        // parse the query
        return parseQuery(m.snd).flatMap(n -> {
        String query = n.fst;

        // parse the fragment - It is either empty or it isn't - no error is possible
        String fragment = parseFragment(n.snd);

        // If we get to here we have parsed every part that we need and can create the URL object

        return ImEither.Right( NewImUrl.on(scheme, user, password, host, port, path, query, fragment));

        });});});});});

        // @formatter:on

    }

    /**
     *
     */
    private static NewImUrl on(String scheme, String user, String password, String host, String port, String path, String query, String fragment)
    {
        // We do need to do a little more processing - on the path and query

        return new NewImUrl(scheme, user, password, host, port, path, query, fragment);
    }

    public static ImEither<String, ImPair<String, ImList<Character>>> parseScheme(String input)
    {
        ImPair<ImList<Character>, ImList<Character>> pair = ImList.onString(input).cutIntoTwo(c -> !Eq.uals(Character.valueOf(':'), c));

        if (pair.snd.isEmpty())
            return ImEither.Left(pair.fst.toString(""));
        else
        {
            ImList<Character> remainder = pair.snd.tail();

            // slurp up all the / characters
            ImPair<ImList<Character>, ImList<Character>> p2 = remainder.cutIntoTwo(c -> Eq.uals(Character.valueOf('/'), c));

            // There must be at least one. We ignore any others

            if (p2.fst.size() < 1)
            {
                return ImEither.Left(pair.fst.toString(""));
            }

            return ImEither.Right(ImPair.on(pair.fst.toString("").toLowerCase(), p2.snd));
        }

    }

    public static ImEither<String, ImPair<String, ImList<Character>>> parsePath(ImList<Character> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), '/'))
        {
            // No path
            // eg "http://a.b:123?a=b"
            return ImEither.Right(ImPair.on("", input));
        }
        else
        {
            ImPair<ImList<Character>, ImList<Character>> pair = input.cutIntoTwo(c -> !pathDelimiters.contains(c));

            String p = utf8EncodeAndThenPercentEncode(pathPercentEncodeSet, pair.fst.toString(""));

            return ImEither.Right(ImPair.on(p, pair.snd));
        }
    }

    public static ImEither<String, ImPair<String, ImList<Character>>> parsePort(ImList<Character> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), Character.valueOf(':')))
        {
            // eg "http://a.b?a=b"
            return ImEither.Right(ImPair.on("", input));
        }
        else
        {
            // The first char is :
            // we consider the tail of input
            ImPair<ImList<Character>, ImList<Character>> pair = input.tail().cutIntoTwo(c -> !portDelimiters.contains(c));

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
                    if (Parts.decimalPattern.matcher(s).matches())
                    {
                        // The number must not be too large
                        int p = Integer.parseInt(s);

                        return p <= Character.MAX_VALUE // 65535
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

    public static ImEither<String, ImPair<String, ImList<Character>>> parseQuery(ImList<Character> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), '?'))
        {
            // No query
            // eg "http://a.b:123/#aaa"
            return ImEither.Right(ImPair.on("", input));
        }
        else
        {
            // first char is ?
            ImPair<ImList<Character>, ImList<Character>> pair = input.tail().cutIntoTwo(c -> !queryDelimiters.contains(c));

            String p = utf8EncodeAndThenPercentEncode(queryPercentEncodeSet, pair.fst.toString(""));

            return ImEither.Right(ImPair.on(p, pair.snd));
        }
    }

    public static String parseFragment(ImList<Character> input)
    {
        if (input.isEmpty() || !Eq.uals(input.head(), '#'))
        {
            // No fragmnent
            // eg "http://a.b:123/?aaa"
            return "";
        }
        else
        {
            // first char is #
            return utf8EncodeAndThenPercentEncode(fragmentPercentEncodeSet, input.tail().toString(""));
        }
    }

    public static ImEither<String, ImPair<String, ImList<Character>>> parseHost(ImList<Character> input)
    {

        // First, just get the host string out of input
        ImPair<ImList<Character>, ImList<Character>> pair = parseHost$(input);

        ImList<Character> hostChars = pair.fst;

        // If it is empty we return it
        if (hostChars.isEmpty())
            return ImEither.Right(ImPair.on("", pair.snd));
        else
        {

            // For now, if it is IPv6 we just return it with no further checks
            if (Eq.uals(Character.valueOf('['), hostChars.head()))
            {
                return ImEither.Right(ImPair.on(hostChars.toString(""), pair.snd));
            }
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

    public static ImOneOfThree<String, String, String> convertHost(ImList<Character> input)
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
    public static ImOneOfThree<ImList<BigInteger>, String, String> convertHost$(ImList<Character> input)
    {

        // Convert to a string
        String s = input.toString("");

        // Convert all percent encoded strings to characters
        String unicode = URLDecoder.decode(s, StandardCharsets.UTF_8);

        // Check for forbidden
        ImMaybe<Character> forbiddenMaybe = ImList.onString(unicode).find(i -> forbiddenDomainChars.contains(i));

        if (forbiddenMaybe.isPresent())
            return ImOneOfThree.c(s);
        else
        {
            // Convert to lower case
            String lower = unicode.toLowerCase();

            ImOneOfThree<ImList<BigInteger>, String, String> result = parseAsIPv4(lower);

            return result.match(
                    v4 -> result,
                    name -> ImOneOfThree.b(IDN.toASCII(lower, IDN.ALLOW_UNASSIGNED)),
                    error -> result

            );
        }
    }

    public static ImPair<ImList<Character>, ImList<Character>> parseHost$(ImList<Character> input)
    {
        if (input.isNotEmpty() && Eq.uals(Character.valueOf('['), input.head()))
        {
            ImMaybe<Integer> indexMaybe = input.findIndex(c -> Eq.uals(c, Character.valueOf(']')));

            return indexMaybe.ifPresentElse(i -> ImPair.on(input.take(i), input.drop(i)), ImPair.on(ImList.on(), ImList.on()));
        }
        else
        {
            // Cut input into two parts based on the first delimiter that we find
            return input.cutIntoTwo(c -> !hostDelimiters.contains(c));
        }

    }

    public static String percentEncodeByte(int c)
    {
        return ("%" + Integer.toHexString(c)).toUpperCase();
    }

    /**
     * We assume that we are dealing with an input that has already been UTF8 encoded.
     *
     * We assume that the C0 controls - ie [0, 1F] and the characters  in [7F, FF] are already in every set to be encoded
     *
     * This is what is stated in the WhatWg spec in 1.3. Percent-encoded bytes
     *
     */
    public static String percentEncodeString(ImSet<Character> encodeSet, String utf8EncodedInput)
    {
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < utf8EncodedInput.length(); i++)
        {
            char c = utf8EncodedInput.charAt(i);

            if (c > '\u00FF')
                throw new PercentEncodeException(c);
            else if (c <= '\u001F' || c >= '\u007F' || encodeSet.contains(c))
                b.append(percentEncodeByte(c));
            else
                b.append(c);
        }

        return b.toString();
    }

    public static String utf8EncodeAndThenPercentEncode(ImSet<Character> encodeSet, String input)
    {
        byte[] bs = input.getBytes();
        StringBuilder b = new StringBuilder();

        for (int i = 0; i < bs.length; i++)
        {
            // Get the byte as a char and add it to the builder
            b.append((char) (bs[i] & 0xFF));
        }

        return percentEncodeString(encodeSet, b.toString());
    }

    @Override
    public ImList<Object> getValues()
    {
        return ImList.on(scheme, user, password, host, port, path, query, fragment);
    }

    @Override
    public ImList<String> getNames()
    {
        return ImList.on("scheme", "user", "password", "host", "port", "path", "query", "fragment");
    }

    public static class Parts
    {
        private static final Pattern octalPattern = Pattern.compile("0[0-9]*");
        private static final Pattern decimalPattern = Pattern.compile("[1-9][0-9]*");
        private static final Pattern hexPattern = Pattern.compile("0x[0-9a-fA-F]*");

        private final ImList<String> parts;
        private final ImList<ImEither<String, BigInteger>> partsAsType;
        private final ImList<ImEither<String, BigInteger>> effectivePartsAsTypes;

        public Parts(ImList<String> parts, ImList<ImEither<String, BigInteger>> partsAsType, ImList<ImEither<String, BigInteger>> effectivePartsAsTypes)
        {
            this.parts = parts;
            this.partsAsType = partsAsType;
            this.effectivePartsAsTypes = effectivePartsAsTypes;
        }

        public boolean effectiveLastIsNumeric()
        {
            return effectivePartsAsTypes.isNotEmpty() && !effectivePartsAsTypes.last().isLeft;
        }

        public static Parts on(String input)
        {
            ImList<String> ps = ParseUtils.split('.', input);
            ImList<ImEither<String, BigInteger>> partsAsTypes = ps.map(i -> getNumberOrString(i));

            return new Parts(ps, partsAsTypes, calculateEffectiveParts(partsAsTypes));

            //            // If all the numbers are in the range [0, 255]
            //            boolean isIpv4 = pp.size() >= 1 && pp.size() <= 4 && (pp.all(i -> !i.isLeft && i.right <= 255));

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
        public static ImEither<String, BigInteger> getNumberOrString(String part)
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
                    return ImEither.Right(new BigInteger(part.substring(2), 16));
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
     * IPv4|domain name|error
     */
    public static ImOneOfThree<ImList<BigInteger>, String, String> parseAsIPv4(String input)
    {
        // Split the input into parts at the four types of dot
        Parts parts = Parts.on(input);

        ImList<ImEither<String, BigInteger>> eff = parts.effectivePartsAsTypes;

        // If size is >= 1 and <= 4 and all the parts are numbers then it might be IPv4
        if (eff.size() >= 1 && eff.size() <= 4 && eff.all(i -> !i.isLeft))
        {
            // All the parts are numbers
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
            // Some of the parts are not numbers or there are more than 4 parts

            // If the effective last part is a number then this is an error
            return parts.effectiveLastIsNumeric()
                   ? ImOneOfThree.c(input)
                   : ImOneOfThree.b(input);
        }

    }

    private ImList<Integer> expand(ImList<Integer> numbers)
    {
        return Throw.Exception.ifYouGetHere();
    }

}
