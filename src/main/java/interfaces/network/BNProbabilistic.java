package interfaces.network;

import java.util.Set;

public interface BNProbabilistic<K,V, N extends NodeProbabilistic<K,V>, B extends BNProbabilistic<K,V,N,B>> extends BooleanNetwork<N,B> {

    Set<ContextProbabilistic<K,V>> contexts();

}
