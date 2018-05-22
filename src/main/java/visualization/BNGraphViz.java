package visualization;

import interfaces.network.BooleanNetwork;
import interfaces.network.Node;

public class BNGraphViz<N extends Node> implements Writable{

    private BooleanNetwork<N> bn;
    private GraphViz gz;
    public BNGraphViz(BooleanNetwork<N> bn) {
        gz = new GraphViz("digraph", "bn");
        this.bn = bn;
        init();
        arcs();
    }

    private void arcs() {
        for (N node : this.bn.getNodes()) {
            for (N outNode : bn.getOutgoingNodes(node)) {
                gz.addLine(node.getId()
                        + GraphViz.ARC_START
                        + outNode.getId());
            }

        }
    }

    private void init() {
        for (N node : this.bn.getNodes()) {
            String nodeLabel = "\"" + node.getName() + "\"";
            gz.addLine(node.getId() + GraphViz.LABEL_START + nodeLabel + GraphViz.LABEL_END);
        }
    }

    @Override
    public void saveOnDisk(String path) {
        this.gz.generateDotFile(path).generateImg("jpg");
    }

}
