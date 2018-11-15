package interfaces.network;

import java.util.function.Supplier;

public interface TableSupplier<K,V> extends Supplier<Table<K,V>> {

    BNKBias.BiasType getBiasType();

    Double getBias();

    Integer getVariablesNumber(); // number of variables
}
