package generator;

import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import states.ImmutableBinaryState;
import utility.Randomness;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;

public class UniformlyDistributedGenerator implements Generator<BinaryState> {

    private final int nodesNumber;
    private final Random random;
    private BigInteger start;
    private final BigInteger end;
    private BitSet bitSetState;
    private BinaryState binaryState;

    public UniformlyDistributedGenerator(BigInteger combinations, int nodesNumber, Random randomInstance) {
        this.nodesNumber = nodesNumber;
        this.start = BigInteger.ZERO;
        this.end = combinations;
        this.random = randomInstance;

        this.bitSetState = new BitSet(nodesNumber);
    }

    @Override
    public BinaryState nextSample() {
        if (start.compareTo(this.end) < 0) { //corrisponde a if (this.sample < this.combinations){
            start = start.add(BigInteger.ONE);

            nextStateUniformlyDistributed();

            binaryState = new ImmutableBinaryState(nodesNumber, bitSetState);
            return binaryState;
        }
        return null;
    }

    private void nextStateUniformlyDistributed() {
        bitSetState.clear();
        for (int i = 0; i < nodesNumber; i++) {
            if (Randomness.randomBooleanOutcome(0.5, random)) {
                bitSetState.set(i);
            }
        }

    }

    @Override
    public BigInteger totalNumberOfSamplesToBeGenerated() {
        return end;
    }


    public static void main(String[] args) {
        Generator<BinaryState> generator = new UniformlyDistributedGenerator(new BigInteger("3"), 5, RandomnessFactory.newPseudoRandomGenerator(4));

        BinaryState state = generator.nextSample();

        while (state != null) {
            System.out.println(state.getStringRepresentation());
            state = generator.nextSample();
        }
    }
}



