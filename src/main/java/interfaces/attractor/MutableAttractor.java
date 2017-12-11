package interfaces.attractor;

import interfaces.state.State;

import java.util.List;
import java.util.Optional;

public interface MutableAttractor<T extends State> {

    /**
     *  UNOrdered list of states.
     */
    List<T> getStates();

    /**
     * returns the current basin size
     * @return
     */
    Optional<Integer> getBasinSize();

    /**
     * Creates a new Basin with the current information available
     * @return
     */
    Optional<Basin<T>> getBasin();

    void updateBasin(T stateOfItsBasin);
    void updateBasinDimension(Integer dimension);


    Optional<List<Transient<T>>> getTransients();
    Optional<List<Integer>> getTransientsLengths();

    void addTransient(Transient<T> tr);
    void addTransientLength(Integer length);



}
