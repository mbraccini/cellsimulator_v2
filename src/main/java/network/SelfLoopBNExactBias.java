//package network;
//
//import interfaces.network.BNClassic;
//
//import java.util.BitSet;
//import java.util.List;
//import java.util.Random;
//
//public class SelfLoopBNExactBias extends TopologySelfLoopAverageK<BitSet, Boolean> {
//
//    private final double bias;
//
//    public SelfLoopBNExactBias(int nodesNumber, int k, double bias,randomGeneratorrandom) {
//        super(nodesNumber, k, random);
//        this.bias = bias;
//
//        configure();
//        properties.setProperty("bias", "exact");
//    }
//
//    private void configure() {
//        initNodes();
//        initTopology();
//    }
//
//    @Override
//    protected void initNodes() {
//        int totalOutcomesNumber = 0;
//
//        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
//            totalOutcomesNumber += Double.valueOf(Math.pow(2, this.incomingNodesPerNode[nodeId])).intValue();
//        }
//
//        List<Boolean> outcomeList = BNClassic.generateExactBiasOutcomes(totalOutcomesNumber, bias, random);
//
//        int incomingNodesNumber;
//        int k;
//        int indicesSumSoFar = 0;
//        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
//            k = this.incomingNodesPerNode[nodeId];
//
//            incomingNodesNumber = Double.valueOf(Math.pow(2, k)).intValue();
//            nodes.add(new AbstractNode<>("gene_" + nodeId, nodeId,
//                    new ConfigurableTable(k, outcomeList.subList(indicesSumSoFar, indicesSumSoFar + incomingNodesNumber))));
//            indicesSumSoFar += incomingNodesNumber;
//        }
//
//    }
//
//
//}
