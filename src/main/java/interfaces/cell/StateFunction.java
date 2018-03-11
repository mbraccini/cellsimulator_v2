package interfaces.cell;

import interfaces.state.BinaryState;
import interfaces.state.State;

import java.util.Random;
import java.util.function.Function;

public interface StateFunction<T extends State> extends Function<T,T>{

    @SuppressWarnings("unchecked")
    static <V extends BinaryState> StateFunction<V> withNoise(int numOfperturbationsAtOnce, Random r) {
        return v -> (V) v.flipNodesValues(r.ints(numOfperturbationsAtOnce, 0, v.getLength()).boxed().toArray(Integer[]::new));
    }


}
