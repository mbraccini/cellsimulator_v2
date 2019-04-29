package experiments.methylation;

import dynamic.KnockOutDynamicsDecorator;
import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.BNKBias;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.text.similarity.HammingDistance;
import tes.StaticAnalysisTES;
import utility.Files;
import utility.RandomnessFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MainFrozenExp2ProgressiveDerridaNumAttractors {

    //ANALISI 2
    static final int BN_SAMPLES = 100;
    static final String STATES_SAMPLE_DERRIDA = "1000";
    static final String CSV_SEPARATOR = ",";
    //static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "100000";


    public static void main(String args[]){

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        int numNodes = Integer.valueOf(args[0]);
        int k = Integer.valueOf(args[1]);
        double bias = Double.valueOf(args[2]);
        int [] percentagesOfNodesToFreeze = new int[]{10,20,50};
        /*
        int numNodes = 10;
        int k = 3 ;
        double bias = 0.5;
        */
        System.out.println("...ANALISI 2...\n" +
                "numNodes: " + numNodes + "\n" +
                "k: " + k + "\n" +
                "bias: " + bias + "\n" +
                "STATES_SAMPLE_DERRIDA: " + STATES_SAMPLE_DERRIDA + "\n" +
                //"COMBINATIONS_FOR_COMPUTING_ATTRS: " + COMBINATIONS_FOR_COMPUTING_ATTRS + "\n" +
                "percentagesOfNodesToFreeze: " + Arrays.toString(percentagesOfNodesToFreeze)
        );

        String pathFolder = "FrozenAnalisi2"  + Files.FILE_SEPARATOR;
        Files.createDirectories(pathFolder);
        String filename = "n" + numNodes + "k" + k + "p" + bias ;
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + filename + "_stats.csv", true))) {
            // HEADER
            csv.append("wildAttr, wildDerrida, 10%FrAttr, 10%FrDerrida, 20%FrAttr, 20%FrDerrida, 50%FrAttr, 50%FrDerrida\n" );
            // FINE HEADER
            for (int i = 0; i < BN_SAMPLES; i++) {
                forEachBN( numNodes, k, bias, r, csv, percentagesOfNodesToFreeze);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    static private void forEachBN(
                                    final int numNodes,
                                    final int k,
                                    final double bias,
                                    final RandomGenerator r,
                                    final BufferedWriter stats,
                                    int [] percentagesToFreeze) throws IOException {
        /* PASSO 1 (WILD)
           #attrs
           Derrida caso semplice (solo stati a caso)
        */
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn;
        bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.EXACT,BooleanNetworkFactory.SelfLoop.WITHOUT,numNodes,k,bias,r);
        Dynamics<BinaryState> wildDyn = new SynchronousDynamicsImpl(bn);
        /*
        Generator<BinaryState> wildGen = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_COMPUTING_ATTRS),numNodes,r);
        Attractors<BinaryState> wildAttrs = StaticAnalysisTES.attractors(wildGen, wildDyn);
        // write no. attrs
        stats.append(wildAttrs.numberOfAttractors() + CSV_SEPARATOR);
        */
        stats.append(0 + CSV_SEPARATOR);


        // DERRIDA
        Generator<BinaryState> wildDerridaSamples
                = new UniformlyDistributedGenerator(new BigInteger(STATES_SAMPLE_DERRIDA), numNodes, r);
        stats.append(Derrida(wildDerridaSamples, wildDyn, IntStream.range(0, numNodes).boxed().collect(Collectors.toSet())) + CSV_SEPARATOR);


        // FOR EACH CONFIG
        int toFreeze;
        for (int i = 0; i < percentagesToFreeze.length; i++) {
            toFreeze = (int)((((double) numNodes) / 100) * percentagesToFreeze[i]);
            //System.out.println("toFreeze: " + toFreeze);
            //FREEZE
            List<Integer> indicesToFreeze = IntStream.range(0, toFreeze).boxed().collect(Collectors.toList());
            //KNOCK OUT DYNAMICS

            Dynamics<BinaryState> dynamicsKO = DecoratingDynamics
                    .from(new SynchronousDynamicsImpl(bn))
                    .decorate(dyn -> new KnockOutDynamicsDecorator(dyn, indicesToFreeze));
            /*
            Generator<BinaryState> samplesKO
                    = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_COMPUTING_ATTRS), numNodes, r);

            Generator<BinaryState> genKO = new BagOfStatesGenerator<>(Stream.generate(samplesKO::nextSample)
                    .limit(Integer.valueOf(COMBINATIONS_FOR_COMPUTING_ATTRS))
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreeze.toArray(new Integer[0])))
                    .collect(Collectors.toList()));
            //ATTRACTORS
            Attractors<BinaryState> attrsKO = StaticAnalysisTES.attractors(genKO, dynamicsKO);
            stats.append(attrsKO.numberOfAttractors() + CSV_SEPARATOR);
            */
            stats.append(0 + CSV_SEPARATOR);

            // DERRIDA
            Generator<BinaryState> samplesDerridaKO
                    = new UniformlyDistributedGenerator(new BigInteger(STATES_SAMPLE_DERRIDA), numNodes, r);

            Generator<BinaryState> genDerridaKO = new BagOfStatesGenerator<>(Stream.generate(samplesDerridaKO::nextSample)
                    .limit(Integer.valueOf(STATES_SAMPLE_DERRIDA))
                    .map(sample -> sample.setNodesValues(Boolean.FALSE, indicesToFreeze.toArray(new Integer[0])))
                    .collect(Collectors.toList()));

            stats.append(Derrida(genDerridaKO, dynamicsKO, IntStream.range(toFreeze, numNodes).boxed().collect(Collectors.toSet())) + "");
            if (i != percentagesToFreeze.length - 1) {
                stats.append(CSV_SEPARATOR);
            }
        }
        stats.append("\n");

    }


    static private double Derrida(Generator<BinaryState> samples, Dynamics<BinaryState> dyn, Set<Integer> notFreezedIndices){
        //mean of means
        BinaryState state = samples.nextSample();
        HammingDistance hd = new HammingDistance();
        List<Double> means = new ArrayList<>();
        int hammingDistSum;
        while(state != null){
            //
            hammingDistSum = 0;
            for (Integer idx : notFreezedIndices) {
                /*
                System.out.println("state: " + state);
                System.out.println("afterFlip: " + dyn.nextState(state.flipNodesValues(idx)));
                System.out.println("onlyUpdate: " + dyn.nextState(state));
                System.out.println("hamming:" + hd.apply(dyn.nextState(state.flipNodesValues(idx)).getStringRepresentation(), dyn.nextState(state).getStringRepresentation()));
                */
                hammingDistSum += hd.apply(dyn.nextState(state.flipNodesValues(idx)).getStringRepresentation(), dyn.nextState(state).getStringRepresentation());
            }
            means.add((double)hammingDistSum / notFreezedIndices.size());

            // next state
            state = samples.nextSample();
        }
        //System.out.println("means: " + means);
        return means.stream().mapToDouble(x -> x).average().orElse(-1);
    }





}
