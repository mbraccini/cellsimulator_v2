package attractor;

import interfaces.attractor.MutableAttractor;
import interfaces.attractor.Basin;
import interfaces.attractor.Transient;
import interfaces.state.State;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Information about an attractor; its states are unordered.
 * @param <T>
 */
public class MutableAttractorImpl<T extends State> implements MutableAttractor<T> {

    private final List<T> states;
    private Set<T> basin;
    private int basinDimension;
    private List<Transient<T>> transients;
    private List<Integer> transientsLengths;


    public MutableAttractorImpl(List<T> states) {
        this.states = states;
    }

    @Override
    public List<T> getStates() {
        return this.states;
    }

    @Override
    public Integer getLength() {
        return this.states.size();
    }


    @Override
    public Optional<Integer> getBasinSize() {
        if (!Objects.isNull(basin)) {
            return Optional.of(basin.size());
        }
        return Optional.of(basinDimension);
    }

    @Override
    public Optional<Basin<T>> getBasin() {
        if (Objects.isNull(basin)) {
            return Optional.empty();
        }
        return Optional.of(new BasinImpl<T>(basin));
    }

    @Override
    public void updateBasin(T stateOfItsBasin) {
        if (Objects.isNull(basin)) {
            basin = new HashSet<>();
        }
        basin.add(stateOfItsBasin);
    }

    @Override
    public void updateBasinDimension(Integer dimension) {
        basinDimension += dimension;
    }

    @Override
    public Optional<List<Transient<T>>> getTransients() {
        if (Objects.isNull(transients)) {
            return Optional.empty();
        }
        return Optional.of(transients);
    }

    @Override
    public Optional<List<Integer>> getTransientsLengths() {
        if (Objects.isNull(transientsLengths)) {
            if (Objects.isNull(transients)) {
                return Optional.empty();
            } else {
                return Optional.of(transients.stream().map(x->x.getLength()).collect(Collectors.toList()));
            }
        }
        return Optional.of(transientsLengths);
    }

    @Override
    public void addTransient(Transient<T> tr) {
        if (Objects.isNull(transients)) {
            transients = new ArrayList<>();
        }
        transients.add(tr);
    }

    @Override
    public void addTransientLength(Integer length) {
        if (Objects.isNull(transientsLengths)) {
            transientsLengths = new ArrayList<>();
        }
        transientsLengths.add(length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MutableAttractorImpl<?> that = (MutableAttractorImpl<?>) o;
        return Objects.equals(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }

    @Override
    public String toString() {
        return "MutableAttractor{" +
                "states=" + states +
                '}';
    }
}

