package network;

import exceptions.SimulatorExceptions;
import interfaces.network.BooleanNetwork;
import interfaces.network.Builder;
import interfaces.network.Node;
import interfaces.network.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBNBuilder
                    <NODE extends Node,
                    BN extends BooleanNetwork<NODE>,
                    SUB extends AbstractBNBuilder<NODE,BN,SUB>>

                    implements Builder<BN> {

        protected Map<Integer, List<Integer>> newTopology;
        protected BN bn;
        protected Set<NODE> nodes;

        public AbstractBNBuilder(BN bn) {
            this.bn = bn;
            this.nodes = new HashSet<>(bn.getNodes());
            this.newTopology = bn.topology();
        }

        public SUB reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeId) throws SimulatorExceptions.NetworkNodeException {

            if (!newTopology.get(targetNodeId).contains(incomingNodeId)){
                throw new SimulatorExceptions.NetworkNodeException.NodeNotPresentException();
            } else {
                nodes.stream().filter(x -> x.getId().intValue() == newInputNodeId).findAny().orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
                newTopology.get(targetNodeId).set(newTopology.get(targetNodeId).indexOf(incomingNodeId), newInputNodeId);
            }

            return self();
        }

        protected abstract SUB self();

        public abstract BN build();

    }