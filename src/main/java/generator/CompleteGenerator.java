package generator;

import interfaces.state.BinaryState;
import interfaces.attractor.Generator;
import states.ImmutableBinaryState;
import states.States;

import java.math.BigInteger;
import java.util.BitSet;

public class CompleteGenerator implements Generator<BinaryState> {

    private final int nodesNumber;
    private BigInteger start;
    private final BigInteger end;
    private BitSet bitSetState;
    private BinaryState binaryState;

    public CompleteGenerator(int nodesNumber) {
        this.nodesNumber = nodesNumber;
        this.start = BigInteger.ZERO;

        this.end = BigInteger.valueOf(2).pow(nodesNumber);

        this.bitSetState = new BitSet(nodesNumber);
    }

    @Override
    public BinaryState nextSample() {
        if (this.start.compareTo(this.end) < 0) { //corrisponde a if (this.sample < this.combinations){
            this.start = this.start.add(BigInteger.ONE);
            this.binaryState = new ImmutableBinaryState(this.nodesNumber, this.bitSetState);
            States.addOneToBitSet(this.bitSetState);
            return this.binaryState;
        }
        return null;
    }

    @Override
    public BigInteger totalNumberOfSamplesToBeGenerated() {
        return this.end;
    }


    public static void main(String[] args) {
        Generator<BinaryState> generator = new CompleteGenerator(5);

        BinaryState state = generator.nextSample();

        while (state != null) {
            System.out.println(state.getStringRepresentation());
            state = generator.nextSample();
        }
    }
}

