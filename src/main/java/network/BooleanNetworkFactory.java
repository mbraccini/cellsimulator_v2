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

    /**
     * miRNA network with fixed FAN-OUT and INCOMING NODES NUMBER (K); the miRNA nodes if active turn off the affected downstream nodes.
     *
     * @return
     */
    public static miRNABNClassic<BitSet,
                                Boolean,
                                NodeDeterministic<BitSet,Boolean>,
                                BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>>,
                                NodeDeterministic<BitSet,Boolean>> miRNANetworkInstance(BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> wrappedBN,
                                                                                        int miRNA_NodesNumber,
                                                                                        int miRNA_K,
                                                                                        double miRNA_bias,
                                                                                        int miRNA_FanOut,
                                                                                        Random r) {

        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new BiasedTable(miRNA_K, miRNA_bias, r);


        int[] fixedFanOut = new int[miRNA_NodesNumber];
        Arrays.fill(fixedFanOut, miRNA_FanOut);

        return miRNABNClassicImpl.newInstance(miRNA_NodesNumber,
                fixedFanOut,
                wrappedBN,
                r,
                suppliermiRNA,
                UtilitiesBooleanNetwork.miRNADownstreamNodesSupplier());

    }

    /**
     * miRNA network with fixed FAN-OUT and INCOMING NODES NUMBER (K); the miRNA nodes if active turn off the affected downstream nodes.
     *
     * @return
     */
    public static miRNABNClassic<BitSet,
            Boolean,
            NodeDeterministic<BitSet,Boolean>,
            BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>>,
            NodeDeterministic<BitSet,Boolean>> miRNAOneInput(BNClassic<BitSet, Boolean,NodeDeterministic<BitSet,Boolean>> wrappedBN,
                                                                    int miRNA_NodesNumber,
                                                                    int miRNA_FanOut,
                                                                    Random r) {

        Supplier<Table<BitSet, Boolean>> suppliermiRNA = () -> new OrTable(1);


        int[] fixedFanOut = new int[miRNA_NodesNumber];
        Arrays.fill(fixedFanOut, miRNA_FanOut);

        return miRNABNClassicImpl.newInstance(miRNA_NodesNumber,
                fixedFanOut,
                wrappedBN,
                r,
                suppliermiRNA,
                UtilitiesBooleanNetwork.miRNADownstreamNodesSupplier());

    }




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
