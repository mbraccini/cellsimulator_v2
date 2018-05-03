
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;

import attractor.AttractorsImpl;
import attractor.ImmutableAttractorImpl;
import attractor.MutableAttractorImpl;
import attractor.AttractorsUtility;
import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import generator.UniformlyDistributedGenerator;
import interfaces.attractor.*;
import interfaces.sequences.Generator;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.junit.Test;

import simulator.AttractorsFinderService;
import states.ImmutableBinaryState;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;

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
    public void bnFromBoolNet() {

        /** BN from file "sync_bn" **/
        String bnFilename = rootDirectory
                            + Files.FILE_SEPARATOR
                            + "sync_bn_1";
        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);

        System.out.println(bn);

        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Complete Enumeration **/
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        /** Sync AttractorsUtility Finder **/
        Attractors<BinaryState> attractorsFound = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);

        System.out.println(attractorsFound);

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
        List<MutableAttractor<BinaryState>> attInfo  = new ArrayList<>(Arrays.asList(
                                                                        new MutableAttractorImpl<>(fixed_point_0),
                                                                        new MutableAttractorImpl<>(fixed_point_1),
                                                                        new MutableAttractorImpl<>(cyclic_attractor)
                                                                ));
        Attractors<BinaryState> manuallyDefinedAttractors = new AttractorsImpl<>(attInfo);
        

        /** Test 1 **/
		/* we check if there are 3 attractors */
        assertTrue(attractorsFound.numberOfAttractors() == 3);

        /** Test 2 **/
        /* we check if the attractors found contain 0000 */
        /* we check if the attractors found contain 1111 */
		/* we check if the attractors found contain [{1010}, {1001}, {0110}] */
        assertTrue("Gli Attrattori trovati differiscono da quelli ottenuti con BoolNet", attractorsFound.equals(manuallyDefinedAttractors));



        /********************* Basins of Attraction *********************/

        for (Attractor<BinaryState> att : attractorsFound.getAttractors()) {
            if (att.getBasin().isPresent()) {
                System.out.println("Dimensione bacini -> " + att.getBasin().get().getDimension());
                if (att.equals(new ImmutableAttractorImpl<>(new MutableAttractorImpl<>(fixed_point_0), 1))) {
                    System.out.println(att.getBasin().get());
                    assertTrue(att.getBasin().get().getDimension() == 4);
                } else if (att.equals(new ImmutableAttractorImpl<>(new MutableAttractorImpl<>(fixed_point_1), 3))) {
                    System.out.println(att.getBasin().get());
                    assertTrue(att.getBasin().get().getDimension() == 4);
                } else if (att.equals(new ImmutableAttractorImpl<>(new MutableAttractorImpl<>(cyclic_attractor), 2))) {
                    System.out.println(att.getBasin().get());
                    assertTrue(att.getBasin().get().getDimension() == 8);
                }
            }
        }


    }

    @Test
    public void ifStatesAreDifferent() {
        Random rnd = RandomnessFactory.newPseudoRandomGenerator(10);
        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newRBN(BooleanNetworkFactory.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, 50, 3, 0.7, rnd);


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

    /**
     * Network from: "Shape-Dependent Control of Cell Growth, Differentiation, and Apoptosis: Switching between Attractors in Cell Regulatory Networks"
     */
    @Test
    public void bn_Huang_article() {

        /** BN from file "sync_bn" **/
        String bnFilename = rootDirectory
                + Files.FILE_SEPARATOR
                + "bn_shape_dependent_control_huang";
        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);

        System.out.println(bn);

        /** Synchronous dynamics **/
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);

        /** Complete Enumeration **/
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());

        /** Sync AttractorsUtility Finder **/
        Attractors<BinaryState> attractorsFound = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);


        attractorsFound.getAttractors().forEach(System.out::println);
        System.out.println(attractorsFound.numberOfAttractors());


        for (Attractor<BinaryState> att : attractorsFound.getAttractors()) {
            if (att.getBasin().isPresent()) {
                System.out.println(att.getBasin().get().getDimension());
            }
        }

        Atm<BinaryState> atm = new CompletePerturbations().apply(attractorsFound, dynamics, Constant.PERTURBATIONS_CUTOFF);

        GenericUtility.printMatrix(atm.getMatrix());
    }

}
