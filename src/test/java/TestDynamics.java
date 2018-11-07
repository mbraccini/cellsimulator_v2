import dynamic.FrozenNodesDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.BeforeClass;
import org.junit.Test;
import utility.RandomnessFactory;

import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDynamics {

    static RandomGenerator randomInstance;
    static int iterations;

    @BeforeClass
    public static void initializationRunOnce() {
        randomInstance = RandomnessFactory.getPureRandomGenerator();
    }

    @Test
    public void test_FrozenDynamicsDecorator(){

        int nodesNumber = 20;
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn =
                BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL,BooleanNetworkFactory.SelfLoop.WITHOUT,nodesNumber, 3, 0.5, randomInstance);
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        List<Tuple2<Integer,Boolean>> l = List.of(new Tuple2<>(randomInstance.nextInt(nodesNumber),Boolean.FALSE),
                                                                new Tuple2<>(randomInstance.nextInt(nodesNumber),Boolean.TRUE),
                                                                new Tuple2<>(randomInstance.nextInt(nodesNumber),Boolean.FALSE));

        Dynamics<BinaryState> dynamics = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn,
                      l));

        //TEST FOR 50 SAMPLES (INTIAL STATES)
        for (int i = 0; i < 500; i++) {
            BinaryState sample = generator.nextSample();
            //100 STEPS OF UPDATING FOR EACH INITIAL STATE
            for (int j = 0; j < 100; j++) {
                sample = dynamics.nextState(sample);

                for (Tuple2<Integer,Boolean> t : l) {
                    assertEquals("Nodes specified by means of the list MUST BE FROZEN (SAME VALUES)",t._2(), sample.getNodeValue(t._1()));
                }
            }
        }

    }
}
