package interfaces.dynamic;

public interface Dynamics<TState> {
	TState nextState(TState state);
}
