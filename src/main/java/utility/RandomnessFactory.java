package utility;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

public class RandomnessFactory {

    private static RandomGenerator random;

    private RandomnessFactory() { }

    public static RandomGenerator getPureRandomGenerator() {
        if (random == null) {
            random = new MersenneTwister();
        }
        return random;
    }

    public static RandomGenerator newPseudoRandomGenerator(long seed) {
        return new MersenneTwister(seed);
    }

}
