package interfaces.simulator;

import interfaces.attractor.MutableAttractor;
import interfaces.state.State;

public interface AttractorFinderResult<T extends State>{

    MutableAttractor<T> attractorFound();
    Boolean isCutOff();
    Boolean wasAlreadyPresent();

}
