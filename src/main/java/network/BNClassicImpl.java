package network;

import exceptions.SimulatorExceptions;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BNClassicImpl<K,V, N extends NodeDeterministic<K,V>> extends AbstractBooleanNetwork<N> implements BNClassic<K,V,N>{

    protected BNClassicImpl() {
        super();
    }

    public BNClassicImpl(Set<N> nodes, Map<Integer, List<Integer>> map) {
        super(nodes, map);
        checkVariablesNumberIncomingNodes();
    }

    /**
     * We check if the node as the same incoming nodes number of its function table.
     */
    protected void checkVariablesNumberIncomingNodes() {
        nodes.forEach(node -> {
                                if (incomingNodesMap.get(node.getId()).size() > node.getFunction().getVariablesNumber()) {
                                    throw new SimulatorExceptions.NetworkNodeException.FunctionTopologyMismatch();
                                }
                            });
    }

    @Override
    public BNClassic<K,V,N> newInstance(Set<N> nodes, Map<Integer, List<Integer>> topology) {
        return new BNClassicImpl<>(nodes,topology);
    }


    @Override
    public String toString() {
        return super.toString()
                + "\n"
                + context();
    }

}
