package interfaces.attractor;

import java.util.List;
import java.util.Optional;

public interface AttractorInfo<T extends Comparable<? super T>> {

    /**
     *  Ordered list of states.
     */
    List<T> getStates();

    /**
     * First state of the attractor.
     * @return
     */
    T getFirstState();


    Optional<Integer> getID();
    void setID(Integer id);

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
