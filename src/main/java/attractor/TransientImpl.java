package attractor;

import interfaces.attractor.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class TransientImpl<T> implements Transient<T> {

    private final Integer l;
    private List<T> t;
    public TransientImpl(List<T> t) {
        this(t.size());
        this.t = t;
    }

    public TransientImpl(Integer l) {
        this.l = l;
    }

    @Override
    public Optional<List<T>> getStates() {
        return Optional.of(Collections.unmodifiableList(t));
    }

    @Override
    public Integer getLength() {
        return l;
    }

    @Override
    public String toString() {
        return "Traj{"  + t +
                ",lngth=" + l +
                '}';
    }
}
