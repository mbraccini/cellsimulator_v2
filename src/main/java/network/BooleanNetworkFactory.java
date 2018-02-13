package network;

import exceptions.RowNotFoundException;
import generator.RandomnessFactory;
import interfaces.network.*;
import interfaces.networkdescription.NetworkAST;
import states.States;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BooleanNetworkFactory {
    private BooleanNetworkFactory() {
    }

    /**
     * BN from a file description
     *
     * @param filename
     * @return
     */
    public static BooleanNetwork<BitSet, Boolean> newNetworkFromFile(String filename) {
        NetworkAST ast = new NaiveBNParser(filename).parse();
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }

    /**
     * BN from an AST description
     *
     * @param ast
     * @return
     */
    public static BooleanNetwork<BitSet, Boolean> newNetworkFromAST(NetworkAST ast) {
        return new BNFromASTDescription(ast.getTopology().size(), ast);
    }

    /**
     * miRNA network with fixed FAN-OUT and INCOMING NODES NUMBER (K); the miRNA nodes if active turn off the affected downstream nodes.
     *
     * @return
     */
    public static miRNABooleanNetwork<BitSet, Boolean> miRNANetworkInstance(
            BooleanNetwork<BitSet, Boolean> wrappedBN,
            int miRNA_NodesNumber,
            int miRNA_K,
            double miRNA_bias,
            int miRNA_FanOut,
            Random r) {

        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new BiasedTable(miRNA_K, miRNA_bias, r);

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
                    return ConfigurableGenericTable.newInstance(variablesNumber, rows);
                };


        int[] fixedFanOut = new int[miRNA_NodesNumber];
        Arrays.fill(fixedFanOut, miRNA_FanOut);

        return miRNABN.newInstance(miRNA_NodesNumber,
                fixedFanOut,
                wrappedBN,
                r,
                suppliermiRNA,
                supplierDownstreamNode);

    }


    public enum BiasType {
        EXACT, CLASSICAL
    }

    /**
     * RandomBN classical and with exact bias
     *
     * @param biasType
     * @param nodesNumber
     * @param k
     * @param bias
     * @param r
     * @return
     */
    public static BooleanNetwork<BitSet, Boolean> newRBN(BiasType biasType, int nodesNumber, int k, double bias, Random r) {
        Supplier<Table<BitSet, Boolean>> supplier;

        if (biasType == BiasType.EXACT) {
            List<Table<BitSet, Boolean>> list = exactBiasNodesGenerator(nodesNumber, k, bias, r);
            Iterator<Table<BitSet, Boolean>> iterator = list.iterator();
            supplier = () -> iterator.next();

            return RBN.<BitSet, Boolean>newInstance(nodesNumber, k, r, supplier);
        } else {
            supplier = () -> new BiasedTable(k, bias, r);

            return RBN.<BitSet, Boolean>newInstance(nodesNumber, k, r, supplier);
        }
    }

    /**
     * Utility method for generating List of table with exact bias
     * @param nodesNumber
     * @param k
     * @param bias
     * @param r
     * @return
     */
    private static List<Table<BitSet, Boolean>> exactBiasNodesGenerator(int nodesNumber, int k, double bias, Random r) {
        List<Table<BitSet, Boolean>> list = new ArrayList<>();
        int outcomesForTruthTable = Double.valueOf(Math.pow(2, k)).intValue(); //2^(variablesNumber)
        int totalOutcomesNumber = nodesNumber * outcomesForTruthTable;
        List<Boolean> outcomeList = BooleanNetwork.generateExactBiasOutcomes(totalOutcomesNumber, bias, r);
        for (int id = 0; id < nodesNumber; id++) {
            list.add(new ConfigurableTable(k, outcomeList.subList(id * outcomesForTruthTable, (id * outcomesForTruthTable) + outcomesForTruthTable)));
        }
        return list;
    }


    public static void main(String[] args) {
        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(1222);
        System.out.println(BooleanNetworkFactory.newRBN(BiasType.CLASSICAL,10, 2, 0.5 , pseudoRandom));

    }

}
