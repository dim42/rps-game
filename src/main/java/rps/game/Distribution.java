package rps.game;

import java.util.NavigableMap;
import java.util.concurrent.ThreadLocalRandom;

public class Distribution<T> {

    private final NavigableMap<Double, T> distribution;
    private final double max;

    public Distribution(NavigableMap<Double, T> distribution, double max) {
        this.distribution = distribution;
        this.max = max;
    }

    public T rnd() {
        double rnd = ThreadLocalRandom.current().nextDouble(max);
        return distribution.ceilingEntry(rnd).getValue();
    }
}
