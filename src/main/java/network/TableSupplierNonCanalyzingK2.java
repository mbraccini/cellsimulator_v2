package network;

import interfaces.network.BNKBias;
import interfaces.network.Table;
import interfaces.network.TableSupplier;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.BitSet;
import java.util.Iterator;

public class TableSupplierNonCanalyzingK2 implements TableSupplier<BitSet,Boolean> {

    private final Iterator<Table<BitSet, Boolean>> iterator;

    public TableSupplierNonCanalyzingK2() {
        this.iterator = UtilitiesBooleanNetwork.nonCanalyzingFunctionK2().iterator();
    }

    @Override
    public BNKBias.BiasType getBiasType() {
        return BNKBias.BiasType.CLASSICAL;
    }

    @Override
    public Double getBias() {
        return 0.5;
    }

    @Override
    public Integer getVariablesNumber() {
        return 2;
    }

    @Override
    public Table<BitSet, Boolean> get() {
        return iterator.next();
    }
}
