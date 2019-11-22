package experiments.methylation;

import attractor.AttractorsUtility;
import dynamic.KnockOutDynamicsDecorator;
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
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import states.ImmutableRealState;
import utility.Files;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class CheckModelFrozenPropagation {


    public static ImmutableAttractor<BinaryState> retrieveSignificantAttractor(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                               final RandomGenerator r){

        Generator<BinaryState> gen = new UniformlyDistributedGenerator(BigInteger.valueOf(1000), bn.getNodesNumber(),r);
        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,dyn,true,true,AttractorsFinderService.CUT_OFF_PERCENTAGE_TERMINATION.apply(bn.getNodesNumber()));
        return atts.getAttractors().stream().findFirst().get();
    }

    public static List<Integer> nodesWithExpressionValueStrictlyGreaterThanZero(ImmutableRealState a){
        int l = a.getLength();
        //System.out.println(a);
        List<Integer> list_indices = IntStream.range(0,l).boxed().filter(x -> a.getNodeValue(x) > 0).collect(Collectors.toList());
        //System.out.println(list_indices);
        return list_indices;
    }

    public static void experiment(final double percentageToFreeze,
                                  final int howManyNetworks,
                                  final RandomGenerator r){

        final int nodesNumber = 10;
        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(toList());
        final int k = 2;
        final double bias = 0.5;
        int nodesToFreeze = (int)Math.ceil(percentageToFreeze * nodesNumber);
        //System.out.println("nodesToFreeze: \n" + nodesToFreeze);
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        int net = 0;
        do {
            //System.out.println("net: \n" + net);
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
            ImmutableAttractor<BinaryState> startingAttr = retrieveSignificantAttractor(bn, r);
            ImmutableRealState meanAttr = AttractorsUtility.attractorMeanRepresentativeState(startingAttr);
            List<Integer> notZeroIndices = nodesWithExpressionValueStrictlyGreaterThanZero(meanAttr);
            if (notZeroIndices.size() < nodesToFreeze) {
                //System.out.println("CONTINUE");
                continue;
            }
            BinaryState sample = startingAttr.getStates().get(startingAttr.getStates().size() - 1); //PRENDIAMO L'ULTIMO IN ORDINE LESSICOGRAFICO COSì DOVREBBE AVERE PIU' NODI A 1
            List<Integer> actualNodesToFreeze = notZeroIndices.subList(0,nodesToFreeze);
            followStory(sample,bn,actualNodesToFreeze);
            net++;
        } while(net < howManyNetworks);

    }

    public static ImmutableAttractor<BinaryState> followStory(final BinaryState sampleNotFrozen,
                                                                     final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                                     final List<Integer> indicesToKnockOut){

        System.out.println("sampleNO \n" +sampleNotFrozen);

        BinaryState sample = sampleNotFrozen.setNodesValues(Boolean.FALSE, indicesToKnockOut.toArray(new Integer[0]));
        System.out.println("sample \n" +sample);

        Dynamics<BinaryState> dynKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));

        Attractors<BinaryState> att = AttractorsFinderService.apply(new BagOfStatesGenerator<>(List.of(sample)),
                dynKO,
                false,
                false,
                AttractorsFinderService.TRUE_TERMINATION);
        ImmutableAttractor<BinaryState> reachedAttractor = att.getAttractors().get(0);

        System.out.println(att);

        BinaryState previousState = sample;
        BinaryState nextState = previousState;
        diff(previousState,nextState);
        System.out.println("sample \n" +sample);
        while(!belongToAttractor(nextState,reachedAttractor)){
            nextState = dynKO.nextState(previousState);
            diff(previousState,nextState);
            previousState=nextState;
        }
        return att.getAttractors().get(0);
    }
    public static Tuple diff(BinaryState prev, BinaryState next){
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
        System.out.println("prev:\n"+prev);
        System.out.println("next:\n"+next);
        System.out.println("toOnes:\n"+toOnes);
        System.out.println("toZeros:\n"+toZeros);

        return Tuple.of(toOnes,toZeros);
    }

    public static Boolean belongToAttractor(BinaryState state, ImmutableAttractor<BinaryState> att){
        return att.getStates().stream().anyMatch(x -> x.equals(state));
    }


    public static void main(String args[]){
        System.out.println("Frozen Propagation");
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        final int howManyNetworks = 1;

        List.of(0.2).forEach( toFreeze ->
                experiment(toFreeze, howManyNetworks,r)
        );
    }
}
