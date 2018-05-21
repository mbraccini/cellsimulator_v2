package network;

import exceptions.RowNotFoundException;
import generator.RandomnessFactory;
import interfaces.network.BNKBias;
import interfaces.network.BNKBias.BiasType;

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
    public static BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> newNetworkFromFile(String filename) {
        return new BNFromASTDescription(new NaiveBNParser(filename).parse()).build();
    }

    /**
     * BN from an AST description
     *
     * @param ast
     * @return
     */
    public static BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> newNetworkFromAST(NetworkAST ast) {
        return new BNFromASTDescription(ast).build();
    }
//
//    /**
//     * miRNA network with fixed FAN-OUT and INCOMING NODES NUMBER (K); the miRNA nodes if active turn off the affected downstream nodes.
//     *
//     * @return
//     */
//    public static miRNABooleanNetwork<BitSet, Boolean> miRNANetworkInstance(
//            BNClassic<BitSet, Boolean> wrappedBN,
//            int miRNA_NodesNumber,
//            int miRNA_K,
//            double miRNA_bias,
//            int miRNA_FanOut,
//            Random r) {
//
//        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new BiasedTable(miRNA_K, miRNA_bias, r);
//
//        BiFunction<Integer, Table<BitSet, Boolean>, Table<BitSet, Boolean>> supplierDownstreamNode =
//                (Integer variablesToAdd, Table<BitSet, Boolean> table) ->
//                {
//                    int variablesNumber = table.getVariablesNumber() + variablesToAdd;
//                    List<Row<BitSet, Boolean>> rows = new ArrayList<>();
//                    int rowsNumber = Double.valueOf(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
//                    for (int i = 0; i < rowsNumber; i++) {
//                        BitSet input = States.convert(i, variablesNumber);
//                        try {
//                            rows.add(new RowImpl<>(input, table.getRowByInput(input).getOutput()));
//                        } catch (RowNotFoundException e) {
//                            rows.add(new RowImpl<>(input, false));
//                        }
//                    }
//                    return ConfigurableGenericTable.newInstance(variablesNumber, rows);
//                };
//
//
//        int[] fixedFanOut = new int[miRNA_NodesNumber];
//        Arrays.fill(fixedFanOut, miRNA_FanOut);
//
//        return miRNABNClassic.newInstance(miRNA_NodesNumber,
//                fixedFanOut,
//                wrappedBN,
//                r,
//                suppliermiRNA,
//                supplierDownstreamNode);
//
//    }
//
//


    public enum SelfLoop {
        WITH, WITHOUT
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
    public static BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> newRBN(BiasType biasType, SelfLoop selfLoop, int nodesNumber, int k, double bias, Random r) {
        Supplier<Table<BitSet, Boolean>> supplier;

        if (biasType == BiasType.EXACT) {
            List<Table<BitSet, Boolean>> list = UtilitiesBooleanNetwork.exactBiasNodesGenerator(nodesNumber, k, bias, r);
            Iterator<Table<BitSet, Boolean>> iterator = list.iterator();
            supplier = () -> iterator.next();
        } else {
            supplier = () -> new BiasedTable(k, bias, r);
        }

        if (selfLoop == SelfLoop.WITH) {
            return new BNKBiasImpl(nodesNumber,  k,  bias,  r,  biasType, Boolean.TRUE);
        } else {
            return new BNKBiasImpl(nodesNumber,  k,  bias,  r,  biasType, Boolean.FALSE);
        }

    }




    public static void main(String[] args) {
        Random pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(1222);
        System.out.println(BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, SelfLoop.WITH,10, 2, 0.5 , pseudoRandom));

    }

}
