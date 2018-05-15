package network;

import interfaces.network.NodeDeterministic;
import interfaces.network.Table;

import java.util.Objects;

public class NodeDeterministicImpl<K,V> extends AbstractNode implements NodeDeterministic<K,V> {

    protected Table<K,V> truthTable;

    public NodeDeterministicImpl(String nodeName, Integer id, Table<K, V> truthTable) {
        super(nodeName, id);
        this.truthTable = truthTable;
    }

    @Override
	public Table<K, V> getFunction() {
		return this.truthTable;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeDeterministicImpl<?, ?> that = (NodeDeterministicImpl<?, ?>) o;
        return Objects.equals(truthTable, that.truthTable);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), truthTable);
    }
}
