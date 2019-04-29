package interfaces.dynamic;

import interfaces.state.State;

import java.util.function.Function;

/**
 * Highly inspired from these tutorials: https://blog.codefx.org/design/patterns/decorator-pattern-java-8/
 * and https://codingjam.it/decorator-patten-corretto-lambda-con-java-8/
 * @param <T>
 */
public interface DecoratingDynamics<T extends State> extends Dynamics<T> {

    static <T extends State> DecoratingDynamics<T> from(Dynamics<T> dyn){
        return state -> dyn.apply(state);
    }


    default DecoratingDynamics<T> decorate(
            Function<? super DecoratingDynamics<T>, ? extends DecoratingDynamics<T>> decorator){
        return decorator.apply(this);
    }

}
