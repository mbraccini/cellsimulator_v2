package interfaces.network;

public interface BNKBias<K, V, N extends NodeDeterministic<K,V>> extends BNClassic<K, V, N> {


    enum BiasType {
        EXACT, CLASSICAL
    }

    /**
     * The K parameter
     * @return
     */
    default Integer K() {
        return context().context().get(0).getVariablesNumber();
    }

    /**
     * The nominal bias of the BN.
     */
    double nominalBias();


    /**
     * Returns the bias type that the BN implements
     * @return
     */
    BiasType getBiasType();
}
