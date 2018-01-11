package interfaces.tes;

import interfaces.attractor.ImmutableList;
import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;

import java.math.BigDecimal;
import java.util.Optional;

public interface Atm<T extends State> {

	ImmutableList<ImmutableAttractor<T>> getAttractors();
	
	Optional<Integer[][]> getOccurrencesMatrix();
	
	Double[][] getMatrix();

	Double[][] getMatrixCopy();
}
