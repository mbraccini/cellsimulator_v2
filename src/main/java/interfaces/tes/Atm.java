package interfaces.tes;

import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.state.State;

import java.util.Optional;

public interface Atm<T extends State> {

	ImmutableList<LabelledOrderedAttractor<T>> getAttractors();
	
	Optional<Integer[][]> getOccurrencesMatrix();
	
	Double[][] getMatrix();

	Double[][] getMatrixCopy();
}
