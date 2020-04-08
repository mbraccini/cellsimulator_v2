package generator;

import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FrozenGenerator implements Generator<BinaryState> {

    Generator<BinaryState> internalGenerator;
    public FrozenGenerator(BigInteger samples,
                           int nodesNumber,
                           RandomGenerator randomInstance,
                           List<Integer> indicesToKnockOut){

        internalGenerator = new FixedNodesGenerator(samples,
                nodesNumber,
                randomInstance,
                indicesToKnockOut,
                IntStream.range(0, indicesToKnockOut.size()).mapToObj(i -> Boolean.FALSE).collect(Collectors.toList()));
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
