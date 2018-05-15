package interfaces.network;

import exceptions.SimulatorExceptions;
import network.BNBuilderImpl;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import utility.Files;

import java.io.StringWriter;
import java.util.*;

public interface BooleanNetwork<N extends Node, B extends BooleanNetwork<N,B>> {

    Integer getNodesNumber();

    /**
     * From id = 0 to id = N-1
     * @return
     */
    List<N> getNodes();

    N getNodeByName(String name) throws SimulatorExceptions.NetworkNodeException;

    N getNodeById(Integer id) throws SimulatorExceptions.NetworkNodeException;

    /*a è influenzato da b?
    Boolean isAffectedBy(N a, N b);  Per chiedere se sono connessi non  è necessario sapere il tipo di nodo
                                                     infatti valuterei la possibilità di mettere come parametri (Node<?,?> a,
	 												Node<?,?> b)*/

    Graph<N, DefaultEdge> asGraph();

    B newInstance(Graph<N, DefaultEdge> graph);

    B getThis();

    default BNBuilderImpl<N,B> modifyFromThis(){
        return new BNBuilderImpl<>(getThis());
    }

    List<N> getIncomingNodes(N node);

    List<N> getOutgoingNodes(N node);

    Integer getInDegree(N node);

    Integer getOutDegree(N node);

    /**
     * Return the number of nodes with selfloop
     * @return
     */
    default Integer numberOfNodeWithSelfloops() {
        return (int) getNodes().stream().filter(x -> this.getIncomingNodes(x).contains(x)).count();
    }

    /* Static methods */

    public static List<Boolean> generateExactBiasOutcomes(int totalOutcomesNumber, double bias, Random randomInstance) {
        List<Boolean> outcomeList;

        int ones = (int) Math.round((totalOutcomesNumber) * bias);
        int zeros = totalOutcomesNumber - ones;

        outcomeList = new ArrayList<Boolean>(Collections.nCopies(ones, true));
        outcomeList.addAll(new ArrayList<Boolean>(Collections.nCopies(zeros, false)));

        Collections.shuffle(outcomeList, randomInstance);
        return outcomeList;
    }

    public static <K, Boolean, N extends NodeDeterministic<K,Boolean>> double computeActualAverageBias(BNClassic<?, Boolean, N,?> bn) {
        Double average = bn.getNodes().stream().mapToDouble(x -> x.getFunction().getRows().stream()
                .mapToDouble(
                        y -> {
                            return (y.getOutput().equals(java.lang.Boolean.TRUE)) ? Double.valueOf(1.0) : Double.valueOf(0.0);
                        }
                ).average().getAsDouble()
        ).average().getAsDouble();
        return average;
    }

    public static <N extends Node> double computeActualAverageIncomingNodes(BooleanNetwork<N,?> bn) {
        Double average = bn.getNodes().stream().mapToInt(x -> bn.getIncomingNodes(x).size()).average().getAsDouble();
        return average;
    }

    public static <N extends Node> double computeAverageNumberSelfLoopsPerNode(BooleanNetwork<N,?> bn) {
        return bn.getNodes().stream().mapToInt(node -> {
                    if (bn.getIncomingNodes(node).stream().anyMatch(x -> x.equals(node))) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        ).average().getAsDouble();
    }

    public static <K, Boolean, N extends NodeDeterministic<K,Boolean>> String getBNFileRepresentation(BNClassic<K,Boolean,N,?> bn) {
        List<N> nodesList = bn.getNodes();
        List<N> incomingNodes = null;
        List<Row<K, Boolean>> rowsTruthTable = null;

        StringWriter writer = null;
        writer = new StringWriter();
        /*
         * Topology section
         */
        writer.append("Topology:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            incomingNodes = bn.getIncomingNodes(node);
            for (int i = 0; i < incomingNodes.size(); i++) {
                writer.write((i != (incomingNodes.size() - 1)) ? incomingNodes.get(i).getId() + "," : incomingNodes.get(i).getId() + "");
            }
            writer.append(Files.NEW_LINE);
        }
        /*
         * Functions section
         */
        writer.append("Functions E:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            rowsTruthTable = node.getFunction().getRows();
            Boolean outputValue;
            for (int i = 0; i < rowsTruthTable.size(); i++) {
                outputValue = rowsTruthTable.get(i).getOutput();
                //if (outputValue instanceof Boolean) {
                writer.write((outputValue.equals(true)) ? "1" : "0");
                //} else {
                //	writer.write(outputValue.toString());
                //}
            }
            writer.append(Files.NEW_LINE);

        }

        /*
         * Names section
         */
        writer.append("Names:");
        writer.append(Files.NEW_LINE);
        for (N node : nodesList) {
            writer.write(node.getId() + ":");
            writer.write(node.getName());
            writer.append(Files.NEW_LINE);
        }

        return writer.toString();
    }
}
