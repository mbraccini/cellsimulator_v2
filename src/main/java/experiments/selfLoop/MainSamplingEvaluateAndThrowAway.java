package experiments.selfLoop;

import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.network.Table;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Tuple6;
import network.*;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;
import visualization.AtmGraphViz;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSamplingEvaluateAndThrowAway {

    public static final int SAMPLES_NUMBER = 30; //for each number of attractors
    public static final int NUMBER_OF_ATTRACTORS_LIMIT = 20;


    public static Tuple6<Double, Double, Double, Double, Atm<BinaryState>, List<Double>> extEvaluate(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn) {
        Atm<BinaryState> atmObj = GeneticAlgFitness.simulateBN(bn);
        Double[][] atm = atmObj.getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);
        List<Double> sortedDiagonalValues = IntStream.range(0, doubleSorted.length)
                .mapToDouble(i -> doubleSorted[i][i])
                .boxed()
                .collect(Collectors.toList());

        return new Tuple6<>(GeneticAlgFitness.f1_robustness_min(doubleSorted),
                GeneticAlgFitness.f2_equallyDistributed(doubleSorted),
                GeneticAlgFitness.f3_triangleDifference(doubleSorted),
                GeneticAlgFitness.f4_robustness_max(doubleSorted),
                atmObj,
                sortedDiagonalValues
        );
    }


    public static void main(String[] args) {

        /*if (args.length < 2) {
            System.err.println("Fornire 2 input [startSelfLoop][stopSelfLoop]");
            System.exit(-1);
        }*/
        System.out.println("MainSamplingEvaluateAndThrowAway - 20 nodi da 0 a 15 self-loop");

        /*Object[] o = GenericUtility.fromArgsStringToObjects(args,
                List.of(Integer.class,
                        Integer.class));
        */
        //int selfLoopStart = (Integer) o[0];
        //int selfLoopStop = (Integer) o[1];

        int selfLoopStart = 0;
        int selfLoopStop = 15;

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        final int k = 2;
        final int nodesNumber = 20;
        final double bias = 0.5;


        //4 casi
        for (BooleanNetworkFactory.WIRING_TYPE wiringType : BooleanNetworkFactory.WIRING_TYPE.values()) {
            String directory = wiringType + Files.FILE_SEPARATOR;
            Files.createDirectories(directory);

            while (selfLoopStart <= selfLoopStop) {
                run(selfLoopStart, nodesNumber, k, bias, SAMPLES_NUMBER, r, NUMBER_OF_ATTRACTORS_LIMIT, wiringType, directory);
                selfLoopStart++;
            }
            selfLoopStart = 0;
        }
    }


    private static void run(int selfLoop, int nodesNumber, int k, double bias, int samplesForEachAttractorsNumber, RandomGenerator r, int numberOfAttractorsLimit, BooleanNetworkFactory.WIRING_TYPE wiringType, String folder) {
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> current_bn = null;
        Tuple6<Double, Double, Double, Double, Atm<BinaryState>, List<Double>> evaluation;

        String directory = folder + selfLoop + Files.FILE_SEPARATOR;
        Files.createDirectories(directory);


        int numOfAttractorsWeAreAnalyzing = 1;

        int howManyNetworksGenerated = 0;

        try (BufferedWriter mainCSV = new BufferedWriter(new FileWriter(folder + selfLoop + ".csv", true))) {

            while (numOfAttractorsWeAreAnalyzing <= numberOfAttractorsLimit) {

                int counterSamples = 0;

                while (counterSamples < samplesForEachAttractorsNumber) {
                    if (selfLoop == 0) {
                        current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                    } else {
                        current_bn = BooleanNetworkFactory.newBNwithSelfLoop(k, bias, nodesNumber, r, selfLoop, wiringType);
                    }

                    if (current_bn.numberOfNodeWithSelfloops() != selfLoop) {
                        throw new RuntimeException("Mismatch in selfloops number, expected" + selfLoop + ", present " + current_bn.numberOfNodeWithSelfloops());
                    }

                    evaluation = extEvaluate(current_bn);

                    int numOfAttractorsInThisNetwork = evaluation._5().getAttractors().numberOfAttractors();

                    if (numOfAttractorsInThisNetwork == numOfAttractorsWeAreAnalyzing) {
                        String subFolder = directory + howManyNetworksGenerated + Files.FILE_SEPARATOR;
                        Files.createDirectories(subFolder);

                        Files.writeBooleanNetworkToFile(current_bn, subFolder + "bn_" + howManyNetworksGenerated);
                        Files.writeAttractorsToReadableFile(evaluation._5().getAttractors().getAttractors(), subFolder + "attrctrs_" + howManyNetworksGenerated);
                        Files.writeListToTxt(List.of(evaluation._6().stream().map(Object::toString).collect(Collectors.joining(" "))), subFolder + "diag_" + howManyNetworksGenerated);
                        new AtmGraphViz(evaluation._5()).saveOnDisk(subFolder + "atm_" + howManyNetworksGenerated);

                        Files.zip(subFolder, subFolder, true); //zip and delete

                        String res = howManyNetworksGenerated + ";"
                                + evaluation._1() + ";"
                                + evaluation._2() + ";"
                                + evaluation._3() + ";"
                                + evaluation._4() + ";"
                                + evaluation._5().getAttractors().numberOfAttractors() + ";"
                                + evaluation._5().getAttractors().getNumberOfFixedPoints() + Files.NEW_LINE;

                        mainCSV.write(res);
                        counterSamples++;
                    }

                    howManyNetworksGenerated++;
                }

                numOfAttractorsWeAreAnalyzing++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Files.zip(directory, directory, true); //zip and delete

    }
}
