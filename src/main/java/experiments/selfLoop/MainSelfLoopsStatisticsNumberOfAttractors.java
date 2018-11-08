package experiments.selfLoop;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.Constant;
import utility.Files;
import utility.MatrixUtility;
import utility.RandomnessFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSelfLoopsStatisticsNumberOfAttractors {


    public static final int SAMPLES_NUMBER = 20000; //for each configuration
    public static final int FROM_NUMBER_OF_SELFLOOPS = 0;
    public static final int TO_NUMBER_OF_SELFLOOPS = 15;


    public static void main(String[] args) {
        System.out.println("v3.0");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = null;


        for (BooleanNetworkFactory.WIRING_TYPE wiringType : BooleanNetworkFactory.WIRING_TYPE.values()) {
            String directory = wiringType + Files.FILE_SEPARATOR;
            Files.createDirectories(directory);
            String subBNfolder = directory + "BNs" + Files.FILE_SEPARATOR;
            Files.createDirectories(subBNfolder);

            for (int i = FROM_NUMBER_OF_SELFLOOPS; i <= TO_NUMBER_OF_SELFLOOPS; i++) {
                List<String[]> attrctrs = new ArrayList<>();
                List<String[]> minMaxDiagonalATM = new ArrayList<>();


                for (int j = 0; j < SAMPLES_NUMBER; j++) {
                    bn = BooleanNetworkFactory.newBNwithSelfLoop(2, 0.5, 15, r, i, wiringType);

                    Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
                    Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
                    Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics, true,false);
                    //ATM
                    Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
                    Double[][] atmMtrx = atm.getMatrixCopy();
                    Number[][] sorted = MatrixUtility.reorderByDiagonalValues(atmMtrx);
                    double[][] doubleSorted = MatrixUtility.fromNumberToDoubleMatrix(sorted);
                    List<Double> sortedDiagonalValues = IntStream.range(0, doubleSorted.length)
                            .mapToDouble(x -> doubleSorted[x][x])
                            .boxed()
                            .collect(Collectors.toList());

                    attrctrs.add(new String[]{attractors.numberOfAttractors().toString(),attractors.getNumberOfFixedPoints().toString()});
                    minMaxDiagonalATM.add(new String[]{sortedDiagonalValues.get(0).toString(), sortedDiagonalValues.get(sortedDiagonalValues.size() - 1).toString()});

                    Files.writeBooleanNetworkToFile(bn, subBNfolder + Files.FILE_SEPARATOR + "bn_sl_" + i + "#_" + j);
                    Files.writeListToTxt(List.of(sortedDiagonalValues.stream().map(Object::toString)
                                                .collect(Collectors.joining(" "))),
                                                subBNfolder + Files.FILE_SEPARATOR +  "bn_sl_" + i + "#_" + j + "_diag");

                }
                Files.writeToCsv(minMaxDiagonalATM, directory + Files.FILE_SEPARATOR + i + "_minMAX");
                Files.writeToCsv(attrctrs, directory + Files.FILE_SEPARATOR + i + "_attrs");
            }
            Files.zip(subBNfolder, subBNfolder,true); //zip and delete
        }
    }



}
