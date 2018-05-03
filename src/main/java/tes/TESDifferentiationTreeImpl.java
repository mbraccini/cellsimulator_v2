package tes;

import interfaces.state.State;
import interfaces.tes.DifferentiationNode;
import interfaces.tes.TESDifferentiationTree;
import interfaces.tes.Tes;

import java.util.List;

public class TESDifferentiationTreeImpl<T extends State, E extends Tes<T>> extends DifferentiationTreeImpl<E> implements TESDifferentiationTree<T, E> {

    protected List<Double> thresholds;

    public TESDifferentiationTreeImpl(List<DifferentiationNode<E>> rootLevel, List<Double> thresholds){
        super(rootLevel);
        this.thresholds = thresholds;
    }

    @Override
    public List<Double> getThresholds() {
        return this.thresholds;
    }
}
