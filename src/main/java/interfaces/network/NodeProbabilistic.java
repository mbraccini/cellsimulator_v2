package interfaces.network;

import io.vavr.Tuple2;

import java.math.BigDecimal;
import java.util.Set;

public interface NodeProbabilistic<K,V> extends Node {

   Set<Tuple2<Table<K,V>, BigDecimal>> getPairFunctionProbability();

}
