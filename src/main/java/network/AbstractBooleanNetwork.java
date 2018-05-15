package network;

import java.util.*;
import java.util.stream.Collectors;

import exceptions.SimulatorExceptions;
import interfaces.network.BooleanNetwork;
//import interfaces.network.BooleanNetworkBuilder;
import interfaces.network.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;
import utility.GenericUtility;

public abstract class AbstractBooleanNetwork<N extends Node> implements BooleanNetwork<N> {

	protected int nodesNumber;

	protected Graph<N, DefaultEdge> graph;

	public AbstractBooleanNetwork(Graph<N, DefaultEdge> graph){
		this.graph = new AsUnmodifiableGraph<>(graph);
		//this.graph = graph;
		this.nodesNumber = graph.vertexSet().size();
		//fromGraphToInternalStructures(graph);
	}

	/*private void fromGraphToInternalStructures(Graph<N,DefaultEdge> g){
		nodesList = new ArrayList<>(g.vertexSet()); //nodesList initialization
		List<Integer> incoming;
		for (N node : g.vertexSet()) {				//nodesMap initialization
			incoming = new ArrayList<>();
			for (DefaultEdge edge : g.incomingEdgesOf(node)){
				incoming.add(g.getEdgeSource(edge).getId());
			}
			nodesMap.put(node.getId(), incoming);
		}
	}*/

	/* Immutable */

	@Override
	public Integer getNodesNumber() {
		return this.nodesNumber;
	}

	@Override
	public List<N> getNodes() {
		return graph.vertexSet().stream().sorted(Comparator.comparingInt(N::getId)).collect(Collectors.toList());
	}
	
	@Override
	public N getNodeByName(String name) {
		return graph.vertexSet().stream().filter(x->x.getName().equals(name)).findFirst().orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
	}

	@Override
	public N getNodeById(Integer id) {
		return graph.vertexSet().stream().filter(x->x.getId().equals(id)).findFirst().orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
	}

	/*@Override
	public void reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeIndex) {
		/*
		GraphBuilder<N, DefaultEdge, DefaultDirectedGraph<N,DefaultEdge>> builder
				= new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));
		Graph<N, DefaultEdge> newGraph = builder.addGraph(graph).build();

		if (Objects.nonNull(newGraph.removeEdge(getNodeById(incomingNodeIndex), getNodeById(targetNodeId)))){
			if (Objects.isNull(newGraph.addEdge(getNodeById(newInputNodeId), getNodeById(targetNodeId)))){
				throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
			}
		} else {
			throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
		}

		return ;

	}*/

	@Override
	public Graph<N, DefaultEdge> asGraph() {
		return new AsUnmodifiableGraph<>(graph);
		//return graph;
	}

	@Override
	public List<N> getIncomingNodes(N node) {
		List<N> incoming = new ArrayList<>();
		for (DefaultEdge edge : graph.incomingEdgesOf(node)){
			incoming.add(graph.getEdgeSource(edge));
		}
		return incoming;
	}

	@Override
	public List<N> getOutgoingNodes(N node) {
		List<N> outgoing = new ArrayList<>();
		for (DefaultEdge edge : graph.outgoingEdgesOf(node)){
			outgoing.add(graph.getEdgeTarget(edge));
		}
		return outgoing;
	}

	@Override
	public Integer getInDegree(N node) {
		 return graph.inDegreeOf(node);
	}

	@Override
	public Integer getOutDegree(N node) {
		return graph.outDegreeOf(node);
	}

	@Override
	public String toString() {
		return "----["+this.getClass().getSimpleName() +  "]----\n" +
							getNodes()
							.stream()
							.map( r-> {
							return r.toString()
							+ " <--- "+ getIncomingNodes(r);}).collect(Collectors.joining(" \n"));
	
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractBooleanNetwork<?> that = (AbstractBooleanNetwork<?>) o;
		return nodesNumber == that.nodesNumber &&
				Objects.equals(graph, that.graph);
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodesNumber, graph);
	}





	public static abstract class AbstractBuilder
					<NODE extends Node,
					BN extends BooleanNetwork<NODE>,
					SUB extends AbstractBuilder<NODE,BN,SUB>> {

		protected int[][] adj;
		protected int[][] adjToModify;
		protected Graph<NODE, DefaultEdge> inProgress;
		public AbstractBuilder(BN bn) {

			GraphBuilder<NODE, DefaultEdge, DefaultDirectedGraph<NODE,DefaultEdge>> builder
					= new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));
			this.inProgress = builder.addGraph(bn.asGraph()).build();
			initializeAdjacencyMatrix();
		}

		private void initializeAdjacencyMatrix(){
			adj = new int[inProgress.vertexSet().size()]
							[inProgress.vertexSet().size()];

			adjToModify = new int[inProgress.vertexSet().size()]
					[inProgress.vertexSet().size()];

			for (NODE n: inProgress.vertexSet()) {
				for (DefaultEdge e : inProgress.incomingEdgesOf(n)){
					adj[n.getId()][inProgress.getEdgeSource(e).getId()] = 1;
					adjToModify[n.getId()][inProgress.getEdgeSource(e).getId()] = 1;
				}
			}

		}

		public SUB reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeId) throws SimulatorExceptions.NetworkNodeException {

			adjToModify[targetNodeId][newInputNodeId] = 1;
			adjToModify[targetNodeId][incomingNodeId] = 0;

			/*if (Objects.nonNull(inProgress.removeEdge(incomingNode, targetNode))){
				if (Objects.isNull(inProgress.addEdge(newInputNode, targetNode))){
					throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
				}
			} else {
				throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
			}*/
			return self();
		}


		protected abstract SUB self();
		public abstract BN build();
	}

}
