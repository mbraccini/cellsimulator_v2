package experiments.selfLoop;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.network.Table;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import network.BiasedTable;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple6;
import simulator.AttractorsFinderService;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class MainSamplingEvaluateAndThrowAway {


    public static Tuple5<Double, Double, Double, Double, Attractors<BinaryState>> extEvaluate(BooleanNetwork<BitSet, Boolean> bn) {
        Atm<BinaryState> atmObj = GeneticAlgFitness.simulateBN(bn);
        Double[][] atm = atmObj.getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);

        return new Tuple5<>(GeneticAlgFitness.f1_robustness_min(doubleSorted),
                GeneticAlgFitness.f2_equallyDistributed(doubleSorted),
                GeneticAlgFitness.f3_triangleDifference(doubleSorted),
                GeneticAlgFitness.f4_robustness_max(doubleSorted),
                atmObj.getAttractors()
        );
    }


    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println("Fornire 3 input [startSelfLoop][stopSelfLoop][attractorsLimit]");
            System.exit(-1);
        }

        Object[] o = GenericUtility.fromArgsStringToObjects(args,
                List.of(Integer.class,
                        Integer.class,
                        Integer.class));

        int selfLoopStart = (Integer) o[0];
        int selfLoopStop = (Integer) o[1];
        int numberOfAttractorsLimit = (Integer) o[2];


        Random r = RandomnessFactory.getPureRandomGenerator();

        final int k = 2;
        final int nodesNumber = 15;
        final double bias = 0.5;

        int samplesForEachAttractorsNumber = 30; //for each number of attractors



        while (selfLoopStart <= selfLoopStop) {

            run(selfLoopStart, nodesNumber, k, bias, samplesForEachAttractorsNumber, r, numberOfAttractorsLimit);

            selfLoopStart++;
        }
    }


    private static void run(int selfLoop, int nodesNumber, int k, double bias, int samplesForEachAttractorsNumber, Random r, int numberOfAttractorsLimit) {

        Supplier<Table<BitSet, Boolean>> supplier = () -> new BiasedTable(k, bias, r);
        BooleanNetwork<BitSet, Boolean> current_bn;
        Tuple5<Double, Double, Double, Double, Attractors<BinaryState>> evaluation;

        String directory = selfLoop + Files.FILE_SEPARATOR;
        Files.createDirectories(directory);


        int numOfAttractorsWeAreAnalyzing = 1;

        int howManyNetworksGenerated = 0;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(selfLoop + ".csv", true))) {

            while(numOfAttractorsWeAreAnalyzing <= numberOfAttractorsLimit) {

                int counterSamples = 0;

                while (counterSamples < samplesForEachAttractorsNumber) {
                    if (selfLoop == 0) {
                        current_bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                    } else if (selfLoop == -1) {
                        System.out.println("numero casuale di selfloop");
                        current_bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITH, nodesNumber, k, bias, r);
                    } else {
                        current_bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

                        int selfloopsToAdd = 0;
                        while (selfloopsToAdd < selfLoop) {
                            current_bn.reconfigureIncomingEdge(selfloopsToAdd, selfloopsToAdd, 0);
                            selfloopsToAdd++;
                        }

                        System.out.println("numSelfLoop " + current_bn.numberOfNodeWithSelfloops());
                    }

                    evaluation = extEvaluate(current_bn);

                    int numOfAttractorsInThisNetwork = evaluation.v5().numberOfAttractors();

                    if (numOfAttractorsInThisNetwork == numOfAttractorsWeAreAnalyzing) {
                        Files.writeBooleanNetworkToFile(current_bn, directory + "bn_" + howManyNetworksGenerated);

                        String res = howManyNetworksGenerated + ";"
                                + evaluation.v1() + ";"
                                + evaluation.v2() + ";"
                                + evaluation.v3() + ";"
                                + evaluation.v4() + ";"
                                + evaluation.v5().numberOfAttractors() + ";"
                                + evaluation.v5().getNumberOfFixedPoints() + Files.NEW_LINE;

                        bw.write(res);
                        counterSamples++;
                    }

                    howManyNetworksGenerated++;
                }

                numOfAttractorsWeAreAnalyzing++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Files.zip(directory, directory,true);

    }
}
