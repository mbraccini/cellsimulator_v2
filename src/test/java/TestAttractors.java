import attractor.ImmutableAttractorImpl;
import attractor.MutableAttractorImpl;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Attractors;
import interfaces.dynamic.Dynamics;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import network.BooleanNetworkFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import simulator.AttractorsFinderService;
import states.ImmutableBinaryState;
import utility.Files;

import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class TestAttractors {

    /**
     * Retrieves the path
     * @param path
     * @return
     */
    private String path(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
    }

    static Random pureRandomGenerator;

    @BeforeClass
    public static void initializationRunOnce() {
        pureRandomGenerator = RandomnessFactory.getPureRandomGenerator();
    }


    @Test(expected = UnsupportedOperationException.class)
    public void TestImmutabilityWithAddAndSort() {

         String bnFilename = path("testing"
                + Files.FILE_SEPARATOR
                + "sync"
                + Files.FILE_SEPARATOR
                + "sync_bn_1");

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);

        System.out.println(bn);

        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Complete Enumeration **/
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        /** Sync AttractorsUtility Finder **/
        Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics, true, true);


        attractors.getAttractors().add(new ImmutableAttractorImpl<BinaryState>(new MutableAttractorImpl<>(List.of(ImmutableBinaryState.valueOf("0100"))),5));

        attractors.getAttractors().sort((x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)));

    }


}
