package cell;

import interfaces.state.BinaryState;

import java.util.Random;

public class NoiseFunction implements java.util.function.Function<BinaryState, BinaryState> {

    Random random;

    public NoiseFunction(Random random) {
        this.random = random;
    }

    @Override
    public BinaryState apply(BinaryState binaryState) {
        int i = random.nextInt(binaryState.getLength()); // sbagliato perché la successiva invocazione di questa funzione con lo stesso binaryState produrrà un diverso stato!!!
        System.out.println(i);
        return binaryState.flipNodesValues(i);
    }
}
