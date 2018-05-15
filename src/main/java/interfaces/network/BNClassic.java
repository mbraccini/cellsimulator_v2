package interfaces.network;

public interface BNClassic<K, V, N extends NodeDeterministic<K,V>, B extends BNClassic<K,V,N,B>> extends BooleanNetwork<N,B>{

    Context<K,V> context();

}
