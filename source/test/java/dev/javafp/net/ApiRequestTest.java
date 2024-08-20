package dev.javafp.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.javafp.lst.ImList;
import dev.javafp.set.ImMap;
import dev.javafp.tuple.ImPair;
import dev.javafp.tuple.Pai;
import junit.framework.TestCase;
import org.junit.Test;

import static dev.javafp.util.Say.say;

public class ApiRequestTest extends TestCase
{

    private static JsonParser jsonParser = new JsonParser();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Test
    //    public void testSendGetRequest() throws IOException
    //    {
    //
    //        String requestURL = "http://www.google.com";
    //        DatagramSocket HttpUtility;
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

    private static ImUrl makeUrl(String urlString)
    {
        return ImUrl.parse(urlString).right;
    }

    @Test
    public void testExampleDotCom()
    {
        GetRequest getRequest = GetRequest.on(makeUrl("https://example.com"));

        getRequest = getRequest.addQuery("foo", "bar").addQuery("bish", "bash");

        say("getRequest", getRequest);

        ApiResponse response = getRequest.send();
        say("response", response);

        assertEquals(200, response.status);

    }

    @Test
    public void testVirtualShrimpSandwiches()
    {
        GetRequest getRequest = GetRequest.on(makeUrl("https://räksmörgås.josefsson.org"));

        say("getRequest", getRequest);

        ApiResponse response = getRequest.send();
        say("response", response);

        assertEquals(200, response.status);

    }

    //    @Test
    //    public void testGetLocal()
    //    {
    //        // Start the test api server in a thread on a random port
    //        int port = ApiServerForTesting.startInAThreadOnARandomPort();
    //
    //        GetRequest getRequest = GetRequest.on(makeUrl("http://localhost/hello").withPort(port));
    //
    //        getRequest = getRequest.addQuery("foo", "bar").addQuery("bish", "bash");
    //
    //        say("getRequest", getRequest);
    //
    //        ApiResponse response = getRequest.send();
    //        say("response", response);
    //
    //        assertEquals(200, response.status);
    //
    //        assertEquals("Hello World!", response.body);
    //
    //    }

    //    @Test
    //    public void testUrlWithQuery()
    //    {
    //        GetRequest getRequest = GetRequest.on(makeUrl("http://localhost:80/hello?foo=bar&bing=bong"));
    //
    //        getRequest = getRequest.addQuery("bish", "bash").addQuery("bing", "bong");
    //
    //        assertEquals("http://localhost:80/hello", "" + getRequest.url);
    //
    //        say("getRequest", getRequest);
    //
    //    }

    //    @Test
    //    public void testURLEncoding() throws URISyntaxException
    //    {
    //        Charset utf8 = StandardCharsets.UTF_8;
    //
    //        URI uri = new URI("http", "www.example.com", "/a/b", "a=?&&b=c", null);
    //
    //        say("URI", uri);
    //
    //        ImList<String> chars = ImRange.inclusive(32, 126).map(i -> "" + (char) (i.intValue()));
    //
    //        ImList<String> converted = chars.map(c -> URLEncoder.encode(c, utf8));
    //        ImList<ImPair<String, String>> unconverted = chars.zip(converted).filter(p -> Eq.uals(p.fst, p.snd));
    //
    //        ImList<String> un = unconverted.map(p -> p.fst).filter(c -> !isAlphaNum(c.charAt(0)));
    //
    //        say("unconverted", un.toString("\n"));
    //
    //        assertEquals("*", URLEncoder.encode("*", utf8));
    //        assertEquals("%2F", URLEncoder.encode("/", utf8));
    //
    //    }

    private boolean isAlphaNum(char c)
    {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    @Test
    public void testPostToPostman()
    {
        PostRequest client = PostRequest.on(makeUrl("https://postman-echo.com/post"));

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

        /**
         *
         * ApiResponse:
         *
         * status:        200
         * statusMessage: OK
         * headers:       [Server->[nginx/1.25.3], Date->[Tue, 23 Jul 2024 09:51:52 GMT], ETag->[W/"316-MrK8FC3WKXcbzf311jnymImxyvU"],
         *                 Content-Type->[application/json; charset=utf-8], Connection->[keep-alive],
         *                 set-cookie->[sails.sid=s%3Ab204PPA3ADeVkvH-1GEQqjsgogjTcWZe.4lfVEQVLk4j2JLPVlb7zEU2cqmGhpwAIm%2FQoPXZVLBc; Path=/; HttpOnly],
         *                 Content-Length->[790]]
         * body:          {
         *                  "args": {},
         *                  "data": "",
         *                  "files": {},
         *                  "form": {
         *                    "grant_type": "authorization_code",
         *                    "client_id": "12345",
         *                    "code": "12345",
         *                    "redirect_uri": "https://com.myclientapp.myclient/redirect"
         *                  },
         *                  "headers": {
         *                    "host": "postman-echo.com",
         *                    "x-request-start": "t=1721728312.989",
         *                    "content-length": "120",
         *                    "x-forwarded-proto": "https",
         *                    "x-forwarded-port": "443",
         *                    "x-amzn-trace-id": "Root=1-669f7d38-65ed026573b4ab6f1e0e8d23",
         *                    "content-type": "application/x-www-form-urlencoded",
         *                    "user-agent": "Java/20.0.1",
         *                    "accept": "*\/*"                                                       <- Had to add a backslash here
         *                  },
         *                  "json": {
         *                    "grant_type": "authorization_code",
         *                    "client_id": "12345",
         *                    "code": "12345",
         *                    "redirect_uri": "https://com.myclientapp.myclient/redirect"
         *                  },
         *                  "url": "https://postman-echo.com/post?"
         *                }
         *
         */

        JsonObject obj = JsonParser.parseString(response.body).getAsJsonObject();

        say("keys", obj.keySet());

        say("json keys", obj.get("json").getAsJsonObject().keySet());
        ImList<ImPair<String, String>> ps = ImList.onAll(obj.get("json").getAsJsonObject().entrySet()).map(e -> ImPair.on(e.getKey(), e.getValue().getAsString()));

        assertEquals(ImMap.fromPairs(kv), ImMap.fromPairs(ps));

    }

}