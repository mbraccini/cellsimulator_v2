package tes;

import exceptions.AtmException;
import interfaces.attractor.ImmutableList;
import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;
import interfaces.tes.Atm;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Optional;



public class AtmImpl<T extends State> implements Atm<T>, Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	protected int[][] intMatrix;
	protected Integer[][] occurrenciesIntegerMatrix;
	protected ImmutableList<ImmutableAttractor<T>> attractorsList;
	protected BigDecimal[][] atm;
	protected Double[][] doubleAtm;
	protected int[] perturbationsNumberPerAttractor;

	/**
	 * It is assumed that the occurrences structure follows the ordering of the attractorsList passed.
	 * @param occurrenciesIntMatrix
	 * @param attractorsList
	 */
	public AtmImpl(int[][] occurrenciesIntMatrix, ImmutableList<ImmutableAttractor<T>> attractorsList) {
		this.intMatrix = occurrenciesIntMatrix;
		this.attractorsList = attractorsList;
		initPerturbationsPerAttractor();
	}

	private AtmImpl(ImmutableList<ImmutableAttractor<T>> attractorsList, BigDecimal[][] atm) {
		this.attractorsList = attractorsList;
		this.atm = atm;
	}

	public static <T extends State> Atm<T> newInstance(ImmutableList<ImmutableAttractor<T>> attractorsList, BigDecimal[][] atm) {
		return new AtmImpl<>(attractorsList, atm);
	}

	/**
	 * Initialize perturbations per attractor array.
	 */
	private void initPerturbationsPerAttractor() {
		this.perturbationsNumberPerAttractor = new int[this.intMatrix.length];
		for (int i = 0; i < this.intMatrix.length; i++) {
			this.perturbationsNumberPerAttractor[i] = Arrays.stream(this.intMatrix[i]).sum();
		}
	}

	/**
	 * TO DO: fare in modo che l'atm si copi con una deep-copy la lista di attrattori e ritorni una vista di questa lista
	 * non modificabile cosicchè si riesca anche a seguito di modifiche alla lista originale di attrattori a ricavare
	 * la giusta corrispondenza tra attrattori e matrice ATM!
	 */
	@Override
	public ImmutableList<ImmutableAttractor<T>> getAttractors() {
		return this.attractorsList;
	}

	@Override
	public Optional<Integer[][]> getOccurrencesMatrix() {
		if (this.occurrenciesIntegerMatrix !=  null) {
			return Optional.of(this.occurrenciesIntegerMatrix);
		}
		this.matrixIntToInteger();
		return Optional.ofNullable(this.occurrenciesIntegerMatrix);
	}

	// Convert int[][] to Integer[][]
	private void matrixIntToInteger() {
		if (intMatrix != null) {
			this.occurrenciesIntegerMatrix = new Integer[intMatrix.length][intMatrix.length];
			for (int i = 0; i < occurrenciesIntegerMatrix.length; i++) {
				for (int j = 0; j < occurrenciesIntegerMatrix.length; j++) {
					this.occurrenciesIntegerMatrix[i][j] = intMatrix[i][j];
				}
			}
		}
	}

	@Override
	public Double[][] getMatrix() {
		if (this.doubleAtm != null) {
			return this.doubleAtm;
		}
		this.normalize();
		this.roundAndCheckInvariant();
		this.checkAsserts();
		this.initializeDoubleAtm();
		return this.doubleAtm;
	}

	private void initializeDoubleAtm() {
		this.doubleAtm = new Double[this.atm.length][this.atm.length];
		for (int i = 0; i < this.doubleAtm.length; i++) {
			for (int j = 0; j < this.doubleAtm.length; j++) {
				this.doubleAtm[i][j] = this.atm[i][j].doubleValue();
			}
		}
	}


	protected void normalize() {
		this.atm = new BigDecimal[intMatrix.length][intMatrix.length];
		for (int i = 0; i < atm.length; i++) {
			for (int j = 0; j < atm.length; j++) {
				atm[i][j] = BigDecimal.valueOf(intMatrix[i][j])
						.divide(BigDecimal.valueOf(this.perturbationsNumberPerAttractor[i]), 2, RoundingMode.HALF_EVEN);
			}
		}
	}


	@Override
	public Double[][] getMatrixCopy() {
		if (this.doubleAtm == null) {
			/* ATM not yet initialized */
			getMatrix();
		}
		Double[][] newAtm = new Double[this.atm.length][this.atm.length];
		for (int i = 0; i < newAtm.length; i++) {
			for (int j = 0; j < newAtm.length; j++) {
				newAtm[i][j] = this.doubleAtm[i][j];
			}
		}
		return newAtm;
	}


	private void checkAsserts() {
		BigDecimal rowSum;
		for (int i = 0; i < atm.length; i++) {
			rowSum = Arrays.stream(atm[i]).reduce(BigDecimal.ZERO, BigDecimal::add);

			if (rowSum.compareTo(BigDecimal.ONE) != 0) { // cioè non è 1
				throw new AtmException("The sum of each row must be 1.0, rowSum = " + rowSum);
			}

			for (int j = 0; j < atm.length; j++) {
				if (atm[i][j].compareTo(BigDecimal.ZERO) < 0  || atm[i][j].compareTo(BigDecimal.ONE) > 0) {
					throw new AtmException("Each element of the ATM must be between 0.0 and 1.0!\n element value = " + atm[i][j]);
				}
			}
		}
	}


	private void roundAndCheckInvariant() {
		// This method round the double.
		// The sum of the entry of each row must be 1!

		for (int i = 0; i < this.atm.length; i++) {
			BigDecimal rowSum = BigDecimal.ZERO;
			for (int j = 0; j < this.atm.length; j++) {
				rowSum = rowSum.add(atm[i][j]);
			}


			//check invariant for each row!
			if (rowSum.compareTo(BigDecimal.ONE) != 0) {
				BigDecimal rest = BigDecimal.ONE.subtract(rowSum);

				//prendiamo il primo valore diverso da zero e aggiungiamo ciò che manca per arrivare ad 1.0
				for (int j = 0; j < this.atm.length; j++) {
					if (atm[i][j].compareTo(BigDecimal.ZERO) != 0
							&& atm[i][j].add(rest).compareTo(BigDecimal.ZERO) >= 0
							&& atm[i][j].add(rest).compareTo(BigDecimal.ONE) <= 0) {

						atm[i][j] = atm[i][j].add(rest);

						break;
					}
				}

			}
		}

	}

}
