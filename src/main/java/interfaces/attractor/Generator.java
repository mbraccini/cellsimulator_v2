package interfaces.attractor;

import java.math.BigInteger;
import java.util.Optional;

public interface Generator<T> {

    /**
     * Produces the next sample.
     * @return
     */
    T nextSample();

    /**
     * Total number of samples required to this generator.
     * @return
     */
    BigInteger totalNumberOfSamplesToBeGenerated();
}
