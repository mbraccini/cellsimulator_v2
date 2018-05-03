import attractor.ImmutableAttractorImpl;
import attractor.MutableAttractorImpl;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import simulator.AttractorsFinderService;
import utility.Files;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class TestAttractors {

    static Random pureRandomGenerator;

    @BeforeClass
    public static void initializationRunOnce() {
        pureRandomGenerator = RandomnessFactory.getPureRandomGenerator();
    }


    @Test(expected = UnsupportedOperationException.class)
    public void TestImmutabilityWithAddAndSort() {

         String bnFilename = "testing"
                + Files.FILE_SEPARATOR
                + "sync"
                + Files.FILE_SEPARATOR
                + "sync_bn_1";

        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);

        System.out.println(bn);

        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Complete Enumeration **/
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        /** Sync AttractorsUtility Finder **/
        Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);


        attractors.getAttractors().add(new ImmutableAttractorImpl<BinaryState>(new MutableAttractorImpl<>(List.of(BinaryState.valueOf("0100"))),5));

        attractors.getAttractors().sort((x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)));

    }


}
