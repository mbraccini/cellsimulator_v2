package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.FrozenGenerator;
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
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PathDependencyTCS {
    /**                                  |
     *  __    __   __    __              |  __    __   __    __
     * |F |  |F | |F |  |F |  ...        | |  |  |  | |  |  |  |
     *  --    --   --    --              |  --    --   --    --
     * [congelabili] [(1-M)/100]*No.Nodes| (M/100)*No.Nodes  [proiezione/fenotipo]
     */


    public static void tryPairOftriplets(final BinaryState sample,
                                         final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                         final List<Integer> indicesToKnockOut){
        Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));

        Attractors<BinaryState> att = AttractorsFinderService.apply(new BagOfStatesGenerator<>(List.of(sample)),
                                      dynamicsKO,
                                     true,
                                     true,
                                      AttractorsFinderService.TRUE_TERMINATION);
        System.out.println("Starting Attractor (A)");
        System.out.println(att);

        ImmutableAttractor<BinaryState> A = att.getAttractors().get(0);



    }

   /* public static void differentLevelOfFreezing(final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                                final List<Integer> indicesToKnockOut,
                                                final Integer howManySamples,
                                                final RandomGenerator r){



    }*/


    public static void investigatePathDependecyProperty(final int nodesNumber,
                                                    final int k,
                                                    final double bias,
                                                    final int howManyNetworks,
                                                    final int howManySamples,
                                                    final List<Double> freezingLevelFraction,
                                                    final Double phenotypeFraction,
                                                    final RandomGenerator r){
        //freezingLevelFraction rappresenta la frazione di nodi da congelare che appartiene alla parte dei geni congelabili, e quindi non quelli su cui si farà la proiezione degli attrattori

        for (int i = 0; i < howManyNetworks; i++) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

            for(Double frozenFraction : freezingLevelFraction){
                int numberOfFrozenStartingNodes = (int)Math.ceil(frozenFraction * (1-phenotypeFraction) * nodesNumber);
                List<Integer> indicesToKnockOut = IntStream.range(0, numberOfFrozenStartingNodes).boxed().collect(Collectors.toList());

                Generator<BinaryState> samples
                        = new FrozenGenerator(BigInteger.valueOf(howManySamples), bn.getNodesNumber(), r, indicesToKnockOut,true);

                Stream.generate(samples::nextSample)
                        .limit(samples.totalNumberOfSamplesToBeGenerated().intValue())
                        .forEach((BinaryState sample)-> tryPairOftriplets(sample,
                                                                          bn,
                                                                          indicesToKnockOut));

            }
        }


    }

    public static void main(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int nodesNumber = 10;
        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());

        final int k = 2;
        final double bias = 0.5;
        final int howManyNetworks = 10;
        final int howManySamplesForEachFrozenLevel = 2;
        investigatePathDependecyProperty(nodesNumber,k,bias,howManyNetworks,howManySamplesForEachFrozenLevel,List.of(0.2,0.4,0.6), 0.3 ,r);
    }
}
