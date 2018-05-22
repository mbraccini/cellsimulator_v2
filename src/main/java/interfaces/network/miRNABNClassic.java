package interfaces.network;

public interface miRNABNClassic<K,
                                V,
                                N extends NodeDeterministic<K,V>,
                                B extends BNClassic<K,V,N>,
                                M extends NodeDeterministic<K,V>>
                                extends miRNABooleanNetwork<N,B,M>  ,      BNClassic<K,V,N>
                                {
}
