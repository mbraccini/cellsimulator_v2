package experiments.methylation.differentiation.atm;

import attractor.AttractorsUtility;
import dynamic.FrozenNodesDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.CompleteGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import io.jenetics.util.IntRange;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import noise.IncompletePerturbations;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.*;


import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OverallRobustnessChangeCausedByMethylation {

    private static Set<Integer> retrieveIndicesOfFixedOnes(ImmutableAttractor<BinaryState> a) {
        BinaryState referenceState = a.getFirstState(); //tanto se sono fissi i nodi posso guardarli in qualunque stato.
        int nodesNumber = referenceState.getLength();
        Set<Integer> fixedIndices = null;
        if (!a.isFixedPoint()) {
            //se non è punto fisso cerco i nodi fissi
            fixedIndices = AttractorsUtility.fixed(a);
        } else {
            //se è punto fisso sono, ovviamente, tutti fissi!
            fixedIndices = IntRange.of(0, nodesNumber).stream().boxed().collect(Collectors.toSet());
        }
        return fixedIndices.stream().filter(x -> referenceState.getNodeValue(x).booleanValue() == Boolean.TRUE).collect(Collectors.toSet());
    }



    public static BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> generateNet(int nodesNumber,
                                                                                             int k,
                                                                                             double bias,
                                                                                             BooleanNetworkFactory.WIRING_TYPE wiringType,
                                                                                             int selfLoop,
                                                                                             RandomGenerator r) {

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn = null;

        if (selfLoop == 0) {
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
        } else {
            bn = BooleanNetworkFactory.newBNwithSelfLoop(k, bias, nodesNumber, r, selfLoop, wiringType);
        }
        if (bn.numberOfNodeWithSelfloops() != selfLoop) {
            throw new RuntimeException("Mismatch in selfloops number, expected" + selfLoop + ", present " + bn.numberOfNodeWithSelfloops());
        }
        return bn;
    }


    private static void experiment(final String parentDirectory,
                                    final int howManyNetworks,
                                    final int selfLoop,
                                    final RandomGenerator r,
                                    final int nodesNumber,
                                    final int k,
                                    final double bias,
                                    final BooleanNetworkFactory.WIRING_TYPE wiringType,
                                    final int Kcombinations,
                                    final int numOfInitialStates,
                                    final int numberOfPerturbations){
        String dir = parentDirectory + "sl_" +selfLoop + Files.FILE_SEPARATOR + "froz_" +Kcombinations + Files.FILE_SEPARATOR;
        Files.createDirectories(dir);
        int count = 0;
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        while (count < howManyNetworks) {
            bn = generateNet(nodesNumber, k, bias, wiringType, selfLoop, r);

            // GENERATOR
            // esaustivo
            Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
            // campionato
            //Generator<BinaryState> generator = new UniformlyDistributedGenerator( BigInteger.valueOf(numOfInitialStates),nodesNumber,r);
            // DYNAMICS
            Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
            // ATTRACTORS
            //esaustivo
            Attractors<BinaryState> attractors = AttractorsFinderService.apply(generator, dynamics, true, false, AttractorsFinderService.TRUE_TERMINATION);
            //campionato
            //Attractors<BinaryState> attractors = AttractorsFinderService.apply(generator, dynamics, true, false, AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(nodesNumber));
            if (attractors.numberOfAttractors() <= 1) {
                continue; // vogliamo almeno 2 attrattori
            }
            // ATM
            //esaustivo
            Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
            //campionato
            //Atm<BinaryState> atm = new IncompletePerturbations().apply(attractors, dynamics,numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, List.of());

            Tuple2<Number[][], String[]> a = MatrixUtility.reorderByDiagonalValuesATM(atm);
            double[][] sortedATM = MatrixUtility.fromNumberToDoubleMatrix(a._1());
            String[] sortedHeader = a._2(); //Arrays.stream(a._2()).map(Integer::valueOf).collect(Collectors.toList());
            int index = 0;
            for(; index < sortedATM.length && sortedATM[index][index] == 0; index++);
            if (index >= sortedATM.length ){
                //se non ci sono valori nella diagonale != da zero, scarto la rete!
                continue;
            }
            ImmutableAttractor<BinaryState> pluripotent = attractors.getAttractorById(Integer.valueOf(sortedHeader[index]));
            Set<Integer> ones = retrieveIndicesOfFixedOnes(pluripotent);
            if (ones.size() >= Kcombinations) {
                count++; //valid network
            } else {
                continue;
            }

            /*double startingf2 = MatrixUtility.f2_equallyDistributed(sortedATM);
            double startingf3 = MatrixUtility.f3_triangleDifference(sortedATM);
            double startingf3_divided = startingf3 / attractors.numberOfAttractors();
            */

            List<List<Integer>> combinations = org.paukov.combinatorics3.Generator.combination(ones)
                    .simple(Kcombinations)
                    .stream()
                    .limit(1) //ne prendiamo solo una
                    .collect(Collectors.toList());
            /*
            List<Double> frozenf2list = new ArrayList<>();
            frozenf2list.add(startingf2);
            List<Double> frozenf3list = new ArrayList<>();
            frozenf3list.add(startingf3);
            List<Double> frozenf3dividedlist = new ArrayList<>();
            frozenf3dividedlist.add(startingf3_divided);
             */

            List<Integer> combFrozenIndices = combinations.get(0); //ne prendo solo una, di combinazione.
            //for (List<Integer> combFrozenIndices : combinations) {
                //List<Integer> combFrozenIndices = combinations.get(0);
                //System.out.println(combFrozenIndices);
            Dynamics<BinaryState> dynFrozen = DecoratingDynamics
                        .from(dynamics)
                        .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, combFrozenIndices));

            //GENERATOR
            // esaustivo
            Generator<BinaryState> genComplete = new CompleteGenerator(bn.getNodesNumber());//new CompleteGenerator(bn.getNodesNumber());
            Generator<BinaryState> genFrozen = new BagOfStatesGenerator<>(Stream.generate(genComplete::nextSample)
                    .limit((long) Math.floor(Math.pow(2, nodesNumber)))
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, combFrozenIndices.toArray(new Integer[0])))
                    .collect(Collectors.toSet())); //esaustivo richiede il Set (per non avere replicati)
            //System.out.println(Stream.generate(genFrozen::nextSample).limit(((long)Math.pow(2, nodesNumber - Kcombinations))).collect(Collectors.toList()).size());
            if (genFrozen.totalNumberOfSamplesToBeGenerated().longValue() != (long) Math.floor(Math.pow(2, nodesNumber - Kcombinations))) {
                    System.out.println("stati: " +genFrozen.totalNumberOfSamplesToBeGenerated().longValue());
                   System.out.println("da generare: " +(long) Math.floor(Math.pow(2, nodesNumber - Kcombinations)));
                    System.out.println("comb: " + Kcombinations);
                throw new RuntimeException("Mismatch in number of initial states!");
            }
            //campionato
            //Generator<BinaryState> genComplete = new UniformlyDistributedGenerator( BigInteger.valueOf(numOfInitialStates),nodesNumber,r);
            //Generator<BinaryState> genFrozen = new BagOfStatesGenerator<>(Stream.generate(genComplete::nextSample)
            //            .limit(numOfInitialStates)
            //            .map(sample -> sample.setNodesValues(Boolean.FALSE, combFrozenIndices.toArray(new Integer[0])))
            //            .collect(Collectors.toList())); //mentre qui non ci preoccupiamo che ci siano replicati o meno (quindi usiamo una list)

            // ATTRACTORS
            //esaustivo
            Attractors<BinaryState> attsFrozen = AttractorsFinderService.apply(genFrozen, dynFrozen, true, false,  AttractorsFinderService.TRUE_TERMINATION);
            //campionato
            //Attractors<BinaryState> attsFrozen = AttractorsFinderService.apply(genFrozen, dynFrozen, true, false,  AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(nodesNumber - Kcombinations));

            // ATM
            //esaustivo
            Atm<BinaryState> atmFrozen = new CompletePerturbations().apply(attsFrozen, dynFrozen, Constant.PERTURBATIONS_CUTOFF, combFrozenIndices);
            //campionato
            //Atm<BinaryState> atmFrozen = new IncompletePerturbations().apply(attsFrozen, dynFrozen, numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, combFrozenIndices);

            Tuple2<Number[][], String[]> frozenSorted = MatrixUtility.reorderByDiagonalValuesATM(atmFrozen);
            double[][] sortedFrozenATM = MatrixUtility.fromNumberToDoubleMatrix(frozenSorted._1());
                /*
                double frozenf2 = MatrixUtility.f2_equallyDistributed(sortedFrozenATM);
                double frozenf3 = MatrixUtility.f3_triangleDifference(sortedFrozenATM);
                double frozenf3_divided = frozenf3 / attsFrozen.numberOfAttractors();

                frozenf2list.add(frozenf2);
                frozenf3list.add(frozenf3);
                frozenf3dividedlist.add(frozenf3_divided);
                */

                // TO FILES
                //String combFrozenIndicesString =  combFrozenIndices.stream().map(String::valueOf).collect(Collectors.joining("_"));
                Files.writeListToTxt(combFrozenIndices, dir + count + "_frozen_indices");
                Files.writeAttractorsToReadableFile(attsFrozen,
                        dir + count + "_attrs_frozen");
                Files.writeMatrixToCsv(sortedFrozenATM,   dir + count + "_atm_frozen", frozenSorted._2());
            //}
            DecimalFormat df = new DecimalFormat("#.###");
            df.setRoundingMode(RoundingMode.HALF_UP);
            Files.writeBooleanNetworkToFile(bn,             dir + count + "_bn");
            Files.writeAttractorsToReadableFile(attractors, dir + count + "_attrs_start");
            Files.writeMatrixToCsv(sortedATM,               dir + count + "_atm_start", sortedHeader);
            Files.writeListToTxt(List.of(sortedHeader[index]),dir + count + "_pluripotent_attractor_ID");

            /*
            Files.writeListsToCsv(List.of(frozenf2list.stream().map(x -> df.format(x)).collect(Collectors.toList())), dir + count + "_f2.csv");
            Files.writeListsToCsv(List.of(frozenf3list.stream().map(x -> df.format(x)).collect(Collectors.toList())), dir + count + "_f3.csv");
            Files.writeListsToCsv(List.of(frozenf3dividedlist.stream().map(x -> df.format(x)).collect(Collectors.toList())), dir + count + "_f3_divided.csv");
            */
        }
    }

    public static void main(String[] args) {
        System.out.println("OverallRobustnessChangeCausedByMethylation-v3-esaustivo");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int numOfPerturbations = 100;
        final int numOfInitialStates = 1000;
        final int nodesNumber = 15;
        final int k = 2;
        final double bias = 0.5;
        final BooleanNetworkFactory.WIRING_TYPE wiringType = BooleanNetworkFactory.WIRING_TYPE.OR_K_FIXED;
        final int howManyNetworks = 1000;
        String parentDirectory = "differentiationByMethylation" + Files.FILE_SEPARATOR;
        Files.createDirectories(parentDirectory);
        org.paukov.combinatorics3.Generator.cartesianProduct( List.of(0,3,6,9), List.of(3,6,9))  //lista numero autoanelli, lista numero nodi frozen
                    .stream()
                    .forEach((List<Integer> comb) -> experiment(parentDirectory,howManyNetworks,comb.get(0),r,nodesNumber,k,bias,wiringType,comb.get(1),numOfInitialStates,numOfPerturbations));
    }
}