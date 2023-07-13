package dev.javafp.net;

import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.util.Say;
import spark.Spark;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple server to be used for logging.
 *
 * /Users/aove215/projects/drum/jan/ad-hoc-projects/events
 * contains the test HTML file  that I use
 *
 * python -m SimpleHTTPServer 8000
 *
 * is what I use to start a small server that serves files in this dir
 *
 * I use it for testing events on HTML pages. I log the events to this API. It is useful to see the events when closing a window or browser which would be difficult
 * to see otherwise.
 *
 * it starts on port 8001 and this is encoded in the html file that uses it:
 *
 *
 *     function log( s )
 *     {
 *        var ss =  new Date().toLocaleString() + ":   " + s;
 *        console.log(ss);
 *
 *        ta.value = ta.value  + ss + "\n";
 *        ta.scrollTop = ta.scrollHeight;
 *
 *        fetch('http://localhost:8001/log', {
 *          mode: 'no-cors',
 *          method: 'POST',
 *          headers: {
 *            'Content-Type': 'text/plain'
 *          },
 *          body: s,
 *        });
 *     }
 *
 *
 *
 */
public class LoggerApi
{

    public static void main(String[] args)
    {
        startInAThreadOnAFixedPort(8001);
    }

    public static int startInAThreadOnAFixedPort(int port)
    {
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Future<Integer> future = executor.submit(() -> startServer(port));

        try
        {
            return future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public static int startServer(int port)
    {

        Say.say("Starting API server");

        Say.say("port", port);

        Spark.port(port);

        Spark.post("/log", (request, response) ->
                {
                    System.out.println(request.body());
                    return ""; // just to keep Spark happy
                }
        );

        Spark.awaitInitialization();

        return port;
    }
}