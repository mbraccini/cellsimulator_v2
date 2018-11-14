package experiments.methylation;

import com.google.common.math.Stats;
import dynamic.FrozenNodesDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainFrozenFromAttractorStates {
    static BigInteger INITIAL_SAMPLES_STATES_NUMBER = BigInteger.valueOf(1000);

    public static void main(String args[]) {

        System.out.println("MainFrozenFromAttractorStates_100samples_RNDstates_1000InitialStates");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = 50;
        int k = 2;
        double bias = 0.5;

        int SAMPLES = 100;

        int[] frozenNodesConfigurations = new int[]{1, 2, 3, 5, 10, 20, 30, 40};

        for (int config : frozenNodesConfigurations) {
            forEachConfiguration(config, numNodes, k, bias, SAMPLES, r);
        }

    }

    static private void forEachConfiguration(int numOfFrozenNodes,
                                             int numNodes,
                                             int k,
                                             double bias,
                                             int samples,
                                             RandomGenerator r
    ) {
        String configurationPath = numOfFrozenNodes + "_frozen" + Files.FILE_SEPARATOR;
        Files.createDirectories(configurationPath);

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;

        try (FileWriter fw = new FileWriter(numOfFrozenNodes + "_frozen_summmary.txt", true); //in append mode
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(
                    "id\t" +
                    "attrWild\tpfWild\tmeanLengthWild\tstdLengthWild\tmeanTransWild\tstdTransWild\t" +
                    "attrFroz\tpfFroz\tmeanLengthFroz\tstdLengthFroz\tmeanTransFroz\tstdTransFroz");
            while (samples > 0) {
                //BN GENERATION
                bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, numNodes, k, bias, r);


                //BN (wild-type) ANALYSIS
                Dynamics<BinaryState> dynWildType = new SynchronousDynamicsImpl(bn);
                Generator<BinaryState> genWildType = new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, bn.getNodesNumber(), r);
                Attractors<BinaryState> attrsWildType = StaticAnalysisTES.attractors(genWildType, dynWildType);


                List<Integer> frozenIndices = Stream
                        .generate(() -> r.nextInt(numNodes))
                        .limit(numOfFrozenNodes)
                        .collect(Collectors.toList());


                //BN (frozen) ANALYSIS
                Dynamics<BinaryState> dynFrozenType = DecoratingDynamics
                        .from(dynWildType)
                        .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, frozenIndices));

                /****** generator from attractors' states *******/
                //Set<BinaryState> newInitialStates = attrsWildType.getAttractors().stream().flatMap(a -> a.getStates().stream()).collect(Collectors.toSet());
                //Generator<BinaryState> genFrozenType = new BagOfStatesGenerator<>(newInitialStates);
                /****** RND generator ***************************/
                Generator<BinaryState> genFrozenType = new UniformlyDistributedGenerator(INITIAL_SAMPLES_STATES_NUMBER, bn.getNodesNumber(), r);
                /****** generator RND + 1 step for each state ***/
                genFrozenType = new BagOfStatesGenerator<>(Stream.generate(genFrozenType::nextSample).limit(INITIAL_SAMPLES_STATES_NUMBER.intValue()).map(dynWildType::nextState).collect(Collectors.toList()));

                Attractors<BinaryState> attrsFrozenType = StaticAnalysisTES.attractors(genFrozenType, dynFrozenType);
                //SAVING DATA ON DISK
                resultOnDisk(bn, attrsWildType, attrsFrozenType, configurationPath, samples);

                Stats statLengthWild = Stats.of(attrsWildType.getAttractors().stream().mapToInt(x -> x.getLength()).iterator());
                Stats statLengthFrozen = Stats.of(attrsFrozenType.getAttractors().stream().mapToInt(x -> x.getLength()).iterator());

                Stats statTransWild = Stats.of(attrsWildType
                                                .getAttractors()
                                                .stream()
                                                .mapToDouble(x -> x.getTransientsLengths().get().stream().mapToInt(y -> y).average().getAsDouble())
                                                .iterator());
                Stats statTransFrozen = Stats.of(attrsFrozenType
                                                    .getAttractors()
                                                    .stream()
                                                    .mapToDouble(x -> x.getTransientsLengths().get().stream().mapToInt(y -> y).average().getAsDouble())
                                                    .iterator());



                out.println(samples + "\t"
                        //WILD
                            + attrsWildType.numberOfAttractors() + "\t"
                            + attrsWildType.getNumberOfFixedPoints() + "\t"
                            + statLengthWild.mean() + "\t"
                            + (statLengthWild.count() > 1 ? statLengthWild.sampleStandardDeviation() : " ") + "\t"
                            + statTransWild.mean() + "\t"
                            + (statTransWild.count() > 1 ? statTransWild.sampleStandardDeviation() : " ") + "\t"
                        //FROZEN
                            + attrsFrozenType.numberOfAttractors() + "\t"
                            + attrsFrozenType.getNumberOfFixedPoints() + "\t"
                            + statLengthFrozen.mean() + "\t"
                            + (statLengthFrozen.count() > 1 ? statLengthFrozen.sampleStandardDeviation() : " ") + "\t"
                            + statTransFrozen.mean() + "\t"
                            + (statTransFrozen.count() > 1 ? statTransFrozen.sampleStandardDeviation() : " ")
                            );

                //DECREASE OF SAMPLES NUMBER
                samples--;
            }


        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }


    static private void resultOnDisk(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                     Attractors<BinaryState> attrsWildType,
                                     Attractors<BinaryState> attrsFrozenType,
                                     String path,
                                     Integer id) {
        //BN SAVE
        Files.writeBooleanNetworkToFile(bn, path + id + "_id_bn");
        Files.writeAttractorsToReadableFile(attrsWildType, path + id + "_id_attrs_wild");
        Files.writeAttractorsToReadableFile(attrsFrozenType, path + id + "_id_attrs_frozen");
    }

}
