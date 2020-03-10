package experiments.frozenSea;

import attractor.AttractorsUtility;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.dynamic.SynchronousDynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
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
import java.util.function.Predicate;
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
        System.out.println("TWINK-Perturbations");
        randomBN();
        //cellcollective();

       /* String str = "/cellcollective/bbb.txt";
        Pattern p = Pattern.compile("\\/cellcollective\\/(.*?).txt");
        Matcher m = p.matcher(str);

        String a = m.results().findFirst().get().group(1);
        System.out.println(a);
            //m.results().forEach(x -> System.out.println(x.group(1))); //is your string. do what you want
        */
    }

    public static void randomBN(){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        String folder = "twinkling" + Files.FILE_SEPARATOR;
        Files.createDirectories(folder);
        final int nodesNumber = 100;
        final int k = 2;
        final double bias = 0.5;
        IntStream.range(0, NUM_OF_BNS).forEach(
                idBN -> { BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn =
                        BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
                    analiseNet(bn,folder, idBN, r, Boolean.FALSE);});
    }
    public static void cellcollective(){
        Pattern p = Pattern.compile("\\/cellcollective\\/(.*?)_v1.txt");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        String folder = "twinkling_cellcollective" + Files.FILE_SEPARATOR;
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

        Attractors<BinaryState> atts = getAttractors(bn,
                                                completeExploration,
                                                r);
                //(completeExploration.equals(Boolean.TRUE) ? AttractorsFinderService.TRUE_TERMINATION : AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()))
                 //                   );

        //System.out.println("atts\n"+atts);
        Set<Integer> fix = AttractorsUtility.fixedNodesAllAttractors(atts);
        //System.out.println("fixed:\n"+fix);
        Set<Integer> blink =  AttractorsUtility.blinkingNodesAllAttractors(atts);
        //System.out.println("blink:\n"+blink);

        //BN SAVE
        Files.writeBooleanNetworkToFile(bn, folder + idBN +  "_bn");
        Files.writeAttractorsToReadableFile(atts, folder + idBN + "_attrs");
        new BNGraphViz<>(bn).saveOnDisk(folder + idBN +  "_bn_dot");
        Files.writeListsToCsv(List.of(new ArrayList<>(blink)), folder + idBN +  "_blinking.csv");
        perturb(folder, idBN,bn, atts, blink, r);
    }

    private static void perturb( final String folder,
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
                for (ImmutableAttractor<BinaryState> a : atts.getAttractors()) {
                    System.out.println("attractorStart:" + a.getId());

                    for (BinaryState s : a.getStates()) {
                        //System.out.println("stato: "+s);
                        BinaryState startState = s.flipNodesValues(toFlip);
                        Tuple3<List<BinaryState>,List<BinaryState>,Integer> traj_attr = untilAttractor(bn, startState, atts);

                        if (traj_attr != null){ //not reached known attractors in MAX_STEPS_TO_FIND_ATTRS
                            /*
                            System.out.println("traj");
                            traj_attr._1.stream().forEach(System.out::println);
                            System.out.println("attra");
                            traj_attr._2.stream().forEach(System.out::println);
                            */
                            Integer id_attrattore_finale_trovato = traj_attr._3();
                            List<Integer> traj = findDifferences(startState,traj_attr._1(), blink, toFlip);//diff in transient's states
                            List<Integer> attr;
                            if(id_attrattore_finale_trovato.equals(a.getId())){ //se è uguale a quello iniziale le differenze sono 0
                                System.out.println("Attrattore finale e inziale uguali");
                                attr = new ArrayList<>();
                            } else {
                                attr = findDifferences(startState, traj_attr._2(), blink, toFlip);//differences in attractors's states
                            }
                            traj.add(0, a.getId());
                            attr.add(0, a.getId());
                            traj.add(0, toFlip);
                            attr.add(0, toFlip);
                            //System.out.println("TRAJ_RES"+ traj);
                            // quindi avremo  NODO_FLIPPED_TRA_I_BLINKING; ID_ATTRATTORE; DIFFERENCES ...
                            csv.append(traj.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
                            csv.append("\n");
                            csv.append(attr.stream().map(x -> x.toString()).collect(Collectors.joining(",")));
                            csv.append("\n");
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static List<Integer> findDifferences(final BinaryState startState,
                                                final List<BinaryState> statesList,
                                                final Set<Integer> blink,
                                                final Integer flipped) {
        Set<Integer> touched = new HashSet<>();
        Set<Integer> blinkingMinusPerturbedNode = new HashSet<>(blink);
        blinkingMinusPerturbedNode.remove(flipped);
        //System.out.println("blinkingMinusPerturbedNode:\n"+blinkingMinusPerturbedNode);
        for (BinaryState s: statesList) {
            for (Integer b : blinkingMinusPerturbedNode) {
                if (s.getNodeValue(b) != startState.getNodeValue(b)){
                    touched.add(b);
                }
            }
        }
        return new ArrayList<>(touched);

    }

    /**
     * list of states of transient, list of states of possibly different attractor reached
     * @param bn
     * @param start
     * @param atts
     * @return
     */
    private static Tuple3<List<BinaryState>,List<BinaryState>, Integer> untilAttractor(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                           final BinaryState start,
                                                           final Attractors<BinaryState> atts){
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
            System.out.println("attractorFound:" + attractorFound);
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
