package dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import interfaces.network.Table;
import interfaces.state.BinaryState;

public abstract class AbstractDynamics<K, V> {

	protected BooleanNetwork<K, V> bn;
	protected int nodesNumber;
	
	protected List<Node<K, V>> nodesList;
	protected List<Table<K, V>> functionsList;

	protected int[][] incomingNodesMatrix;


	public AbstractDynamics(BooleanNetwork<K, V> bn) {
		this.bn = bn;
		configure();
	}
	
	private void configure(){
		this.nodesNumber = this.bn.getNodesNumber(); //numero di nodi corrisponde ai bit dello stato
		this.nodesList = this.bn.getNodes();

		incomingNodesMatrix = new int[this.nodesNumber][];
		
		functionsList = new ArrayList<>();
		for(Node<K, V> node : nodesList){
			
			incomingNodesMatrix[node.getId()] = new int[this.bn.getInDegree(node)];
			List<Integer> listIn = this.bn.getIncomingNodes(node).stream().map(x->x.getId()).collect(Collectors.toList());

			//listIn.toArray(incomingNodesMatrix[node.getId()]);
			incomingNodesMatrix[node.getId()] = listIn.stream().mapToInt(i -> i).toArray();
			
			functionsList.add(node.getId(), node.getFunction());
		}
		//Stampo
		/*this.mapIncomingNodes.entrySet().stream().forEach(x->System.out.println(x.getKey() +", "+x.getValue()));
		for (int i = 0; i < incomingNodesMatrix.length; i++) {
		    for (int j = 0; j < incomingNodesMatrix[i].length; j++) {
		        System.out.print(incomingNodesMatrix[i][j] + " ");
		    }
		    System.out.println();
		}*/
	}


}
