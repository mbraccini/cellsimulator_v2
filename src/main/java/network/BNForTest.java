package network;

import interfaces.network.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;


public class BNForTest extends AbstractBooleanNetwork<BitSet, Boolean> {

	public BNForTest() {
		super(4);

		configure();
		properties.setProperty("typology", "bnForTesting");
	}

	private final void configure(){
		initNodes();
		initTopology();
	}
	
	private void initNodes() {

		nodesList.add(new NodeImpl<>("gene_"+0, 0, new OrTable(2)));
		nodesList.add(new NodeImpl<>("gene_"+1, 1, new AndTable(2)));
		nodesList.add(new NodeImpl<>("gene_"+2, 2, new OrTable(2)));
		nodesList.add(new NodeImpl<>("gene_"+3, 3, new AndTable(2)));

	}
	
	private void initTopology() {
		this.addNodes(0, new ArrayList<>(Arrays.asList(1, 2)));
		this.addNodes(1, new ArrayList<>(Arrays.asList(0, 3)));
		this.addNodes(2, new ArrayList<>(Arrays.asList(1, 3)));
		this.addNodes(3, new ArrayList<>(Arrays.asList(0, 2)));

	}

	
	private void addNodes(int indexNode, List<Integer> indexListToAdd){
		List<Node<BitSet, Boolean>> list = new ArrayList<>();
		for (int i : indexListToAdd) {
			list.add(nodesList.get(i));
		}
		this.nodesMap.put(this.nodesList.get(indexNode), list);

	}
}
