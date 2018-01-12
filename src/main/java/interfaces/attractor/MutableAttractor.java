package interfaces.attractor;

import interfaces.state.State;

import java.util.List;
import java.util.Optional;


/**
 * Mutable Attractor
 *  with UNORDERED list of states.
 */
public interface MutableAttractor<T extends State> extends Attractor<T> {


    /**
     * returns the current basin size
     * @return
     */
    Optional<Integer> getBasinSize();


    void updateBasin(T stateOfItsBasin);
    void updateBasinDimension(Integer dimension);


    /**
     * returns the current transients
     * @return
     */
    Optional<List<Integer>> getTransientsLengths();

    void addTransient(Transient<T> tr);
    void addTransientLength(Integer length);



}
