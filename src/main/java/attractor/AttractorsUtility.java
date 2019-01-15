package attractor;

import interfaces.attractor.Attractor;
import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.MutableAttractor;
import interfaces.state.BinaryState;
import interfaces.state.State;
import states.ImmutableRealState;
import utility.GenericUtility;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttractorsUtility {
    private AttractorsUtility(){}

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


    /**
     * Retrieves the attractor to which the state belongs, if it's presents in the attractors list.
     * @param state
     * @param attractorsList
     * @param <T>
     * @return
     */
    public static <T extends State> Optional<Attractor<T>> retrieveAttractor(T state, Collection<? extends Attractor<T>> attractorsList) {
        if (Objects.isNull(attractorsList)) {
            return Optional.empty();
        }
        for (Iterator<? extends Attractor<T>> i = attractorsList.iterator(); i.hasNext();) {
            Attractor<T> att = i.next();
            if (att.getStates().contains(state)) {
                return Optional.of(att);
            }
        }
        return Optional.empty();
    }


    /**
     * Compute nodes' indices of fixed nodes in attractors
     * @param attrs
     * @return
     */
    public static Set<Integer> fixedAttractors(Attractors<BinaryState> attrs){
        List<ImmutableAttractor<BinaryState>> noFixedPoints = attrs.getAttractors().stream().filter(x -> x .getLength() > 1).collect(Collectors.toList());
        Set<Integer> intersect = null;
        for (int attIdx = 0; attIdx < noFixedPoints.size(); attIdx++) {
            if (intersect == null) {
                intersect = new HashSet<>(fixed(noFixedPoints.get(attIdx)));
            } else {
                intersect.retainAll(fixed(noFixedPoints.get(attIdx)));
            }
        }
        return intersect;
    }

    public static Set<Integer> fixed(ImmutableAttractor<BinaryState> a) {
        Set<Integer> indices = new HashSet<>();
        Integer numNodes = a.getFirstState().getLength();
        BinaryState prev, succ;
        boolean first = true;
        for (int state = 0; state < a.getLength() - 1; state++) {
            prev = a.getStates().get(state);
            succ = a.getStates().get(state + 1);

            for (int i = 0; i < numNodes; i++) {
                if (prev.getNodeValue(i) == succ.getNodeValue(i)){
                    if (first) {
                        indices.add(i);
                    }
                } else if (indices.contains(i)) {
                    indices.remove(i);
                }
            }
            first = false;
        }
        return indices;
    }

    /**
     * Compute nodes' indices of blinking nodes in attractors
     * @param attrs
     * @return
     */
    public static Set<Integer> blinkingAttractors(Attractors<BinaryState> attrs) {
        List<ImmutableAttractor<BinaryState>> noFixedPoints = attrs.getAttractors().stream().filter(x -> x .getLength() > 1).collect(Collectors.toList());
        if (noFixedPoints.size() == 0) {
            return null;
        }

        Set<Integer> total = IntStream.range(0, attrs.getAttractors().get(0).getFirstState().getLength()).boxed().collect(Collectors.toSet());
        for (int attIdx = 0; attIdx < noFixedPoints.size(); attIdx++) {
            total.removeAll(fixed(noFixedPoints.get(attIdx)));
        }
        return total;
    }


    /**
     * Compute the representative mean activation of the attractor
     * @return
     */
    public static <T extends BinaryState>  ImmutableRealState attractorMeanRepresentativeState(Attractor<T> a){
        List<T> states = a.getStates();
        int statesNumber = a.getStates().size();
        int stateLength = a.getStates().get(0).getLength();
        double[] newState = new double[stateLength];
        int sum;
        for (int i = 0; i < stateLength; i++) {
            sum = 0;
            for (T state : states) {
               sum += (state.getNodeValue(i) == Boolean.FALSE ? 0 : 1);
            }
            newState[stateLength - i -1] = ((double)sum/statesNumber);
        }
        return new ImmutableRealState(newState);
    }
}
