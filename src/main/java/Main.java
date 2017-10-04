import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Generator;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import network.ExactBiasClassical;
import network.ExactBiasSelfLoop;
import network.RBNSelfLoop;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import simulator.AttractorsFinderService;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main (String [] args) {



        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(120);
        BooleanNetwork<BitSet, Boolean> bn;
        // bn = new RBNClassical(20,2, 0.6, pseudoRandom);
        // bn = new BNForTest();
        //bn = new ExactBiasClassical(10, 2 , 0.6, pseudoRandom);
        bn = new RBNSelfLoop(10, 2 , 0.7, pseudoRandom);
        //bn2 = new ExactBiasSelfLoop(100, 3 , 0.7, pseudoRandom);
        System.out.println("AVG bias: " + BooleanNetwork.computeActualAverageBias(bn));

        System.out.println(bn);
        System.out.println(bn.getNetworkProperties());
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        /*generator.nextSample();
        generator.nextSample();
        BinaryState state = generator.nextSample();

        System.out.println(state);
        state = dynamics.nextState(state);
        System.out.println(state);
        state = dynamics.nextState(state);
        System.out.println(state);*/

        DateTime startDate = new DateTime();
        new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        DateTime endDate = new DateTime();
        Interval interval = new Interval(startDate, endDate);
        System.out.println("Duration in seconds: " + interval.toDuration().getStandardSeconds());

        System.out.println("yuppi");

        //System.out.println(Files.readFile("/Users/michelebraccini/Desktop/sshd_config"));
        List.of();
    }

}
