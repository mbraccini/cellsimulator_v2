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

    private enum EXP_TYPE {
        RND_K2, RND_K2_plus_1, OR_K2, OR_K2_plus_1
    }

    public static final int SAMPLES_NUMBER = 2; //for each number of attractors
    public static final int NUMBER_OF_ATTRACTORS_LIMIT = 2;


    public static Tuple6<Double, Double, Double, Double, Atm<BinaryState>, List<Double>> extEvaluate(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn) {
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

//        if (args.length < 2) {
//            System.err.println("Fornire 2 input [startSelfLoop][stopSelfLoop]");
//            System.exit(-1);
//        }
        System.out.println("MainSamplingEvaluateAndThrowAway 1June2018 - OR_K2");

        Object[] o = GenericUtility.fromArgsStringToObjects(args,
                List.of(Integer.class,
                        Integer.class));

//        int selfLoopStart = (Integer) o[0];
//        int selfLoopStop = (Integer) o[1];

        int selfLoopStart = 0;
        int selfLoopStop = 3;

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        final int k = 2;
        final int nodesNumber = 8;
        final double bias = 0.5;



        while (selfLoopStart <= selfLoopStop) {

            run(selfLoopStart, nodesNumber, k, bias, SAMPLES_NUMBER, r, NUMBER_OF_ATTRACTORS_LIMIT, EXP_TYPE.RND_K2_plus_1);

            selfLoopStart++;
        }
    }


    private static void run(int selfLoop, int nodesNumber, int k, double bias, int samplesForEachAttractorsNumber, RandomGenerator r, int numberOfAttractorsLimit, EXP_TYPE expType) {

        Supplier<Table<BitSet, Boolean>> supplier = () -> new BiasedTable(k, bias, r);
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> current_bn;
        Tuple6<Double, Double, Double, Double,Atm<BinaryState>, List<Double>> evaluation;

        String directory = selfLoop + Files.FILE_SEPARATOR;
        Files.createDirectories(directory);


        int numOfAttractorsWeAreAnalyzing = 1;

        int howManyNetworksGenerated = 0;

        try (BufferedWriter mainCSV = new BufferedWriter(new FileWriter(selfLoop + ".csv", true))) {

            while(numOfAttractorsWeAreAnalyzing <= numberOfAttractorsLimit) {

                int counterSamples = 0;

                while (counterSamples < samplesForEachAttractorsNumber) {
                    if (selfLoop == 0) {
                        current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                    } else if (selfLoop == -1) {
                        System.out.println("numero casuale di selfloop");
                        current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITH, nodesNumber, k, bias, r);
                    } else {
                        current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

                        int selfloopsToAdd = 0;
                        while (selfloopsToAdd < selfLoop) {
                            NodeDeterministic<BitSet,Boolean> node = current_bn.getNodeById(selfloopsToAdd);
                            switch (expType){
                                case OR_K2:
                                            current_bn = new BNClassicBuilder<>(current_bn)
                                                            .reconfigureIncomingEdge(node.getId(), node.getId(), current_bn.getIncomingNodes(node).get(0).getId())
                                                            .replaceNode(node, new NodeDeterministicImpl<>("r_" + node.getName(), node.getId(), new OrTable(node.getFunction().getVariablesNumber())))
                                                            .build();
                                            break;
                                case RND_K2:
                                            current_bn = new BNClassicBuilder<>(current_bn)
                                                            .reconfigureIncomingEdge(node.getId(), node.getId(), current_bn.getIncomingNodes(node).get(0).getId())
                                                            .build();
                                            break;
                                case OR_K2_plus_1:
                                            current_bn = new BNClassicBuilder<>(current_bn)
                                                            .addIncomingNode(node.getId(),node.getId()) //selfloop
                                                            .replaceNode(node,
                                                                            new NodeDeterministicImpl<>("r_" + node.getName(),
                                                                                                        node.getId(),
                                                                                                        UtilitiesBooleanNetwork.extendTable(node.getFunction(),1, () -> true)))
                                                            .build();
                                            break;
                                case RND_K2_plus_1:

                                            current_bn = new BNClassicBuilder<>(current_bn)
                                                            .addIncomingNode(node.getId(),node.getId()) //selfloop
                                                            .replaceNode(node,
                                                                    new NodeDeterministicImpl<>("r_" + node.getName(),
                                                                            node.getId(),
                                                                            UtilitiesBooleanNetwork.extendTable(node.getFunction(),1, () -> r.nextBoolean())))
                                                            .build();
                                            break;
                            }

                            selfloopsToAdd++;
                        }

                        //System.out.println("numSelfLoop " + current_bn.numberOfNodeWithSelfloops());

                    }

                    if (current_bn.numberOfNodeWithSelfloops() != selfLoop) {
                        throw new RuntimeException("Mismatch in selfloops number, expected" + selfLoop  + ", present " + current_bn.numberOfNodeWithSelfloops());
                    }

                    evaluation = extEvaluate(current_bn);

                    int numOfAttractorsInThisNetwork = evaluation._5().getAttractors().numberOfAttractors();

                    if (numOfAttractorsInThisNetwork == numOfAttractorsWeAreAnalyzing) {
                        String subFolder = directory + howManyNetworksGenerated + Files.FILE_SEPARATOR;
                        Files.createDirectories(subFolder);

                        Files.writeBooleanNetworkToFile(current_bn, subFolder + "bn_" + howManyNetworksGenerated);
                        Files.writeAttractorsToReadableFile(evaluation._5().getAttractors().getAttractors(),subFolder + "attrctrs_" + howManyNetworksGenerated);
                        Files.writeListToTxt(List.of(evaluation._6().stream().map(Object::toString).collect(Collectors.joining(" "))), subFolder + "diag_" + howManyNetworksGenerated);
                        new AtmGraphViz(evaluation._5()).saveOnDisk(subFolder + "atm_" + howManyNetworksGenerated);

                        Files.zip(subFolder, subFolder,true); //zip and delete

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


        Files.zip(directory, directory,true); //zip and delete

    }
}
