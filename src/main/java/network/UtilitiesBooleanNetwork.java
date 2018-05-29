package network;

import exceptions.RowNotFoundException;
import interfaces.network.BNKBias;
import interfaces.network.BNKBias.BiasType;
import interfaces.network.Row;
import interfaces.network.Table;
import states.States;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class UtilitiesBooleanNetwork {
    private UtilitiesBooleanNetwork(){}


    /**
     * Returns an exact or classical table supplier
     * @param biasType
     * @param nodesNumber
     * @param k
     * @param bias
     * @param r
     * @return
     */
    public static Supplier<Table<BitSet, Boolean>> rndTableSupplier(BiasType biasType, int nodesNumber, int k, double bias, Random r) {
        Supplier<Table<BitSet, Boolean>> supplier;

        if (biasType == BiasType.EXACT) {
            List<Table<BitSet, Boolean>> list = exactBiasNodesGenerator(nodesNumber, k, bias, r);
            Iterator<Table<BitSet, Boolean>> iterator = list.iterator();
            supplier = () -> iterator.next();
        } else {
            supplier = () -> new BiasedTable(k, bias, r);
        }
        return supplier;
    }


    /**
     * Function that returns a new table that turns off the node (see miRNA downstream nodes)
     * @return
     */
    public static BiFunction<Integer, Table<BitSet, Boolean>, Table<BitSet, Boolean>> miRNADownstreamNodesSupplier(){
        BiFunction<Integer, Table<BitSet, Boolean>, Table<BitSet, Boolean>> supplierDownstreamNode =
                (Integer variablesToAdd, Table<BitSet, Boolean> table) ->
                {
                    int variablesNumber = table.getVariablesNumber() + variablesToAdd;
                    List<Row<BitSet, Boolean>> rows = new ArrayList<>();
                    int rowsNumber = Double.valueOf(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
                    for (int i = 0; i < rowsNumber; i++) {
                        BitSet input = States.convert(i, variablesNumber);
                        try {
                            rows.add(new RowImpl<>(input, table.getRowByInput(input).getOutput()));
                        } catch (RowNotFoundException e) {
                            rows.add(new RowImpl<>(input, false));
                        }
                    }
                    return new ConfigurableGenericTable<>(variablesNumber, rows);
                };
        return supplierDownstreamNode;
    }

    /**
     * Utility method for generating List of table with exact bias
     * @param nodesNumber
     * @param k
     * @param bias
     * @param r
     * @return
     */
    public static List<Table<BitSet, Boolean>> exactBiasNodesGenerator(int nodesNumber, int k, double bias, Random r) {
        List<Table<BitSet, Boolean>> list = new ArrayList<>();
        int outcomesForTruthTable = Double.valueOf(Math.pow(2, k)).intValue(); //2^(variablesNumber)
        int totalOutcomesNumber = nodesNumber * outcomesForTruthTable;
        List<Boolean> outcomeList = generateExactBiasOutcomes(totalOutcomesNumber, bias, r);
        for (int id = 0; id < nodesNumber; id++) {
            list.add(new ConfigurableTable(k, outcomeList.subList(id * outcomesForTruthTable, (id * outcomesForTruthTable) + outcomesForTruthTable)));
        }
        return list;
    }


    /**
     *
     * @param totalOutcomesNumber
     * @param bias
     * @param randomInstance
     * @return
     */
    public static List<Boolean> generateExactBiasOutcomes(int totalOutcomesNumber, double bias, Random randomInstance) {
        List<Boolean> outcomeList;

        int ones = (int) Math.round((totalOutcomesNumber) * bias);
        int zeros = totalOutcomesNumber - ones;

        outcomeList = new ArrayList<Boolean>(Collections.nCopies(ones, true));
        outcomeList.addAll(new ArrayList<Boolean>(Collections.nCopies(zeros, false)));

        Collections.shuffle(outcomeList, randomInstance);
        return outcomeList;
    }
}
