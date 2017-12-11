package attractor;

import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.MutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.state.State;

import java.util.*;
import java.util.stream.IntStream;

public class AttractorsUtility {
    private AttractorsUtility(){}

    public static <T extends State> ImmutableList<ImmutableAttractor<T>> fromInfoToAttractors(final Collection<MutableAttractor<T>> infoCollection) {
        List<MutableAttractor<T>> copyOfInfoCollection = new ArrayList<>(infoCollection);

        copyOfInfoCollection.stream().forEach(x -> Collections.sort(x.getStates())); //ordino gli stati dell'attroctorInfo

        Collections.sort(copyOfInfoCollection, (x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)));

        List<ImmutableAttractor<T>> temp = new ArrayList<>();
        Integer counter = 1;
        for (MutableAttractor<T> aInfo : copyOfInfoCollection) {
            temp.add(new ImmutableAttractorImpl<>(aInfo, counter));
            counter++;
        }

        //List<ImmutableAttractor<T>> result = new ImmutableListImpl<>(temp);
        //temp = null;
        return new ImmutableListImpl<>(temp);
    }


    /**
     * Given the state, it returns the Id of the attractor to which it belongs, -1 if no attractor is found.
     * @param state
     * @param attractorsList
     * @return
     */
    public static <T extends State> int retrieveAttractorId(T state, List<ImmutableAttractor<T>> attractorsList) {
        return attractorsList.stream().filter(x -> x.getStates().contains(state)).map(x -> x.getId()).findAny().orElseGet(() -> -1);
    }


    public static <T extends State> int retrieveAttractorListIndex(T state, List<ImmutableAttractor<T>> attractorsList) {
        return IntStream.range(0, attractorsList.size()).filter(i -> attractorsList.get(i).getStates().contains(state)).findAny().orElseGet(() -> -1);
    }
}
