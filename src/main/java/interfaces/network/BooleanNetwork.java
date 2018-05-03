package interfaces.network;

import exceptions.SimulatorExceptions;
import utility.Files;

import java.io.StringWriter;
import java.util.*;

public interface BooleanNetwork<K, V> { //OK

    /* Immutable */

    Integer getNodesNumber();

    List<Node<K, V>> getNodes();

    Optional<Node<K, V>> getNodeByName(String name);

    Optional<Node<K, V>> getNodeById(Integer id);

    //a è influenzato da b?
    Boolean isAffectedBy(Node<?, ?> a, Node<?, ?> b); /* Per chiedere se sono connessi non  è necessario sapere il tipo di nodo
                                                     infatti valuterei la possibilità di mettere come parametri (Node<?,?> a,
	 												Node<?,?> b)*/

    //Boolean reconfigureIncomingEdge(Node<K, V> targetNode, Node<K, V> oldInputNode, Node<K, V> newInputNode); //tolgo un arco e metto un altro arco proveniente da un altro nodo
    void reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeIndex) throws SimulatorExceptions.NetworkNodeException;
    // tolgo oldInput e allo stesso posto inserisco newInput nella map

    List<Node<K, V>> getIncomingNodes(Node<K, V> node);

    List<Node<K, V>> getOutcomingNodes(Node<K, V> node);

    Integer getInDegree(Node<?, ?> node);

    Integer getOutDegree(Node<?, ?> node);

    Properties getNetworkProperties();

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

    public static double computeActualAverageBias(BooleanNetwork<?, Boolean> bn) {
        Double average = bn.getNodes().stream().mapToDouble(x -> x.getFunction().getRows().stream()
                .mapToDouble(
                        y -> {
                            return (y.getOutput() == true) ? Double.valueOf(1.0) : Double.valueOf(0.0);
                        }
                ).average().getAsDouble()
        ).average().getAsDouble();
        return average;
    }

    public static <K> double computeActualAverageIncomingNodes(BooleanNetwork<K, Boolean> bn) {
        Double average = bn.getNodes().stream().mapToInt(x -> bn.getIncomingNodes(x).size()).average().getAsDouble();
        return average;
    }

    public static <K, V> double computeAverageNumberSelfLoopsPerNode(BooleanNetwork<K, V> bn) {
        return bn.getNodes().stream().mapToInt(node -> {
                    if (bn.getIncomingNodes(node).stream().anyMatch(x -> x.equals(node))) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        ).average().getAsDouble();
    }

    public static <K, Boolean> String getBNFileRepresentation(BooleanNetwork<K, Boolean> bn) {
        List<Node<K, Boolean>> nodesList = bn.getNodes();
        List<Node<K, Boolean>> incomingNodes = null;
        List<Row<K, Boolean>> rowsTruthTable = null;

        StringWriter writer = null;
        writer = new StringWriter();
        /*
         * Topology section
         */
        writer.append("Topology:");
        writer.append(Files.NEW_LINE);
        for (Node<K, Boolean> node : nodesList) {
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
        for (Node<K, Boolean> node : nodesList) {
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
        for (Node<K, Boolean> node : nodesList) {
            writer.write(node.getId() + ":");
            writer.write(node.getName());
            writer.append(Files.NEW_LINE);
        }

        return writer.toString();
    }
}
