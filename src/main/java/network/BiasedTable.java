package network;

import interfaces.network.Row;
import states.States;
import utility.Randomness;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


public class BiasedTable extends AbstractTable<BitSet, Boolean> {

	private final Random random;
	protected double bias;
	

	public BiasedTable(int variableNumber, double bias, Random random) {
		super(variableNumber);
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
