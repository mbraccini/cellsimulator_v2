package interfaces.cell;

import interfaces.state.State;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

public class LiveCellImpl<K, V, T extends State> implements LiveCell<K, V, T> {

    private final Function<T, T> fun;
    private T initialState;


    public LiveCellImpl(Function<T, T> fun, T initialState) {
        this.fun = fun;
        this.initialState = initialState;
    }

    @Override
    public Iterator<T> iterator() {
        return new LiveCellIterator(initialState);
    }


    private class LiveCellIterator implements Iterator<T> {

        private T initialState,
                currentState,
                nextState;

        public LiveCellIterator(T initialState) {
            this.initialState = initialState;
        }

        @Override
        public T next() {
            if (Objects.isNull(nextState)) {
                nextState = fun.apply(initialState);
                return initialState;
            }
            currentState = nextState;
            nextState = fun.apply(currentState);
            return currentState;

        }

        @Override
        public boolean hasNext() {
            return true;
        }
    }
}
