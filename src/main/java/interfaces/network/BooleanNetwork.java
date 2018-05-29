package interfaces.network;

import exceptions.SimulatorExceptions;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface BooleanNetwork<N extends Node>{

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

    /*B newInstance(Graph<N, DefaultEdge> graph);

    B getThis();

    default BNBuilderImpl<N,B> modifyFromThis(){
        return new BNBuilderImpl<>(getThis());
    }*/


    List<N> getIncomingNodes(N node);

    List<N> getOutgoingNodes(N node);

    Integer getInDegree(N node);

    Integer getOutDegree(N node);

    /**
     * NodeId, IncomingNodes Id
     * @return
     */
    Map<Integer, List<Integer>> topology();

    BooleanNetwork<N> newInstance(Set<N> nodes, Map<Integer, List<Integer>> topology);


    default Boolean hasSelfLoop() {
        return numberOfNodeWithSelfloops() > 0;
    }
    /**
     * Return the number of nodes with selfloop
     * @return
     */
    default Integer numberOfNodeWithSelfloops() {
        return (int) getNodes().stream().filter(x -> this.getIncomingNodes(x).contains(x)).count();
    }


    /**
     * Actual Average Incoming Nodes
     * @return
     */
    default double computeActualAverageIncomingNodes() {
        Double average = getNodes().stream().mapToInt(x -> getIncomingNodes(x).size()).average().getAsDouble();
        return average;
    }

    /**
     * computeAverageNumberSelfLoopsPerNode
     * @return
     */
    default double computeAverageNumberSelfLoopsPerNode() {
        return getNodes().stream().mapToInt(node -> {
                    if (getIncomingNodes(node).stream().anyMatch(x -> x.equals(node))) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
        ).average().getAsDouble();
    }






}
