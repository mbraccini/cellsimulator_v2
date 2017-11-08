package network;

import java.util.BitSet;
import java.util.Random;

public class RBN extends TopologyClassicalRBN<BitSet, Boolean> {

    private final double bias;

    public RBN(int nodesNumber, int k, double bias, Random random) {
        super(nodesNumber, k, random);
        this.bias = bias;

        configure();
        properties.setProperty("bias", "unconstrained");
    }

    private void configure() {
        initNodes();
        initTopology();
    }

    @Override
    protected void initNodes() {
        for (int id = 0; id < this.nodesNumber; id++) {
            nodesList.add(new NodeImpl<>("gene_" + id, id, new BiasedTable(k, bias, random)));
        }
    }

}
