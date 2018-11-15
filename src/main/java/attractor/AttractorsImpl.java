package attractor;

import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.MutableAttractor;
import interfaces.state.State;

import java.util.*;

import com.google.common.collect.ImmutableList;
import utility.Files;

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
        StringBuilder sb = new StringBuilder();
        List<T> statesInAttractor;
        for (ImmutableAttractor<T> attractor : attractors) {
            statesInAttractor = attractor.getStates();
            sb.append("[id: " + attractor.getId() + " Attractor] Length= " + attractor.getLength()
                    + ", BasinSize= " + (attractor.getBasin().isPresent() ? attractor.getBasin().get().getDimension() : "")
                    + ", MeanTransientsLength= " + (attractor.getTransientsLengths().isPresent() ? String.format("%.2f", attractor.getTransientsLengths().get().stream().mapToInt(x -> x).average().orElseGet(() -> -1)) : "")
                    + ":"
                    );
            sb.append(Files.NEW_LINE);

            for (int i = 0; i < attractor.getLength(); i++) {
                sb.append("s" + i + ":  " + statesInAttractor.get(i).toString());
                sb.append(Files.NEW_LINE);
            }
            sb.append(Files.NEW_LINE);
        }

        return sb.toString();
    }

    @Override
    public Iterator<ImmutableAttractor<T>> iterator() {
        return attractors.iterator();
    }
}
