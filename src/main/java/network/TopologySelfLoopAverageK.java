package network;

import exceptions.InputConnectionsException;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class TopologySelfLoopAverageK<K, V> extends AbstractBooleanNetwork<K, V> {

    protected final Random random;
    protected final int averageK;
    protected final int[] incomingNodesPerNode;
    private final PoissonDistribution poisson;

    public TopologySelfLoopAverageK(int nodesNumber, int averageK, Random random) {
        super(nodesNumber);
        this.averageK = averageK;
        this.random = random;

        checkNodesNumberInvariant();

        this.incomingNodesPerNode = new int[nodesNumber];
        poisson = new PoissonDistribution(this.averageK); //not yet reproducible
        properties.setProperty("topology", "self-loop");

        getNumberOfIncomingNodesByMeansOfPoissonDistribution();
    }

    /** Avoids repetition of the same arc **/
    private final void checkNodesNumberInvariant() {
        if (averageK > (nodesNumber)) {
            throw new InputConnectionsException("K must be less than #nodes!");
        }
    }

    protected abstract void initNodes();


    private final void getNumberOfIncomingNodesByMeansOfPoissonDistribution() {
        int incomingNodesNumberSampled;
        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
            do {
                incomingNodesNumberSampled = poisson.sample();
            } while (incomingNodesNumberSampled > this.nodesNumber || incomingNodesNumberSampled == 0); //non permettiamo un nodo con 0 input e un numero multiplo di stessi archi

            this.incomingNodesPerNode[nodeId] = incomingNodesNumberSampled;
        }
    }

    protected final void initTopology() {
        List<Integer> list;
        int numberOfIncomingNodes;
        for (int nodeId = 0; nodeId < this.nodesNumber; nodeId++) {
            numberOfIncomingNodes = this.incomingNodesPerNode[nodeId];
            list = new ArrayList<>(numberOfIncomingNodes);
            int nodesAdded = 0;
            do {
                int candidate = random.nextInt(this.nodesNumber);

                if (!list.stream().anyMatch(x -> x == candidate)) {
                    list.add(candidate);
                    nodesAdded++;
                }
            } while (nodesAdded < numberOfIncomingNodes);

			/* ora con le informazioni calcolate posso riempire la map */
            this.nodesMap.put(this.nodesList.get(nodeId),
                    list.stream().map(x->nodesList.get(x)).collect(Collectors.toList()));
        }
    }

}
