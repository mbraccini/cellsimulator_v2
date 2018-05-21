package network;

import dynamic.SynchronousDynamicsImpl;
import generator.RandomnessFactory;
import interfaces.core.Factory;
import interfaces.network.*;
import interfaces.state.BinaryState;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import utility.Files;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BNForTest extends AbstractBooleanNetwork<NodeDeterministic<BitSet,Boolean>> implements BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>{


	public BNForTest() {
		super();
		configure();
	}

	private final void configure(){
		initNodes();
		initTopology();
	}
	
	private void initNodes() {

		nodesList.add(new NodeDeterministicImpl<>("gene_"+0, 0, new OrTable(2)));
		nodesList.add(new NodeDeterministicImpl<>("gene_"+1, 1, new AndTable(2)));
		nodesList.add(new NodeDeterministicImpl<>("gene_"+2, 2, new OrTable(2)));
		nodesList.add(new NodeDeterministicImpl<>("gene_"+3, 3, new AndTable(2)));

	}
	
	private void initTopology() {
		incomingNodesMap.put(0, new ArrayList<>(Arrays.asList(1, 2)));
		incomingNodesMap.put(1, new ArrayList<>(Arrays.asList(0, 3)));
		incomingNodesMap.put(2, new ArrayList<>(Arrays.asList(1, 3)));
		incomingNodesMap.put(3, new ArrayList<>(Arrays.asList(0, 2)));

	}

	@Override
	public BNClassic<BitSet, Boolean,NodeDeterministic<BitSet, Boolean>> newInstance(List<NodeDeterministic<BitSet, Boolean>> nodes, Map<Integer, List<Integer>> topology) {
		return new BNClassicImpl<>(nodes,topology);
	}


	public static void main(String a[]){
		Random r = RandomnessFactory.getPureRandomGenerator();
		/*BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNKBiasImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> test = new BNForTest().newInstance();
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNKBiasImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> bn
					= new BNKBiasImpl<>(4,2,r,() -> new BiasedTable(2,0.5,r)).newInstance();


		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNKBiasImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> cTest =
				test.modifyFromThis()
						.reconfigureIncomingEdge(0, 0,2)
						.build();*/

		/*BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test = new BNForTest().newInstance();
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> bn
				= new BNKBiasImpl<>(4,2,r,() -> new BiasedTable(2,0.5,r)).newInstance();
		*/

		/*BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> cTest =
		BNKBiasImpl.from(test)
				.changeNode(3, new NodeDeterministicImpl<>("Gauss", 3, new BiasedTable(3,0.5,r)))
				.reconfigureIncomingEdge(2,2,1)
				.changeNode(2, new NodeDeterministicImpl<>("Euclide", 2, new BiasedTable(2,0.5,r)))
				.reconfigureIncomingEdge(3,3,1)
				.build();*/

		System.out.println(   new File("src/test/data").getAbsolutePath());
		System.out.println(new File("src/test/resources/testing/sync/sync_bn_1").getAbsolutePath());
		BooleanNetworkFactory.newNetworkFromFile("testing/sync/bn_shape_dependent_control_huang");

		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test = new BNForTest();
		BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test2 = new BNKBiasImpl(3,2,0.5,r,BNKBias.BiasType.EXACT,false);


		//System.out.println(bn.getThis());
		System.out.println(test2);
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> o = new BNClassicBuilder<>(test2)
																			.build();
		//BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> o = BNKBiasImpl.modifyFrom(test2).build();

		System.out.println(o);
		System.out.println(o==test2);
		System.out.println(o.equals(test2));





		/*System.out.println(cTest);
		System.out.println(test.equals(cTest));
		System.out.println(test == cTest);*/

		//System.out.println(test.equals(test.newInstance(test.asGraph())));

		//System.out.println(bn.newInstance(bn.asGraph()).equals(bn));

		System.out.println(Stream.iterate(BinaryState.valueOf("0011"), new SynchronousDynamicsImpl(test)::nextState).limit(10).collect(Collectors.toList()));

		System.out.println(new SynchronousDynamicsImpl(test)
								.nextState(BinaryState.valueOf("0011")
								));
	}


}
