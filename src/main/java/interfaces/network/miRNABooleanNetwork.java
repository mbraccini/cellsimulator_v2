package interfaces.network;


import java.util.List;

public interface miRNABooleanNetwork<   N extends Node,
                                        B extends BooleanNetwork<N>
                                      , M extends Node> //in linea di principio il miRNA può essere di un altro tipo di nodo
                                        extends BooleanNetwork<N> {

    /**
     * Unmodified Network connected to the miRNA network
     * @return
     */
    B getWrappedBooleanNetwork();

    /**
     * miRNA nodes number
     * @return
     */
    Integer miRNANumber();

    /**
     * miRNA nodes.
     * @return
     */
    List<M> miRNANodes();

    /**
     * Nodes influenced by the miRNA nodes.
     * @return
     */
    List<N> miRNADownstreamNodes();
}
