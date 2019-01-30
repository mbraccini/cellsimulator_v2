package experiments.methylation;

import dynamic.SynchronousDynamicsImpl;
import generator.BagOfStatesGenerator;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
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
import java.util.stream.Stream;

public class MainFrozenExp1PhasesAttrCombinations {


    //ANALISI 2
    static final int BN_SAMPLES = 1;
    static final String CSV_SEPARATOR = ",";
    static final String COMBINATIONS_FOR_COMPUTING_ATTRS = "4";//"100000";
    static final Integer HOW_MANY_INDICES_COMBINATIONS = 20;

    public static void main(String args[]){

        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        /*int numNodes = Integer.valueOf(args[0]);
        int k = Integer.valueOf(args[3]);
        double bias = Double.valueOf(args[4]);
        */
        int numNodes = 10;
        int k = 2;
        double bias = 0.5;
        int frozenNodesNum = 5;
        int sizeOfIdxCombinations=3; //triplette

        String pathFolder = "FrozenAnalisi1"  + Files.FILE_SEPARATOR;
        Files.createDirectories(pathFolder);
        try (BufferedWriter csv = new BufferedWriter(new FileWriter(pathFolder + "stats.csv", true))) {
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
        Generator<BinaryState> wildGen = new UniformlyDistributedGenerator(new BigInteger(COMBINATIONS_FOR_COMPUTING_ATTRS),numNodes,r);
        Dynamics<BinaryState> wildDyn = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> wildAttrs = StaticAnalysisTES.attractors(wildGen, wildDyn);

        // We take one RND attrs
        int numAttrs = wildAttrs.numberOfAttractors();
        ImmutableAttractor<BinaryState> attr = wildAttrs.getAttractors().get(r.nextInt(numAttrs));
        // We save the length
        stats.append(attr.getLength() + CSV_SEPARATOR);

        // we freeze "frozenNodesNumber" nodes
        //Per ogni combinazione di indici congelati (triplette)
            //Per ogni fase dell'attrattore
                //calcolo numero di attrattori e riporto in stats.csv il numero assoluto

        for (int i = 0; i < HOW_MANY_INDICES_COMBINATIONS; i++) {
            System.out.println(rndIndices(numNodes,frozenNodesNumber,sizeOfIdxCombinations,r));
        }

    }

    private static List<Integer> rndIndices(final int bnNumOfNodes, final int alreadyFrozenMaxIndex, final int howMany, final RandomGenerator r){
        List<Integer> l = new ArrayList<>();
        int range = bnNumOfNodes - alreadyFrozenMaxIndex;
        for (int i = 0; i < howMany; i++) {
            l.add(r.nextInt(range) + alreadyFrozenMaxIndex);
        }
        return l;
    }
}
