package experiments.miRNA;

import dynamic.SynchronousDynamicsImpl;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.TESDifferentiationTree;
import interfaces.tes.Tes;
import network.BooleanNetworkFactory;
import tes.StaticAnalysisTES;
import utility.Files;
import visualization.DifferentiationTesTreeGraphViz;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Random;

public class Main_miRNA2 {
    /**
     * PARAMETERS
     */
    public static final String SAMPLES = "50000";
    public static final int NODES_NUMBER = 50;
    public static final int NODES_K = 2;
    public static final double NODES_BIAS = 0.5;

    public static final int miRNA_FANOUT = 1;
    public static final int NETWORKS_NUMBER = 30;


    public static void main(String[] args) {

        /** pseudorandom generator **/
        RandomGenerator pseudoRandom = RandomnessFactory.getPureRandomGenerator();

        int iterations = 0;
        while (iterations < NETWORKS_NUMBER) {
            System.out.println(iterations);
            oneRun(iterations, pseudoRandom);
            iterations++;
        }

    }

    public static void oneRun(int iteration, RandomGenerator r) {

        String path = "miRNA_" + iteration + Files.FILE_SEPARATOR;
        Files.createDirectories(path);


        System.out.println("original");
        /** 0 miRNA -ORIGINAL BN- **/
        String pathBN = path + "originalBN" + Files.FILE_SEPARATOR;
        Files.createDirectories(pathBN);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN( BNKBias.BiasType.EXACT,
                                                                                                        BooleanNetworkFactory.SelfLoop.WITHOUT,
                                                                                                        NODES_NUMBER,
                                                                                                        NODES_K,
                                                                                                        NODES_BIAS,
                                                                                                        r);

        simulate(bn, r, pathBN);

        System.out.println("mu1");
        /** 1 miRNA**/
        String path_mu1 = path + "mu1" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu1);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu1 = BooleanNetworkFactory.miRNAOneInput(bn, 1, miRNA_FANOUT, r);
        simulate(miRNA_mu1, r, path_mu1);

        System.out.println("mu2");
        /** 2 miRNA**/
        String path_mu2 = path + "mu2" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu2);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu2 = BooleanNetworkFactory.miRNAOneInput(bn, 2, miRNA_FANOUT, r);
        simulate(miRNA_mu2, r, path_mu2);

        System.out.println("mu3");
        /** 3 miRNA**/
        String path_mu3 = path + "mu3" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu3);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu3 = BooleanNetworkFactory.miRNAOneInput(bn, 3, miRNA_FANOUT, r);
        simulate(miRNA_mu3, r, path_mu3);


        System.out.println("mu4");
        /** 4 miRNA**/
        String path_mu4 = path + "mu4" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu4);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu4 = BooleanNetworkFactory.miRNAOneInput(bn, 4, miRNA_FANOUT, r);
        simulate(miRNA_mu4, r, path_mu4);

        System.out.println("mu5");
        /** 5 miRNA**/
        String path_mu5 = path + "mu5" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu5);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu5 = BooleanNetworkFactory.miRNAOneInput(bn, 5, miRNA_FANOUT, r);
        simulate(miRNA_mu5, r, path_mu5);

    }

    private static void simulate(BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> bn,RandomGenerator r, String path) {
        System.out.println("pre-generator");
        Generator<BinaryState> generator = new UniformlyDistributedGenerator(new BigInteger(SAMPLES), bn.getNodesNumber(), r);
        System.out.println("pre-dynamics");
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        System.out.println("pre-attractor-search");
        Attractors<BinaryState> attractors = StaticAnalysisTES.attractors(generator, dynamics);
        System.out.println("pre-atm-complete-perturbations");
        Atm<BinaryState> atm = StaticAnalysisTES.atmFromCompletePerturbations(attractors,dynamics);
        System.out.println("pre-atm-differentiation-tree");
        TESDifferentiationTree<BinaryState, Tes<BinaryState>> differentiationTree = StaticAnalysisTES.TESDifferentiationTree(atm, r);
        System.out.println("pre-write-on-disk");
        writeResultsOnDisk(path, bn, atm, attractors, differentiationTree);
    }



    private static void writeResultsOnDisk(String path,
                                           BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> bn,
                                           Atm<BinaryState> atm,
                                           Attractors<BinaryState> attractors,
                                           TESDifferentiationTree<BinaryState, Tes<BinaryState>> differentiationTree) {
        /* bn */
        Files.writeStringToFileUTF8(path + "bn", BNClassic.getBNFileRepresentation(bn));

        /* atm */
        Files.writeMatrixToCsv(atm.getMatrixCopy(), path + "atm",null);

        /* attractors */
        Files.writeAttractorsToReadableFile(attractors.getAttractors(), path + "attractors");

        /* differentiation tree */
        new DifferentiationTesTreeGraphViz<>(differentiationTree).saveOnDisk(path + "diffTree");

    }



}
