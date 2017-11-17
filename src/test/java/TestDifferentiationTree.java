import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.DifferentiationTree;
import interfaces.tes.Tes;
import network.NetworkFromFile;
import noise.CompletePerturbations;
import org.junit.Test;
import simulator.AttractorsFinderService;
import tes.TesCreator;

public class TestDifferentiationTree {

	@Test
	public void DiffTree_1() {

		/** BN from file*/
		BooleanNetwork<BitSet, Boolean> read_bn = NetworkFromFile.newNetworkFromFile("testing/diff_trees/bn");

		/** Synchronous dynamics **/
		Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(read_bn);

		/** Complete Enumeration **/
		Generator<BinaryState> generator = new CompleteGenerator(read_bn.getNodesNumber());

		/** Sync Attractors Finder **/
		ImmutableList<LabelledOrderedAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();

		assertTrue("Dovrebbe trovare 4 attrattori", attractors.size() == 4);

		
		CompletePerturbations cp = new CompletePerturbations(attractors, dynamics, 50000);
		Atm<BinaryState> atm = cp.call();
		
		DifferentiationTree<Tes<BinaryState>> tesTree = new TesCreator<BinaryState>(atm, RandomnessFactory.getPureRandomGenerator()).call();
		
		List<Double> thresCheck = new ArrayList<>(Arrays.asList(0.0, 0.2, 0.45));
		
		System.out.println(tesTree.getThresholds());
		
		assertTrue("L'albero dei TES deve avere 1 radice", tesTree.getRootLevel().size() == 1);

		assertTrue("L'albero dei TES deve avere 3 livelli", tesTree.getLevelsNumber() == 3);
		
		assertTrue("L'albero dei TES deve avere queste soglie 0.0, 0.2 e 0.45", thresCheck.equals(tesTree.getThresholds()));


	}
}
