package simulator;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import network.RBNExactBias;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.BitSet;
import java.util.Random;

public class Core {
    public static void main(String [] args) {


        //Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(120); da controllare  perché dava problemi, forse già risolti, nodes=15, k=2, p=0.5
        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(120);
        BooleanNetwork<BitSet, Boolean> bn;
        bn = new RBNExactBias(22, 2 , 0.5, pseudoRandom);


        System.out.println(bn);
        System.out.println(bn.getNetworkProperties());
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);


        DateTime startDate = new DateTime();
        ImmutableList<LabelledOrderedAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        System.out.println("1-:" + attractors);
        DateTime endDate = new DateTime();
        Interval interval = new Interval(startDate, endDate);
        System.out.println("1-Duration in seconds: " + interval.toDuration().getStandardSeconds());

        Generator<BinaryState> generator2 = new CompleteGenerator(bn.getNodesNumber());

        final int poolSize = Runtime.getRuntime().availableProcessors() + 1;
        DateTime startDate2 = new DateTime();
        ImmutableList<LabelledOrderedAttractor<BinaryState>> attractors2 = new ConcurrentAttractorFinderService<BinaryState>(generator2, dynamics, poolSize).call();
        System.out.println("multi-:" + attractors2);
        DateTime endDate2 = new DateTime();
        Interval interval2 = new Interval(startDate2, endDate2);
        System.out.println("multi-Duration in seconds: " + interval2.toDuration().getStandardSeconds());

    }

}
