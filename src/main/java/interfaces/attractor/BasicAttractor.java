package interfaces.attractor;

public interface BasicAttractor {

    /**
     * Attractor's length.
     * @return
     */
    Integer getLength();


    /**
     * If it is a fixed point
     * @return
     */
    default boolean isFixedPoint() {
        return getLength() == 1;
    }


}
