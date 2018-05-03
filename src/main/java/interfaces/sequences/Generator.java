package interfaces.sequences;

import org.jooq.lambda.Seq;

import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Stream;

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


    //Stream<T> asSequentialStream();

    //Seq<T> asSequentialSeq();

}
