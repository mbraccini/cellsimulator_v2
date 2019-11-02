package network;

import experiments.selfLoop.MainSelfLoopsStatisticsNumberOfAttractors;
import org.apache.commons.math3.random.RandomGenerator;
import utility.RandomnessFactory;
import interfaces.network.BNKBias;
import interfaces.network.BNKBias.BiasType;

import interfaces.network.*;

import interfaces.networkdescription.NetworkAST;

import java.util.*;
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
                                                                                        RandomGenerator r) {

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
                                                                    RandomGenerator r) {

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
    public static BNKBias<BitSet,Boolean,NodeDeterministic<BitSet,Boolean>> newRBN(BiasType biasType, SelfLoop selfLoop, int nodesNumber, int k, double bias,RandomGenerator r) {
        /*Supplier<Table<BitSet, Boolean>> supplier;

        if (biasType == BiasType.EXACT) {
            List<Table<BitSet, Boolean>> list = UtilitiesBooleanNetwork.exactBiasNodesGenerator(nodesNumber, k, bias, r);
            Iterator<Table<BitSet, Boolean>> iterator = list.iterator();
            supplier = () -> iterator.next();
        } else {
            supplier = () -> new BiasedTable(k, bias, r);
        }*/

        TableSupplier<BitSet,Boolean> tableSupplier = null;
        if (biasType == BiasType.EXACT) {
            tableSupplier = new TableSupplierExactBias(nodesNumber, k, bias, r);
        } else {
            tableSupplier = new TableSupplierClassicalBias(k, bias, r);
        }


        if (selfLoop == SelfLoop.WITH) {
            return new BNKBiasImpl(nodesNumber,  r, Boolean.TRUE, tableSupplier);
        } else {
            return new BNKBiasImpl(nodesNumber,  r, Boolean.FALSE, tableSupplier);
        }

    }


    public enum WIRING_TYPE {
        RND_K_FIXED, RND_K_plus_1, OR_K_FIXED, OR_K_plus_1, AND_K_FIXED
    }

    /**
     *
     * @param k
     * @param bias
     * @param nodesNumber
     * @param r
     * @param selfLoopNumber
     * @param wiringType
     * @return
     */
    public static BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> newBNwithSelfLoop(int k, double bias, int nodesNumber, RandomGenerator r, int selfLoopNumber, WIRING_TYPE wiringType) {
        Supplier<Table<BitSet, Boolean>> supplier = () -> new BiasedTable(k, bias, r);
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet, Boolean>> current_bn;

        if (selfLoopNumber == 0) {
            current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);
        } else {
            current_bn = BooleanNetworkFactory.newRBN(BNKBias.BiasType.CLASSICAL, BooleanNetworkFactory.SelfLoop.WITHOUT, nodesNumber, k, bias, r);

            int selfloopsToAdd = 0;
            while (selfloopsToAdd < selfLoopNumber) {
                NodeDeterministic<BitSet, Boolean> node = current_bn.getNodeById(selfloopsToAdd);
                switch (wiringType) {
                    case AND_K_FIXED:
                        current_bn = new BNClassicBuilder<>(current_bn)
                                .reconfigureIncomingEdge(node.getId(), node.getId(), current_bn.getIncomingNodes(node).get(0).getId())
                                .replaceNode(node, new NodeDeterministicImpl<>("r_and_" + node.getName(), node.getId(), new AndTable(node.getFunction().getVariablesNumber())))
                                .build();
                        break;
                    case OR_K_FIXED:
                        current_bn = new BNClassicBuilder<>(current_bn)
                                .reconfigureIncomingEdge(node.getId(), node.getId(), current_bn.getIncomingNodes(node).get(0).getId())
                                .replaceNode(node, new NodeDeterministicImpl<>("r_or_" + node.getName(), node.getId(), new OrTable(node.getFunction().getVariablesNumber())))
                                .build();
                        break;
                    case RND_K_FIXED:
                        current_bn = new BNClassicBuilder<>(current_bn)
                                .reconfigureIncomingEdge(node.getId(), node.getId(), current_bn.getIncomingNodes(node).get(0).getId())
                                .build();
                        break;
                    case OR_K_plus_1:
                        current_bn = new BNClassicBuilder<>(current_bn)
                                .addIncomingNode(node.getId(), node.getId()) //selfloop
                                .replaceNode(node,
                                        new NodeDeterministicImpl<>("r_or_plus_1_" + node.getName(),
                                                node.getId(),
                                                UtilitiesBooleanNetwork.extendTable(node.getFunction(), 1, () -> true)))
                                .build();
                        break;
                    case RND_K_plus_1:
                        current_bn = new BNClassicBuilder<>(current_bn)
                                .addIncomingNode(node.getId(), node.getId()) //selfloop
                                .replaceNode(node,
                                        new NodeDeterministicImpl<>("r_rnd_plus_1_" + node.getName(),
                                                node.getId(),
                                                UtilitiesBooleanNetwork.extendTable(node.getFunction(), 1, () -> r.nextBoolean())))
                                .build();
                        break;
                }

                selfloopsToAdd++;
            }
        }

        if (current_bn.numberOfNodeWithSelfloops() != selfLoopNumber) {
            throw new RuntimeException("Mismatch in selfloops number, expected" + selfLoopNumber + ", present " + current_bn.numberOfNodeWithSelfloops());
        }

        return current_bn;
    }


    public static void main(String[] args) {
        RandomGenerator pseudoRandom = RandomnessFactory.newPseudoRandomGenerator(1222);
        System.out.println(BooleanNetworkFactory.newRBN(BiasType.CLASSICAL, SelfLoop.WITH,10, 2, 0.5 , pseudoRandom));

    }

}
