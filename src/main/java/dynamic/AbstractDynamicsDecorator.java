package dynamic;

import interfaces.dynamic.DecoratingDynamics;
import interfaces.dynamic.Dynamics;
import interfaces.state.State;

public abstract class AbstractDynamicsDecorator<T extends State> implements DecoratingDynamics<T> {

    protected Dynamics<T> dynamics;

    public AbstractDynamicsDecorator(Dynamics<T> dynamics){
        this.dynamics = dynamics;
    }

}
