package rps.game;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Result {
    Win(1), Loss(-1), Draw(0);

    private static final Map<Integer, Result> values = new HashMap<>();

    static {
        Arrays.stream(values()).forEach(result -> values.put(result.val, result));
    }

    final int val;

    Result(int val) {
        this.val = val;
    }

    public static Result of(int val) {
        return values.get(val);
    }
}
