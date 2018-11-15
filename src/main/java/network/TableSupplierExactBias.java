package network;

import interfaces.network.BNKBias;
import interfaces.network.Table;
import interfaces.network.TableSupplier;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

public class TableSupplierExactBias implements TableSupplier<BitSet,Boolean> {

    private final Iterator<Table<BitSet, Boolean>> iterator;
    private final double bias;
    private final int variablesNumberPerNode;

    public TableSupplierExactBias(int nodesNumber, int variablesNumberPerNode, double bias, RandomGenerator r) {
        this.bias = bias;
        this.variablesNumberPerNode = variablesNumberPerNode;
        this.iterator = UtilitiesBooleanNetwork.exactBiasNodesGenerator(nodesNumber, variablesNumberPerNode, bias, r).iterator();
    }

    @Override
    public BNKBias.BiasType getBiasType() {
        return BNKBias.BiasType.EXACT;
    }

    @Override
    public Double getBias() {
        return bias;
    }

    @Override
    public Integer getVariablesNumber() {
        return variablesNumberPerNode;
    }

    @Override
    public Table<BitSet, Boolean> get() {
        return iterator.next();
    }
}
