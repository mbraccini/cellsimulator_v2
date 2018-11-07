package interfaces.dynamic;

import interfaces.state.State;

import java.util.function.Function;

public interface DecoratingDynamics<T extends State> extends Dynamics<T> {

    static <T extends State> DecoratingDynamics<T> from(Dynamics<T> dyn){
        return state -> dyn.apply(state);
    }


    default DecoratingDynamics<T> decorate(
            Function<? super DecoratingDynamics<T>, ? extends DecoratingDynamics<T>> decorator){
        return decorator.apply(this);
    }

}
