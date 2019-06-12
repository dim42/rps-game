package rps.game.http;

import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rps.game.Result;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static java.lang.String.format;

public class Client extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private static final String HELP_TEXT = "Enter command:\n" +
            "start (st) - to start the new game\n" +
            "next (n) - to make moves\n" +
            "end (e) - to terminate the game and observe the statistics\n" +
            "help (h) - to see this help\n" +
            "quit (q) - to quit the program";

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private final long timeoutMs = 500;

    private HttpClient client;

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Client());
    }

    @Override
    public void start() {
        client = vertx.createHttpClient(new HttpClientOptions());
    }

    public void run() {
        System.out.println(HELP_TEXT);
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
            String gameId = null;
            while (scanner.hasNext()) {
                String next = scanner.next();
                String command = next.toLowerCase();
                switch (command) {
                    case "start":
                    case "st": {
                        gameId = request(this::startGame);
                        System.out.println("Enter your shape: rock (r), paper (p), scissors (s).");
                        break;
                    }
                    case "rock":
                    case "r":
                    case "paper":
                    case "p":
                    case "scissors":
                    case "s": {
                        if (gameId == null) {
                            System.out.println("Enter \"start\" at first.");
                            break;
                        }
                        String finalGameId = gameId;
                        String result = request(() -> request(finalGameId, command));
                        System.out.println(format("Response: %s\nEnter command.", result));
                        break;
                    }
                    case "end":
                    case "e": {
                        if (gameId == null) {
                            System.out.println("Start the game at first.");
                            break;
                        }
                        String finalGameId = gameId;
                        String result = request(() -> stat(finalGameId));
                        System.out.println(format("Response: %s\nEnter command.", result));
                        break;
                    }
                    case "help":
                    case "h": {
                        System.out.println(HELP_TEXT);
                        break;
                    }
                    case "quit":
                    case "q": {
                        System.out.println("The End.");
                        System.exit(0);
                    }
                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println(HELP_TEXT);
                }
            }
        } finally {
            client.close();
        }
    }

    private String request(Supplier<CompletableFuture<String>> supplier) {
        Exception ex;
        try {
            return supplier.get().get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Interruption during request for command '%s'", e);
            Thread.currentThread().interrupt();// Reset interrupted status
            ex = e;
        } catch (TimeoutException e) {
            log.warn("Error timeout for command '%s'", e);
            ex = e;
        } catch (Exception e) {
            log.error("Failed for command '%s'", e);
            ex = e;
        }
        return ex.getMessage();
    }

    private CompletableFuture<String> startGame() {
        HttpClientRequest request = client.post(PORT, HOST, "/start")
                .setTimeout(timeoutMs);
        CompletableFuture<String> future = new CompletableFuture<>();
        request.toObservable().subscribe(response -> response.bodyHandler(buffer -> {
                    future.complete(buffer.toString());
                }),
                error -> {
                    log.error("Start request error", error);
                    future.completeExceptionally(error.getCause());
                }
        );
        request.setChunked(true).putHeader(CONTENT_TYPE, TEXT_PLAIN)
                .end();
        return future;
    }

    private CompletableFuture<String> request(String gameId, String command) {
        CompletableFuture<String> future = new CompletableFuture<>();
        HttpClientRequest request = client.post(PORT, HOST, "/game/" + gameId)
                .setTimeout(timeoutMs);
        request.toObservable().subscribe(
                response -> response.bodyHandler(buffer -> {
                    JsonObject json = buffer.toJsonObject();
                    String error = json.getString("error");
                    String value;
                    if (error != null) {
                        value = format("Some error:%s", error);
                    } else {
                        value = String.format("Robot shape:%s, result:%s", json.getValue("robot"), Result.of(json.getInteger("result")));
                    }
                    future.complete(value);
                }),
                error -> {
                    log.error(format("Error for game: %s,%s", gameId, command), error);
                    future.completeExceptionally(error);
                }
        );
        request.setChunked(true).putHeader(CONTENT_TYPE, TEXT_PLAIN)
                .write(command)
                .end();
        return future;
    }

    private CompletableFuture<String> stat(String gameId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        HttpClientRequest request = client.get(PORT, HOST, "/game/" + gameId + "/stat")
                .setTimeout(timeoutMs);
        request.toObservable().subscribe(
                response -> response.bodyHandler(buffer -> {
                    future.complete(buffer.toString());
                }),
                error -> {
                    log.error(format("Error for game: %s", gameId), error);
                    future.completeExceptionally(error);
                }
        );
        request.setChunked(true).putHeader(CONTENT_TYPE, TEXT_PLAIN)
                .end();
        return future;
    }
}
