package attractor;

import interfaces.attractor.AttractorInfo;
import interfaces.attractor.Basin;
import interfaces.attractor.Transient;
import interfaces.state.State;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Information about an attractor; its states are unordered.
 * @param <T>
 */
public class AttractorInfoImpl<T extends State> implements AttractorInfo<T> {

    private final List<T> states;
    //private final Set<T> basin;
    //private final List<Transient<T>> transients;

    public AttractorInfoImpl(List<T> states) {
        this.states = states;
    }

    @Override
    public List<T> getStates() {
        return this.states;
    }


    @Override
    public Optional<Integer> getBasinSize() {
        return null;
    }

    @Override
    public Optional<Basin<T>> getBasin() {
        return null;
    }

    @Override
    public void updateBasin(T stateOfItsBasin) {

    }

    @Override
    public void updateBasinDimension(Integer dimension) {

    }

    @Override
    public Optional<List<Transient<T>>> getTransients() {
        return null;
    }

    @Override
    public Optional<List<Integer>> getTransientsLengths() {
        return null;
    }

    @Override
    public void addTransient(Transient<T> tr) {

    }

    @Override
    public void addTransientLength(Integer length) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttractorInfoImpl<?> that = (AttractorInfoImpl<?>) o;
        return Objects.equals(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }
}

