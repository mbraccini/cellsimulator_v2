package network;

import interfaces.network.Row;
import states.States;

import java.util.BitSet;

public class AndTable extends AbstractTable<BitSet, Boolean> {

	protected int rowsNumber;

	public AndTable(int variablesNumber) {
		super(variablesNumber);
		this.rowsNumber = Double.valueOf(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
		configure();
	}

	@Override
	protected void configure() {
		Row<BitSet, Boolean> entry = null;

		for (int i = 0; i < this.rowsNumber; i++) {

			BitSet input = States.convert(i, variablesNumber);
			if (i != (this.rowsNumber - 1)) {
				entry = new RowImpl<BitSet, Boolean>(input, false);
			} else {
				entry = new RowImpl<BitSet, Boolean>(input, true);
			}
			this.rows.add(entry);
			this.mapRows.put(input, entry);
		}	
	}


}
