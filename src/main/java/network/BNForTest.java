package network;

import dynamic.SynchronousDynamicsImpl;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import interfaces.network.*;
import states.ImmutableBinaryState;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BNForTest extends BNClassicImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> implements BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>{


	public BNForTest() {
		super();
		configure();
	}

	private final void configure(){
		initNodes();
		initTopology();
	}
	
	private void initNodes() {

		nodes.add(new NodeDeterministicImpl<>("gene_"+0, 0, new OrTable(2)));
		nodes.add(new NodeDeterministicImpl<>("gene_"+1, 1, new AndTable(2)));
		nodes.add(new NodeDeterministicImpl<>("gene_"+2, 2, new OrTable(2)));
		nodes.add(new NodeDeterministicImpl<>("gene_"+3, 3, new AndTable(2)));

	}
	
	private void initTopology() {
		incomingNodesMap.put(0, new ArrayList<>(Arrays.asList(1, 2)));
		incomingNodesMap.put(1, new ArrayList<>(Arrays.asList(0, 3)));
		incomingNodesMap.put(2, new ArrayList<>(Arrays.asList(1, 3)));
		incomingNodesMap.put(3, new ArrayList<>(Arrays.asList(0, 2)));

	}

	@Override
	public BNClassic<BitSet, Boolean,NodeDeterministic<BitSet, Boolean>> newInstance(Set<NodeDeterministic<BitSet, Boolean>> nodes, Map<Integer, List<Integer>> topology) {
		return new BNClassicImpl<>(nodes,topology);
	}


	public static void main(String a[]){
		RandomGenerator r = RandomnessFactory.getPureRandomGenerator();

		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test = new BNForTest();
		BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test2 = new BNKBiasImpl(3,2,0.5,r,BNKBias.BiasType.EXACT,false);
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> miRNA =
									BooleanNetworkFactory.miRNANetworkInstance(test,2,2,0.5,1,r);



		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> o = new BNClassicBuilder<>(test2)
																			.build();
		//BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> o = BNKBiasImpl.modifyFrom(test2).build();


		miRNABNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>,NodeDeterministic<BitSet,Boolean>> omiRNA = BooleanNetworkFactory.miRNAOneInput(test,2,2,r);
		/*BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> oM = new BNClassicBuilder<>(omiRNA)
				.modifyBooleanFunction(new NodeDeterministicImpl<>(miRNA.getNodeById(1).getName(),1, new OrTable(2)))
				.build();*/




		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> test3 = new BNClassicBuilder<>(test)
															.reconfigureIncomingEdge(0,0,1)
															.addIncomingNode(0,3)
															.replaceNode(test.getNodeById(0), new NodeDeterministicImpl<>("sostituito",0, new OrTable(3)))
															.build();

		System.out.println(test);
		System.out.println(test3);
		System.out.println(test.equals(test3));
		System.out.println(test3.equals(test));
		System.out.println(test == test3);








		/*System.out.println(cTest);
		System.out.println(test.equals(cTest));
		System.out.println(test == cTest);*/

		//System.out.println(test.equals(test.newInstance(test.asGraph())));

		//System.out.println(bn.newInstance(bn.asGraph()).equals(bn));

		System.out.println(Stream.iterate(ImmutableBinaryState.valueOf("0011"), new SynchronousDynamicsImpl(test)::nextState).limit(10).collect(Collectors.toList()));

		System.out.println(new SynchronousDynamicsImpl(test)
								.nextState(ImmutableBinaryState.valueOf("0011")
								));
	}


}
