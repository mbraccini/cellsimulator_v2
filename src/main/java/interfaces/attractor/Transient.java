package interfaces.attractor;

import java.util.Optional;
import java.util.stream.Stream;

public interface Transient<T> {

    Optional<Stream<T>> getStates();

    Integer getLength();
}
