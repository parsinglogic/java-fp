package dev.javafp.net;

import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImTriple;
import dev.javafp.val.ImCodePoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for parsing authority strings
 */
public class AuthorityTests
{

    ImMap<String, String> empty = ImMap.empty();

    ImList<ImMap<String, String>> authFixtures = ImList.on(
            empty
                    .put("input", "")
                    .put("user", "")
                    .put("password", "")
                    .put("rest", ""),

            empty
                    .put("input", "user:password@")
                    .put("user", "user")
                    .put("password", "password")
                    .put("rest", ""),

            empty
                    .put("input", "a.b?@:/")
                    .put("user", "")
                    .put("password", "")
                    .put("rest", "a.b?@:/"),

            empty
                    .put("input", "user@host:123")
                    .put("user", "user")
                    .put("password", "")
                    .put("rest", "host:123"),

            empty
                    .put("input", "example.com")
                    .put("user", "")
                    .put("password", "")
                    .put("rest", "example.com"),

            empty
                    .put("input", "a?@a")
                    .put("user", "")
                    .put("password", "")
                    .put("rest", "a?@a"),

            empty
                    .put("input", "[1:2]")
                    .put("user", "")
                    .put("password", "")
                    .put("rest", "[1:2]"),

            empty
                    .put("input", "u:pas@sw@ord@@a?@")
                    .put("user", "u")
                    .put("password", "pas%40sw%40ord%40")
                    .put("rest", "a?@"),

            empty
                    .put("input", "a<>!£$%&*()@a:\":\"{}{[]<><>_+_-=-~`@a")
                    .put("user", "a%3C%3E!%C2%A3$%&*()%40a")
                    .put("password", "%22%3A%22%7B%7D%7B%5B%5D%3C%3E%3C%3E_+_-%3D-~%60")
                    .put("rest", "a"),

            empty
                    .put("input", "a:::@@@@b.c")
                    .put("user", "a")
                    .put("password", "%3A%3A%40%40%40")
                    .put("rest", "b.c"),

            empty
                    .put("input", "    @a:   @a")
                    .put("user", "%20%20%20%20%40a")
                    .put("password", "%20%20%20")
                    .put("rest", "a")

    );

    @Test
    public void testAllAuthorities()
    {
        authFixtures.foreach(i -> testAuthFixture(i));
    }

    public void testAuthFixture(ImMap<String, String> fx)
    {
        try
        {
            ImTriple<String, String, ImList<ImCodePoint>> res = ImUrl.parseAuthority(ImList.onString(fx.get("input")));

            ImMap<String, String> resMap = empty.put("user", res.e1).put("password", res.e2).put("rest", res.e3.toString(""));

            assertEquals("Failure on " + fx.get("input"), fx.remove("input"), resMap);

        } catch (Exception e)
        {
            throw new UrlTestFail(fx.get("input"), e);
        }

    }

}