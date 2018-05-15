package network;

import interfaces.network.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;


public class BNClassicImpl<K,V, N extends NodeDeterministic<K,V>>
            extends AbstractBooleanNetwork<N,BNClassicImpl<K,V,N>>
            implements BNClassic<K,V,N,BNClassicImpl<K,V,N>>{


    protected Context<K,V> context;

    public BNClassicImpl(Graph<N, DefaultEdge> graph) {
        super(graph);
        createContext();
    }



    private void createContext(){
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

    @Override
    public BNClassicImpl<K, V, N> newInstance(Graph<N, DefaultEdge> graph) {
        return new BNClassicImpl<>(graph);
    }


    @Override
    public BNClassicImpl<K, V, N> getThis() {
        return this;
    }
}