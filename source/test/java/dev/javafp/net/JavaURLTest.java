package dev.javafp.net;

import dev.javafp.ex.Throw;
import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.file.ReaderUtil;
import dev.javafp.lst.ImList;
import dev.javafp.tuple.ImPair;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;

import static dev.javafp.util.Say.say;

public class JavaURLTest
{

    ImList<String> tryThese = ImList.on(

            "https://räksmörgås.josefsson.org",
            "example.com",
            "https://xn--rksmrgs-5wao1o.josefsson.org?foo=💩",
            "http://127.0.0.1",
            "http://127.1",

            "file:///Applications",
            "http://👨‍🦲.com",
            "http://////a.b",
            "http://127.1",
            "http://1.2.3.4.5"

    );

    @Test
    public void testAll()
    {
        say(tryThese.map(i -> ImPair.on(i, tryConnecting(i))));
    }

    @Test
    public void testConnectingToAllViaURI() throws MalformedURLException
    {
        say(tryThese.map(i -> ImPair.on(i, tryConnectingViaURI(i))));
    }

    @Test
    public void testGoogle()
    {
        try
        {
            new URL("google.com");
            Throw.Exception.ifYouGetHere();
        } catch (MalformedURLException e)
        {

        }

    }

    @Test
    public void testSchemes() throws Exception
    {
        //        ftp, //   21
        //                file, //
        //                http, //  80
        //                https, // 443
        //                ws,  //   80
        //                wss  //

        new URL("http://a.com");
        new URL("https://a.com");
        new URL("file://a.com");
        new URL("ftp://a.com");
        new URL("jar:file:!/home");
        //        new URL("ws://a.com");
        //        new URL("wss://a.com");
    }

    private String tryConnecting(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            say("trying url string", urlString);
            say("url parses host to", url.getHost(), "path", url.getPath());

            HttpURLConnection connection = createConnection(url);

            say(getStatus(connection));

            String responseBody = getResponseBody(connection);

            say(responseBody.substring(0, 200));

            return "OK";
        } catch (Exception e)
        {
            return "ERROR:" + e.getMessage() == null ? "" : e.getMessage();
        }
    }

    private String tryConnectingViaURI(String urlString)
    {
        try
        {
            say("");
            say("trying url string", urlString);
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            say("URI", uri);
            say("URI,getHost()", uri.getHost());
            say("URL", url);

            HttpURLConnection connection = createConnection(url);

            say(getStatus(connection));

            String responseBody = getResponseBody(connection);

            say(responseBody.substring(0, 200));

            return "OK";
        } catch (Exception e)
        {
            String er = "ERROR:" + e.getMessage() == null ? "" : e.getMessage();
            say(er);

            return er;
        }
    }

    private String tryConnectingViaURL(String urlString)
    {
        try
        {
            say("");
            say("trying url string", urlString);

            URL url = new URL(urlString);

            say("URL", url);

            HttpURLConnection connection = createConnection(url);

            say(getStatus(connection));

            String responseBody = getResponseBody(connection);

            say(responseBody.substring(0, 200));

            return "OK";
        } catch (Exception e)
        {
            String er = "ERROR:" + e.getMessage() == null ? "" : e.getMessage();
            say(er);

            return er;
        }
    }

    /**
     * operating on a Java URL
     */

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

    public static void ping(String address) throws Exception
    {

        String ipAddress = "127.0.0.1";
        InetAddress inet = InetAddress.getByName(ipAddress);

        System.out.println("Sending Ping Request to " + ipAddress);
        System.out.println(inet.isReachable(5000) ? "Host is reachable" : "Host is NOT reachable");

    }

}