package interfaces.attractor;

import java.util.Optional;
import java.util.Set;

public interface Basin<T> {

    /**
     * For performance purposes we can store only the basin dimension.
     * @return
     */
    Optional<Set<T>> getStates();

    Integer getDimension();

}
