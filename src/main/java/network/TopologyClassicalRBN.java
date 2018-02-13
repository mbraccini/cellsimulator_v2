package network;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exceptions.InputConnectionsException;
import interfaces.network.BooleanNetwork;
import interfaces.network.Table;

public class TopologyClassicalRBN<K, V> extends AbstractBooleanNetwork<K, V> {

    protected final Random random;
    protected final int k;
    protected Supplier<Table<K, V>> tableSupplier;

    public TopologyClassicalRBN(int nodesNumber, int k, Random random) {
        super(nodesNumber);
        this.k = k;
        this.random = random;
        checkNodesNumberInvariant();
        properties.setProperty("topology", "random");
    }

    private TopologyClassicalRBN(int nodesNumber, int k, Random random, Supplier<Table<K, V>> tableSupplier) {
        super(nodesNumber);
        this.k = k;
        this.random = random;
        this.tableSupplier = tableSupplier;
        checkNodesNumberInvariant();
        configure();
        properties.setProperty("topology", "random");
    }

    private final void checkNodesNumberInvariant() {
        if (this.k > (this.nodesNumber - 1)) {
            throw new InputConnectionsException("K must be less than (#nodes - 1)!");
        }
    }

    public static <K,V> BooleanNetwork<K,V> newInstance(int nodesNumber, int k, Random random, Supplier<Table<K, V>> tableSupplier){
        return new TopologyClassicalRBN<K,V>(nodesNumber, k, random, tableSupplier);
    }

    private void configure() {
        initNodes();
        initTopology();
    }

    protected void initNodes() {
        nodesList = IntStream.range(0, nodesNumber)
                .mapToObj(x -> {
                    return new NodeImpl<>("gene_" + x, x, tableSupplier.get());
                })
                .collect(Collectors.toList());

    }

    protected final void initTopology() {
        List<Integer> list;
        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
            list = new ArrayList<>(this.k);
            int nodesAdded = 0;
            do {
                int candidate = random.nextInt(this.nodesNumber);

                if (candidate != nodeId && !list.stream().anyMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < this.k);

			/* ora con le informazioni calcolate posso riempire la map */
            this.nodesMap.put(this.nodesList.get(nodeId),
                    list.stream().map(x -> nodesList.get(x)).collect(Collectors.toList()));
        }

    }

}
