package network;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exceptions.SimulatorExceptions;
import interfaces.network.BooleanNetwork;
import interfaces.network.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public abstract class AbstractBooleanNetwork<N extends Node> implements BooleanNetwork<N> {


	List<N> nodesList = new ArrayList<>();
	protected Map<Integer, List<Integer>> incomingNodesMap = new HashMap<>();

	protected AbstractBooleanNetwork(){ }

	protected AbstractBooleanNetwork(List<N> nodesList, Map<Integer, List<Integer>> map){
		this.nodesList = nodesList;
		this.incomingNodesMap = map;
		check();
	}


	/**
	 * NodesList must have node ids from 0 to N-1
	 */
	protected void check() {
		IntStream.range(0, nodesList.size())
					.forEach(x -> nodesList.stream()
									.filter(y -> y.getId() == x)
									.findAny()
									.orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.BooleanNetworkNodeIdConfigurationException()));

		for (List<Integer> incoming : incomingNodesMap.values()) {   //check that all incoming nodes are distinct, no more than one arc of the same type
			if (new HashSet<>(incoming).size() < incoming.size()) {
				throw new SimulatorExceptions.NetworkNodeException.IncomingArcAlredyPresent();
			}
		}
	}


	/* Immutable */

	@Override
	public Integer getNodesNumber() {
		return nodesList.size();
	}

	@Override
	public List<N> getNodes() {
		return nodesList.stream().sorted(Comparator.comparingInt(N::getId)).collect(Collectors.toList());
	}
	
	@Override
	public N getNodeByName(String name) {
		return nodesList.stream().filter(x->x.getName().equals(name)).findFirst().orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
	}

	@Override
	public N getNodeById(Integer id) {
		return nodesList.stream().filter(x->x.getId().equals(id)).findFirst().orElseThrow(() -> new SimulatorExceptions.NetworkNodeException.NodeNotPresentException());
	}


	@Override
	public Graph<N, DefaultEdge> asGraph() {
		Graph<N, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		nodesList.stream().forEach(x -> graph.addVertex(x)); 								//addToTheGraph
		for (Map.Entry<Integer, List<Integer>> incoming : incomingNodesMap.entrySet()) {	//addIncomingNodes
			for (Integer i: incoming.getValue()) {
				graph.addEdge(getNodeById(incoming.getKey()), getNodeById(i));
			}
		}
		return new AsUnmodifiableGraph<>(graph);
	}

	@Override
	public List<N> getIncomingNodes(N node) {
		return incomingNodesMap.get(node.getId()).stream().map(x -> nodesList.get(x)).collect(Collectors.toList());
	}

	@Override
	public List<N> getOutgoingNodes(N node) {
		return this.incomingNodesMap.entrySet().stream()
									.filter(x->x.getValue().stream().anyMatch(n->n.equals(node.getId())))
									.map(entry->getNodeById(entry.getKey())).collect(Collectors.toList());
	}

	@Override
	public Integer getInDegree(N node) {
		return incomingNodesMap.get(node.getId()).size();
	}

	@Override
	public Integer getOutDegree(N node) {
		return Math.toIntExact(incomingNodesMap.entrySet().stream().filter(x->x.getValue().stream().anyMatch(n->n.equals(node))).map(entry->entry.getKey()).count());
	}


	@Override
	public Map<Integer, List<Integer>> topology(){
		// a copy of the map
		Map<Integer, List<Integer>> map = new HashMap<>();
		for (Map.Entry<Integer, List<Integer>> incoming : incomingNodesMap.entrySet()) {
			map.put(incoming.getKey(), new ArrayList<>(incoming.getValue()));
		}
		return map;
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
		//self check
		if (this == o)
			return true;

		// null check
		if (o == null)
			return false;

		// type check and cast
		if (!(o instanceof AbstractBooleanNetwork))
			return false;

		AbstractBooleanNetwork<?> that = (AbstractBooleanNetwork<?>) o;
		return Objects.equals(nodesList, that.nodesList) &&
				Objects.equals(incomingNodesMap, that.incomingNodesMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash( nodesList, incomingNodesMap);
	}
}


	/*@Override
	public void reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeIndex) {


		if (Objects.nonNull(graph.removeEdge(getNodeById(incomingNodeIndex), getNodeById(targetNodeId)))){
			if (Objects.isNull(graph.addEdge(getNodeById(newInputNodeId), getNodeById(targetNodeId)))){
				throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
			}
		} else {
			throw new SimulatorExceptions.NetworkNodeException.ReconfiguringNodeException();
		}
	}*/




//	public static abstract class AbstractBuilder
//					<NODE extends Node,
//					BN extends BooleanNetwork<NODE>,
//					SUB extends AbstractBuilder<NODE,BN,SUB>> {
//
//		protected int[][] adj;
//		protected int[][] adjToModify;
//		protected Graph<NODE, DefaultEdge> inProgress;
//		protected Map<Integer, NODE> nodeSubstitutions = new HashMap<>();
//
//		public AbstractBuilder(BN bn) {
//
//			GraphBuilder<NODE, DefaultEdge, DefaultDirectedGraph<NODE,DefaultEdge>> builder
//					= new GraphBuilder<>(new DefaultDirectedGraph<>(DefaultEdge.class));
//			this.inProgress = builder.addGraph(bn.asGraph()).build();
//			initializeAdjacencyMatrix();
//		}
//
//		private void initializeAdjacencyMatrix(){
//			adj = new int[inProgress.vertexSet().size()]
//							[inProgress.vertexSet().size()];
//
//			adjToModify = new int[inProgress.vertexSet().size()]
//					[inProgress.vertexSet().size()];
//
//			for (NODE n: inProgress.vertexSet()) {
//				for (DefaultEdge e : inProgress.incomingEdgesOf(n)){
//					adj[n.getId()][inProgress.getEdgeSource(e).getId()] = 1;
//					adjToModify[n.getId()][inProgress.getEdgeSource(e).getId()] = 1;
//				}
//			}
//
//		}
//
//		public SUB reconfigureIncomingEdge(Integer targetNodeId, Integer newInputNodeId, Integer incomingNodeId) throws SimulatorExceptions.NetworkNodeException {
//
//			adjToModify[targetNodeId][newInputNodeId] = 1;
//			adjToModify[targetNodeId][incomingNodeId] = 0;
//
//			return self();
//		}
//
//		protected void check(){
//			checkSubstitutions();
//			checkModification();
//		}
//
//		protected abstract SUB self();
//		public abstract BN build();
//
//
//		protected SUB changeNode(Integer outId, NODE in) {
//			if (outId != in.getId().intValue()) {
//				throw new SimulatorExceptions.NetworkNodeException.NodeIdMismatch();
//			}
//			nodeSubstitutions.put(outId,in);
//			return self();
//		}
//
//		private void checkModification() {
//			for (int i = 0; i < adjToModify.length; i++) {
//				for (int j = 0; j < adjToModify[i].length; j++) {
//					if (adj[i][j] != adjToModify[i][j]){
//						if (adjToModify[i][j] == 0) {   //arco rimosso
//							inProgress.removeEdge(
//									retrieveNode(j),retrieveNode(i));
//						} else {                        //arco aggiunto
//							inProgress.addEdge(
//									retrieveNode(j),retrieveNode(i));
//						}
//					}
//				}
//			}
//		}
//
//		private void checkSubstitutions() {
//			for (Map.Entry<Integer, NODE> entry : nodeSubstitutions.entrySet()) {
//				NODE out = retrieveNode(entry.getKey());
//				List<Tuple2<NODE,NODE>> edges = new ArrayList<>(); //source,target
//
//				for (DefaultEdge edge: inProgress.edgesOf(out)) {
//					edges.add(new Tuple2<>(inProgress.getEdgeSource(edge),inProgress.getEdgeTarget(edge)));
//				}
//
//				System.out.println(edges);
//				inProgress.removeVertex(out);
//				inProgress.addVertex(entry.getValue());
//
//				for (Tuple2<NODE,NODE> edge: edges) {
//					if (edge._1().getId().intValue() == out.getId().intValue()) {       //ifSource
//						inProgress.addEdge(entry.getValue(), edge._2());
//					} else if(edge._2().getId().intValue() == out.getId().intValue()){  //ifTarget
//						inProgress.addEdge(edge._1(), entry.getValue());
//					}
//				}
//			}
//
//
//		}
//
//		protected NODE retrieveNode(int id){
//			return inProgress.vertexSet().stream().filter(x-> x.getId() == id).findFirst().get();
//		}
//	}


