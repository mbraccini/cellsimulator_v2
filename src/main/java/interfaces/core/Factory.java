package interfaces.core;


@FunctionalInterface
public interface Factory<T> {

    /**
     * Create a new instance of type T.
     * @return
     */

    T newInstance();

}
