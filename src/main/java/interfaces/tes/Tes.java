package interfaces.tes;

import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;

import java.util.List;
import java.util.Optional;

public interface Tes<T extends State> {

	List<ImmutableAttractor<T>> getTesAttractors();
	
	Optional<String> getName(); /* Name of the cell type */
	
	void setName(String name); 	/* The name of a TES might not be known at the time of its creation */

}
