import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Attractors;
import interfaces.sequences.Generator;
import interfaces.attractor.ImmutableAttractor;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import interfaces.tes.DifferentiationTree;
import interfaces.tes.TESDifferentiationTree;
import interfaces.tes.Tes;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.junit.Test;
import simulator.AttractorsFinderService;
import tes.TesCreator;
import utility.Constant;
import visualization.DifferentiationTesTreeGraphViz;
import visualization.DifferentiationTreeGraphViz;

public class TestDifferentiationTree {

	@Test
	public void DiffTree_1() {

		/** BN from file*/
		BooleanNetwork<BitSet, Boolean> read_bn = BooleanNetworkFactory.newNetworkFromFile("testing/diff_trees/bn");

		/** Synchronous dynamics **/
		Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(read_bn);

		/** Complete Enumeration **/
		Generator<BinaryState> generator = new CompleteGenerator(read_bn.getNodesNumber());

		/** Sync Attractors Finder **/
		Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics);

		assertTrue("Dovrebbe trovare 4 attrattori", attractors.numberOfAttractors() == 4);


		Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);

		TESDifferentiationTree<BinaryState, Tes<BinaryState>> tesTree = new TesCreator<BinaryState>(atm, RandomnessFactory.getPureRandomGenerator()).call();
		
		List<Double> thresCheck = new ArrayList<>(Arrays.asList(0.0, 0.2, 0.45));
		
		System.out.println(tesTree.getThresholds());
		
		assertTrue("L'albero dei TES deve avere 1 radice", tesTree.getRootLevel().size() == 1);

		assertTrue("L'albero dei TES deve avere 3 livelli", tesTree.getLevelsNumber() == 3);
		
		assertTrue("L'albero dei TES deve avere queste soglie 0.0, 0.2 e 0.45", thresCheck.equals(tesTree.getThresholds()));


	}
}
