package interfaces.network;

import java.util.Set;

public interface BNProbabilistic<K,V, N extends NodeProbabilistic<K,V>> extends BooleanNetwork<N> {

    Set<ContextProbabilistic<K,V>> contexts();

}
