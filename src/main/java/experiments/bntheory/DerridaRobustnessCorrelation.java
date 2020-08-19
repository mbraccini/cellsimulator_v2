package experiments.bntheory;

import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.CompleteGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.*;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DerridaRobustnessCorrelation {
    private static final int CUT_OFF = 100;
    private static final int NUM_OF_BNS = 1000;
    static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "100000";

    public static void main(String args[]){
        rbn(args);
    }
    private static void rbn(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int nodesNumber = 50;
        final int k = 2;
        final double bias = 0.5;

        final int RBN_type = Integer.parseInt(args[0]);
        final int SL_type = Integer.parseInt(args[1]);
        final int sl_number = Integer.parseInt(args[2]);

        System.out.println("DerridaRobustnessBasinEntropy");

        if (RBN_type == 0){
            /**
             * RBN
             */
            String folder = "DerridaRobustness_RBN_n"+nodesNumber+"_k_"+k+"_p_"+bias+"_samples_"+COMBINATIONS_FOR_COMPUTING_ATTRS+ Files.FILE_SEPARATOR;
            Files.createDirectories(folder);
            Set<Integer> allIndices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toSet());

            IntStream.range(0, NUM_OF_BNS).forEach(
                    idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                            BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                        analiseNet(bn,folder, idBN, r, allIndices);});
        } else {
            /**
             * SELF-LOOPS
             */
            String folder;
            Set<Integer> allIndices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toSet());

            switch(SL_type) {
                case 0:
                    /**
                     * RND
                     */
                    folder = "DerridaRobustness_SELFLOOPS_RND_"+sl_number+"_n"+nodesNumber+"_k_"+k+"_p_"+bias+"_samples_"+COMBINATIONS_FOR_COMPUTING_ATTRS+ Files.FILE_SEPARATOR;
                    Files.createDirectories(folder);

                    IntStream.range(0, NUM_OF_BNS).forEach(
                            idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                                    BooleanNetworkFactory.newBNwithSelfLoop(k,bias,nodesNumber,r,sl_number, BooleanNetworkFactory.WIRING_TYPE.RND_K_FIXED);
                                analiseNet(bn,folder, idBN, r, allIndices);});
                    break;
                case 1:
                    /**
                     * OR
                     */
                    folder = "DerridaRobustness_SELFLOOPS_OR_"+sl_number+"_n"+nodesNumber+"_k_"+k+"_p_"+bias+"_samples_"+COMBINATIONS_FOR_COMPUTING_ATTRS+ Files.FILE_SEPARATOR;
                    Files.createDirectories(folder);

                    IntStream.range(0, NUM_OF_BNS).forEach(
                            idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                                    BooleanNetworkFactory.newBNwithSelfLoop(k,bias,nodesNumber,r,sl_number, BooleanNetworkFactory.WIRING_TYPE.OR_K_FIXED);
                                analiseNet(bn,folder, idBN, r, allIndices);});
                    break;
                case 2:
                    /**
                     * AND
                     */
                    folder = "DerridaRobustness_SELFLOOPS_AND_"+sl_number+"_n"+nodesNumber+"_k_"+k+"_p_"+bias+"_samples_"+COMBINATIONS_FOR_COMPUTING_ATTRS+ Files.FILE_SEPARATOR;
                    Files.createDirectories(folder);

                    IntStream.range(0, NUM_OF_BNS).forEach(
                            idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                                    BooleanNetworkFactory.newBNwithSelfLoop(k,bias,nodesNumber,r,sl_number, BooleanNetworkFactory.WIRING_TYPE.AND_K_FIXED);
                                analiseNet(bn,folder, idBN, r, allIndices);});
                    break;
            }

        }
    }

    private static void analiseNet(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                   final String folder,
                                   final int idBN,
                                   final RandomGenerator r,
                                   final Set<Integer> indicesToPerturb) {
        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        /***
         * No. of samples equal to COMBINATIONS_FOR_COMPUTING_ATTRS for computing attractors.
         */
        Generator<BinaryState> gen = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_COMPUTING_ATTRS),
                                                                        bn.getNodesNumber(),
                                                                        r);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,
                                                                    dyn,
                                                                    true,
                                                                    false,
                                                                    AttractorsFinderService.TRUE_TERMINATION);

        /**
         * Basins's sizes
         */
        List<Double> basins = IntStream.rangeClosed(1, atts.numberOfAttractors())
                .mapToDouble(idAttractor -> atts.getAttractorById(idAttractor).getBasinSize().get())
                .boxed()
                .collect(Collectors.toList());

        /**
         * Complete perturbations for ATM
         */
        Atm<BinaryState> atm = new CompletePerturbations().apply(atts,dyn,CUT_OFF);
        Double[][] atmM = atm.getMatrixCopy();
        //System.out.println("matrix");
        //GenericUtility.printMatrix(atmM);
        //System.out.println("header"+Arrays.toString(atm.header()));
        //System.out.println("diag");

        /***
         * Derrida
         */
        List<Double> derrida = IntStream.rangeClosed(1, atts.numberOfAttractors())
                .mapToDouble(idAttractor -> AnalysisUtility.Derrida(new BagOfStatesGenerator<>(atts.getAttractorById(idAttractor).getStates()), dyn, indicesToPerturb))
                .boxed()
                .collect(Collectors.toList());

        //BagOfStatesGenerator<BinaryState> att1 = new BagOfStatesGenerator<>(atts.getAttractorById(1).getStates());

        //System.out.println("derrida  : "+ derrida);
        /***
         * ATM Diagonal
         */
        double[] diag = MatrixUtility.mainDiagonal(atmM);
        List<Double> diagList = Arrays.stream(diag).boxed().collect(Collectors.toList());
        //System.out.println( Arrays.toString(diag));

        /***
         * To files
         */
        Files.writeListsToCsv(List.of(derrida,diagList,basins),folder + idBN + "_DerridaRobustnessBasins.csv");
        Files.writeBooleanNetworkToFile(bn, folder + idBN +  "_bn");
        Files.writeAttractorsToReadableFile(atts, folder + idBN + "_attrs");
    }
}
