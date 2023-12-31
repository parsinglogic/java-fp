package dev.javafp.net;

import dev.javafp.eq.Eq;
import dev.javafp.lst.ImList;
import dev.javafp.lst.ImRange;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import junit.framework.TestCase;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static dev.javafp.util.Say.say;

public class ApiRequestTest extends TestCase
{

    //    private static JsonParser jsonParser = new JsonParser();
    //    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    //    @Test
    //    public void testSendGetRequest() throws IOException
    //    {
    //
    //        String requestURL = "http://www.google.com";
    //        try
    //        {
    //            HttpUtility.sendGetRequest(requestURL);
    //            String[] response = HttpUtility.readMultipleLinesRespone();
    //            for (String line : response)
    //            {
    //                System.out.println(line);
    //            }
    //        } catch (IOException ex)
    //        {
    //            ex.printStackTrace();
    //        }
    //        HttpUtility.disconnect();
    //    }

    //    https://api.coronavirus.data.gov.uk/v1/data?filters=areaType=nation;areaName=england&structure=%7B%22name%22:%22areaName%22%7D

    @Test
    public void testGetLocal()
    {
        // Start the test api server in a thread on a random port
        int port = ApiServerForTesting.startInAThreadOnARandomPort();

        GetRequest getRequest = GetRequest.on(ImUrl.on("http://localhost/hello").withPort(port));

        getRequest = getRequest.addQuery("foo", "bar").addQuery("bish", "bash");

        say("getRequest", getRequest);

        ApiResponse response = getRequest.send();
        say("response", response);

        assertEquals(200, response.status);

        assertEquals("Hello World!", response.body);

    }

    @Test
    public void testUrlWithQuery()
    {
        GetRequest getRequest = GetRequest.on(ImUrl.on("http://localhost:80/hello?foo=bar&bing=bong"));

        getRequest = getRequest.addQuery("bish", "bash").addQuery("bing", "bong");

        assertEquals("http://localhost:80/hello", "" + getRequest.url);

        say("getRequest", getRequest);
    }

    @Test
    public void testURLEncoding() throws URISyntaxException
    {
        Charset utf8 = StandardCharsets.UTF_8;

        URI uri = new URI("http", "www.example.com", "/a/b", "a=?&&b=c", null);

        say("URI", uri);

        ImList<String> chars = ImRange.inclusive(32, 126).map(i -> "" + (char) (i.intValue()));

        ImList<String> converted = chars.map(c -> URLEncoder.encode(c, utf8));
        ImList<ImPair<String, String>> unconverted = chars.zip(converted).filter(p -> Eq.uals(p.fst, p.snd));

        ImList<String> un = unconverted.map(p -> p.fst).filter(c -> !isAlphaNum(c.charAt(0)));

        say("unconverted", un.toString("\n"));

        assertEquals("*", URLEncoder.encode("*", utf8));
        assertEquals("%2F", URLEncoder.encode("/", utf8));

    }

    private boolean isAlphaNum(char c)
    {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    @Test
    public void testPostToPostman()
    {

        PostRequest client = PostRequest.on(ImUrl.on("https://postman-echo.com/post"));

        ImList<ImPair<String, String>> kv = ImList.on(
                Pai.r("grant_type", "authorization_code"),
                Pai.r("client_id", "12345"),
                Pai.r("code", "12345"),
                Pai.r("redirect_uri", "https://com.myclientapp.myclient/redirect")
        );

        client = client.setBodyFormParameters(kv);
        ApiResponse response = client.send();

        assertEquals(200, response.status);

        say(client);
        say(response);

        //        JsonObject jobj = jsonParser.parse(response.body).getAsJsonObject();
        //
        //        Say.say("JSON", gson.toJson(jobj));

    }

}