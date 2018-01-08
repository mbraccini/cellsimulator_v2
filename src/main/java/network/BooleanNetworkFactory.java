package network;

import exceptions.RowNotFoundException;
import experiments.selfLoop.BNGeneticAlgFitness;
import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;
import interfaces.network.Row;
import interfaces.network.Table;
import interfaces.network.miRNABooleanNetwork;
import interfaces.networkdescription.NetworkAST;
import states.States;
import utility.GenericUtility;

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

    public static void main(String[] args) {
        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(1222);
        BooleanNetwork<BitSet, Boolean> bn = new RBNExactBias(15, 2, 0.5, pseudoRandom);
        GenericUtility.printMatrix(BNGeneticAlgFitness.simulateBN(bn).getMatrixCopy());

        miRNABooleanNetwork<BitSet, Boolean> miRNA = BooleanNetworkFactory.miRNANetworkInstance(bn,5,2,0.5,3,pseudoRandom);
        GenericUtility.printMatrix(BNGeneticAlgFitness.simulateBN(miRNA).getMatrixCopy());
    }

}
