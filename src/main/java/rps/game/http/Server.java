package rps.game.http;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.Router;
import rps.game.Game;
import rps.game.Shape;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static java.lang.String.format;

public class Server extends AbstractVerticle {

    private static final AtomicLong seq = new AtomicLong();

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private final ConcurrentMap<Long, Game> gameDataMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new Server());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.post("/start").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            request.bodyHandler(buffer -> {
                Long gameId = seq.incrementAndGet();
                Game game = new Game();
                game.startGame();
                gameDataMap.put(gameId, game);
                routingContext.response().putHeader(CONTENT_TYPE, request.getHeader(CONTENT_TYPE)).end(String.valueOf(gameId));
            });
        });

        router.post("/game/:gameId").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            request.bodyHandler(buffer -> {
                JsonObject json;
                try {
                    String gameId = request.getParam("gameId");
                    Shape humanShape = Shape.of(new String(buffer.getBytes()));
                    Game game = gameDataMap.get(Long.valueOf(gameId));
                    Map<String, Object> result = game.runGame(humanShape);
                    json = new JsonObject(result);
                } catch (Exception e) {
                    json = new JsonObject().put("error", e + (e.getCause() != null ? ", cause:" + e.getCause() : ""));
                }
                routingContext.response().setChunked(true).putHeader(CONTENT_TYPE, APPLICATION_JSON).end(json.encode());
            });
        });

        router.get("/game/:gameId/stat").handler(routingContext -> {
            HttpServerRequest request = routingContext.request();
            request.bodyHandler(buffer -> {
                String response;
                try {
                    String gameId = request.getParam("gameId");
                    Game game = gameDataMap.get(Long.valueOf(gameId));
                    response = format("Total games:%d, wins:%d, losses:%d, draws:%d",
                            game.getGamesTotal(), game.getWins(), game.getLosses(), game.getDraws());
                } catch (Exception e) {
                    response = "error:" + e + (e.getCause() != null ? ", cause:" + e.getCause() : "");
                }
                routingContext.response().setChunked(true).putHeader(CONTENT_TYPE, TEXT_PLAIN).end(response);
            });
        });

        vertx.createHttpServer().requestHandler(router).listen(PORT, HOST);
    }
}
