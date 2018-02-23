package visualization;

import org.jooq.lambda.tuple.Tuple2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SCCGraphViz implements Writable{
    List<List<Integer>> components;
    int[][] adjacencyMatrix;
    private GraphViz gz;
    public SCCGraphViz(Tuple2<List<List<Integer>>, int[][]> tuple){
        this.gz = new GraphViz("digraph", "components");
        adjacencyMatrix = tuple.v2();
        components = tuple.v1();
        init();
        arcs();
    }

    private void init() {
        for (int i = 0; i < components.size(); i++) {

            StringBuilder sb = new StringBuilder();
            sb.append("\"Attrcs:\n");
            int count = 0;
            for (Integer idx : components.get(i)) {
                count++;
                if (count % 15 == 0) {
                    sb.append(idx + "\n");
                } else {
                    sb.append(idx + ", ");
                }
            }
            sb.append("\"");

            gz.addLine(i + GraphViz.LABEL_START + sb.toString() + GraphViz.LABEL_END);
        }
    }

    private void arcs() {
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            for (int j = 0; j < adjacencyMatrix.length; j++) {
                if (adjacencyMatrix[i][j] > 0.0) {
                    gz.addLine(i
                            + GraphViz.ARC_START
                            + j);
                }
            }
        }

    }

    @Override
    public void saveOnDisk(String path) {
        this.gz.generateDotFile(path).generateImg("jpg");
    }
}
