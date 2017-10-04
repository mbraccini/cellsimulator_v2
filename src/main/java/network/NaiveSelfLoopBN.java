package network;

import generator.RandomnessFactory;
import interfaces.network.BooleanNetwork;

import java.util.BitSet;
import java.util.Random;

public class NaiveSelfLoopBN extends SelfLoopTopology<BitSet, Boolean> {

    private final double bias;

    public NaiveSelfLoopBN(int nodesNumber, int k, double bias, Random random) {
        super(nodesNumber, k, random);
        this.bias = bias;

        configure();
        properties.setProperty("bias", "classical");
    }

    private void configure() {
        initNodes();
        initTopology();
    }

    @Override
    protected void initNodes() {
        for (int id = 0; id < this.nodesNumber; id++) {
            nodesList.add(new NodeImpl<>("gene_" + id, id, new BiasedTable(incomingNodesPerNode[id], this.bias, random)));
        }
    }


}
