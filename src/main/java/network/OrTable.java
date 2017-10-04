package network;

import java.util.BitSet;
import java.util.stream.Collectors;

import interfaces.network.Row;
import states.States;

public class OrTable extends AbstractTable<BitSet, Boolean> {

    public OrTable(int variablesNumber) {
        super(variablesNumber);
        configure();
    }

    @Override
    protected void configure() {
        Row<BitSet, Boolean> entry = null;

        for (int i = 0; i < this.rowsNumber; i++) {

            BitSet input = States.convert(i, variablesNumber);

            if (input.isEmpty() == false) {
                entry = new RowImpl<BitSet, Boolean>(input, true);
            } else {
                entry = new RowImpl<BitSet, Boolean>(input, false);
            }
            this.rows.add(entry);
            this.mapRows.put(input, entry);

        }
    }


}
