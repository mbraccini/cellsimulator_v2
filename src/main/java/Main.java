import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.DifferentiationTree;
import interfaces.tes.Tes;
import network.RBNExactBias;
import network.SelfLoopBN;
import noise.CompletePerturbations;
import noise.IncompletePerturbations;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import simulator.AttractorsFinderService;
import tes.TesCreator;
import utility.GenericUtility;
import visualization.AtmGraphViz;
import visualization.BNGraphViz;
import visualization.DifferentiationTesTreeGraphViz;
import visualization.DifferentiationTreeGraphViz;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

public class Main {
    public static void main (String [] args) {



        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(120);
        BooleanNetwork<BitSet, Boolean> bn;
        // bn = new RBN(20,2, 0.6, pseudoRandom);
        // bn = new BNForTest();
        //bn = new RBNExactBias(10, 2 , 0.6, pseudoRandom);
        bn = new RBNExactBias(15, 2 , 0.5, pseudoRandom);

        //bn2 = new SelfLoopBNExactBias(100, 3 , 0.7, pseudoRandom);
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
        ImmutableList<LabelledOrderedAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        System.out.println("DOPO:" + attractors);

        DateTime endDate = new DateTime();
        Interval interval = new Interval(startDate, endDate);
        System.out.println("Duration in seconds: " + interval.toDuration().getStandardSeconds());

        //Callable<Atm<BinaryState>> cp = new CompletePerturbations(attractors, dynamics, 50000);
        Callable<Atm<BinaryState>> cp = new IncompletePerturbations(attractors, dynamics, 100, 80, 50000, pseudoRandom);
        Atm<BinaryState> atm = null;
        try {
            atm = cp.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GenericUtility.printMatrix(atm.getMatrix());

        Callable<DifferentiationTree<Tes<BinaryState>>> tesCreator = new TesCreator<BinaryState>(atm, pseudoRandom);
        DifferentiationTree<Tes<BinaryState>> differentiationTree = null;
        try {
            differentiationTree = tesCreator.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(differentiationTree.getRootLevel().get(0).getTreeLikeRepresentation("",true));
        System.out.println(differentiationTree.getTreeRepresentation());



        Callable<DifferentiationTree<Tes<BinaryState>>> tesCreator2 = new TesCreator<BinaryState>(atm, Arrays.asList(0.29), pseudoRandom);
        DifferentiationTree<Tes<BinaryState>> differentiationTree2 = null;
        try {
            differentiationTree2 = tesCreator2.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Con soglia");
        System.out.println(differentiationTree2.getTreeRepresentation());

        //System.out.println(Files.readFile("/Users/michelebraccini/Desktop/sshd_config"));

        //new AtmGraphViz(atm,"atm").generateDotFile().generateImg("jpg");
        //new BNGraphViz<>(bn, "bn").generateDotFile().generateImg("jpg");
        new DifferentiationTreeGraphViz<>(differentiationTree, "diffTree2").generateDotFile().generateImg("jpg");
        new DifferentiationTesTreeGraphViz<BinaryState>(differentiationTree, "diffTree4").generateDotFile().generateImg("jpg");

        List.of();
    }

}
