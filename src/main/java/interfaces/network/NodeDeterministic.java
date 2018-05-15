package interfaces.network;

public interface NodeDeterministic<K,V> extends Node {

    Table<K,V> getFunction();

}
