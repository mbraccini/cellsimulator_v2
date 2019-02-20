package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractor;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import states.ImmutableBinaryState;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainFrozenEstimateOfDiversity {


    //ANALISI 2
    static final int BN_SAMPLES = 100;
    //static final String CSV_SEPARATOR = ",";
    static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "100000";
    static final Integer HOW_MANY_PAIRS_OF_TRIPLET = 1000;

    public static void main(String args[]) {

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = Integer.valueOf(args[0]);
        int k = Integer.valueOf(args[1]);
        double bias = Double.valueOf(args[2]);

        /*
        int numNodes = 50;
        int k = 2;
        double bias = 0.5;
        */

        System.out.println("...PAIRS_OF_TRIPLET...\n" +
                "numNodes: " + numNodes + "\n" +
                "k: " + k + "\n" +
                "bias: " + bias + "\n" +
                "HOW_MANY_PAIRS_OF_TRIPLET: " + HOW_MANY_PAIRS_OF_TRIPLET + "\n" +
                "COMBINATIONS_FOR_COMPUTING_ATTRS: " + COMBINATIONS_FOR_COMPUTING_ATTRS
        );

        String pathFolder = "pOt_" + k + bias + Files.FILE_SEPARATOR;
        Files.createDirectories(pathFolder);
        String filename = "n" + numNodes + "k" + k + "p" + bias;
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + filename + "_stats.csv", true))) {
            for (int i = 0; i < BN_SAMPLES; i++) {
                forEachBN(i, numNodes, k, bias, r, csv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static private void forEachBN(final int id,
                                  final int numNodes,
                                  final int k,
                                  final double bias,
                                  final RandomGenerator r,
                                  final BufferedWriter stats) throws IOException {
        final int LAST_FROZEN_NODE = 20;
        // GENERATE BN
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, numNodes, k, bias, r);
        // FOR EACH INITIAL NUMBER OF NODES FROZEN
        for (Integer frozenNodesNumber : IntStream.range(0, LAST_FROZEN_NODE + 1).boxed().collect(Collectors.toList())) {
            //FREEZE
            List<Integer> indicesToFreeze = IntStream.range(0, frozenNodesNumber).boxed().collect(Collectors.toList());
            //KNOCK OUT DYNAMICS
            Dynamics<BinaryState> dynamics = DecoratingDynamics
                    .from(new SynchronousDynamicsImpl(bn))
                    .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToFreeze));

            Generator<BinaryState> samples
                    = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_COMPUTING_ATTRS), numNodes, r);

            Generator<BinaryState> generator = new BagOfStatesGenerator<>(Stream.generate(samples::nextSample)
                    .limit(Integer.valueOf(COMBINATIONS_FOR_COMPUTING_ATTRS))
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreeze.toArray(new Integer[0])))
                    .collect(Collectors.toList()));

            Attractors<BinaryState> attrs = StaticAnalysisTES.attractors(generator, dynamics);

            // WE CHOOSE A RND ATTRACTOR
            int numAttrs = attrs.numberOfAttractors();
            ImmutableAttractor<BinaryState> attr = attrs.getAttractors().get(r.nextInt(numAttrs));

            // THE FIRST PHASE
            BinaryState initialState = attr.getFirstState();


            // GENERATE PHENOTYPE
            List<Integer> phenotype = phenotype(10, numNodes, r);
            // STATISTICS VALUES
            int countDifferentAttractorsTriplets = 0;
            int countDifferentAttractorsPhenotype = 0;
            //GENERATE TRIPLETS
            for (int i = 0; i < HOW_MANY_PAIRS_OF_TRIPLET; i++) {
                // WE GENERATE 2 RND TRIPLET FROM THE FREE NODES (NOT FROZEN)
                List<List<Integer>> triplets = new ArrayList<>();
                for (int j = 0; j < 2; j++) {
                    List<Integer> triplet = new ArrayList<>();
                    triplet.add(r.nextInt(numNodes - frozenNodesNumber) + frozenNodesNumber);
                    do {
                        int temp = r.nextInt(numNodes - frozenNodesNumber) + frozenNodesNumber;
                        if (!triplet.contains(temp)) {
                            triplet.add(temp);
                        }
                    } while (triplet.size() < 3);
                    triplets.add(triplet);
                }
                // WE FREEZE THE TRIPLETS AND CHECK IF THE 2 REACHED ATTRACTORS ARE EQUAL FOR THE REMAINING (FREE) PARTS
                List<ImmutableAttractor<BinaryState>> pairsOfAttrs = new ArrayList<>();
                for (int j = 0; j < 2; j++) {
                    List<Integer> idxs = new ArrayList<>(indicesToFreeze);
                    indicesToFreeze.addAll(triplets.get(j));
                    pairsOfAttrs.add(freezeAndComputeAttractor(bn, initialState, idxs));
                }


                //System.out.println("0: " +pairsOfAttrs.get(0));
                //System.out.println("1: " +pairsOfAttrs.get(1));

                if (pairsOfAttrs.get(0).getLength().intValue() == pairsOfAttrs.get(1).getLength().intValue()) {
                    Set<Integer> all = IntStream.range(0, numNodes).boxed().collect(Collectors.toSet()); //all indices
                    Set<Integer> alreadyFrozen = new HashSet<>(indicesToFreeze);
                    alreadyFrozen.addAll(triplets.get(0));
                    alreadyFrozen.addAll(triplets.get(1));
                    all.removeAll(alreadyFrozen);
                    List<Integer> indicesToMaintainList = new ArrayList<>(all);
                    Collections.sort(indicesToMaintainList);

                    //System.out.println("0 trip: " +triplets.get(0));
                    //System.out.println("1 trip: " +triplets.get(1));
                    //System.out.println("indicesToMaintainList: " +indicesToMaintainList);

                    // TRIPLETS
                    if (checkIfEqual(pairsOfAttrs.get(0), pairsOfAttrs.get(1), indicesToMaintainList)) {
                        countDifferentAttractorsTriplets++;
                    }
                    if (checkIfEqual(pairsOfAttrs.get(0), pairsOfAttrs.get(1), phenotype)) {
                        countDifferentAttractorsPhenotype++;
                    }
                }
            }
            stats.append(countDifferentAttractorsTriplets + ", " + countDifferentAttractorsPhenotype);
            if (frozenNodesNumber == LAST_FROZEN_NODE){
                stats.append("\n");
            } else {
                stats.append(",");
            }
        }
    }

    private static Boolean checkIfEqual(Attractor<BinaryState> att1, Attractor<BinaryState> att2, List<Integer> indicesToMaintainList){
        List<BinaryState> first = constructNewAttractor(att1, indicesToMaintainList);
        List<BinaryState> second = constructNewAttractor(att2, indicesToMaintainList);
        //System.out.println("first: " +first);
        //System.out.println("second: " +second);

        if (first.equals(second)){
            //System.out.println("UGUALI");
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private static List<Integer> phenotype(int numOfIndices, int numNodes, RandomGenerator r){
        List<Integer> phenotype = new ArrayList<>();
        phenotype.add(r.nextInt(numNodes));
        int val;
        do {
            val = r.nextInt(numNodes);
            if (!phenotype.contains(val)){
                phenotype.add(val);
            }
        } while(phenotype.size() < 10);
        return phenotype;
    }

    private static List<BinaryState> constructNewAttractor(Attractor<BinaryState> oldAttractor, List<Integer> indicesToMaintainList){
        List<BinaryState> thisAttractor = new ArrayList<>();

        for (BinaryState state: oldAttractor.getStates()) {
            BitSet newBit = new BitSet(indicesToMaintainList.size());
            int i = 0;
            for (Integer j : indicesToMaintainList) {
                newBit.set(i,state.getNodeValue(j));
                i++;
            }
            thisAttractor.add(new ImmutableBinaryState(indicesToMaintainList.size(), newBit));
        }

        Collections.sort(thisAttractor);
        return thisAttractor;
    }

    static private ImmutableAttractor<BinaryState> freezeAndComputeAttractor(BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                  BinaryState initialState,
                                                  List<Integer> indicesToFreeze) {



        //KNOCK OUT DYNAMICS
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToFreeze));

        //GENERATOR
        Generator<BinaryState> genStartingState =
                new BagOfStatesGenerator<>(List.of(initialState));

        Generator<BinaryState> genKO = new BagOfStatesGenerator<>(Stream.generate(genStartingState::nextSample)
                .limit(1)
                .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreeze.toArray(new Integer[0])))
                .collect(Collectors.toList()));
        //ATTRACTORS
        Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genKO, dynamicsKO);
        if (atts.numberOfAttractors() != 1) {
            throw new IllegalStateException("Not Admissible number of attractors starting from one intial state");
        }
        return atts.getAttractors().get(0);
    }
}