package interfaces.attractor;

import attractor.AttractorImpl;
import attractor.ImmutableAttractorsListImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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


    public static <T extends Comparable<? super T>> List<LabelledOrderedAttractor<T>> fromInfoToAttractors(Collection<AttractorInfo<T>> infoCollection) {
        infoCollection.stream().forEach(x -> Collections.sort(x.getStates())); //ordino gli stati dell'attroctorInfo

        List<AttractorInfo<T>> ordered = infoCollection.stream()
                                                    .sorted((x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)))
                                                    .collect(Collectors.toList());
        List<LabelledOrderedAttractor<T>> temp = new ArrayList<>();
        Integer counter = 1;
        for (AttractorInfo<T> aInfo : ordered) {
            temp.add(new AttractorImpl<>(aInfo, counter));
            counter++;
        }
        return new ImmutableAttractorsListImpl<>(temp);
    }
}
