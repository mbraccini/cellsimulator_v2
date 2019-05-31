package experiments.miRNA;

import dynamic.SynchronousDynamicsImpl;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.TESDifferentiationTree;
import interfaces.tes.Tes;
import network.*;
import tes.StaticAnalysisTES;
import utility.*;
import visualization.DifferentiationTesTreeGraphViz;

import java.math.BigInteger;
import java.util.BitSet;

public class Main_miRNA {

    /**
     * PARAMETERS
     */
    public static final String SAMPLES = "50000";
    public static final int NODES_NUMBER = 50;
    public static final int NODES_K = 2;
    public static final double NODES_BIAS = 0.5;

    public static final double miRNA_BIAS = 0.5;
    public static final int miRNA_K = 1;

    public static final RandomGenerator pureRnd = RandomnessFactory.getPureRandomGenerator();

    public static void main(String[] args) {

        /** pseudorandom generator **/
        long seed = pureRnd.nextLong();
        RandomGenerator pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(seed);

        int iterations = 0;
        while (iterations < 30) {
            System.out.println(iterations);
            oneRun(iterations, pseudoRandom);
            iterations++;
        }

    }

    public static void oneRun(int iteration, RandomGenerator pseudoRandom) {

        String path = "miRNA_" + iteration + Files.FILE_SEPARATOR;
        Files.createDirectories(path);


        /** ORIGINAL BN **/
        String pathBN = path + "originalBN" + Files.FILE_SEPARATOR;
        Files.createDirectories(pathBN);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, NODES_NUMBER, NODES_K, NODES_BIAS, pseudoRandom);

        simulate(bn, pseudoRandom, pathBN);


        /** 5 miRNA, FANOUT=2 **/
        String path_mu5fo2 = path + "mu5fo2" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu5fo2);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu5fo2 = BooleanNetworkFactory.miRNANetworkInstance(bn, 5, miRNA_K, miRNA_BIAS, 2, pseudoRandom);
        simulate(miRNA_mu5fo2, pseudoRandom, path_mu5fo2);

        /** 5 miRNA, FANOUT=5 **/
        String path_mu5fo5 = path + "mu5fo5" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu5fo5);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu5fo5 = BooleanNetworkFactory.miRNANetworkInstance(bn, 5, miRNA_K, miRNA_BIAS, 5, pseudoRandom);
        simulate(miRNA_mu5fo5, pseudoRandom, path_mu5fo5);

        /** 10 miRNA, FANOUT=2 **/
        String path_mu10fo2 = path + "mu10fo2" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu10fo2);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu10fo2 = BooleanNetworkFactory.miRNANetworkInstance(bn, 10, miRNA_K, miRNA_BIAS, 2, pseudoRandom);
        simulate(miRNA_mu10fo2, pseudoRandom, path_mu10fo2);


        /** 10 miRNA, FANOUT=5 **/
        String path_mu10fo5 = path + "mu10fo5" + Files.FILE_SEPARATOR;
        Files.createDirectories(path_mu10fo5);

        BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> miRNA_mu10fo5 = BooleanNetworkFactory.miRNANetworkInstance(bn, 10, miRNA_K, miRNA_BIAS, 5, pseudoRandom);
        simulate(miRNA_mu10fo5, pseudoRandom, path_mu10fo5);
    }

    private static void simulate(BNClassic<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>> bn,RandomGenerator pseudoRandom, String path) {
        Generator<BinaryState> generator = new UniformlyDistributedGenerator(new BigInteger(SAMPLES), bn.getNodesNumber(), pseudoRandom);
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> attractors = StaticAnalysisTES.attractors(generator, dynamics);
        Atm<BinaryState> atm = StaticAnalysisTES.atmFromCompletePerturbations(attractors,dynamics);
        TESDifferentiationTree<BinaryState, Tes<BinaryState>> differentiationTree = StaticAnalysisTES.TESDifferentiationTree(atm,pseudoRandom);

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
        new DifferentiationTesTreeGraphViz<BinaryState>(differentiationTree).saveOnDisk(path + "diffTree");

    }



}
