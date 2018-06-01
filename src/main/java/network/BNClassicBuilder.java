package network;

import exceptions.SimulatorExceptions;
import interfaces.network.BNClassic;
import interfaces.network.Node;
import interfaces.network.NodeDeterministic;
import interfaces.network.Table;

import java.util.function.Consumer;

public class BNClassicBuilder
                <K,V,NODE extends NodeDeterministic<K,V>>
                extends AbstractBNBuilder<NODE,BNClassic<K,V,NODE>,BNClassicBuilder<K,V,NODE>> {


    public BNClassicBuilder(BNClassic<K, V, NODE> bn) {
        super(bn);
    }

    @Override
    protected BNClassicBuilder<K, V, NODE> self() {
        return this;
    }


    /**
     * Try to replace the node with the same ID and Name of the provided one.
     * @param newNode
     * @return
     * @throws SimulatorExceptions.NetworkNodeException
     */
     public BNClassicBuilder<K, V, NODE> replaceNode(NODE oldNode,NODE newNode) throws SimulatorExceptions.NetworkNodeException {
         nodes.remove(oldNode);
         nodes.add(newNode);
         return self();
     }


//    public BNClassicBuilder<K, V, NODE> replaceNode(NODE newNode) throws SimulatorExceptions.NetworkNodeException {
//
//        nodes.stream()
//                .filter(x -> x.getId().equals(newNode.getId()) && x.getName().equals(newNode.getName()))
//                .findFirst()
//                .ifPresentOrElse( old-> tryToReplace(old, newNode), ()-> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
//
//        return self();
//    }
//
//    private void tryToReplace(NODE oldNode, NODE newNode) {
//       // if (oldNode.getFunction().getVariablesNumber().equals(newNode.getFunction().getVariablesNumber())) {
//            nodes.remove(oldNode);
//            nodes.add(newNode);
//        //} else {
//         //   throw new SimulatorExceptions.NetworkNodeException.FunctionTopologyMismatch();
//        //}
//    }


    @Override
    public BNClassic<K, V, NODE> build() {
        return (BNClassic<K, V, NODE>) bn.newInstance(nodes, newTopology);
    }
}



