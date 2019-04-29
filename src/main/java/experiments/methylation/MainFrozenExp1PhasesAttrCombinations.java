package experiments.methylation;

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
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainFrozenExp1PhasesAttrCombinations {


    //ANALISI 2
    static final int BN_SAMPLES = 100;
    static final String CSV_SEPARATOR = ",";
    static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "100000";
    static final Integer HOW_MANY_INDICES_COMBINATIONS = 1000;

    public static void main(String args[]){

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = Integer.valueOf(args[0]);
        int k = Integer.valueOf(args[1]);
        double bias = Double.valueOf(args[2]);
        int frozenNodesNum = Integer.valueOf(args[3]);
        int sizeOfIdxCombinations = Integer.valueOf(args[4]);

        /*
        int numNodes = 50;
        int k = 2;
        double bias = 0.1;
        int frozenNodesNum = 5;
        int sizeOfIdxCombinations=5; //triplette
        */

        System.out.println("...ANALISI 1...\n" +
                "numNodes: " + numNodes + "\n" +
                "k: " + k + "\n" +
                "bias: " + bias + "\n" +
                "initialFrozenNodesNum: " +  frozenNodesNum +"\n" +
                "blockSizeIdxs: " +  sizeOfIdxCombinations + "\n" +
                "HOW_MANY_INDICES_COMBINATIONS: " + HOW_MANY_INDICES_COMBINATIONS +"\n" +
                "COMBINATIONS_FOR_COMPUTING_ATTRS: " + COMBINATIONS_FOR_COMPUTING_ATTRS
        );

        String pathFolder = "FrozenAnalisi1"  + Files.FILE_SEPARATOR;
        Files.createDirectories(pathFolder);
        String filename = "n" + numNodes + "k" + k + "p" + bias + "frz" + frozenNodesNum + "block" + sizeOfIdxCombinations;
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + filename + "_stats.csv", true))) {
            for (int i = 0; i < BN_SAMPLES; i++) {
                forEachBN(i, numNodes, k, bias, r, csv, frozenNodesNum,sizeOfIdxCombinations);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static private void forEachBN(  final int id,
                                    final int numNodes,
                                    final int k,
                                    final double bias,
                                    final RandomGenerator r,
                                    final BufferedWriter stats,
                                    final int frozenNodesNumber,
                                    final int sizeOfIdxCombinations) throws IOException {

        // COMPUTE ATTRACTORS
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT,BooleanNetworkFactory.SelfLoop.WITHOUT,numNodes,k,bias,r);
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

        //ATTRACTORS
        Attractors<BinaryState> attrs = StaticAnalysisTES.attractors(generator, dynamics);

        // We take one RND attrs
        int numAttrs = attrs.numberOfAttractors();
        ImmutableAttractor<BinaryState> attr = attrs.getAttractors().get(r.nextInt(numAttrs));
        /*ImmutableAttractor<BinaryState> attr =  chooseAttractorBiggerBOA(attrs);*/
        // We save the length
        int attLength = attr.getLength();
        stats.append(attLength + CSV_SEPARATOR);

        // we freeze "frozenNodesNumber" nodes
        //Per ogni combinazione di indici congelati (triplette)
            //Per ogni fase dell'attrattore
                //calcolo numero di attrattori e riporto in stats.csv il numero assoluto

        for (int i = 0; i < HOW_MANY_INDICES_COMBINATIONS; i++) {
            List<Integer> idxs = rndIndices(numNodes,frozenNodesNumber,sizeOfIdxCombinations,r);
            //System.out.println("indices: "+ idxs);
            List<Integer> indicesToFreezeForPhases = new ArrayList<>(indicesToFreeze);
            indicesToFreezeForPhases.addAll(idxs);
            //System.out.println("indicesToFreezeForPhases: " + indicesToFreezeForPhases);

            //KNOCK OUT DYNAMICS
            Dynamics<BinaryState> dynamicSinglePhase = DecoratingDynamics
                        .from(new SynchronousDynamicsImpl(bn))
                        .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToFreezeForPhases));

                //GENERATOR
            Generator<BinaryState> sampleSinglePhase =
                        new BagOfStatesGenerator<>(new ArrayList<>(attr.getStates()));

            Generator<BinaryState> genSinglePhase = new BagOfStatesGenerator<>(Stream.generate(sampleSinglePhase::nextSample)
                    .limit(attLength)
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreezeForPhases.toArray(new Integer[0])))
                    .collect(Collectors.toList()));

            //ATTRACTORS
            Attractors<BinaryState> atts = StaticAnalysisTES.attractors(genSinglePhase, dynamicSinglePhase);

            stats.append(atts.numberOfAttractors() + "");

            if (i != HOW_MANY_INDICES_COMBINATIONS - 1) {
                stats.append(CSV_SEPARATOR);
            }
        }

        stats.append("\n");
    }

    private static ImmutableAttractor<BinaryState> chooseAttractorBiggerBOA(Attractors<BinaryState> attrs){
        int bigger = 0;
        ImmutableAttractor<BinaryState> attBigger = null;
        for (ImmutableAttractor<BinaryState> a : attrs) {
            if(a.getBasinSize().isPresent()){
                int temp = a.getBasinSize().get();
                if (temp > bigger){
                    attBigger = a;
                    bigger = temp;
                }
            }
        }
        return attBigger;
    }

    private static List<Integer> rndIndices(final int bnNumOfNodes, final int alreadyFrozenMaxIndex, final int howMany, final RandomGenerator r){
        List<Integer> l = new ArrayList<>();
        int range = bnNumOfNodes - alreadyFrozenMaxIndex;
        int temp;
        for (int i = 0; i < howMany;) {
            temp = r.nextInt(range) + alreadyFrozenMaxIndex;
            if (!l.contains(temp)){
                l.add(temp);
                i++;
            }
        }
        return l;
    }
}
