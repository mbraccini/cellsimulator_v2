package interfaces.attractor;

import interfaces.state.Immutable;
import interfaces.state.State;

import java.util.*;

public interface LabelledOrderedAttractor<T extends State> extends Immutable{

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
