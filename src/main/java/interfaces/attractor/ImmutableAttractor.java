package interfaces.attractor;

import interfaces.state.Immutable;
import interfaces.state.State;

import java.util.*;


/**
 * Immutable attractor with all states of the attractor lexicograpically ordered
 * @param <T>
 */
public interface ImmutableAttractor<T extends State> extends Attractor<T>, Immutable{

    /**
     * Gets the attractor's ID.
     * @return
     */
    Integer getId();


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



}
