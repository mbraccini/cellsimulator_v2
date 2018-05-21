package network;

import exceptions.SimulatorExceptions;
import interfaces.network.BNClassic;
import interfaces.network.BooleanNetwork;
import interfaces.network.Builder;
import interfaces.network.NodeDeterministic;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class BNClassicImpl<K,V, N extends NodeDeterministic<K,V>> extends AbstractBooleanNetwork<N> implements BNClassic<K,V,N>{

    protected BNClassicImpl() {
        super();
    }

    public BNClassicImpl(List<N> nodesList, Map<Integer, List<Integer>> map) {
        super(nodesList, map);
        checkVariablesNumberIncomingNodes();
    }

    /**
     * We check if the node as the same incoming nodes number of its function table.
     */
    protected void checkVariablesNumberIncomingNodes() {
        NodeDeterministic<K, V> node;
        for (int i = 0; i < nodesList.size(); i++) {
            node = nodesList.get(i);
            if (incomingNodesMap.get(i).size() > node.getFunction().getVariablesNumber()) {
                throw new SimulatorExceptions.NetworkNodeException.FunctionTopologyMismatch();
            }
        }
    }

    @Override
    public BNClassic<K,V,N> newInstance(List<N> nodes, Map<Integer, List<Integer>> topology) {
        return new BNClassicImpl<>(nodes,topology);
    }


    @Override
    public String toString() {
        return super.toString()
                + "\n"
                + context();
    }

}
