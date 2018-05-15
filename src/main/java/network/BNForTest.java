package network;

import dynamic.SynchronousDynamicsImpl;
import generator.RandomnessFactory;
import interfaces.core.Factory;
import interfaces.network.*;
import interfaces.state.BinaryState;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BNForTest implements Factory<BNClassicImpl<BitSet,Boolean, NodeDeterministic<BitSet,Boolean>>> {


	private List<NodeDeterministicImpl<BitSet,Boolean>> nodesList = new ArrayList<>();

	private GraphBuilder<NodeDeterministic<BitSet,Boolean>, DefaultEdge, DefaultDirectedGraph<NodeDeterministic<BitSet,Boolean>,DefaultEdge>> builder
		 = new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));


	public BNForTest() {
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
		this.addIncomingNodes(0, new ArrayList<>(Arrays.asList(1, 2)));
		this.addIncomingNodes(1, new ArrayList<>(Arrays.asList(0, 3)));
		this.addIncomingNodes(2, new ArrayList<>(Arrays.asList(1, 3)));
		this.addIncomingNodes(3, new ArrayList<>(Arrays.asList(0, 2)));

	}


	private void addIncomingNodes(Integer nodeId, List<Integer> incoming){
		for (Integer i: incoming) {
			builder.addEdge(nodesList.get(i), nodesList.get(nodeId));
		}
	}

	@Override
	public BNClassicImpl<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> newInstance() {
		return new BNClassicImpl<>(builder.build());
	}




	public static void main(String a[]){
		Random r = RandomnessFactory.getPureRandomGenerator();
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNClassicImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> test = new BNForTest().newInstance();
		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNClassicImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> bn
					= new RBNFactory<>(4,2,r,() -> new BiasedTable(2,0.5,r)).newInstance();


		BNClassic<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>,BNClassicImpl<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>>> cTest =
				test.modifyFromThis()
						.reconfigureIncomingEdge(0, 0,2)
						.build();
		//System.out.println(bn.getThis());
		System.out.println(test);
		System.out.println(cTest);
		//System.out.println(test.equals(test.newInstance(test.asGraph())));

		//System.out.println(bn.newInstance(bn.asGraph()).equals(bn));

		System.out.println(Stream.iterate(BinaryState.valueOf("0011"), new SynchronousDynamicsImpl(test)::nextState).limit(10).collect(Collectors.toList()));

		System.out.println(new SynchronousDynamicsImpl(test)
								.nextState(BinaryState.valueOf("0011")
								));
	}
}
