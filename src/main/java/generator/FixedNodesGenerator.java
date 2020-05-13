package generator;

import exceptions.GeneratorException;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import org.apache.commons.math3.random.RandomGenerator;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FixedNodesGenerator implements Generator<BinaryState>  {

    Generator<BinaryState> internalGenerator;
    public FixedNodesGenerator(BigInteger samples,
                               int nodesNumber,
                               RandomGenerator randomInstance,
                               List<Integer> indicesToKnock,
                               List<Boolean> relatedValues
                               ){

        if (indicesToKnock.size() != relatedValues.size()){
            throw new GeneratorException.FixedGeneratorException.IndicesValuesDimensionsMismatch();
        }

        List<Integer> zeros = IntStream.range(0,relatedValues.size())
                .filter(idx -> relatedValues.get(idx) == Boolean.FALSE)
                .map(idx -> indicesToKnock.get(idx))
                .boxed()
                .collect(Collectors.toList());
        List<Integer> ones = IntStream.range(0,relatedValues.size())
                .filter(idx -> relatedValues.get(idx) == Boolean.TRUE)
                .map(idx -> indicesToKnock.get(idx))
                .boxed()
                .collect(Collectors.toList());


        Generator<BinaryState> gen =
                new UniformlyDistributedGenerator(samples, nodesNumber,randomInstance);

        this.internalGenerator = new BagOfStatesGenerator<BinaryState>(Stream.generate(gen::nextSample)
                .limit(samples.intValue())
                .map(sample -> {
                    BinaryState newState = sample.setNodesValues(Boolean.FALSE, zeros.toArray(new Integer[0]));
                    return newState.setNodesValues(Boolean.TRUE, ones.toArray(new Integer[0]));
                })
                .collect(Collectors.toList()));
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
