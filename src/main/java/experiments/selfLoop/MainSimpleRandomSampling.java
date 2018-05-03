package experiments.selfLoop;

import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;
import interfaces.network.Table;
import network.BiasedTable;
import network.BooleanNetworkFactory;
import org.jooq.lambda.tuple.Tuple5;
import utility.Files;
import utility.GenericUtility;
import utility.MatrixUtility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class MainSimpleRandomSampling {

    public static Tuple5<Double, Double, Double, Double, Integer> extEvaluate(BooleanNetwork<BitSet, Boolean> bn) {
        Double[][] atm = GeneticAlgFitness.simulateBN(bn).getMatrixCopy();
        Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atm);
        double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);

        return new Tuple5<>(GeneticAlgFitness.f1_robustness_min(doubleSorted),
                GeneticAlgFitness.f2_equallyDistributed(doubleSorted),
                GeneticAlgFitness.f3_triangleDifference(doubleSorted),
                GeneticAlgFitness.f4_robustness_max(doubleSorted),
                atm.length
        );
    }


    public static void main(String[] args) {

        boolean inputFormTerminal = false;

        if (inputFormTerminal) {
            if (args.length < 6) {
                System.exit(-1);
            }

            Object[] o = GenericUtility.fromArgsStringToObjects(args,
                    List.of(Integer.class,
                            Integer.class,
                            Double.class,
                            Integer.class,
                            Long.class,
                            Integer.class));

            int k = (Integer) o[0];
            int nodesNumber = (Integer) o[1];
            double bias = (Double) o[2];
            int samples = (Integer) o[3];
            long seed = (Long) o[4];
            Integer selfLoop = (Integer) o[5];

        /*int k = 2;
        int nodesNumber = 5;
        double bias = 0.5;
        int samples = 10;
        long seed = 2;
        Integer selfLoop = 5;*/


            if (selfLoop > nodesNumber) {
                System.exit(-1);
            }

            System.out.println("NODES_NUMBER: " + nodesNumber);
            System.out.println("K: " + k);
            System.out.println("BIAS: " + bias);
            System.out.println("SAMPLES: " + samples);
            System.out.println("SEED: " + seed);
            System.out.println("SELF_LOOP: " + selfLoop);


            Random r = RandomnessFactory.newPseudoRandomGenerator(seed);

            run(selfLoop, nodesNumber, k, bias, samples, r);


        } else {

            Random r = RandomnessFactory.getPureRandomGenerator();

            int k = 2;
            int nodesNumber = 15;
            double bias = 0.5;
            int samples = 10000;

            int selfLoop = 0;

            while (selfLoop <= nodesNumber) {

                run(selfLoop, nodesNumber, k, bias, samples, r);

                selfLoop++;
            }
        }

    }


    private static void run(int selfLoop, int nodesNumber, int k, double bias, int samples, Random r) {

        Supplier<Table<BitSet, Boolean>> supplier = () -> new BiasedTable(k, bias, r);
        BooleanNetwork<BitSet, Boolean> current_bn;
        int counter = 0;
        Tuple5<Double, Double, Double, Double, Integer> evaluation;

        String directory = selfLoop + Files.FILE_SEPARATOR;
        Files.createDirectories(directory);


        try (BufferedWriter bw = new BufferedWriter(new FileWriter(selfLoop + ".csv", true))) {


            while (counter < samples) {
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

                Files.writeBooleanNetworkToFile(current_bn, directory + "bn_" + counter);

                String res = counter + ";"
                        + evaluation.v1() + ";"
                        + evaluation.v2() + ";"
                        + evaluation.v3() + ";"
                        + evaluation.v4() + ";"
                        + evaluation.v5() + Files.NEW_LINE;

                bw.write(res);
                counter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        Files.zip(directory, directory,true);

    }
}
