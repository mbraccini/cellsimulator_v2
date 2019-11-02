package interfaces.state;

public interface RealState extends State {
    /**
     * Returns the value of the node.
     *
     * @param index
     * @return
     */
    Double getNodeValue(Integer index);
}



