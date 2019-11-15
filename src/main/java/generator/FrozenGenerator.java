package generator;

import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigInteger;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FrozenGenerator implements Generator<BinaryState> {

    Generator<BinaryState> internalGenerator;
    public FrozenGenerator(BigInteger samples,
                           int nodesNumber,
                           RandomGenerator randomInstance,
                           Collection<Integer> indicesToKnockOut,
                           boolean withReplacement){
        Collector<BinaryState, ?, ? extends Collection<BinaryState>> myCollectors;
        if (withReplacement) {
            myCollectors = Collectors.toList();
        } else {
            myCollectors = Collectors.toSet();
        }

        Generator<BinaryState> gen =
                new UniformlyDistributedGenerator(samples, nodesNumber,randomInstance);

        this.internalGenerator = new BagOfStatesGenerator<BinaryState>(Stream.generate(gen::nextSample)
                .limit(samples.intValue())
                .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0])))
                .collect(myCollectors));
    }

    @Override
    public BinaryState nextSample() {
        return this.internalGenerator.nextSample();
    }

    @Override
    public BigInteger totalNumberOfSamplesToBeGenerated() {
        return this.internalGenerator.totalNumberOfSamplesToBeGenerated();
    }
}
