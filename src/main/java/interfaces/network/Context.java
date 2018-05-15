package interfaces.network;


import java.util.Map;

public interface Context<K,V> {

    /**
     * Pair of ::NodeId, FunctionTable::
     * @return
     */
    Map<Integer, Table<K,V>> context();

}
