package attractor;

import exceptions.AttractorIDNotSpecified;
import interfaces.attractor.AttractorInfo;
import interfaces.attractor.Basin;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.attractor.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AttractorImpl<T extends Comparable<? super T>> implements LabelledOrderedAttractor<T> {

    private final Integer id;

    private final AttractorInfo<T> info;
    private final List<T> states;

    public AttractorImpl(AttractorInfo<T> info, Integer id) {
        this.info = info;
        if (Objects.isNull(id)) {
            throw new AttractorIDNotSpecified();
        }
        this.id = id;
        this.states = new ArrayList<>(this.info.getStates()); //defensive copy;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public List<T> getStates() {
        return Collections.unmodifiableList(states);
    }

    @Override
    public T getFirstState() {
        return states.get(0);
    }

    @Override
    public Integer getLength() {
        return states.size();
    }

    @Override
    public Optional<Basin<T>> getBasin() {
        return info.getBasin();
    }

    @Override
    public Optional<List<Transient<T>>> getTransients() {
        return info.getTransients();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttractorImpl<?> that = (AttractorImpl<?>) o;
        return Objects.equals(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hash(states);
    }

    @Override
    public String toString() {
        return "Att{ id="
                + id + ", "
                + states +
                '}';
    }
}
