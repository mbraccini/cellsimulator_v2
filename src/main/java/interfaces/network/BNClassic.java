package interfaces.network;

public interface BNClassic<K, V, N extends NodeDeterministic<K,V>> extends BooleanNetwork<N>{

    Context<K,V> context();

}
