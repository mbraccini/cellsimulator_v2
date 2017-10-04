package network;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import exceptions.InputConnectionsException;

public abstract class TopologyClassical<K, V> extends AbstractBooleanNetwork<K, V> {

    protected final Random random;
    protected final int k;

    public TopologyClassical(int nodesNumber, int k, Random random) {
        super(nodesNumber);
        this.k = k;
        this.random = random;
        checkNodesNumberInvariant();
        properties.setProperty("topology", "classical");
    }

    private final void checkNodesNumberInvariant() {
        if (this.k > (this.nodesNumber - 1)) {
            throw new InputConnectionsException("K must be less than (#nodes - 1)!");
        }
    }

    protected abstract void initNodes();


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
