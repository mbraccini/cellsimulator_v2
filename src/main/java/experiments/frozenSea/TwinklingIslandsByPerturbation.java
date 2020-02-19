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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TwinklingIslandsByPerturbation {

    private static final int MAX_STEPS_TO_FIND_ATTRS = 10000; //serve perché potrei non trovarmi in uno degli attrattori trovati con la ricerca incompleta, senza di questa controllerebbe all'infinito
    private static final int INIT_SAMPLES = 1000;
    private static final int NUM_OF_BNS = 100;

    public static void main(String [] args){
        System.out.println("TWINK-Perturbations");

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        String folder = "twinkling" + Files.FILE_SEPARATOR;
        Files.createDirectories(folder);
        final int nodesNumber = 100;
        final int k = 2;
        final double bias = 0.5;
        IntStream.range(0, NUM_OF_BNS).forEach(idBN -> analiseNet(folder, idBN, nodesNumber,k,bias, r, Boolean.FALSE));
    }

    public static void analiseNet(  final String folder,
                                    final int idBN,
                                    final int nodesNumber,
                                    final int k,
                                    final double bias,
                                    final RandomGenerator r,
                                    final Boolean completeExploration){

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

        Attractors<BinaryState> atts = getAttractors(bn, completeExploration, r);

        //System.out.println("atts\n"+atts);
        Set<Integer> fix = AttractorsUtility.fixedNodesAllAttractors(atts);
        //System.out.println("fixed:\n"+fix);
        Set<Integer> blink =  AttractorsUtility.blinkingNodesAllAttractors(atts);
        //System.out.println("blink:\n"+blink);

        //BN SAVE
        Files.writeBooleanNetworkToFile(bn, folder + idBN +  "_bn");
        Files.writeAttractorsToReadableFile(atts, folder + idBN + "_attrs");
        new BNGraphViz<>(bn).saveOnDisk(folder + idBN +  "_bn_dot");
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
                    for (BinaryState s : a.getStates()) {
                        //System.out.println("stato: "+s);
                        BinaryState startState = s.flipNodesValues(toFlip);
                        Tuple2<List<BinaryState>,List<BinaryState>> traj_attr = untilAttractor(bn, startState, atts);

                        if (traj_attr != null){ //not reached known attractors in MAX_STEPS_TO_FIND_ATTRS
                            /*
                            System.out.println("traj");
                            traj_attr._1.stream().forEach(System.out::println);
                            System.out.println("attra");
                            traj_attr._2.stream().forEach(System.out::println);
                            */
                            List<Integer> traj = findDifferences(startState,traj_attr._1(), blink, toFlip);//diff in transient's states
                            List<Integer> attr = findDifferences(startState,traj_attr._2(), blink, toFlip);//differences in attractors's states
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
    private static Tuple2<List<BinaryState>,List<BinaryState>> untilAttractor(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
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
            return new Tuple2<>(traj,AttractorsUtility.retrieveAttractor(previous, atts.getAttractors()).get().getStates());
        }
        return null;

    }

    private static Boolean withinAnAttractor(final BinaryState state, final Attractors<BinaryState> atts){
        return atts.getAttractors().stream().anyMatch(x -> x.getStates().stream().anyMatch(y -> y.equals(state)));
    }


    private static Attractors<BinaryState> getAttractors(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                         final Boolean completeExploration,
                                                         final RandomGenerator r) {
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
