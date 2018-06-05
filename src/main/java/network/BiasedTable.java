package network;

import interfaces.network.Row;
import interfaces.network.Table;
import org.apache.commons.math3.random.RandomGenerator;
import states.States;
import utility.Randomness;

import java.util.BitSet;
import java.util.List;
import java.util.Random;


public class BiasedTable extends AbstractTable<BitSet, Boolean> {

	private final RandomGenerator random;
	protected double bias;
	protected int rowsNumber;



	public BiasedTable(int variableNumber, double bias,RandomGenerator random) {
		super(variableNumber);
		this.rowsNumber = Double.valueOf(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
		this.bias = bias;
		this.random = random;
		configure();
	}

	@Override
	protected void configure() {
		for(int i = 0; i < this.rowsNumber; i++){
			
			BitSet input = States.convert(i, this.variablesNumber);

			Boolean output = Randomness.randomBooleanOutcome(bias, random);
			Row<BitSet, Boolean> entry = new RowImpl<BitSet, Boolean>(input, output);

			this.rows.add(entry);
			this.mapRows.put(input, entry);

		}
	}

}
