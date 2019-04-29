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
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import states.ImmutableBinaryState;
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


    static final int BN_SAMPLES = 100;
    //static final String CSV_SEPARATOR = ",";
    static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "1"; //ne basta una di condizione random
    static final Integer HOW_MANY_PAIRS_OF_TRIPLET = 100;
    static final int[] PHENOTYPES_DIMENSION = new int[]{10,50,100};
    public static void main(String args[]) {

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        final int numNodes = Integer.valueOf(args[0]);
        final int k = Integer.valueOf(args[1]);
        final double bias = Double.valueOf(args[2]);
        final int lastFrozenNode = Integer.valueOf(args[3]);
        final int frozenStep = Integer.valueOf(args[4]);
        boolean tripletsBetweenOnes = Boolean.valueOf(args[5]);

        //final int phenotypeDimension = Integer.valueOf(args[4]);
        //final int phenotypeDimension = 10;
        /*
        final int numNodes = 200;
        final int k = 2;
        final double bias = 0.5;
        final int lastFrozenNode = 20;
        final int frozenStep = 5;
        final boolean tripletsBetweenOnes = true;
        */


        System.out.println("...PAIRS_OF_TRIPLET...\n" +
                "numNodes: " + numNodes + "\n" +
                "k: " + k + "\n" +
                "bias: " + bias + "\n" +
                "lastFrozenNode: " + lastFrozenNode + "\n" +
                "HOW_MANY_PAIRS_OF_TRIPLET: " + HOW_MANY_PAIRS_OF_TRIPLET + "\n" +
                "COMBINATIONS_FOR_COMPUTING_ATTRS: " + COMBINATIONS_FOR_COMPUTING_ATTRS + "\n" +
                "tripletsBetweenOnes: " + tripletsBetweenOnes + "\n" +
                "frozenStep: " + frozenStep
        );

        String pathFolder = "pOt_" + k + bias + Files.FILE_SEPARATOR;
        Files.createDirectories(pathFolder);
        String filename = "n" + numNodes + "k" + k + "p" + bias;
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + filename + "_stats.csv", true))) {
            for (int i = 0; i < BN_SAMPLES; ) {
                System.out.print("sample #" + i);
                if (forEachBN(i, numNodes, k, bias, r, csv, lastFrozenNode, frozenStep, tripletsBetweenOnes)){
                    i++;
                    System.out.println(" DONE");
                } else {
                    System.out.println(" FAILED");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static private boolean forEachBN(final int id,
                                  final int numNodes,
                                  final int k,
                                  final double bias,
                                  final RandomGenerator r,
                                  final BufferedWriter stats,
                                  final int LAST_FROZEN_NODE,
                                  final int FROZEN_STEP,
                                  final Boolean tripletsBetweenOnes ) throws IOException {
        // GENERATE BN
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT, BooleanNetworkFactory.SelfLoop.WITHOUT, numNodes, k, bias, r);
        // FOR EACH INITIAL NUMBER OF NODES FROZEN
        //for (Integer frozenNodesNumber : IntStream.range(0, LAST_FROZEN_NODE + 1).boxed().collect(Collectors.toList())) {
        final int ITERATIONS = (LAST_FROZEN_NODE / FROZEN_STEP) + 1;
        StringBuilder sb = new StringBuilder();
        for (Integer frozenNodesNumber : IntStream.iterate(0, i -> i + FROZEN_STEP).limit(ITERATIONS).boxed().collect(Collectors.toList())) {
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

            //Attractors<BinaryState> attrs = StaticAnalysisTES.attractors(generator, dynamics);

            //ATTRACTORS
            Attractors<BinaryState> attrs = AttractorsFinderService.apply(generator,
                    dynamics,
                    false,
                    false,
                    AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(numNodes));


            // WE CHOOSE A RND ATTRACTOR
            int numAttrs = attrs.numberOfAttractors();
            if (numAttrs != 1){
                return false;
            }

            ImmutableAttractor<BinaryState> attr = attrs.getAttractors().get(0);

            // THE FIRST PHASE
            BinaryState initialState = attr.getFirstState();
            //System.out.println("initialState: " + initialState);

            List<Integer> onesInInitialState = getIndicesOfNodesWithOneAsValue(initialState);
            if (onesInInitialState.size() < 3) {
                //dato che devo prendere almeno delle triplette
                return false;
            }
            //System.out.println("onesInInitialState: " + onesInInitialState);
            // GENERATE PHENOTYPE
            List<List<Integer>> phenotypes = new ArrayList<>();
            List<Set<List<BinaryState>>> phenotypesSET = new ArrayList<>();
            for (int x = 0 ; x < PHENOTYPES_DIMENSION.length; x++) {
                phenotypes.add(phenotype(PHENOTYPES_DIMENSION[x], numNodes, r));
                phenotypesSET.add(new HashSet<>());
            }
            //System.out.println(phenotypes);
            // STATISTICS VALUES
            int countDifferentAttractorsTriplets = 0;
            List<Integer> countDifferentAttractorsPhenotypes = new ArrayList<>(Collections.nCopies(PHENOTYPES_DIMENSION.length, 0));
            int countDifferentAttractorsAllEnnuple = 0;

            Set<ImmutableAttractor<BinaryState>> allEnnupleSET = new HashSet<>();

            int numberOfActualTriplets = 0;

            //GENERATE TRIPLETS
            for (int i = 0; i < HOW_MANY_PAIRS_OF_TRIPLET; i++) {
                // WE GENERATE 2 RND TRIPLET FROM THE FREE NODES (NOT FROZEN)
                // BUT WE CHOOSE ONLY BETWEEN THE 1's
                List<List<Integer>> triplets;
                if (tripletsBetweenOnes) {
                    triplets = twoONEStriplets(onesInInitialState, r);
                } else {
                    triplets = twoRNDtriplets(numNodes, frozenNodesNumber, r);
                }
                //System.out.println("triplets: " + triplets);

                // WE FREEZE THE TRIPLETS AND CHECK IF THE 2 REACHED ATTRACTORS ARE EQUAL FOR THE REMAINING (FREE) PARTS
                //System.out.println("triplets: " + triplets);
                List<ImmutableAttractor<BinaryState>> pairsOfAttrs = new ArrayList<>();
                for (int j = 0; j < 2; j++) {
                    //System.out.println("chosen triplet: " + triplets.get(j));

                    List<Integer> idxs = new ArrayList<>(indicesToFreeze);
                    idxs.addAll(triplets.get(j));
                    ImmutableAttractor<BinaryState> a = freezeAndComputeAttractor(bn, initialState, idxs);
                    if (a != null) {
                        pairsOfAttrs.add(a);
                    }
                }
                if (pairsOfAttrs.size() != 2) {
                    continue;
                }

                numberOfActualTriplets++;

                //System.out.println("0: " +pairsOfAttrs.get(0));
                //System.out.println("1: " +pairsOfAttrs.get(1));

                // ALL ENNUPLE
                allEnnupleSET.add(pairsOfAttrs.get(0));
                allEnnupleSET.add(pairsOfAttrs.get(1));

                // ALL PHENOTYPE
                for (int x = 0 ; x < PHENOTYPES_DIMENSION.length; x++) {
                    phenotypesSET.get(x).add(constructNewAttractor(pairsOfAttrs.get(0), phenotypes.get(x)));
                    phenotypesSET.get(x).add(constructNewAttractor(pairsOfAttrs.get(1), phenotypes.get(x)));
                }


                // ALL ENNUPLE PAIR CHECK
                if (pairsOfAttrs.get(0).equals(pairsOfAttrs.get(1))){
                    countDifferentAttractorsAllEnnuple++;
                }

                // PAIR OF TRIPLET CHECK AND PHENOTYPE
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
                    //System.out.println("BEFORE");
                    if (checkIfEqual(pairsOfAttrs.get(0), pairsOfAttrs.get(1), indicesToMaintainList)) {
                        countDifferentAttractorsTriplets++;
                    }
                    //System.out.println("AFTER");
                    for (int x = 0 ; x < PHENOTYPES_DIMENSION.length; x++) {
                        if (checkIfEqual(pairsOfAttrs.get(0), pairsOfAttrs.get(1), phenotypes.get(x))) {
                            countDifferentAttractorsPhenotypes.set(x, countDifferentAttractorsPhenotypes.get(x) + 1);
                        }
                    }
                }
            }

            sb.append(countDifferentAttractorsTriplets + "," + countDifferentAttractorsPhenotypes.stream().map(x -> x + "").collect(Collectors.joining(",")) + "," + countDifferentAttractorsAllEnnuple + "," + allEnnupleSET.size() + "," + phenotypesSET.stream().map(x -> x.size() + "").collect(Collectors.joining(",")) + "," + numberOfActualTriplets);
            if (frozenNodesNumber == LAST_FROZEN_NODE){
                sb.append("\n");
            } else {
                sb.append(",");
            }
        }
        stats.append(sb.toString());
        return true;
    }

    private static List<Integer> getIndicesOfNodesWithOneAsValue(BinaryState initialState) {
        List<Integer> idxs = new ArrayList<>();
        for (int i = 0; i < initialState.getLength(); i++) {
            if (initialState.getNodeValue(i)){
                idxs.add(i);
            }
        }
        return idxs;
    }

    /**
     * 2 coppie di triplette con nodi scelti tra quelli con valore 1 nello stato specificato di partenza
     * @param ones
     * @return
     */
    private static List<List<Integer>> twoONEStriplets(List<Integer> ones, RandomGenerator r) {
        List<List<Integer>> triplets = new ArrayList<>();
        for (int j = 0; j < 2; j++) {
            List<Integer> triplet = new ArrayList<>();
            triplet.add(ones.get(r.nextInt(ones.size())));
            do {
                int temp = ones.get(r.nextInt(ones.size()));
                if (!triplet.contains(temp)) {
                    triplet.add(temp);
                }
            } while (triplet.size() < 3);
            triplets.add(triplet);
        }
        return triplets;
    }

    /**
     * 2 coppie di triplette Random con indici scelti tra i nodi NON CONGELATI
     * @param numNodes
     * @param frozenNodesNumber
     * @param r
     * @return
     */
    private static List<List<Integer>> twoRNDtriplets(int numNodes, int frozenNodesNumber, RandomGenerator r) {
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
        return triplets;
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
        } while(phenotype.size() < numOfIndices);
        return phenotype;
    }

    private static List<BinaryState> constructNewAttractor(Attractor<BinaryState> oldAttractor, List<Integer> indicesToMaintainList){
        //System.out.println("indicesToMaintainList: " + indicesToMaintainList);

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

        //System.out.println("indicesToFreeze: " + indicesToFreeze);


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
        //Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genKO, dynamicsKO);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(genKO,
                dynamicsKO,
                false,
                false,
                AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()));

        if (atts.numberOfAttractors() != 1) {
            //throw new IllegalStateException("Not Admissible number of attractors starting from one intial state");
            return null;
        }
        return atts.getAttractors().get(0);
    }
}