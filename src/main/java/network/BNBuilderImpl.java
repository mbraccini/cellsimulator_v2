//package network;
//
//import exceptions.SimulatorExceptions;
//import interfaces.network.BooleanNetwork;
//import interfaces.network.BooleanNetworkBuilder;
//import interfaces.network.Node;
//import org.jgrapht.graph.DefaultDirectedGraph;
//import org.jgrapht.graph.DefaultEdge;
//import org.jgrapht.graph.builder.GraphBuilder;
//
//import java.util.Objects;
//
//public class BNBuilderImpl<NODE extends Node, BN extends BooleanNetwork<NODE,BN>> implements BooleanNetworkBuilder<NODE,BN> {
//
//    private GraphBuilder<NODE, DefaultEdge, DefaultDirectedGraph<NODE,DefaultEdge>> builder
//            = new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));
//
//    private BN bn;
//    public BNBuilderImpl(BN bn) {
//        this.bn = bn;
//        builder.addGraph(bn.asGraph());
//    }
//
//    @Override
//    public BNBuilderImpl<NODE, BN> reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeIndex) throws SimulatorExceptions.NetworkNodeException {
//        if (Objects.nonNull(builder.removeEdge(bn.getNodeById(incomingNodeIndex), bn.getNodeById(targetNodeId)))){
//            if (Objects.isNull(builder.addEdge(bn.getNodeById(newInputNodeId), bn.getNodeById(targetNodeId)))){
//                throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
//            }
//        } else {
//            throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
//        }
//        return this;
//    }
//
//
//        @Override
//    public BN build(){
//        return bn.newInstance(builder.build());
//    }
//
//}
