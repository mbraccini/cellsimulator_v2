package attractor;

import interfaces.attractor.Basin;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable basin
 * @param <T>
 */
public class BasinImpl<T> implements Basin<T>{


    private Set<T> basin;
    private Integer basinDimension;

    public BasinImpl(Set<T> basin) {
        this.basin = basin;
    }


    public BasinImpl(Integer basinDimension) {
        this.basinDimension = basinDimension;
    }

    @Override
    public Optional<Set<T>> getStates() {
        return Optional.empty();
    }

    @Override
    public Integer getDimension() {
        if (Objects.nonNull(basin)) {
            return basin.size();
        }
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
