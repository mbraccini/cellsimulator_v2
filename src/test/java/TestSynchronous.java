
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.*;

import attractor.AttractorImpl;
import attractor.AttractorInfoImpl;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.Generator;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import network.RBN;
import network.NetworkFromFile;
import org.junit.Test;

import simulator.AttractorsFinderService;
import states.ImmutableBinaryState;
import utility.Files;

public class TestSynchronous {

    private String rootDirectory = "testing"
                                    + Files.FILE_SEPARATOR
                                    + "sync";

    /**
     * Tests a simple bn ("sync_bn_1") with synchronous update scheme, previously verified with BoolNet
     *
     *  BoolNet:
     *
     *  Attractor 1 is a simple attractor consisting of 1 state(s) and has a basin of 4 state(s):
     *  |--<---|
     *  V      |
     *  0000   |
     *  V      |
     *  |-->---|
     *
     *  Genes are encoded in the following order: Gene0 Gene1 Gene2 Gene3
     *
     *  Attractor 2 is a simple attractor consisting of 1 state(s) and has a basin of 4 state(s):
     *  |--<---|
     *  V      |
     *  1111   |
     *  V      |
     *  |-->---|
     *
     *  Genes are encoded in the following order: Gene0 Gene1 Gene2 Gene3
     *
     *  Attractor 3 is a simple attractor consisting of 3 state(s) and has a basin of 8 state(s):
     *
     *  |--<---|
     *  V      |
     *  1010   |
     *  1001   |
     *  0110   |
     *  V      |
     *  |-->---|
     *
     *  Genes are encoded in the following order: Gene0 Gene1 Gene2 Gene3
     **/
    @Test
    public void Sync_TestBN_BoolNet() {

        /** BN from file "sync_bn" **/
        String bnFilename = rootDirectory
                            + Files.FILE_SEPARATOR
                            + "sync_bn_1";
        BooleanNetwork<BitSet, Boolean> bn = NetworkFromFile.newNetworkFromFile(bnFilename);

        System.out.println(bn);

        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Complete Enumeration **/
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        /** Sync Attractors Finder **/
        List<LabelledOrderedAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();

        attractors.forEach(System.out::println);

        /** Tests **/
		/* we check if there are 3 attractors */
        assertTrue(attractors.size() == 3);

        /*********************************************************/ //Attrattori
        List<BinaryState> fixed_point_0 =  new ArrayList<>();
        BinaryState attr_0 = new ImmutableBinaryState(4);
        fixed_point_0.add(attr_0);
        /*********************************************************/
        List<BinaryState> fixed_point_1 =  new ArrayList<>();
        BinaryState attr_1 = new ImmutableBinaryState(4, 0, 1, 2, 3);
        fixed_point_1.add(attr_1);
        /*********************************************************/
        List<BinaryState> cyclic_attractor =  new ArrayList<>();
        BinaryState s_1 = new ImmutableBinaryState(4, 1, 2);
        cyclic_attractor.add(s_1);

        BinaryState s_2 = new ImmutableBinaryState(4,0, 2);
        cyclic_attractor.add(s_2);

        BinaryState s_3 = new ImmutableBinaryState(4,0, 3);
        cyclic_attractor.add(s_3);
        /*********************************************************/
        LabelledOrderedAttractor<BinaryState> attractor_fixed_point = new AttractorImpl<>(new AttractorInfoImpl<>(fixed_point_0, 1));
		/* we check if the attractors found contain 0000 */
        assertTrue(attractors.contains(attractor_fixed_point));

        LabelledOrderedAttractor<BinaryState> attractor_all_ones = new AttractorImpl<>(new AttractorInfoImpl<>(fixed_point_1, 3));
		/* we check if the attractors found contain 1111 */
        assertTrue(attractors.contains(attractor_all_ones));

		/* we check if the attractors found contain [{1010}, {1001}, {0110}] */
        LabelledOrderedAttractor<BinaryState> attractor_cyclics = new AttractorImpl<>(new AttractorInfoImpl<>(cyclic_attractor, 2));
        assertTrue(attractors.contains(attractor_cyclics));

        /********************* Basins of Attraction *********************/
        /*
        for (CyclicAttractor<BitSet> att : attractors) {
            System.out.println("Dimensione bacini -> " + att.getBasin().size());
            if (att.equals(new CyclicAttractorImpl<BitSet>(fixed_point_0, 1))) {
                assertTrue(att.getBasin().size() == 4);
            } else if (att.equals(new CyclicAttractorImpl<BitSet>(fixed_point_1, 2))) {
                assertTrue(att.getBasin().size() == 4);
            } else if (att.equals(new CyclicAttractorImpl<BitSet>(cyclic_attractor, 3))) {
                assertTrue(att.getBasin().size() == 8);
            }
        }
        */

    }

    @Test
    public void Sync_TestIfStatesAreDifferent() {
        Random rnd = RandomnessFactory.newPseudoRandomGenerator(10);
        BooleanNetwork<BitSet, Boolean> bn = new RBN(50, 3, 0.7, rnd);


        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Generator **/
        Generator<BinaryState> generator = new UniformlyDistributedGenerator(new BigInteger("10000"), 50, rnd);

        while (true) {
            System.out.println(".");
            BinaryState sample = generator.nextSample();
            if (sample == null) {
                break;
            }
            BinaryState state = dynamics.nextState(sample);
            assertTrue("sample does not have to change!!!", sample.equals(sample));
            assertTrue("object references must be different!!!", state != sample);
        }

    }

}
