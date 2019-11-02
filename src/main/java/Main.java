//import dynamic.SynchronousDynamicsImpl;
//import generator.CompleteGenerator;
//import utility.RandomnessFactory;
//import interfaces.attractor.Attractors;
//import interfaces.network.BNClassic;
//import interfaces.sequences.Generator;
//import interfaces.dynamic.Dynamics;
//import interfaces.state.BinaryState;
//import interfaces.tes.Atm;
//import interfaces.tes.TESDifferentiationTree;
//import interfaces.tes.Tes;
//import network.BooleanNetworkFactory;
//import noise.IncompletePerturbations;
//import org.joda.time.DateTime;
//import org.joda.time.Interval;
//import simulator.AttractorsFinderService;
//import tes.TesCreator;
//import utility.Constant;
//import utility.Files;
//import visualization.AtmGraphViz;
//import visualization.BNGraphViz;
//import visualization.DifferentiationTesTreeGraphViz;
//import visualization.DifferentiationTreeGraphViz;
//
//import java.util.Arrays;
//import java.util.BitSet;
//import java.util.List;
//import java.util.Random;
//
//public class Main {
//    public static void main (String [] args) {
//       randomGeneratorpseudoRandom = RandomnessFactory.newPseudoRandomGenerator(120);
//        BNClassic<BitSet, Boolean> bn;
//        // bn = new BNForTest();
//        bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, 5, 2 , 0.5, pseudoRandom);
//
//
//        //bn2 = new SelfLoopBNExactBias(100, 3 , 0.7, pseudoRandom);
//        System.out.println("AVG bias: " + BNClassic.computeActualAverageBias(bn));
//
//        System.out.println(bn);
//        System.out.println(bn.getNetworkProperties());
//        //Generator<BinaryState> generator = new UniformlyDistributedGenerator(BigInteger.valueOf(10000),bn.getNodesNumber(), pseudoRandom);
//        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
//
//        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
//        /*generator.nextSample();
//        generator.nextSample();
//        BinaryState state = generator.nextSample();
//
//        System.out.println(state);
//        state = dynamics.nextState(state);
//        System.out.println(state);
//        state = dynamics.nextState(state);
//        System.out.println(state);*/
//
//        DateTime startDate = new DateTime();
//        Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);
//        Files.writeAttractorsToReadableFile(attractors.getAttractors(), "pkut");
//        System.out.println("DOPO:" + attractors);
//
//        DateTime endDate = new DateTime();
//        Interval interval = new Interval(startDate, endDate);
//        System.out.println("Duration in seconds: " + interval.toDuration().getStandardSeconds());
//
//        //Callable<Atm<BinaryState>> cp = new CompletePerturbations(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
//        Atm<BinaryState> atm = new IncompletePerturbations().apply(attractors, dynamics, 100, 80, Constant.PERTURBATIONS_CUTOFF, pseudoRandom);
//
//
//
//
//
//
//        TESDifferentiationTree<BinaryState, Tes<BinaryState>> differentiationTree = new TesCreator<>(atm, pseudoRandom).call();
//
//
//        //System.out.println(differentiationTree.getRootLevel().get(0).getTreeLikeRepresentation("",true));
//        System.out.println(differentiationTree.getTreeRepresentation());
//
//
//        TESDifferentiationTree<BinaryState, Tes<BinaryState>> differentiationTree2 = new TesCreator<>(atm, Arrays.asList(0.29) ,pseudoRandom).call();
//
//        System.out.println("Con soglia");
//        System.out.println(differentiationTree2.getTreeRepresentation());
//
//        //System.out.println(Files.readFile("/Users/michelebraccini/Desktop/sshd_config"));
//
//        new AtmGraphViz(atm).saveOnDisk("ATM");
//        new BNGraphViz<>(bn).saveOnDisk("BN");
//        new DifferentiationTreeGraphViz<>(differentiationTree).saveOnDisk("DIFFTREE");
//        new DifferentiationTesTreeGraphViz<BinaryState>(differentiationTree).saveOnDisk("DIFFTREE2");
//
//        List.of();
//    }
//
//}


import com.google.common.collect.ImmutableBiMap;
import dynamic.FrozenNodesDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
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
import states.ImmutableBinaryState;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main (String [] args) {
        RandomGenerator r = RandomnessFactory.newPseudoRandomGenerator(2);
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn =
                //BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL,BooleanNetworkFactory.SelfLoop.WITHOUT,10, 2, 0.5, r);
                bn = BooleanNetworkFactory.newBNwithSelfLoop(2, 0.5, 10, r, 1, BooleanNetworkFactory.WIRING_TYPE.OR_K_FIXED);

        System.out.println(bn.getIncomingNodes(bn.getNodeById(0)));
        System.out.println(bn.getIncomingNodes(bn.getNodeById(1)));
        Dynamics<BinaryState> d = new SynchronousDynamicsImpl(bn);
        BinaryState i = ImmutableBinaryState.valueOf("0000000011");
        System.out.println(i);
        System.out.println(d.nextState(i));


        List<Integer> combFrozenIndices = IntStream.range(0,0).boxed().collect(Collectors.toList());
        System.out.println(combFrozenIndices);

        Dynamics<BinaryState> dynFrozen = DecoratingDynamics
                .from(d)
                .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, combFrozenIndices));

        int n = 0;
        BinaryState ta = new ImmutableBinaryState(i.getLength(),i.toBitSet());
        BinaryState tb = new ImmutableBinaryState(i.getLength(),i.toBitSet());
        System.out.println("ta" + ta);
        System.out.println("tb" + tb);
        System.out.println("eq" + ta.equals(tb));
        System.out.println("==" + (ta == tb));

        List<BinaryState> a = new ArrayList<>();
        List<BinaryState> b = new ArrayList<>();

        while(n < 10){
            ta = d.nextState(ta);
            a.add(ta);
            tb = dynFrozen.nextState(tb);
            b.add(tb);
            n++;
        }

        System.out.println("eq list" + a.equals(b));
        System.out.println(a);
        System.out.println(b);

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bbb = null;
        bbb = BooleanNetworkFactory.newBNwithSelfLoop(2, 1, 10, r, 2,  BooleanNetworkFactory.WIRING_TYPE.RND_K_FIXED);
        Files.writeBooleanNetworkToFile(bbb, "chicco");

        /***/
        System.exit(1);
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        Dynamics<BinaryState> dynamics = DecoratingDynamics
                                            .from(new SynchronousDynamicsImpl(bn))
                                            .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn,
                                                    List.of(0,1)));

        Dynamics<BinaryState> dynamics2 = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> att = StaticAnalysisTES.attractors(generator,dynamics2);


        Set<ImmutableAttractor> set = new HashSet<>();
        set.add(att.getAttractors().get(0));
        set.add(att.getAttractors().get(0));

        System.out.println(set);
        System.out.println(set.size());

        Files.writeAttractorsToReadableFile(att, "pluto");

    }
}