package interfaces.attractor;

import attractor.AttractorImpl;
import attractor.ImmutableAttractorsListImpl;
import interfaces.state.State;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface LabelledOrderedAttractor<T extends State>{

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
