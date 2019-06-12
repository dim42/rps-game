package rps.game;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;
import static rps.game.Shape.Rock;

public class AppTest {

    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testGame() {
        Game game = new Game();
        game.startGame();
        Shape humanShape = Rock;
        for (int g = 0; g < 100; g++) {
            Map<String, Object> map = game.runGame(humanShape);
            Integer result = (Integer) map.get("result");
            Shape robotShape = (Shape) map.get("robot");
            if (result < 0) {
                humanShape = robotShape.itsWinner();
            } else if (result > 0) {
                // the same
            } else {
                humanShape = robotShape.itsWinner();
            }
        }

        String response = format("Total games:%d, wins:%d, losses:%d, draws:%d",
                game.getGamesTotal(), game.getWins(), game.getLosses(), game.getDraws());
        log.info("Result:{}", response);
        assertTrue(format("%s, %s", game.getWins(), game.getLosses()), game.getWins() < game.getLosses());
    }
}
