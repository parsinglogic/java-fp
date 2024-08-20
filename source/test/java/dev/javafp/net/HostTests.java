package dev.javafp.net;

import dev.javafp.ex.ImException;
import dev.javafp.ex.Throw;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.util.ImEither;
import dev.javafp.util.ImUtils;
import dev.javafp.val.ImCodePoint;
import org.junit.Test;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

/**
 * Tests for parsing host strings
 */
public class HostTests
{

    ImMap<String, String> empty = ImMap.empty();

    /**
     * These are ok:
     *
     * http://:@@b:?sdf
     * http://:@       @b:?sdf
     * http://[1::0008]
     * http://a:
     * http://127.0x1
     * http://127.011
     * http://a: <>[]^`{|} b@c
     *
     * all allowed printable (apart from alphanum)
     * http://~}{`_=;.-,+)('$! hostname = ~}{`_=;.-,+)('$!
     * http://1/
     *
     * This is apple.com
     *
     * https://www.xn--80ak6aa92e.com/
     *
     *
     *
     *
     *
     *
     * [1::0008] is ok
     * 127.0.0.0x0000000000000001 is ok - converts to 127.0.0.1
     * 127.1 is ok - converts to 127.0.0.1
     *
     * http://1.2.3.0000000004 is ok
     * http://127.011 ok - converts to 127.0.0.9
     *
     * http://1.2.3.0xA is ok converts to 1.2.3.10
     * [1::00008] fails
     * [1::0x8] fails
     *
     * http://1.2.3.4.5 fails
     * ttp://1.2.3.0000000009 fails
     *
     *
     * from https://www.xudongz.com/blog/2017/idn-phishing/
     *
     * "短.co" is "xn–s7y.co"
     *
     * аррӏе.com   https://www.xn--80ak6aa92e.com/
     *
     * it is all cyrillic
     *
     *  The screenshots show that the browsers did not convert it. They do now.
     *
     *  This has 3 different types of dots
     *
     *  http:///1｡2。3．4
     *
     *  It seems that these are parsed as being dots for the purposes of IPv4 but ok for host names - they are transformed to . rather than UTf8 % encoded
     */

    ImList<ImMap<String, String>> hostFixtures = ImList.on(
            empty
                    .put("input", "abc?x")
                    .put("host", "abc")
                    .put("rest", "?x"),

            empty
                    .put("input", "abc:12345")
                    .put("host", "abc")
                    .put("rest", ":12345"),

            empty
                    .put("input", "abc#")
                    .put("host", "abc")
                    .put("rest", "#"),

            empty
                    .put("input", "a.123456789123456789.b")
                    .put("host", "a.123456789123456789.b")
                    .put("rest", ""),

            empty
                    .put("input", "a.123456789123456789")
                    .put("fail", ""),

            empty
                    .put("input", "[1:2:3]")
                    .put("fail", ""),

            empty
                    .put("input", "1###")
                    .put("host", "0.0.0.1")
                    .put("rest", "###"),

            empty
                    .put("input", "1765765776")
                    .put("host", "105.63.114.144")
                    .put("rest", ""),

            empty
                    .put("input", "1765765776.com")
                    .put("host", "1765765776.com")
                    .put("rest", ""),

            empty
                    .put("input", "0.600.")
                    .put("host", "0.0.2.88")
                    .put("rest", ""),

            empty
                    .put("input", "0.600.x.")
                    .put("host", "0.600.x.")
                    .put("rest", ""),

            empty
                    .put("input", "127.0.0.0x0000000000000001")
                    .put("host", "127.0.0.1")
                    .put("rest", ""),

            // This used to be the case:  Empty label causes IDN.toAscii to throw an exception
            empty
                    .put("input", "0.♥600..")
                    .put("host", "0.xn--600-yx5a..")
                    .put("rest", ""),

            //  This used to be a fail: Long label  causes IDN.toAscii to throw an exception
            empty
                    .put("input", "0.♥laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf.com")
                    .put("host", "0.xn--laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf-eu53h.com")
                    .put("rest", ""),

            //   Long label  causes IDN.toAscii to throw an exception
            //            empty
            //                    .put("input",
            //                            "0.laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf♥laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf.com")
            //                    .put("host", "0.xn--laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf-eu53h.com")
            //                    .put("rest", ""),

            empty
                    .put("input", "a♥600.💩")
                    .put("host", "xn--a600-5u3b.xn--ls8h")
                    .put("rest", ""),

            empty
                    .put("input", "127.011")
                    .put("host", "127.0.0.9")
                    .put("rest", ""),

            empty
                    .put("input", "127.0x.0X.0")
                    .put("host", "127.0.0.0")
                    .put("rest", ""),

            empty
                    .put("input", "1.2.3.0xA")
                    .put("host", "1.2.3.10")
                    .put("rest", ""),

            empty
                    .put("input", "com..")
                    .put("host", "com..")
                    .put("rest", ""),

            empty
                    .put("input", "räksmörgås.josefsson.org/abc")
                    .put("host", "xn--rksmrgs-5wao1o.josefsson.org")
                    .put("rest", "/abc"),

            empty
                    .put("input", "xn--rksmrgs-5wao1o.josefsson.org")
                    .put("host", "xn--rksmrgs-5wao1o.josefsson.org")
                    .put("rest", "")
    );

    @Test
    public void testAllHosts()
    {
        hostFixtures.foreach(i -> testHostFixture(i));
    }

    public void testHostFixture(ImMap<String, String> fx)
    {
        ImEither<String, ImPair<String, ImList<ImCodePoint>>> resEither;
        try
        {
            resEither = ImUrl.parseHost(ImList.onString(fx.get("input")));

        } catch (Exception e)
        {
            say(e);
            say(ImUtils.getStackTrace(e));
            throw new TestFail(fx.get("input"), e);
        }

        resEither.match(
                error -> fx.keys().contains("fail") ? null : Throw.wrap(new UrlTestFail(fx.get("input"), error)),
                pair -> checkPairAgainstExpected(pair, fx)
        );

    }

    public static class TestFail extends ImException
    {
        public TestFail(String input)
        {
            super("Fail on " + input);
        }

        public TestFail(String input, Exception e)
        {
            super("Fail on " + input + "\n" + e);
        }
    }

    private Void checkPairAgainstExpected(ImPair<String, ImList<ImCodePoint>> pair, ImMap<String, String> fx)
    {
        ImMap<String, String> resMap = empty.put("host", pair.fst).put("rest", pair.snd.toString(""));

        assertEquals(fx.remove("input"), resMap);

        return null;
    }

}