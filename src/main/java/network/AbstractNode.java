package network;

import interfaces.network.Node;
import interfaces.network.Table;

import java.util.Objects;

public abstract class AbstractNode implements Node {

	protected String nodeName;
	protected Integer id;

	public AbstractNode(String nodeName, Integer id){
		this.nodeName = nodeName;
		this.id = id;
	}

	@Override
	public String getName() {
		return this.nodeName;
	}



	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return "Node ["+nodeName + ", id=" + id +"]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractNode that = (AbstractNode) o;
		return Objects.equals(nodeName, that.nodeName) &&
				Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {

		return Objects.hash(nodeName, id);
	}
}
