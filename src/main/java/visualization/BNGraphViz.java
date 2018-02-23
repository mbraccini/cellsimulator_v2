package visualization;

import interfaces.network.BooleanNetwork;
import interfaces.network.Node;

public class BNGraphViz<K, V> implements Writable{

    private BooleanNetwork<K, V> bn;
    private GraphViz gz;
    public BNGraphViz(BooleanNetwork<K, V> bn) {
        gz = new GraphViz("digraph", "bn");
        this.bn = bn;
        init();
        arcs();
    }

    private void arcs() {
        for (Node<K, V> node : this.bn.getNodes()) {
            for (Node<?, ?> outNode : this.bn.getOutcomingNodes(node)) {
                gz.addLine(node.getId()
                        + GraphViz.ARC_START
                        + outNode.getId());
            }

        }
    }

    private void init() {
        for (Node<?, ?> node : this.bn.getNodes()) {
            String nodeLabel = "\"" + node.getName() + "\"";
            gz.addLine(node.getId() + GraphViz.LABEL_START + nodeLabel + GraphViz.LABEL_END);
        }
    }

    @Override
    public void saveOnDisk(String path) {
        this.gz.generateDotFile(path).generateImg("jpg");
    }

}
