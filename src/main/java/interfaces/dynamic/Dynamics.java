package interfaces.dynamic;

import interfaces.state.State;

import java.util.function.Function;

public interface Dynamics<T extends State> extends Function<T,T> {

	T nextState(T state);

	@Override
	default T apply(T t){
		return nextState(t);
	}

}
