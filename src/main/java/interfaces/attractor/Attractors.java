package interfaces.attractor;

import exceptions.AttractorIDNotExisting;
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

    default ImmutableAttractor<T> getAttractorById(Integer id){
        return getAttractors().stream().filter(x -> x.getId().intValue() == id.intValue()).findFirst().orElseThrow(AttractorIDNotExisting::new);
    }

}
