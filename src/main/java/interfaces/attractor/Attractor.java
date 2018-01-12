package interfaces.attractor;

import interfaces.state.State;

import java.util.List;
import java.util.Optional;

public interface Attractor<T extends State> {

    List<T> getStates();

    /**
     * Creates a new Basin with the current information available
     * @return
     */
    Optional<Basin<T>> getBasin();

    /**
     * Returns the transients, if present
     * @return
     */
    Optional<List<Transient<T>>> getTransients();


}
