package network;

import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;

public class BNClassicBuilder
                <K,V,NODE extends NodeDeterministic<K,V>>
                extends AbstractBNBuilder<NODE,BNClassic<K,V,NODE>,BNClassicBuilder<K,V,NODE>> {


    public BNClassicBuilder(BNClassic<K, V, NODE> bn) {
        super(bn);
    }

    @Override
    protected BNClassicBuilder<K, V, NODE> self() {
        return this;
    }

    @Override
    public BNClassic<K, V, NODE> build() {
        return (BNClassic<K, V, NODE>) bn.newInstance(nodes, newTopology);
    }
}



