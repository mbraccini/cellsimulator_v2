//package network;
//
//import java.util.BitSet;
//import java.util.Random;
//
//public class SelfLoopBN extends TopologySelfLoopAverageK<BitSet, Boolean> {
//
//    private final double bias;
//
//    public SelfLoopBN(int nodesNumber, int k, double bias,randomGeneratorrandom) {
//        super(nodesNumber, k, random);
//        this.bias = bias;
//
//        configure();
//        properties.setProperty("bias", "unconstrained");
//    }
//
//    private void configure() {
//        initNodes();
//        initTopology();
//    }
//
//    @Override
//    protected void initNodes() {
//        for (int id = 0; id < this.nodesNumber; id++) {
//            nodes.add(new AbstractNode<>("gene_" + id, id, new BiasedTable(incomingNodesPerNode[id], this.bias, random)));
//        }
//    }
//
//
//}
