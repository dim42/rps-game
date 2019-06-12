package rps.game;

import java.util.HashMap;
import java.util.Map;

import static rps.game.Result.Draw;
import static rps.game.Result.Loss;
import static rps.game.Result.Win;

public enum Shape {
    Rock, Scissors, Paper;

    private static final Map<String, Shape> stringToShape = new HashMap<>();
    private final Map<Shape, Result> compares = new HashMap<>();
    private Shape itsWinner;

    static {
        stringToShape.put("rock", Rock);
        stringToShape.put("r", Rock);
        stringToShape.put("paper", Paper);
        stringToShape.put("p", Paper);
        stringToShape.put("scissors", Scissors);
        stringToShape.put("s", Scissors);

        Rock.init(Draw, Loss, Win, Paper);
        Paper.init(Win, Draw, Loss, Scissors);
        Scissors.init(Loss, Win, Draw, Rock);
    }

    private void init(Result rock, Result paper, Result scissors, Shape itsWinner) {
        compares.put(Rock, rock);
        compares.put(Paper, paper);
        compares.put(Scissors, scissors);
        this.itsWinner = itsWinner;
    }

    public static Shape of(String userShape) {
        return stringToShape.get(userShape);
    }

    public int compare(Shape shape) {
        return compares.get(shape).val;
    }

    public Shape itsWinner() {
        return itsWinner;
    }
}
