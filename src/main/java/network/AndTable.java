package network;

import java.util.BitSet;
import java.util.stream.Collectors;

import interfaces.state.BinaryState;
import interfaces.network.Row;
import states.ImmutableBinaryState;
import states.States;

public class AndTable extends AbstractTable<BitSet, Boolean> {

	public AndTable(int variablesNumber) {
		super(variablesNumber);
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
