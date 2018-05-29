package attractor;

import interfaces.attractor.Basin;

import java.util.*;

/**
 * Immutable basin
 * @param <T>
 */
public class BasinImpl<T> implements Basin<T>{


    private Set<T> basin;
    private final Integer basinDimension;

    public BasinImpl(Set<T> basin) {
        this(basin.size());
        this.basin = basin;
    }


    public BasinImpl(Integer basinDimension) {
        this.basinDimension = basinDimension;
    }

    @Override
    public Optional<Set<T>> getStates() {
        return Optional.of(Collections.unmodifiableSet(basin));
    }

    @Override
    public Integer getDimension() {
        return basinDimension;
    }

    @Override
    public String toString() {
        return "BasinImpl{" +
                "basin=" + basin +
                ", basinDimension=" + basinDimension +
                '}';
    }
}
