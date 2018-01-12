package experiments.miRNA;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.DifferentiationTree;
import interfaces.tes.Tes;
import network.*;
import noise.CompletePerturbations;
import org.apache.commons.math3.random.BitsStreamGenerator;
import simulator.AttractorsFinderService;
import tes.AtmImpl;
import tes.TesCreator;
import utility.*;
import visualization.AtmGraphViz;
import visualization.BNGraphViz;
import visualization.DifferentiationTesTreeGraphViz;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.Callable;

public class Main_miRNA {

    /**
     * PARAMETERS
     */
    public static final String SAMPLES = "50000";
    public static final int NODES_NUMBER = 50;
    public static final int NODES_K = 2;
    public static final double NODES_BIAS = 0.5;

    public static final double miRNA_BIAS = 0.5;
    public static final int miRNA_K = 2;

    public static final Random pureRnd = RandomnessFactory.getPureRandomGenerator();

    public static void main(String[] args) {

        int iterations = 0;
        while (iterations < 20) {
            System.out.println(iterations);
            oneRun(iterations);
            iterations++;
        }

    }

    public static void oneRun(int iteration) {

        String path = "miRNA_" + iteration + Files.FILE_SEPARATOR;
        Files.createDirectories(path);

        /** pseudorandom generator **/
        long seed = pureRnd.nextLong();
        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(seed);


        /** ORIGINAL BN **/
        String pathBN = path + "originalBN" + Files.FILE_SEPARATOR;
        Files.createDirectories(pathBN);

        BooleanNetwork<BitSet, Boolean> bn = new RBNExactBias(NODES_NUMBER, NODES_K, NODES_BIAS, pseudoRandom);
        simulate(bn, pseudoRandom, pathBN);


        /** 5 miRNA, FANOUT=2 **/
        String path_mu5fo2 = path + "mu5fo2" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu5fo2);

        BooleanNetwork<BitSet, Boolean> miRNA_mu5fo2 = BooleanNetworkFactory.miRNANetworkInstance(bn, 5, miRNA_K, miRNA_BIAS, 2, pseudoRandom);
        simulate(miRNA_mu5fo2, pseudoRandom, path_mu5fo2);

        /** 5 miRNA, FANOUT=5 **/
        String path_mu5fo5 = path + "mu5fo5" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu5fo5);

        BooleanNetwork<BitSet, Boolean> miRNA_mu5fo5 = BooleanNetworkFactory.miRNANetworkInstance(bn, 5, miRNA_K, miRNA_BIAS, 5, pseudoRandom);
        simulate(miRNA_mu5fo5, pseudoRandom, path_mu5fo5);

        /** 10 miRNA, FANOUT=2 **/
        String path_mu10fo2 = path + "mu10fo2" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu10fo2);

        BooleanNetwork<BitSet, Boolean> miRNA_mu10fo2 = BooleanNetworkFactory.miRNANetworkInstance(bn, 10, miRNA_K, miRNA_BIAS, 2, pseudoRandom);
        simulate(miRNA_mu10fo2, pseudoRandom, path_mu10fo2);


        /** 10 miRNA, FANOUT=5 **/
        String path_mu10fo5 = path + "mu10fo5" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu10fo5);

        BooleanNetwork<BitSet, Boolean> miRNA_mu10fo5 = BooleanNetworkFactory.miRNANetworkInstance(bn, 10, miRNA_K, miRNA_BIAS, 5, pseudoRandom);
        simulate(miRNA_mu10fo5, pseudoRandom, path_mu10fo5);
    }

    private static void simulate(BooleanNetwork<BitSet, Boolean> bn, Random pseudoRandom, String path) {
        Generator<BinaryState> generator = new UniformlyDistributedGenerator(new BigInteger(SAMPLES), bn.getNodesNumber(), pseudoRandom);
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        ImmutableList<ImmutableAttractor<BinaryState>> attractors = new AttractorsFinderService<>(generator, dynamics).call();
        CompletePerturbations cp = new CompletePerturbations(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
        Atm<BinaryState> atm = cp.call();
        Callable<DifferentiationTree<Tes<BinaryState>>> tesCreator = new TesCreator<>(atm, pseudoRandom);
        DifferentiationTree<Tes<BinaryState>> differentiationTree = null;
        try {
            differentiationTree = tesCreator.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeResultsOnDisk(path, bn, atm,attractors, differentiationTree);
    }



    private static void writeResultsOnDisk(String path,
                                    BooleanNetwork<BitSet, Boolean> bn,
                                    Atm<BinaryState> atm,
                                    ImmutableList<ImmutableAttractor<BinaryState>> attractors,
                                    DifferentiationTree<Tes<BinaryState>> differentiationTree) {
        /* bn */
        Files.writeStringToFileUTF8(path + "bn", BooleanNetwork.getBNFileRepresentation(bn));

        /* atm */
        Files.writeMatrixToCsv(atm.getMatrixCopy(), path + "atm");

        /* attractors */
        Files.writeAttractorsToReadableFile(attractors, path + "attractors");

        /* differentiation tree */
        new DifferentiationTesTreeGraphViz<BinaryState>(differentiationTree, path + "diffTree").generateDotFile().generateImg("jpg");

    }



}
