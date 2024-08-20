package dev.javafp.net;

import com.ibm.icu.text.IDNA;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.net.IDN;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static dev.javafp.util.Say.say;
import static org.junit.Assert.assertEquals;

public class IDNATest
{

    @Test
    public void testOne()
    {
        assertEquals("xn--lzg", IDN.toASCII("€"));
        assertEquals("xn--xn--lzg-s17c.xn--lzg.xn--lzg.", IDN.toASCII("€xn--lzg.xn--lzg.xn--lzg."));
        //        assertEquals("xn--xn--lzg-s17c.xn--lzg.xn--lzg.a", IDN.toASCII("€xn--lzg.xn--lzg.xn--lzg.%41"));
        //        assertEquals("xn--xn--lzg-s17c.a.xn--lzg.xn--lzg", IDN.toASCII("€xn--lzg.%41.xn--lzg.xn--lzg"));
        assertEquals("xn--lzag", IDN.toASCII("xn--lzag"));
        assertEquals("%E2%99%A5", IDN.toASCII("%E2%99%A5"));
    }

    @Test
    public void testEncodeAndDecodeWorkAsExpected()
    {
        String encoded = URLEncoder.encode("♥", StandardCharsets.UTF_8);

        assertEquals("%E2%99%A5", encoded);
        String decodeOfUnicode = URLDecoder.decode("♥", StandardCharsets.UTF_8);
        String decodeOfEncoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

        say("encoded", encoded);
        say(decodeOfUnicode);

        assertEquals("♥", decodeOfUnicode);
        assertEquals("♥", decodeOfEncoded);
    }

    @Test
    public void IDNATest()
    {

        StringBuilder sb = new StringBuilder();

        IDNA uts46Instance = IDNA.getUTS46Instance(IDNA.DEFAULT);

        IDNA.Info info = new IDNA.Info();
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII("räksmörgås.josefsson.org", sb, info);
        StringBuilder stringBuilder = uts46Instance.nameToASCII("👨‍🦰.com", sb, info);
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII("👨.com", sb, info);
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII(
        //                "👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨.👨",
        //                sb, info);
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII("1.2.3.256", sb, info);
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII("\u1f468\u200D\u1F9B0.com", sb, info);  // 1f468 200d 1f9b0
        //        StringBuilder stringBuilder = uts46Instance.nameToASCII(
        //                "0.♥laksjdhlakjhsdlfjkhljhalksjhdfljahsldkjfhlakjshdfljahlsdjhflajhsdlfkjhaljsdhflkjahslkdjfhlajhsdfljkashdlfjkhalsjdfhlasjdhflasjdf.com", sb, info);

        //        StringBuilder stringBuilder = uts46Instance.nameToASCII("a.♥..b.♥org", sb, info);

        say("result", sb.toString());
        say("info errors", info.getErrors());

    }

    @Test
    public void IDNATest2()
    {
        int bad = 0;
        int ok = 0;
        int not = 0;

        for (int i = 0; i < 0X10FFFF; i++)
        {

            if (Character.isDefined(i))
            {
                String s = "a" + Character.toString(i) + "b";
                ImPair<String, Set<IDNA.Error>> pair = map(s);

                if (!pair.snd.isEmpty())
                    bad++;
                    //                    say(i, String.valueOf(pair.snd));
                else
                    ok++;
            }
            else
            {
                not++;
                //                say(i, "Not a character");
            }
        }

        say("ok ", ok);
        say("bad", bad);
        say("not", not);
        say("max", 0X10FFFF);
    }

    public ImPair<String, Set<IDNA.Error>> map(String name)
    {

        StringBuilder sb = new StringBuilder();

        IDNA uts46Instance = IDNA.getUTS46Instance(IDNA.CHECK_BIDI | IDNA.CHECK_CONTEXTJ | IDNA.NONTRANSITIONAL_TO_ASCII | IDNA.NONTRANSITIONAL_TO_UNICODE);

        IDNA.Info info = new IDNA.Info();

        // Run in non strict mode - don't set IDNA.USE_STD3_RULES
        uts46Instance.nameToASCII(name, sb, info);

        return ImPair.on(sb.toString(), info.getErrors());
    }
}
