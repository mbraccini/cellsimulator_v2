package experiments.methylation;

import attractor.AttractorsUtility;
import dynamic.KnockOutKnockInDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
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
import io.vavr.Tuple;
import io.vavr.Tuple2;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import states.ImmutableRealState;
import utility.Files;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CheckModelFrozenPropagation {


    public static Optional<ImmutableAttractor<BinaryState>> retrieveSignificantAttractor(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                               final RandomGenerator r){

        Generator<BinaryState> gen = new UniformlyDistributedGenerator(BigInteger.valueOf(1000), bn.getNodesNumber(),r);
        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,dyn,true,true,AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()));
        return atts.getAttractors().stream().findFirst();
    }

    public static List<Integer> nodesWithExpressionValueStrictlyGreaterThanZero(ImmutableRealState a){
        int l = a.getLength();
        //System.out.println(a);
        List<Integer> list_indices = IntStream.range(0,l).boxed().filter(x -> a.getNodeValue(x) > 0).collect(Collectors.toList());
        //System.out.println(list_indices);
        return list_indices;
    }

    public static List<Double> experiment(final int nodesNumber,
                                          final int k,
                                          final double bias,
                                          final double percentageToFreeze,
                                          final int howManyNetworks,
                                          final RandomGenerator r){

        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(toList());

        int nodesToFreeze = (int)Math.ceil(percentageToFreeze * nodesNumber);
        //System.out.println("nodesToFreeze: \n" + nodesToFreeze);
        List<Double> results = new ArrayList<>();
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        int net = 0;
        do {
            //System.out.println("net: \n" + net);
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
            Optional<ImmutableAttractor<BinaryState>> aaaa = retrieveSignificantAttractor(bn, r);
            if( !aaaa.isPresent()){
                continue;
            }
            ImmutableAttractor<BinaryState>startingAttr = aaaa.get();
            ImmutableRealState meanAttr = AttractorsUtility.attractorMeanRepresentativeState(startingAttr);
            List<Integer> notZeroIndices = nodesWithExpressionValueStrictlyGreaterThanZero(meanAttr);
            if (notZeroIndices.size() < nodesToFreeze) {
                //System.out.println("CONTINUE");
                continue;
            }
            BinaryState sample = startingAttr.getStates().get(startingAttr.getStates().size() - 1); //PRENDIAMO L'ULTIMO IN ORDINE LESSICOGRAFICO COSì DOVREBBE AVERE PIU' NODI A 1
            List<Integer> actualNodesToFreeze = notZeroIndices.subList(0,nodesToFreeze);
            results.add(followStory(sample,bn,actualNodesToFreeze));
            net++;
        } while(net < howManyNetworks);

        return results;
    }

    public static double followStory(final BinaryState sampleNotFrozen,
                                                                     final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                     final List<Integer> indicesToKnockOut){

        //System.out.println("sampleNO \n" +sampleNotFrozen);

        BinaryState sample = sampleNotFrozen.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0]));
        //System.out.println("sample \n" +sample);

        Dynamics<BinaryState> dynKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutKnockInDynamicsDecorator(dyn, indicesToKnockOut, List.of()));

        Attractors<BinaryState> att = AttractorsFinderService.apply(new BagOfStatesGenerator<>(List.of(sample)),
                dynKO,
                false,
                false,
                AttractorsFinderService.TRUE_TERMINATION);
        ImmutableAttractor<BinaryState> reachedAttractor = att.getAttractors().get(0);
        //System.out.println(att);

        Set<Integer> toOnes = new HashSet<>();//con questo tengo traccia degli indici dei nodi che sono cambiati almeno una volta da 0 a 1 nel corso della traiettoria da t>0 al successivo attrattore
        Set<Integer> toZeros = new HashSet<>();
        List<Integer> toOnesSizeHistory = new ArrayList<>(); //con questo tengo traccia del numero di nodi che cambiano valore durante la traiettoria
        List<Integer> toZerosSizeHistory = new ArrayList<>();

        BinaryState previousState = sample;
        BinaryState nextState = previousState;
        diff(previousState,nextState);

        //System.out.println("sample \n" +sample);
        while(!belongToAttractor(nextState,reachedAttractor)){
            nextState = dynKO.nextState(previousState);

            Tuple2<List<Integer>,List<Integer>> a = diff(previousState, nextState);
            toOnes.addAll(a._1());
            toZeros.addAll(a._2());
            //System.out.println("toOnes \n" +toOnes);
            //System.out.println("toZeros \n" +toZeros);

            toOnesSizeHistory.add(a._1().size());
            toZerosSizeHistory.add(a._2().size());

            previousState=nextState;
        }
        navigateReachedAttractorStates(nextState, reachedAttractor, dynKO,toOnes,toZeros, toOnesSizeHistory, toZerosSizeHistory);//va navigato anche l'attrattore perché potrebbero cambiare anche qui i valori dei nodi
        Set<Integer> set = new HashSet<>();
        set.addAll(toOnes);
        set.addAll(toZeros);



        /*System.out.println("ones");
        System.out.println(toOnesSizeHistory);
        System.out.println("zeros");
        System.out.println(toZerosSizeHistory);
        System.out.println("SET");
        System.out.println(set);*/
        /*System.out.println(
                IntStream.range(0,toOnesSizeHistory.size())
                        .boxed()
                        .map(x -> ((double)(toOnesSizeHistory.get(x) + toZerosSizeHistory.get(x))) )
                        .collect(Collectors.toList())
        );*/

        //return (((double)(bn.getNodesNumber() - indicesToKnockOut.size() - set.size()))/(bn.getNodesNumber() - indicesToKnockOut.size()));
        return ((double) set.size());
    }

    public static void navigateReachedAttractorStates(BinaryState s,
                                                      ImmutableAttractor<BinaryState> att,
                                                      Dynamics<BinaryState> dynKO,
                                                      Set<Integer> toOnes,
                                                      Set<Integer> toZeros,
                                                      List<Integer> toOnesSizeHistory,
                                                      List<Integer> toZerosSizeHistory){
        int length = att.getLength();
        BinaryState previousState = s;
        BinaryState nextState = s;

        for (int i = 0; i < length; i++) {
            nextState = dynKO.nextState(previousState);
            Tuple2<List<Integer>,List<Integer>> a = diff(previousState, nextState);
            toOnes.addAll(a._1());
            toZeros.addAll(a._2());
            toOnesSizeHistory.add(a._1().size());
            toZerosSizeHistory.add(a._2().size());
            previousState=nextState;
        }
    }

    public static Tuple2<List<Integer>,List<Integer>> diff(BinaryState prev, BinaryState next){
        //guardiamo quali nodi che assumevano il valore 0 si trasformano in 1, e viceversa.
        int l = prev.getLength();
        List<Integer> toOnes = new ArrayList<>();
        List<Integer> toZeros = new ArrayList<>();

        for (int i = 0; i < l; i++) {
            if (prev.getNodeValue(i)==Boolean.TRUE && next.getNodeValue(i) == Boolean.FALSE){
                toZeros.add(i);
            } else if (prev.getNodeValue(i)==Boolean.FALSE && next.getNodeValue(i) == Boolean.TRUE){
                toOnes.add(i);
            }
        }
        /*System.out.println("prev:\n"+prev);
        System.out.println("next:\n"+next);
        System.out.println("toOnes:\n"+toOnes);
        System.out.println("toZeros:\n"+toZeros);*/


        return Tuple.of(toOnes,toZeros);
    }

    public static Boolean belongToAttractor(BinaryState state, ImmutableAttractor<BinaryState> att){
        return att.getStates().stream().anyMatch(x -> x.equals(state));
    }


    public static void main(String args[]){
        System.out.println("Frozen Propagation");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        final int howManyNetworks = 100;
        final int nodesNumber = 100;
        final int k = 2;
        final double bias = 0.5;

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println();

        String dir = "frozenPropagationVALUE" + dtf.format(now) + Files.FILE_SEPARATOR;
        Files.createDirectories(dir);

        List<List<Double>> res = List.of(0.03, 0.07, 0.15, 0.2, 0.3, 0.4, 0.5,0.6,0.7,0.8).stream().map(  toFreeze ->
                experiment(nodesNumber, k, bias, toFreeze, howManyNetworks, r)).collect(Collectors.toList());
        Files.writeListsToCsv(res, dir + "res");
    }
}
