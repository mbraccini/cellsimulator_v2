package generator;

import io.jenetics.prngine.LCG64ShiftRandom;

import java.util.Random;

public class RandomnessFactory {

    private static Random random;

    private RandomnessFactory() { }

    public static Random getPureRandomGenerator() {
        if (random == null) {
            random = new LCG64ShiftRandom();
        }
        return random;
    }

    public static Random newPseudoRandomGenerator(long seed) {
        return new LCG64ShiftRandom(seed);
    }

}
