package interfaces.tes;

import interfaces.state.State;

import java.util.List;

public interface TESDifferentiationTree<T extends State, E extends Tes<T>> extends DifferentiationTree<E>{

    /**
     * Ordered thresholds used to construct the differentiation tree.
     * @return thresholds
     */
    List<Double> getThresholds(); //DA mettere nell'interfaccia, ancora da creare, DifferentiationTEStRee
}
