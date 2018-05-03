package interfaces.tes;

import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;

import java.math.BigDecimal;
import java.util.Optional;

public interface Atm<T extends State> {

	Attractors<T> getAttractors();
	
	Optional<Integer[][]> getOccurrencesMatrix();
	
	Double[][] getMatrix();

	Double[][] getMatrixCopy();
}
