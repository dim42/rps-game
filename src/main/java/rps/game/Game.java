package rps.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

public class Game {

    private static final double DEFAULT_WEIGHT = 10;
    private static final double INCREASED_WEIGHT = 20;

    private static final Distribution<Shape> initDistribution;
    private static final Map<Shape, Distribution<Shape>> distributions = new HashMap<>();

    private Shape robotShape;
    private int gamesTotal;
    private int wins;
    private int losses;
    private int draws;

    static {
        initDistribution = newDistribution(DEFAULT_WEIGHT, INCREASED_WEIGHT, INCREASED_WEIGHT);
        Arrays.stream(Shape.values()).forEach(shape -> distributions.put(shape, newDistribution(getWeights(shape))));
    }

    private static Distribution<Shape> newDistribution(double... weights) {
        NavigableMap<Double, Shape> distribution = new TreeMap<>();
        double sum = 0;
        for (int i = 0; i < weights.length; i++) {
            double prob = weights[i];
            sum += prob;
            distribution.put(sum, Shape.values()[i]);
        }
        return new Distribution<>(distribution, sum);
    }

    private static double[] getWeights(Shape shape) {
        double[] weights = IntStream.range(0, Shape.values().length).mapToDouble(k -> DEFAULT_WEIGHT).toArray();
        weights[shape.ordinal()] = INCREASED_WEIGHT;
        return weights;
    }

    public void startGame() {
        setRobotShape(initDistribution.rnd());
    }

    public Map<String, Object> runGame(Shape humanShape) {
        Shape robotShape = getRobotShape();
        int result = humanShape.compare(robotShape);
        gamesTotal++;

        Distribution<Shape> distribution;
        if (result < 0) {
            losses++;
            // Assume next human is going to beat the last robot
            distribution = distributions.get(robotShape.itsWinner().itsWinner());
        } else if (result > 0) {
            wins++;
            // Assume next human is going to show the same
            distribution = distributions.get(robotShape.itsWinner());
        } else {
            draws++;
            // Assume next human is going to beat the last robot
            distribution = distributions.get(robotShape.itsWinner().itsWinner());
        }
        setRobotShape(distribution.rnd());

        Map<String, Object> map = new HashMap<>();
        map.put("robot", robotShape);
        map.put("result", result);
        return map;
    }

    public void setRobotShape(Shape robotShape) {
        this.robotShape = robotShape;
    }

    public Shape getRobotShape() {
        return robotShape;
    }

    public int getGamesTotal() {
        return gamesTotal;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }
}
