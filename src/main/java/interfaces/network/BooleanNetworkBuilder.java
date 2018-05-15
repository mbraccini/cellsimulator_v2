package interfaces.network;

import exceptions.SimulatorExceptions;

public interface BooleanNetworkBuilder<NODE extends Node, BN extends BooleanNetwork<NODE,BN>> {


    BooleanNetworkBuilder<NODE,BN> reconfigureIncomingEdge(Integer targetNodeId,
                                                                Integer newInputNodeId,
                                                                    Integer incomingNodeIndex) throws SimulatorExceptions.NetworkNodeException;


    BN build();
}
