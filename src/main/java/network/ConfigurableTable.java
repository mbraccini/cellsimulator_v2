package network;


import interfaces.network.Row;
import states.States;

import java.util.BitSet;
import java.util.List;

public class ConfigurableTable extends AbstractTable<BitSet, Boolean>{
    List<Boolean> outputsList;
    protected int rowsNumber;


    /*
	 * Esempio: outputList->{0,1,0,0,1}
	 * truth table relativa:
	 * Xn,.....,X0 | (t+1)
	 * ............| 0
	 * ............| 1
	 * ............| 0
	 * ............| 0
	 * ............| 1
	 */

    public ConfigurableTable(int variablesNumber, List<Boolean> outputsList) {
        super(variablesNumber);
        this.rowsNumber = Double.valueOf(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
        this.outputsList = outputsList;
        configure();
    }

    @Override
    protected void configure(){
        Row<BitSet, Boolean> entry = null;

        for (int i = 0; i < this.rowsNumber; i++) {

            BitSet input = States.convert(i, variablesNumber);

            entry = new RowImpl<BitSet, Boolean>(input, outputsList.get(i));

            this.rows.add(entry);
            this.mapRows.put(input, entry);
        }
    }
}
