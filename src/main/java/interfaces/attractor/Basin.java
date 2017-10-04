package interfaces.attractor;

import java.util.Optional;
import java.util.Set;

public interface Basin<T> {

    Optional<Set<T>> getStates();

    Integer getDimension();

}
