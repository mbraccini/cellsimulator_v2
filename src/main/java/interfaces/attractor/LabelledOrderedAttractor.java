package interfaces.attractor;

import java.util.List;
import java.util.Optional;

public interface LabelledOrderedAttractor<T extends Comparable<? super T>>{

    /**
     * Gets the attractor's ID.
     * @return
     */
    Integer getId();

    /**
     * The all states of the attractor lexicograpically ordered
     *
     * @return
     */
    List<T> getStates(); //ci teniamo tutti gli stati dell'attrattore

    /**
     * The first, lexicographically ordered, state of the attractor
     *
     * @return
     */
    T getFirstState();

    /**
     * Attractor's length.
     * @return
     */
    Integer getLength();

    Optional<Basin<T>> getBasin();

    Optional<List<Transient<T>>> getTransients();
}
