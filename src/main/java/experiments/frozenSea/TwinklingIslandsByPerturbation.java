package experiments.frozenSea;

import attractor.AttractorsUtility;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.MutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.dynamic.SynchronousDynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.simulator.AttractorFinderResult;
import interfaces.state.BinaryState;
import io.vavr.Tuple3;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorFinderTask;
import simulator.AttractorsFinderService;
import utility.Files;
import utility.RandomnessFactory;
import visualization.BNGraphViz;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TwinklingIslandsByPerturbation {

    private static final int MAX_STEPS_TO_FIND_ATTRS = 10000; //serve perché potrei non trovarmi in uno degli attrattori trovati con la ricerca incompleta, senza di questa controllerebbe all'infinito
    private static final int INIT_SAMPLES = 1000;
    private static final int NUM_OF_BNS = 30;

    public static void main(String [] args){
        System.out.println("Specific Islands v0");
        final int type = Integer.valueOf(args[0]);
        randomBN(type);
        //cellcollective();

       /* String str = "/cellcollective/bbb.txt";
        Pattern p = Pattern.compile("\\/cellcollective\\/(.*?).txt");
        Matcher m = p.matcher(str);

        String a = m.results().findFirst().get().group(1);
        System.out.println(a);
            //m.results().forEach(x -> System.out.println(x.group(1))); //is your string. do what you want
        */
    }

    public static void randomBN(int type){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int nodesNumber = 100;
        final int sl_number = (int)(((double)nodesNumber/100)*20);
        final int k = 2;
        final double bias = 0.5;
        if (type == 0){
            System.out.println("RND_K_FIXED");
            String folder = "RND_K_FIXED" + Files.FILE_SEPARATOR;
            Files.createDirectories(folder);
            IntStream.range(0, NUM_OF_BNS).forEach(
                    idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                            BooleanNetworkFactory.newBNwithSelfLoop(k,bias,nodesNumber,r,sl_number, BooleanNetworkFactory.WIRING_TYPE.RND_K_FIXED);
                        //BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                        analiseNet(bn,folder, idBN, r, Boolean.FALSE);});
        } else {
            System.out.println("RND_K_plus_1");
            String folder = "RND_K_plus_1" + Files.FILE_SEPARATOR;
            Files.createDirectories(folder);
            IntStream.range(0, NUM_OF_BNS).forEach(
                    idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                            BooleanNetworkFactory.newBNwithSelfLoop(k,bias,nodesNumber,r,sl_number, BooleanNetworkFactory.WIRING_TYPE.RND_K_plus_1);
                        //BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                        analiseNet(bn,folder, idBN, r, Boolean.FALSE);});
        }

    }
    public static void cellcollective(){
        Pattern p = Pattern.compile("\\/cellcollective\\/(.*?)_v1.txt");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        String folder = "twinkling_cellcollective_v2" + Files.FILE_SEPARATOR;
        Files.createDirectories(folder);
        try (Stream<Path> paths = java.nio.file.Files.walk(Paths.get("./cellcollective/"))) {
            paths
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(x -> x.toString().endsWith(".txt"))
                    .forEach(x -> {
                        Matcher m = p.matcher(x.toString());
                        String numero_rete = m.results().findFirst().get().group(1);
                        System.out.println(x);
                        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn = BooleanNetworkFactory.newNetworkFromFile(x.toString());
                        analiseNet(bn,folder,Integer.valueOf(numero_rete),r, Boolean.FALSE);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void analiseNet(  final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                    final String folder,
                                    final int idBN,
                                    final RandomGenerator r,
                                    final Boolean completeExploration){
        //System.out.println("SL: "+bn.numberOfNodeWithSelfloops());
        Attractors<BinaryState> atts = getAttractors(bn,
                                                completeExploration,
                                                r);
        if (atts.numberOfAttractors() > 1) {
            //(completeExploration.equals(Boolean.TRUE) ? AttractorsFinderService.TRUE_TERMINATION : AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()))
            //                   );

            //System.out.println("atts\n"+atts);
            Set<Integer> fix = AttractorsUtility.nodesBelongingToCommonSea(atts);
            //System.out.println("fixed:\n"+fix);
            Set<Integer> blink = AttractorsUtility.specificNodes(atts);
            //System.out.println("blink:\n"+blink);

            //BN SAVE
            Files.writeBooleanNetworkToFile(bn, folder + idBN + "_bn");
            Files.writeAttractorsToReadableFile(atts, folder + idBN + "_attrs");
            new BNGraphViz<>(bn).saveOnDisk(folder + idBN + "_bn_dot");
            Files.writeListsToCsv(List.of(new ArrayList<>(blink)), folder + idBN + "_specific.csv");
            Files.writeListsToCsv(List.of(List.of(bn.numberOfNodeWithSelfloops())), folder + idBN + "_no_self_loop.csv");

            perturbFlip(folder, idBN, bn, atts, blink, r);
            //perturbKnock(folder, idBN, bn, atts, blink, r);
        }
    }

    private static void perturbKnock(String folder,
                                     int idBN,
                                     BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                     Attractors<BinaryState> atts,
                                     Set<Integer> blink,
                                     RandomGenerator r) {
    }

    private static void perturbFlip(final String folder,
                                    final int idBN,
                                    final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                    final Attractors<BinaryState> atts,
                                    final Set<Integer> blink,
                                    final RandomGenerator r) {
        String filename = ""+idBN;
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(folder + filename + ".csv", true))) {

        //per ogni stato di ogni attrattore, perturbo i nodi BLINK
            for (Integer toFlip: blink) {
                //System.out.println("toFlip: "+toFlip);
                for (ImmutableAttractor<BinaryState> startingAttractor : atts.getAttractors()) {
                    //System.out.println("attractorStart:" + startingAttractor);

                    /**
                     *  media dell'espressione di ogni gene dell'attrattore di partenza
                     */
                    checkIfInLexicographicalOrder(startingAttractor.getStates());
                    List<Double> meanOfExpressionStartingAttractor = new ArrayList<>();
                    for(int id_node = 0; id_node < bn.getNodesNumber(); id_node++){
                        meanOfExpressionStartingAttractor.add(meanOfExpressionAttractor(nodeSequenceExpressionSequence(startingAttractor.getStates(), id_node)));
                    }
                    /**
                     *
                     */
                    for (BinaryState s : startingAttractor.getStates()) {
                        //System.out.println("stato: "+s);
                        BinaryState startState = s.flipNodesValues(toFlip);
                        MutableAttractor<BinaryState> attractorFound = untilAttractorFlipCase2(bn, startState, atts);
                        //System.out.println("attractorFound: " + attractorFound);

                        if (attractorFound != null){ //not reached known attractors in MAX_STEPS_TO_FIND_ATTRS
                            checkIfInLexicographicalOrder(attractorFound.getStates());
                            /**
                             *
                             */
                            List<Double> meanOfExpressionAttractorFound = new ArrayList<>();
                            for(int id_node = 0; id_node < bn.getNodesNumber(); id_node++){
                                meanOfExpressionAttractorFound.add(meanOfExpressionAttractor(nodeSequenceExpressionSequence(attractorFound.getStates(), id_node)));
                            }
                            /**
                             *
                             */
                            //System.out.println("meanOfExpressionAttractorFound: " + meanOfExpressionAttractorFound);
                            //System.out.println("meanOfExpressionStartingAttractor: " + meanOfExpressionStartingAttractor);
                            List<Integer> perturbationPropagation = new ArrayList<>();
                            for (Integer specific: blink) {
                                if (!specific.equals(toFlip)){
                                    if (!meanOfExpressionAttractorFound.get(specific).equals(meanOfExpressionStartingAttractor.get(specific))){
                                        perturbationPropagation.add(specific);
                                    }
                                }
                            }
                            //System.out.println("perturbationPropagation: " + perturbationPropagation);

                            if (perturbationPropagation.size() != 0) {
                                perturbationPropagation.add(0, toFlip);
                                //System.out.println("TRAJ_RES"+ traj);
                                // quindi avremo  NODO_FLIPPED_TRA_I_BLINKING; ID_ATTRATTORE; DIFFERENCES ...
                                //csv.append(traj.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
                                //csv.append("\n");
                                csv.append(perturbationPropagation.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
                                csv.append("\n");
                            }
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static double meanOfExpressionAttractor(List<Integer> expressionListOfOneGene) {
        double sum = 0.0;
        for (int i = 0; i < expressionListOfOneGene.size(); i++){
            sum += expressionListOfOneGene.get(i);
        }
        return sum/expressionListOfOneGene.size();
    }

    private static List<Integer> nodeSequenceExpressionSequence(List<BinaryState> statesList, int index){
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < statesList.size(); i++){
            l.add(statesList.get(i).getNodeValue(index) == Boolean.TRUE ? 1 : 0);
        }
        return l;
    }

    private static List<Integer> findDifferencesInSequenceOfStates(final List<BinaryState> startingAttractorStatesList,
                                                                   final List<BinaryState> attractorFoundStatesList,
                                                                   final Set<Integer> blink,
                                                                   final Integer flipped) {
        //controlliamo se gli stati sono in ordine lessicografico
        checkIfInLexicographicalOrder(startingAttractorStatesList);
        checkIfInLexicographicalOrder(attractorFoundStatesList);

        Set<Integer> touched = new HashSet<>(); //influenced nodes
        Set<Integer> specificNodesExceptFlippedNode = new HashSet<>(blink);
        specificNodesExceptFlippedNode.remove(flipped);
        //System.out.println("blinkingMinusPerturbedNode:\n"+blinkingMinusPerturbedNode);
        for (Integer idx : specificNodesExceptFlippedNode) {
            List<Integer> sequenceExprStartingAtt = nodeSequenceExpressionSequence(startingAttractorStatesList, idx);
            List<Integer> sequenceExprFoundAtt    = nodeSequenceExpressionSequence(attractorFoundStatesList, idx);
            //System.out.println("sequenceExprStartingAtt: "+ sequenceExprStartingAtt);
            //System.out.println("sequenceExprFoundAtt: "+ sequenceExprFoundAtt);

           /* if (s.getNodeValue(b) != startState.getNodeValue(b)){
                    touched.add(b);
            }*/
        }
        
        return new ArrayList<>(touched);

    }

    private static void checkIfInLexicographicalOrder(List<BinaryState> statesList) {
        int i = 0;
        for (;i < statesList.size() - 1; i++){
            if (statesList.get(i).getStringRepresentation().compareTo(statesList.get(i+1).getStringRepresentation()) >= 0){
                throw new RuntimeException("Attrattore non in ordine lessicografico");
            }
        }
    }

    private static MutableAttractor<BinaryState> untilAttractorFlipCase2(
            final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
            final BinaryState startStateFlipped,
            final Attractors<BinaryState> atts
    ){
        SynchronousDynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);

        Collection<MutableAttractor<BinaryState>> mutableAttractors = new ArrayList<>();
        AttractorFinderResult<BinaryState> result = new AttractorFinderTask<>(startStateFlipped,
                                                                                dyn,
                                                                                mutableAttractors,
                                                                                Boolean.FALSE,
                                                                                Boolean.FALSE,
                                                                                steps -> (steps < MAX_STEPS_TO_FIND_ATTRS))
                                                        .findAttractor();
        MutableAttractor<BinaryState> a = result.attractorFound();
        if (!result.isCutOff() && a != null) {
            if (withinAnAttractor(a.getStates().get(0), atts)) {
                /**
                 * l'attrattore trovato è un attrattore noto, altrimenti non sapremmo chi farebbe parte del mare comune
                 * e di conseguenza dei nodi specifici
                 */
                //System.out.println("PRIMA");
                //System.out.println(a);
                Collections.sort(a.getStates());
                //System.out.println("DOPO");
                //System.out.println(a);
                return a;
            }
        }
        return null;
    }
    /**
     * list of states of transient, list of states of possibly different attractor reached
     * @param bn
     * @param start
     * @param atts
     * @return
     */
    private static Tuple3<List<BinaryState>,List<BinaryState>, Integer> untilAttractorOLD(
            final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
            final BinaryState start,
            final Attractors<BinaryState> atts
            ){
        List<BinaryState> traj = new ArrayList<>();
        SynchronousDynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        BinaryState previous = start;
        int counter = 0;
        while (!withinAnAttractor(previous, atts) && counter < MAX_STEPS_TO_FIND_ATTRS){
            BinaryState next = dyn.nextState(previous);
            traj.add(next);
            previous = next;
            counter++;
        }
        if (counter < MAX_STEPS_TO_FIND_ATTRS){
            if (traj.size() > 0){
                traj.remove(traj.size() - 1);
            }
            Integer attractorFound = AttractorsUtility.retrieveAttractorId(previous, atts.getAttractors());
            //System.out.println("attractorFound:" + attractorFound);
            return new Tuple3<>(traj,AttractorsUtility.retrieveAttractor(previous,
                    atts.getAttractors()).get().getStates(),
                    attractorFound);
        }
        return null;

    }

    private static Boolean withinAnAttractor(final BinaryState state, final Attractors<BinaryState> atts){
        return atts.getAttractors().stream().anyMatch(x -> x.getStates().stream().anyMatch(y -> y.equals(state)));
    }


    private static Attractors<BinaryState> getAttractors(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                         final Boolean completeExploration,
                                                         final RandomGenerator r
                                                         ) {
        Generator<BinaryState> gen;
        Dynamics<BinaryState> dyn;
        if (completeExploration){
            gen = new CompleteGenerator(bn.getNodesNumber());
        } else {
            gen = new UniformlyDistributedGenerator(BigInteger.valueOf(INIT_SAMPLES), bn.getNodesNumber(),r);
        }
        dyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,dyn,false,false, AttractorsFinderService.TRUE_TERMINATION);

        return atts;
    }
}
