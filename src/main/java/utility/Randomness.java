package utility;

import java.util.Random;

public class Randomness {
    private Randomness() {}


    /**
     * Returns a Boolean that represents a random outcome according to the specified bias value.
     * @param bias
     * @return
     */
    public static boolean randomBooleanOutcome(double bias, Random random){
        if (random.nextDouble() <= bias) return true;
        return false;
    }

}
