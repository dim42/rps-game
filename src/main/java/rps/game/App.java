package rps.game;

import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rps.game.http.Client;
import rps.game.http.Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws FileNotFoundException {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new Server());
        sleep();

        System.setIn(new FileInputStream("src/main/resources/test.txt"));

        Client client = new Client();
        vertx.deployVerticle(client);
        sleep();

        client.run();
    }

    private static void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("Process interrupted", e);
            Thread.currentThread().interrupt();// Reset/restore interrupted status
        }
    }
}
