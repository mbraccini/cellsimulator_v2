package network;

import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import interfaces.network.Row;
import interfaces.network.Table;

import java.util.BitSet;
import java.util.List;
import java.util.function.Supplier;

public class ConfigurableGenericTable<K,V> extends AbstractTable<K,V>{

    private List<Row<K,V>> rowsToCopy;
    public ConfigurableGenericTable(int variablesNumber, List<Row<K,V>> rowsToCopy) {
        super(variablesNumber);
        this.rowsToCopy = rowsToCopy;
        configure();
    }

    /*public static <K,V> Table<K,V> newInstance(int variablesNumber, List<Row<K,V>> rowsToCopy) {
        return new ConfigurableGenericTable<K,V>(variablesNumber, rowsToCopy);
    }*/

    @Override
    protected void configure(){
        Row<K,V> row = null;
        for (int i = 0; i < this.rowsToCopy.size(); i++) {
            row = rowsToCopy.get(i);
            row = new RowImpl<K,V>(row.getInput(), row.getOutput());
            this.rows.add(row);
            this.mapRows.put(row.getInput(), row);
        }
    }

    public static void main(String args[]) {
        RandomGenerator r = RandomnessFactory.getPureRandomGenerator();
        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new BiasedTable(2, 0.5, r);
        Table<BitSet,Boolean> b = suppliermiRNA.get();
        Table<BitSet,Boolean> bb = new ConfigurableGenericTable<>(2, b.getRows());
        System.out.println(b == bb);
        System.out.println(b.equals(bb));
        System.out.println(bb.equals(b));

        System.out.println(b);
        System.out.println(bb);



    }

}
