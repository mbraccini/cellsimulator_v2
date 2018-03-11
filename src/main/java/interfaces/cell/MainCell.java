package interfaces.cell;

import cell.CellImpl;
import cell.NoiseFunction;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.BitsStreamGenerator;
import states.ImmutableBinaryState;

import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MainCell {

    public static void main(String[] args) {
        Random r = RandomnessFactory.newPseudoRandomGenerator(5);

        BooleanNetwork<BitSet,Boolean> bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.EXACT,
                BooleanNetworkFactory.SelfLoop.WITHOUT,
                5,
                2 ,
                0.5,
                r);

        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        Cell<BitSet,Boolean,BinaryState> cell = new CellImpl<>(bn, dynamics, "pluto");

        System.out.println();

  /*     cell.getCustom(cell.compose(StateFunction.withNoise(4,r))::apply,
                        BinaryState.valueOf("01011"))
                        .stream()
                        .limit(10)
                        .forEach(System.out::println);


*/


        cell.getDefault(BinaryState.valueOf("00101"))
                .stream()
                .limit(10)
                .forEach(System.out::println);


    }
}
