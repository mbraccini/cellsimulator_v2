package interfaces.tes;

import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;

import java.math.BigDecimal;
import java.util.Optional;

public interface Atm<T extends State> {

	Attractors<T> getAttractors();
	
	Optional<Integer[][]> getOccurrencesMatrix();

	/**
	 * Returns the header of the ATM (Attractors IDs)
	 * @return
	 */
	String[] header();

	Double[][] getMatrix();

	Double[][] getMatrixCopy();

	Integer lostPerturbations();

}
