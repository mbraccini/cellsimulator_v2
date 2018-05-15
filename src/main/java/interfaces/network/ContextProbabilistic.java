package interfaces.network;

import io.vavr.Tuple2;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ContextProbabilistic<K,V> {

    /**
     * Pair of ::NodeId, Tuple2(FunctionTable,Probability)::
     * The sum of all the probabilities of a context must be 1.0
     * @return
     */
    Map<Integer, Tuple2<Table<K,V>, BigDecimal>> functionWithItsDistributionProbability();

}
