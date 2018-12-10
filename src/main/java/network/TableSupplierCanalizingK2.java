package network;

import interfaces.network.BNKBias;
import interfaces.network.Table;
import interfaces.network.TableSupplier;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableSupplierCanalizingK2 implements TableSupplier<BitSet,Boolean> {

    private final Iterator<Table<BitSet, Boolean>> iterator;

    public TableSupplierCanalizingK2(int nodesNumber, RandomGenerator r) {
        List<Table<BitSet, Boolean>> canalyzing = UtilitiesBooleanNetwork.canalizingFunctionK2();
        this.iterator = Stream.generate(() -> r.nextInt(canalyzing.size())).limit(nodesNumber).map(canalyzing::get).collect(Collectors.toList()).iterator();
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
