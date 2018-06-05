package interfaces.cell;

import interfaces.state.BinaryState;
import interfaces.state.State;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.function.Function;

public interface StateFunction<T extends State> extends Function<T,T>{

    @SuppressWarnings("unchecked")
    static <V extends BinaryState> StateFunction<V> withNoise(int numOfperturbationsAtOnce, RandomGenerator r) {

        return v -> {
                        Integer[] a = new Integer[numOfperturbationsAtOnce];
                        for (int i = 0; i < numOfperturbationsAtOnce; i++) {
                            a[i] = r.nextInt(v.getLength());
                        }
                        return (V) v.flipNodesValues(a);
                     };
    }


}
