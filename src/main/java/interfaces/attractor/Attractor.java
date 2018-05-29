package interfaces.attractor;

import interfaces.state.State;

import java.util.List;
import java.util.Optional;

public interface Attractor<T extends State> extends BasicAttractor{

    List<T> getStates();

    /**
     * Basin with the current information available
     * @return
     */
    Optional<Basin<T>> getBasin();
    Optional<Integer> getBasinSize();


    /**
     * Returns the transients, if present
     * @return
     */
    Optional<List<Transient<T>>> getTransients();
    Optional<List<Integer>> getTransientsLengths();





}
