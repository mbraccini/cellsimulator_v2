package attractor;

import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.MutableAttractor;
import interfaces.state.State;

import java.util.*;

import com.google.common.collect.ImmutableList;

public class AttractorsImpl<T extends State> implements Attractors<T> {

    List<ImmutableAttractor<T>> attractors;
    ImmutableList.Builder<ImmutableAttractor<T>> builder = ImmutableList.builder();


    public AttractorsImpl(final Collection<MutableAttractor<T>> infoCollection){
        fromInfoToAttractors(infoCollection);
        attractors = builder.build();
    }

    private void fromInfoToAttractors(final Collection<MutableAttractor<T>> infoCollection) {
        List<MutableAttractor<T>> copyOfInfoCollection = new ArrayList<>(infoCollection);

        copyOfInfoCollection.stream().forEach(x -> Collections.sort(x.getStates())); //ordino gli stati dell'attroctorInfo

        Collections.sort(copyOfInfoCollection, (x, y) -> x.getStates().get(0).compareTo(y.getStates().get(0)));

        Integer counter = 1;
        for (MutableAttractor<T> aInfo : copyOfInfoCollection) {
            builder.add(new ImmutableAttractorImpl<>(aInfo, counter));
            counter++;
        }
    }

    @Override
    public List<ImmutableAttractor<T>> getAttractors() {
        return attractors;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttractorsImpl<?> that = (AttractorsImpl<?>) o;
        return Objects.equals(attractors, that.attractors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attractors);
    }

    @Override
    public String toString() {
        return "AttractorsImpl{" + attractors +
                '}';
    }
}
