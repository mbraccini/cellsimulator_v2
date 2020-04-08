package experiments.net8080cellcollective;

import dynamic.SynchronousDynamicsImpl;
import generator.FixedNodesGenerator;
import generator.FrozenGenerator;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.apache.commons.math3.random.RandomGenerator;
import simulator.AttractorsFinderService;
import utility.Files;
import utility.RandomnessFactory;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.List;

public class TestAttractors {

    private static long SAMPLES = 100000;
    public static void main(String args[]){
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> bn = BooleanNetworkFactory.newNetworkFromFile("8080_v1.txt");

       /* Generator<BinaryState> genfoz = new FrozenGenerator(BigInteger.valueOf(SAMPLES),
                bn.getNodesNumber(),
                r,
                List.of(0,1,2,3,4,5,6,7,8,9,10,11,12,13)
        );
        BigInteger i = BigInteger.ZERO;
        while(i.compareTo(genfoz.totalNumberOfSamplesToBeGenerated())<0){
            System.out.println(genfoz.nextSample());
            i=i.add(BigInteger.ONE);
        }
        System.exit(0);
*/

       Generator<BinaryState> gen = new FixedNodesGenerator(BigInteger.valueOf(SAMPLES),
                bn.getNodesNumber(),
                r,
                List.of(0,1,2,3,4,5,6,7,8,9,10,11,12,13),
                List.of(Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE,Boolean.FALSE));

        Dynamics<BinaryState> dyn = new SynchronousDynamicsImpl(bn);

        Attractors<BinaryState> atts = AttractorsFinderService.apply(gen,
                dyn,
                false,
                false,
                AttractorsFinderService.TRUE_TERMINATION);

        Files.writeAttractorsToReadableFile(atts,"8080_case_1_Michele.txt");
    }
}
