package interfaces.networkdescription;

public interface ExplicitFunExpr {

    /**
     * Node index
     * @return
     */
    String getNode();

    /**
     * String description of the Boolean function, e.g. "0100":
     * x2 x1 | t+1
     * .. .. | 0
     * .. .. | 1
     * .. .. | 0
     * .. .. | 0
     * @return
     */
    String getOutput();
}
