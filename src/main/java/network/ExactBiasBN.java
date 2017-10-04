package network;

import interfaces.network.BooleanNetwork;

import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class ExactBiasBN extends ClassicalTopology<BitSet,Boolean> {

    private final double bias;

    public ExactBiasBN(int nodesNumber, int k, double bias, Random random) {
        super(nodesNumber, k, random);
        this.bias = bias;

        configure();
        properties.setProperty("bias", "exact");
    }


    private void configure() {
        initNodes();
        initTopology();
    }

    @Override
    protected void initNodes() {
            int outcomesForTruthTable = new Double(Math.pow(2, this.k)).intValue(); //2^(variablesNumber)
            int totalOutcomesNumber = this.nodesNumber * outcomesForTruthTable;
            List<Boolean> outcomeList = BooleanNetwork.generateExactBiasOutcomes(totalOutcomesNumber, this.bias, random);
            for (int id = 0; id < this.nodesNumber; id++) {
                nodesList.add(new NodeImpl<>("gene_" + id, id,
                        new ConfigurableTable(this.k, outcomeList.subList(id * outcomesForTruthTable, (id * outcomesForTruthTable) + outcomesForTruthTable))));
            }
    }
}
