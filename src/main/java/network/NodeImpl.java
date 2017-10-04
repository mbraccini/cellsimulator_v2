package network;

import interfaces.network.Node;
import interfaces.network.Table;

public class NodeImpl<K, V> implements Node<K, V> {

	protected String nodeName;
	protected Integer id;
	protected Table<K,V> truthTable;
		
	public NodeImpl(String nodeName, Integer id, Table<K,V> truthTable){
		this.nodeName = nodeName;
		this.id = id;
		this.truthTable = truthTable;
	}

	@Override
	public String getName() {
		return this.nodeName;
	}

	@Override
	public Table<K, V> getFunction() {
		return this.truthTable;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "Node ["+nodeName + ", id=" + id +"]";
	}
	
	


}
