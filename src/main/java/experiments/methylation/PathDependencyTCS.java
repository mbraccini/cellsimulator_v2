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
import states.ImmutableBinaryState;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.util.*;
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

    public static BinaryState fromListBooleanToBinaryState(List<Boolean> l){
        BitSet b = new BitSet(l.size());
        int i = 0;
        for (Boolean v : l){
            b.set(i++, v);
        }
        return new ImmutableBinaryState(l.size(), b);
    }

    public static List<BinaryState> getProjection(ImmutableAttractor<BinaryState> b,
                                                  final int numNodesProjection,
                                                  final int numNodesBooleanNetwork){

        List<Integer> nodesIndicesProjection =  IntStream.range(numNodesBooleanNetwork - numNodesProjection, numNodesBooleanNetwork)
                                                .boxed()
                                                .collect(Collectors.toList());

        List<List<Boolean>> l = b.getStates().stream().map(x -> nodesIndicesProjection
                                    .stream()
                                    .map(y -> x.getNodeValue(y))
                                    .collect(Collectors.toList()))
                                .collect(Collectors.toList());

        List<BinaryState> ll = l.stream()
                                .map(x -> fromListBooleanToBinaryState(x))
                                .collect(Collectors.toList());

        return ll;
    }

    public static int tryPairOfFreezing(final BinaryState sample,
                                         final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                         final List<Integer> indicesToKnockOut,
                                         final int numNodesProjection,
                                         final int sizeOfFrozenSequences){
        /*Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                .from(new SynchronousDynamicsImpl(bn))
                .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToKnockOut));

        Attractors<BinaryState> att = AttractorsFinderService.apply(new BagOfStatesGenerator<>(List.of(sample)),
                                      dynamicsKO,
                                     true,
                                     true,
                                      AttractorsFinderService.TRUE_TERMINATION);
        System.out.println("Starting Attractor (A)");
        System.out.println(att);

        // Prendiamo il primo attrattore tanto ce n'è solo uno dal momento che partiamo da un solo stato iniziale
        ImmutableAttractor<BinaryState> A = att.getAttractors().get(0);
        */
        ImmutableAttractor<BinaryState> A = getFrozenAttractor(sample,bn,indicesToKnockOut);
        System.out.println("Starting Attractor (A)");
        System.out.println(A);

        int maxFrozenIndex = indicesToKnockOut.stream().mapToInt(x -> x).max().getAsInt();

        List<Integer> nodesIndices = IntStream.range(maxFrozenIndex + 1, bn.getNodesNumber() - numNodesProjection).boxed().collect(Collectors.toList());
        System.out.println("nodesIndices");
        System.out.println(nodesIndices);

        Collections.shuffle(nodesIndices);

        if (maxFrozenIndex + sizeOfFrozenSequences >=  bn.getNodesNumber() - numNodesProjection ||
                maxFrozenIndex + (sizeOfFrozenSequences*2) >= bn.getNodesNumber() - numNodesProjection){
            System.err.println("NUMERO DI NODI CONGELATI TALE DA SOVRAPPORSI CON I NODI APPARTENTI ALLA PORZIONE DEDICATA ALLA PROIEZIONE FENOTIPICA");
            System.exit(1);
        }
        List<Integer> seq_1 = new ArrayList<>(nodesIndices.subList(0,sizeOfFrozenSequences));
        List<Integer> seq_2 =  new ArrayList<>(nodesIndices.subList(sizeOfFrozenSequences,  (sizeOfFrozenSequences*  2)));


        System.out.println(getProjection(A, numNodesProjection, bn.getNodesNumber()));
        System.out.println(seq_1 + "\n" + seq_2);

        Set<List<BinaryState>> setAttractorsProjections = new HashSet<>();
        setAttractorsProjections.add(getProjection(A,numNodesProjection,bn.getNodesNumber()));

        //PROVIAMO LE DUE SEQUENZE seq_1 + seq_2 e seq_2 + seq_1
        List<List<BinaryState>> resFirstFreezingSequence = tryFreezingSequence(A.getFirstState(), bn, numNodesProjection, indicesToKnockOut, seq_1, seq_2);
        List<List<BinaryState>> resSecondFreezingSequence = tryFreezingSequence(A.getFirstState(), bn, numNodesProjection, indicesToKnockOut, seq_2, seq_1); //sequenza invertita


        setAttractorsProjections.add(resFirstFreezingSequence.get(0));
        setAttractorsProjections.add(resSecondFreezingSequence.get(0));
        /**
         *          setAttractorsProjections contiene i seguenti nodi dell'albero:
         *              A
         *             / \
         *            B'  B''
         */
        if (setAttractorsProjections.size() != 3){
            return 0;
        }

        setAttractorsProjections.add(resFirstFreezingSequence.get(1));
        setAttractorsProjections.add(resSecondFreezingSequence.get(1));

        /**
         *          setAttractorsProjections contiene i seguenti nodi dell'albero:
         *              A
         *             / \
         *            B'  B''
         *           /     \
         *          C'      C''
         */
        System.out.println("setAttractorsProjections");
        System.out.println(setAttractorsProjections);

        if (setAttractorsProjections.size() == 4){
            return 1;
        } else if (setAttractorsProjections.size() == 5){
            return 2;
        }
        return 0;

    }

    public static List<List<BinaryState>> tryFreezingSequence(final BinaryState sample,
                                           final BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn,
                                           final int numNodesProjection,
                                           final List<Integer> initialIndicesToKnockOutForFreezingLevel,
                                           final List<Integer> seq_1,
                                           final List<Integer> seq_2){
        /**
         *
         *      A
         *      |
         *      B
         *      |
         *      C
         *
         */


        List<Integer> temp_1 = new ArrayList<>(initialIndicesToKnockOutForFreezingLevel);
        temp_1.addAll(seq_1);
        ImmutableAttractor<BinaryState> B = getFrozenAttractor(sample.setNodesValues(Boolean.FALSE, seq_1.toArray(new Integer[0])),bn,temp_1);
        List<BinaryState> b = getProjection(B, numNodesProjection, bn.getNodesNumber());

        BinaryState newSampleFromReachedAttractor = B.getFirstState();
        //ORA DEVO RIPARTIRE DALLO STATO DELL'ATTRATTORE APPENA TROVATO !!!!

        List<Integer> temp_2 = temp_1;
        temp_2.addAll(seq_2);

        List<BinaryState> c = getProjection(getFrozenAttractor(newSampleFromReachedAttractor.setNodesValues(Boolean.FALSE, seq_2.toArray(new Integer[0])),bn,temp_2), numNodesProjection, bn.getNodesNumber());

        //Set<List<BinaryState>> set = new HashSet<>();
        //set.add(b);
        //set.add(c);
        return List.of(b,c);
    }


    public static ImmutableAttractor<BinaryState> getFrozenAttractor(final BinaryState sample,
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

        return att.getAttractors().get(0);
    }




    public static void investigatePathDependecyProperty(final int nodesNumber,
                                                    final int k,
                                                    final double bias,
                                                    final int howManyNetworks,
                                                    final int howManySamplesPerFrozenLevel,
                                                    final List<Double> freezingLevelFraction,
                                                    final Double phenotypeFraction,
                                                    final int sizeOfFreezingSequences,
                                                    final RandomGenerator r){
        //freezingLevelFraction rappresenta la frazione di nodi da congelare che appartiene alla parte dei geni congelabili, e quindi non quelli su cui si farà la proiezione degli attrattori

        for (int i = 0; i < howManyNetworks; i++) {
            BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
            bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

            for(Double frozenFraction : freezingLevelFraction){
                int numberOfFrozenStartingNodes = (int)Math.ceil(frozenFraction * (1-phenotypeFraction) * nodesNumber);
                List<Integer> indicesToKnockOut = IntStream.range(0, numberOfFrozenStartingNodes).boxed().collect(Collectors.toList());

                Generator<BinaryState> samples
                        = new FrozenGenerator(BigInteger.valueOf(howManySamplesPerFrozenLevel), bn.getNodesNumber(), r, indicesToKnockOut,true);

                Stream.generate(samples::nextSample)
                        .limit(samples.totalNumberOfSamplesToBeGenerated().intValue())
                        .forEach((BinaryState sample)-> tryPairOfFreezing(sample,
                                                                          bn,
                                                                          indicesToKnockOut,
                                                                          (int)Math.ceil(phenotypeFraction * nodesNumber),
                                                                          sizeOfFreezingSequences));

            }
        }


    }

    public static void main(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        final int nodesNumber = 10;
        List<Integer> list_indices = IntStream.range(0,nodesNumber).boxed().collect(Collectors.toList());

        final int k = 2;
        final double bias = 0.5;
        final int howManyNetworks = 1;
        final int howManySamplesForEachFrozenLevel = 1;
        investigatePathDependecyProperty(nodesNumber,k,bias,howManyNetworks,howManySamplesForEachFrozenLevel,List.of(0.1,0.4), 0.3 ,2,r);
    }
}
