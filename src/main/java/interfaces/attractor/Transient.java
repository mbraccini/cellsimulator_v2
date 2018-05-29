package interfaces.attractor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Transient<T> {

    Optional<List<T>> getStates();

    Integer getLength();
}
