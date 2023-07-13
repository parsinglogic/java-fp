package dev.javafp.net;

import dev.javafp.ex.UnexpectedChecked;
import dev.javafp.util.Net;
import dev.javafp.util.Say;
import spark.Spark;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A simple server to be used for testing
 *
 */
public class ApiServerForTesting
{

    public static int startInAThreadOnARandomPort()
    {
        ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Future<Integer> future = executor.submit(() -> startServer());

        try
        {
            return future.get();
        } catch (InterruptedException | ExecutionException e)
        {
            throw new UnexpectedChecked(e);
        }
    }

    public static int startServer()
    {

        Say.say("Starting API server");

        int randomPort = Net.findRandomPort();
        Say.say("port", randomPort);

        Spark.port(randomPort);

        Spark.get("/hello", (request, response) -> "Hello World!");

        Spark.post("/hello", (request, response) ->
                "Hello World: " + request.body()
        );

        Spark.get("/private", (request, response) -> {
            response.status(401);
            return "Go Away!!!";
        });

        Spark.get("/users/:name", (request, response) -> "Selected user: " + request.params(":name"));

        Spark.get("/news/:section", (request, response) -> {
            response.type("text/xml");
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" + request.params("section") + "</news>";
        });

        Spark.get("/protected", (request, response) -> {
            Spark.halt(403, "I don't think so!!!");
            return null;
        });

        Spark.get("/redirect", (request, response) -> {
            response.redirect("/news/world");
            return null;
        });

        Spark.get("/", (request, response) -> "root");

        Spark.awaitInitialization();

        return randomPort;
    }
}