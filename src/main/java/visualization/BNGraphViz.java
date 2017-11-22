package visualization;

import interfaces.network.BooleanNetwork;
import interfaces.network.Node;

public class BNGraphViz<K, V> extends GraphViz {

    private BooleanNetwork<K, V> bn;

    public BNGraphViz(BooleanNetwork<K, V> bn, String filenames) {
        super("digraph", "bn", filenames);
        this.bn = bn;
        init();
        arcs();
    }

    private void arcs() {
        for (Node<K, V> node : this.bn.getNodes()) {
            for (Node<?, ?> outNode : this.bn.getOutcomingNodes(node)) {
                addLine(node.getId()
                        + GraphViz.ARC_START
                        + outNode.getId());
            }

        }
    }

    private void init() {
        for (Node<?, ?> node : this.bn.getNodes()) {
            String nodeLabel = "\"" + node.getName() + "\"";
            addLine(node.getId() + GraphViz.LABEL_START + nodeLabel + GraphViz.LABEL_END);
        }
    }


}
