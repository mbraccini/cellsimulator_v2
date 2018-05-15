package network;

import exceptions.SimulatorExceptions;
import interfaces.network.*;
import io.vavr.Tuple2;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BNClassicImpl<K,V, N extends NodeDeterministic<K,V>>
            extends AbstractBooleanNetwork<N>
            implements BNClassic<K,V,N>{


    protected Context<K,V> context;

    public BNClassicImpl(Graph<N, DefaultEdge> graph) {
        super(graph);
        checkVariablesNumberIncomingNodes();
        createContext();
    }

    /**
     * We check if the node as the same incoming nodes number of its function table.
     */
    private void checkVariablesNumberIncomingNodes() {
        for (N node: graph.vertexSet()) {
            if (graph.incomingEdgesOf(node).size() > node.getFunction().getVariablesNumber()) {
                throw new SimulatorExceptions.NetworkNodeException.FunctionTopologyMismatch();
            }
        }
    }

    private void createContext() {
        ContextImpl.ContextBuilder<K,V> builder = ContextImpl.<K,V>builder();
        for (N node: super.getNodes()) {
            builder.add(node.getId(), node.getFunction());
        }
        context = builder.build();
    }

    @Override
    public Context<K, V> context() {
        return context;
    }


    @Override
    public String toString() {
        return super.toString()
                    + "\n"
                    + "Context:\n"
                    + context;
    }

    public static <K,V,NODE extends NodeDeterministic<K,V>> ClassicBuilder<K,V,NODE> from(BNClassic<K,V,NODE> bn){
        return new ClassicBuilder<>(bn);
    }

    public static class ClassicBuilder
            <K,V,NODE extends NodeDeterministic<K,V>>
            extends AbstractBuilder<NODE,BNClassic<K,V,NODE>,ClassicBuilder<K,V,NODE>> {

        protected Map<Integer, NODE> nodeSubstitutions = new HashMap<>();

        private ClassicBuilder(BNClassic<K,V,NODE> bn) {
            super(bn);
        }

        protected ClassicBuilder<K, V, NODE> changeNode(Integer outId, NODE in) {
            if (outId != in.getId().intValue()) {
                throw new SimulatorExceptions.NetworkNodeException.NodeIdMismatch();
            }
            nodeSubstitutions.put(outId,in);
            return self();
        }

        @Override
        protected ClassicBuilder<K, V, NODE> self() {
            return this;
        }

        @Override
        public BNClassic<K,V,NODE> build() {
            checkSubstitutions();
            checkModification();

            //
            return new BNClassicImpl<K,V,NODE>(inProgress);
        }


        private void checkModification() {
            for (int i = 0; i < adjToModify.length; i++) {
                for (int j = 0; j < adjToModify[i].length; j++) {
                    if (adj[i][j] != adjToModify[i][j]){
                        if (adjToModify[i][j] == 0) {   //arco rimosso
                            inProgress.removeEdge(
                                    retrieveNode(j),retrieveNode(i));
                        } else {                        //arco aggiunto
                            inProgress.addEdge(
                                    retrieveNode(j),retrieveNode(i));
                        }
                    }
                }
            }
        }

        private void checkSubstitutions() {
            for (Map.Entry<Integer, NODE> entry : nodeSubstitutions.entrySet()) {
                NODE out = retrieveNode(entry.getKey());
                List<Tuple2<NODE,NODE>> edges = new ArrayList<>(); //source,target

                for (DefaultEdge edge: inProgress.edgesOf(out)) {
                    edges.add(new Tuple2<>(inProgress.getEdgeSource(edge),inProgress.getEdgeTarget(edge)));
                }

                System.out.println(edges);
                inProgress.removeVertex(out);
                inProgress.addVertex(entry.getValue());

                for (Tuple2<NODE,NODE> edge: edges) {
                    if (edge._1().getId().intValue() == out.getId().intValue()) {       //ifSource
                        inProgress.addEdge(entry.getValue(), edge._2());
                    } else if(edge._2().getId().intValue() == out.getId().intValue()){  //ifTarget
                        inProgress.addEdge(edge._1(), entry.getValue());
                    }
                }
            }


        }

        private NODE retrieveNode(int id){
            return inProgress.vertexSet().stream().filter(x-> x.getId() == id).findFirst().get();
        }
    }



}