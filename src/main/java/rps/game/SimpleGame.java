package rps.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class SimpleGame {

    private static final Logger log = LoggerFactory.getLogger(SimpleGame.class);

    public static void main(String[] args) {
        int games = 0;
        int wins = 0;
        int draws = 0;
        int losses = 0;

        List<Integer> probs = Arrays.asList(10, 20, 20);
        NavigableMap<Integer, Shape> spMap = createMap(probs);

        for (int j = 0; j < 100; j++) {
            Integer max = spMap.lowerKey(Integer.MAX_VALUE);
            Shape robotShape = genRobot(spMap, max);

            int i = j % 3;
            Shape humanShape = Shape.values()[i];
            log.info("h:{}", humanShape);

            int result = humanShape.check(robotShape);
            log.info(String.valueOf(result));

            games++;

            if (result < 0) {// Assume human is going to beat the last winner
                losses++;
                probs = new ArrayList<>();
                for (int k = 0; k < Shape.values().length; k++) {
                    probs.add(10);
                }
                Shape win = robotShape.winner();
                int ordinal = win.winner().ordinal();
                probs.set(ordinal, 20);
                spMap = createMap(probs);
            } else if (result > 0) {// Assume human is going to show the same
                wins++;
                probs = new ArrayList<>();
                for (int k = 0; k < Shape.values().length; k++) {
                    probs.add(10);
                }
                Shape win = humanShape.winner();
                int ordinal = win.ordinal();
                probs.set(ordinal, 20);
                spMap = createMap(probs);
            } else {// Assume human is going to win last equals
                draws++;
                probs = new ArrayList<>();
                for (int k = 0; k < Shape.values().length; k++) {
                    probs.add(10);
                }
                Shape win = robotShape.winner();
                int ordinal = win.winner().ordinal();
                probs.set(ordinal, 20);
                spMap = createMap(probs);
            }
        }

        log.info("Total games:{}, w:{}, e:{}, l:{}", games, wins, draws, losses);
    }

    private static TreeMap<Integer, Shape> createMap(List<Integer> probs) {
        TreeMap<Integer, Shape> map = new TreeMap<>();
        int sum = 0;
        for (int i = 0; i < probs.size(); i++) {
            Integer prob = probs.get(i);
            sum += prob;
            map.put(sum, Shape.values()[i]);
        }
        return map;
    }

    private static Shape genRobot(NavigableMap<Integer, Shape> map, Integer max) {
        int rnd = ThreadLocalRandom.current().nextInt(max);
        Shape robotShape = map.ceilingEntry(rnd).getValue();
        log.info("r:{}", robotShape);
        return robotShape;
    }

    private enum Shape {
        R(0, -1, 1), S(-1, 1, 0), P(1, 0, -1);

        private int r;
        private int p;
        private int s;
        private Shape winnerOf;

        static {
            R.setWinnerOf(P);
            P.setWinnerOf(S);
            S.setWinnerOf(R);
        }

        Shape(int r, int p, int s) {
            this.r = r;
            this.s = s;
            this.p = p;
        }

        public int check(Shape shape) {
            switch (shape) {
                case R:
                    return r;
                case P:
                    return p;
                case S:
                    return s;
                default:
                    throw new IllegalArgumentException(String.valueOf(shape));
            }
        }

        public Shape winner() {
            return winnerOf;
        }

        public void setWinnerOf(Shape winnerOf) {
            this.winnerOf = winnerOf;
        }
    }
}
