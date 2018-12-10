package interfaces.attractor;

import interfaces.simulator.Result;
import interfaces.state.State;

import java.util.List;

public interface Attractors<T extends State> extends Iterable<ImmutableAttractor<T>>, Result {

    List<ImmutableAttractor<T>> getAttractors();

    default Integer numberOfAttractors() {
        return getAttractors().size();
    }

    default Integer getNumberOfFixedPoints(){
        return Long.valueOf(getAttractors().stream().filter(x -> x.getLength() == 1).count()).intValue();
    }

}
