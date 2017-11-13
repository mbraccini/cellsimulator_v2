package tes;

import interfaces.attractor.ImmutableList;
import interfaces.attractor.LabelledOrderedAttractor;
import interfaces.state.State;
import interfaces.tes.Atm;
import org.junit.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;



public class AtmImpl<T extends State> implements Atm<T>, Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	protected int[][] intMatrix;
	protected Integer[][] occurrenciesIntegerMatrix;
	protected ImmutableList<LabelledOrderedAttractor<T>> attractorsList;
	protected Double[][] atm;
	protected int[] perturbationsNumberPerAttractor;

	/**
	 * It is assumed that the occurrences structure follows the ordering of the attractorsList passed.
	 * @param occurrenciesIntMatrix
	 * @param attractorsList
	 */
	public AtmImpl(int[][] occurrenciesIntMatrix, ImmutableList<LabelledOrderedAttractor<T>> attractorsList) {
		this.intMatrix = occurrenciesIntMatrix;
		this.attractorsList = attractorsList;
		initPerturbationsPerAttractor();
	}

	private AtmImpl(ImmutableList<LabelledOrderedAttractor<T>> attractorsList, Double[][] atm) {
		this.attractorsList = attractorsList;
		this.atm = atm;
	}

	public static <T extends State> Atm<T> newAtm(ImmutableList<LabelledOrderedAttractor<T>> attractorsList, Double[][] atm) {
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
	public ImmutableList<LabelledOrderedAttractor<T>> getAttractors() {
		return this.attractorsList;
	}

	@Override
	public Optional<Integer[][]> getOccurrenciesMatrix() {
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
					this.occurrenciesIntegerMatrix[i][j] = Integer.valueOf(intMatrix[i][j]);
				}
			}
		}
	}

	@Override
	public Double[][] getMatrix() {
		if (this.atm != null) {
			return this.atm;
		}
		this.normalize();
		this.roundAndCheckInvariant();
		this.checkAsserts();
		return this.atm;
	}

	protected void normalize() {
		this.atm = new Double[intMatrix.length][intMatrix.length];
		for (int i = 0; i < atm.length; i++) {
			for (int j = 0; j < atm.length; j++) {
				//atm[i][j] = (((double) intMatrix[i][j]) / (this.perturbationsNumberPerAttractor[i]));
				atm[i][j] = BigDecimal.valueOf(intMatrix[i][j])
							.divide(BigDecimal.valueOf(this.perturbationsNumberPerAttractor[i]), 2, RoundingMode.HALF_EVEN)
							.doubleValue();
			}
		}
	}


	@Override
	public Double[][] getMatrixCopy() {
		if (this.atm == null) {
			/* ATM not yet initialized */
			getMatrix();
		}
		Double[][] newAtm = new Double[this.atm.length][this.atm.length];
		for (int i = 0; i < newAtm.length; i++) {
			for (int j = 0; j < newAtm.length; j++) {
				newAtm[i][j] = this.atm[i][j];
			}
		}
		return newAtm;
	}

	private void roundAndCheckInvariant() {
		// This method round the double.
		// The sum of the entry of each row must be 1!

		BigDecimal converted;
		for (int i = 0; i < this.atm.length; i++) {
			double rowSum = 0.0;
			for (int j = 0; j < this.atm.length; j++) {
				if (atm[i][j] != 0.0) {

					converted = BigDecimal.valueOf(atm[i][j]);
					converted = converted.setScale(2, RoundingMode.HALF_EVEN);

					atm[i][j] = converted.doubleValue();
				}
				rowSum += atm[i][j];
			}


			//check invariant for each row!
			if (rowSum != 1.0) {
				double rest = 1.0 - rowSum;

				converted = BigDecimal.valueOf(rest);
				converted = converted.setScale(2, RoundingMode.HALF_EVEN);
				rest = converted.doubleValue();

				//prendiamo il primo valore diverso da zero e aggiungiamo ciò che manca per arrivare ad 1.0
				for (int j = 0; j < this.atm.length; j++) {
					if (atm[i][j] != 0.0 && (atm[i][j] + rest) >= 0.0 && (atm[i][j] + rest) <= 1.0) {

						atm[i][j] += rest;
						converted = BigDecimal.valueOf(atm[i][j]);
						converted = converted.setScale(2, RoundingMode.HALF_EVEN);

						atm[i][j] = converted.doubleValue();

						break;
					}
				}

			}
		}
	}

	private void checkAsserts() {
		Double rowSum;
		for (int i = 0; i < atm.length; i++) {
			rowSum = Arrays.asList(atm[i]).stream().mapToDouble(x -> x.doubleValue()).sum();

			BigDecimal converted = BigDecimal.valueOf(rowSum);
			converted = converted.setScale(2, RoundingMode.HALF_EVEN);
			rowSum = converted.doubleValue();

			Assert.assertTrue("The sum of each row must be 1.0", rowSum == 1.0);

			for (int j = 0; j < atm.length; j++) {
				Assert.assertTrue("Each element of the ATM must be between 0.0 and 1.0!", atm[i][j] >= 0.0 && atm[i][j] <= 1.0);
			}
		}
	}


}
