package network;

import java.util.*;
import java.util.stream.Collectors;

import interfaces.network.BooleanNetwork;
import interfaces.network.Node;

public abstract class AbstractBooleanNetwork<K,V> implements BooleanNetwork<K,V> {

	protected Properties properties = new Properties();

	protected int nodesNumber;
	List<Node<K,V>> nodesList = new ArrayList<>();
	protected Map<Node<K,V>, List<Node<K,V>>> nodesMap = new HashMap<>();
	//Mappa con chiave i nodi e valore la lista degli INCOMING NODES DI QUEL NODO(chiave)

	public AbstractBooleanNetwork(int nodesNumber){
		this.nodesNumber = nodesNumber;
	}

	/* Immutable */

	@Override
	public Integer getNodesNumber() {
		return this.nodesNumber;
	}

	@Override
	public List<Node<K,V>> getNodes() {
		//List<Node<K,V>> l = this.nodesMap.keySet().stream().collect(Collectors.toList());
		//this.nodesList.sort((Node<K,V> a, Node<K,V> b) -> a.getId().compareTo(b.getId()));
		return this.nodesList;
	}
	
	@Override
	public Optional<Node<K, V>> getNodeByName(String name) {
		//List<Node<K,V>> l = this.nodesMap.keySet().stream().collect(Collectors.toList());
		return this.nodesList.stream().filter(x->x.getName().equals(name)).findFirst();
	}

	@Override
	public Optional<Node<K, V>> getNodeById(Integer id) {
		//List<Node<K,V>> l = this.nodesMap.keySet().stream().collect(Collectors.toList());
		return this.nodesList.stream().filter(x->x.getId().equals(id)).findFirst();
	}

	@Override
	public Boolean isAffectedBy(Node<?, ?> a, Node<?, ?> b) {
		// controlla se b è nella incoming list di a
		return nodesMap.get(a).stream().anyMatch(x->x == b);
	}

	@Override
	public Boolean reconfigureIncomingEdge(Node<K,V> targetNode, Node<K,V> oldInputNode, Node<K,V> newInputNode) {
		if(this.nodesMap.get(targetNode).stream().anyMatch(e->e.equals(newInputNode)) 
				||
				newInputNode.equals(targetNode)){
			return false;
		}
		int index = this.nodesMap.get(targetNode).indexOf(oldInputNode);
		if (index != -1){			
			this.nodesMap.get(targetNode).add(index, newInputNode);
			this.nodesMap.get(targetNode).remove(index + 1);

			return true;
		}
		return false;
	}

	@Override
	public List<Node<K, V>> getIncomingNodes(Node<K, V> node) {
		return this.nodesMap.get(node);
	}

	@Override
	public List<Node<K, V>> getOutcomingNodes(Node<K, V> node) {
		return this.nodesMap.entrySet().stream().filter(x->x.getValue().stream().anyMatch(n->n.equals(node))).map(entry->entry.getKey()).collect(Collectors.toList());
	}

	@Override
	public Integer getInDegree(Node<?, ?> node) {
		 return this.nodesMap.get(node).size();
	}

	@Override
	public Integer getOutDegree(Node<?, ?> node) {
		return Math.toIntExact(this.nodesMap.entrySet().stream().filter(x->x.getValue().stream().anyMatch(n->n.equals(node))).map(entry->entry.getKey()).count());
	}

	@Override
	public String toString() {
		return "----["+this.getClass().getSimpleName() +  "]----\n" +
					this.nodesMap.entrySet().stream()
							.sorted(Comparator.comparingInt( x -> x.getKey().getId()))
							.map( r-> {
							return r.getKey().toString() 
							+ " <--- "+
							r.getValue().toString();}).collect(Collectors.joining(" \n"))
					+"\n--------------------\n"
					+ this.nodesList.stream().map(x-> {return "NodeId=" + x.getId() +", "+ x.getFunction();}).collect(Collectors.joining(" \n"));

	
	}


	@Override
	public Properties getNetworkProperties() {
		return properties;
	}


	@Override
	public boolean equals(Object o) {
		//self check
		if (this == o)
			return true;

		// null check
		if (o == null)
			return false;

		// type check and cast
		if (!(o instanceof AbstractBooleanNetwork))
			return false;

		AbstractBooleanNetwork<?, ?> that = (AbstractBooleanNetwork<?, ?>) o;
		return nodesNumber == that.nodesNumber &&
				Objects.equals(nodesList, that.nodesList) &&
				Objects.equals(nodesMap, that.nodesMap);
	}

	@Override
	public int hashCode() {

		return Objects.hash(nodesNumber, nodesList, nodesMap);
	}
}
