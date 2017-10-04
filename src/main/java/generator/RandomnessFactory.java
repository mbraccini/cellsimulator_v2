package generator;

import java.util.Random;

public class RandomnessFactory {

    private static Random random;

    private RandomnessFactory() { }

    public static Random getPureRandomGenerator() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    public static Random newPseudoRandomGenerator(long seed) {
        return new Random(seed);
    }

}
