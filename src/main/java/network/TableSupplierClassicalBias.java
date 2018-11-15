package network;

import interfaces.network.BNKBias;
import interfaces.network.Table;
import interfaces.network.TableSupplier;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.BitSet;

public class TableSupplierClassicalBias implements TableSupplier<BitSet,Boolean> {

    private final double bias;
    private final int variablesNumberPerNode;
    private final RandomGenerator r;

    public TableSupplierClassicalBias(int variablesNumberPerNode, double bias, RandomGenerator r) {
        this.bias = bias;
        this.variablesNumberPerNode = variablesNumberPerNode;
        this.r = r;
    }
    @Override
    public BNKBias.BiasType getBiasType() {
        return BNKBias.BiasType.CLASSICAL;
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
        return new BiasedTable(variablesNumberPerNode, bias, r);

    }
}
