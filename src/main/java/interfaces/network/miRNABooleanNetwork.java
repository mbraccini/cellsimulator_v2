package interfaces.network;


import java.util.List;

public interface miRNABooleanNetwork<K, V> extends BooleanNetwork<K, V> {

    /**
     * Unmodified Network connected to the miRNA network
     * @return
     */
    BooleanNetwork<K, V> getWrappedBooleanNetwork();

    /**
     * miRNA nodes number
     * @return
     */
    Integer miRNANumber();

    /**
     * miRNA nodes.
     * @return
     */
    List<Node<K,V>> miRNANodes();

    /**
     * Nodes influenced by the miRNA nodes.
     * @return
     */
    List<Node<K,V>> miRNADownstreamNodes();
}
