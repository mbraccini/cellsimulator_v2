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
import states.ImmutableRealState;
import utility.*;


import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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


    private static void exaustive_experiment(final String parentDirectory,
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

        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());
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
            count++; //RETE VALIDA

            // ATM
            //esaustivo
            Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
            //campionato
            //Atm<BinaryState> atm = new IncompletePerturbations().apply(attractors, dynamics,numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, List.of());

            Tuple2<Number[][], String[]> a = MatrixUtility.reorderByDiagonalValuesATM(atm);
            double[][] sortedATM = MatrixUtility.fromNumberToDoubleMatrix(a._1());
            String[] sortedHeader = a._2(); //Arrays.stream(a._2()).map(Integer::valueOf).collect(Collectors.toList());

            int index = 0;
            /*
            //CAMPIONATO
            for(; index < sortedATM.length && sortedATM[index][index] == 0; index++);
            if (index >= sortedATM.length ){
                //se non ci sono valori nella diagonale != da zero, scarto la rete!
                continue;
            }*/

            ImmutableAttractor<BinaryState> pluripotent = attractors.getAttractorById(Integer.valueOf(sortedHeader[index]));
            /*
            Set<Integer> ones = retrieveIndicesOfFixedOnes(pluripotent);
            if (ones.size() >= Kcombinations) {
                count++; //valid network
            } else {
                continue;
            }
            */

            /*double startingf2 = MatrixUtility.f2_equallyDistributed(sortedATM);
            double startingf3 = MatrixUtility.f3_triangleDifference(sortedATM);
            double startingf3_divided = startingf3 / attractors.numberOfAttractors();
            */

            /*List<List<Integer>> combinations = org.paukov.combinatorics3.Generator.combination(ones)
                    .simple(Kcombinations)
                    .stream()
                    .limit(1) //ne prendiamo solo una
                    .collect(Collectors.toList());
            */
            /*
            List<Double> frozenf2list = new ArrayList<>();
            frozenf2list.add(startingf2);
            List<Double> frozenf3list = new ArrayList<>();
            frozenf3list.add(startingf3);
            List<Double> frozenf3dividedlist = new ArrayList<>();
            frozenf3dividedlist.add(startingf3_divided);
             */

            Collections.shuffle(list_indices);
            List<Integer> combFrozenIndices = IntStream.range(0,Kcombinations).map(i -> list_indices.get(i)).boxed().collect(Collectors.toList());//combinations.get(0); //ne prendo solo una, di combinazione.
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

    private static void sampled_experiment(final String parentDirectory,
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

        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());
        while (count < howManyNetworks) {
            bn = generateNet(nodesNumber, k, bias, wiringType, selfLoop, r);

            // GENERATOR
            // campionato
            Generator<BinaryState> generator = new UniformlyDistributedGenerator( BigInteger.valueOf(numOfInitialStates),nodesNumber,r);
            // DYNAMICS
            Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
            // ATTRACTORS
            //campionato
            Attractors<BinaryState> attractors = AttractorsFinderService.apply(generator, dynamics, true, false, AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(nodesNumber));
            if (attractors.numberOfAttractors() <= 1) {
                continue; // vogliamo almeno 2 attrattori
            }

            // ATM
            //campionato
            Atm<BinaryState> atm = new IncompletePerturbations().apply(attractors, dynamics,numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, List.of());

            Tuple2<Number[][], String[]> a = MatrixUtility.reorderByDiagonalValuesATM(atm);
            double[][] sortedATM = MatrixUtility.fromNumberToDoubleMatrix(a._1());
            String[] sortedHeader = a._2(); //Arrays.stream(a._2()).map(Integer::valueOf).collect(Collectors.toList());

            //CAMPIONATO
            /*int index = 0;
            for(; index < sortedATM.length && Arrays.stream(sortedATM[index]).sum() == 1.0; index++);
            if (index != sortedATM.length ){
                //se c'è anche una sola rete con una riga diversa da zero la scarto.
                continue;
            }*/

            // Ne prendo uno a caso, di attrattore.
            int index = r.nextInt(attractors.numberOfAttractors());

            ImmutableAttractor<BinaryState> pluripotent = attractors.getAttractorById(Integer.valueOf(sortedHeader[index]));
            ImmutableRealState pluripotentMeanActivation = AttractorsUtility.attractorMeanRepresentativeState(pluripotent);
            //System.out.println(pluripotentMeanActivation);
            //CONTIAMO IL NUMERO DI NODI > 0 nella loro attivazione media
            List<Integer> ones = new ArrayList<>();

            for (int j = 0; j < pluripotentMeanActivation.getLength(); j++){
                if (pluripotentMeanActivation.getNodeValue(j) > 0){
                    ones.add(j);
                }
            }
            if (ones.size() < Kcombinations){
                continue;
            }

            //PRENDIAMO UNA FASE A CASO
            BinaryState pluripotent_phase = pluripotent.getStates().get(r.nextInt(pluripotent.getLength()));
            /*for (int i = 0; i < pluripotent_phase.getLength(); i++){
                if (pluripotent_phase.getNodeValue(i) == Boolean.TRUE){
                    ones.add(i);
                }
            }*/


            Collections.shuffle(ones);
            List<Integer> combFrozenIndices = IntStream.range(0,Kcombinations).map(i -> ones.get(i)).boxed().collect(Collectors.toList());//combinations.get(0); //ne prendo solo una, di combinazione.

            Dynamics<BinaryState> dynFrozen = DecoratingDynamics
                    .from(dynamics)
                    .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, combFrozenIndices));

            //GENERATOR

            //campionato
            Generator<BinaryState> genComplete = new UniformlyDistributedGenerator( BigInteger.valueOf(numOfInitialStates),nodesNumber,r);

            Generator<BinaryState> genFrozen = new BagOfStatesGenerator<>(Stream.generate(genComplete::nextSample)
                    .limit(numOfInitialStates)
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, combFrozenIndices.toArray(new Integer[0])))
                    .collect(Collectors.toList())); //mentre qui non ci preoccupiamo che ci siano replicati o meno (quindi usiamo una list)
            // ATTRACTORS
            //campionato
            Attractors<BinaryState> attsFrozen = AttractorsFinderService.apply(genFrozen, dynFrozen, true, false,  AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(nodesNumber - Kcombinations));

            // ATM
            //campionato
            Atm<BinaryState> atmFrozen = new IncompletePerturbations().apply(attsFrozen, dynFrozen, numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, combFrozenIndices);

            Tuple2<Number[][], String[]> frozenSorted = MatrixUtility.reorderByDiagonalValuesATM(atmFrozen);
            double[][] sortedFrozenATM = MatrixUtility.fromNumberToDoubleMatrix(frozenSorted._1());

            //CAMPIONATO
            /*int indexFrozen = 0;
            for(; indexFrozen < sortedFrozenATM.length && Arrays.stream(sortedFrozenATM[indexFrozen]).sum() == 1.0; indexFrozen++);
            if (indexFrozen != sortedFrozenATM.length ){
                //se c'è anche una sola rete con una riga diversa da zero la scarto.
                continue;
            }*/

            // DALL0 STATO A CUI SONO ARRIVATO RIPARTO PER TROVARE L'ATTRATTORE CORRISPONDENTE
            /*Generator<BinaryState> genFrozenPluripotentAttractor
                    = new BagOfStatesGenerator<>(Stream.generate(() -> pluripotent_phase)
                    .limit(1)
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, combFrozenIndices.toArray(new Integer[0])))
                    .collect(Collectors.toList())); //mentre qui non ci preoccupiamo che ci siano replicati o meno (quindi usiamo una list)

            Attractors<BinaryState> attsFrozenPluripotentAttractor = AttractorsFinderService.apply(genFrozenPluripotentAttractor, dynFrozen, true, false,  AttractorsFinderService.TRUE_TERMINATION);

            BinaryState firstStateOfNewPluripotentAttractor = attsFrozenPluripotentAttractor.getAttractors().get(0).getFirstState();
            int foundAttractorID = AttractorsUtility.retrieveAttractorId(firstStateOfNewPluripotentAttractor,attsFrozen.getAttractors());

            if (foundAttractorID == -1){
                continue;
            }*/

            /*System.out.println("pluripotent_phase");
            System.out.println(pluripotent_phase);

            System.out.println("FrozenIndecies");
            System.out.println(combFrozenIndices);

            System.out.println("firstStateOfNewPluripotentAttractor");
            System.out.println(firstStateOfNewPluripotentAttractor);

            System.out.println("foundAttractorID");
            System.out.println(foundAttractorID);

            System.out.println("RElative Attr");
            System.out.println(attsFrozen.getAttractorById(foundAttractorID));*/


            /****RETE*VALIDA****/
            count++;
            /*******************/

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
            Files.writeListToTxt(List.of(sortedHeader[index]),dir + count + "_starting_pluripotent_attractor_ID");
            //Files.writeListToTxt(List.of(foundAttractorID),dir + count + "_of_arrival_attractor_ID");


        }
    }

    public static void main(String[] args) {
        System.out.println("OverallRobustnessChangeCausedByMethylation-wivace2019extension-50nodi-autoanelli");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int numOfPerturbations = 100;
        final int numOfInitialStates = 1000;
        final int nodesNumber = 50;
        //final int nodesNumber = Integer.valueOf(args[0]);
        //final int self_loop_number = 2;
        //final int self_loop_number = Integer.valueOf(args[1]);
        //final int frozen_number = 6;
        //final int frozen_number = Integer.valueOf(args[2]);
        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());

        final int k = 2;
        final double bias = 0.5;
        //final BooleanNetworkFactory.WIRING_TYPE wiringType = BooleanNetworkFactory.WIRING_TYPE.OR_K_FIXED;
        final int howManyNetworks = 100;
        for (BooleanNetworkFactory.WIRING_TYPE wiringType : List.of(BooleanNetworkFactory.WIRING_TYPE.OR_K_FIXED,BooleanNetworkFactory.WIRING_TYPE.AND_K_FIXED, BooleanNetworkFactory.WIRING_TYPE.RND_K_FIXED)) {
        //for (BooleanNetworkFactory.WIRING_TYPE wiringType : List.of(BooleanNetworkFactory.WIRING_TYPE.RND_K_FIXED)) {
            org.paukov.combinatorics3.Generator.cartesianProduct(List.of(5), List.of(0,5,10,20))  //lista numero autoanelli, lista numero nodi frozen
                    .stream()
                    .forEach((List<Integer> comb) -> {
                        int sl = comb.get(0);
                        int fr = comb.get(1);
                        String parentDirectory = nodesNumber + "_nodes_differentiationByMethylation_"+wiringType + Files.FILE_SEPARATOR;
                        Files.createDirectories(parentDirectory);
                        sampled_experiment_wivace2019_extended_work(parentDirectory, howManyNetworks, sl, r, nodesNumber, k, bias, wiringType, fr, numOfInitialStates, numOfPerturbations, list_indices);
                    });
        }
    }


    private static void sampled_experiment_wivace2019_extended_work(final String parentDirectory,
                                           final int howManyNetworks,
                                           final int selfLoop,
                                           final RandomGenerator r,
                                           final int nodesNumber,
                                           final int k,
                                           final double bias,
                                           final BooleanNetworkFactory.WIRING_TYPE wiringType,
                                           final int frozenNodesNumber,
                                           final int numOfInitialStates,
                                           final int numberOfPerturbations,
                                           final List<Integer> list_indices){
        String dir = parentDirectory + "sl_" +selfLoop + Files.FILE_SEPARATOR + "froz_" +frozenNodesNumber + Files.FILE_SEPARATOR;
        Files.createDirectories(dir);
        int count = 0;
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        while (count < howManyNetworks) {
            bn = generateNet(nodesNumber, k, bias, wiringType, selfLoop, r);

            Collections.shuffle(list_indices);
            List<Integer> combFrozenIndices = IntStream.range(0,frozenNodesNumber).map(i -> list_indices.get(i)).boxed().collect(Collectors.toList());

            Generator<BinaryState> gen = new UniformlyDistributedGenerator(BigInteger.valueOf(numOfInitialStates),nodesNumber,r);

            Generator<BinaryState> genFrozen = new BagOfStatesGenerator<>(Stream.generate(gen::nextSample)
                    .limit(numOfInitialStates)
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, combFrozenIndices.toArray(new Integer[0])))
                    .collect(Collectors.toList())); //mentre qui non ci preoccupiamo che ci siano replicati o meno (quindi usiamo una list)

            Dynamics<BinaryState> dynFrozen = DecoratingDynamics
                    .from(new SynchronousDynamicsImpl(bn))
                    .decorate(dyn -> new FrozenNodesDynamicsDecorator(dyn, combFrozenIndices));

            // ATTRACTORS
            Attractors<BinaryState> attsFrozen = AttractorsFinderService.apply(genFrozen, dynFrozen, true, false,  AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(nodesNumber));

            // ATM
            //campionato
            Atm<BinaryState> atmFrozen = new IncompletePerturbations().apply(attsFrozen, dynFrozen, numberOfPerturbations, Constant.PERTURBATIONS_CUTOFF, r, combFrozenIndices);

            Files.writeMatrixToCsv(atmFrozen.getMatrix(),   dir + count + "_atm_frozen", atmFrozen.header());
            Files.writeBooleanNetworkToFile(bn,             dir + count + "_bn");
            Files.writeListToTxt(combFrozenIndices, dir + count + "_frozen_indices");
            Files.writeAttractorsToReadableFile(attsFrozen,
                    dir + count + "_attrs_frozen");
            count++;
        }
    }

}