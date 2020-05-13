package exceptions;

public abstract class SimulatorExceptions extends RuntimeException {

    public static abstract class NetworkNodeException extends SimulatorExceptions {
        public static class NodeNotPresentException extends NetworkNodeException { }
        public static class NodeAlreadyPresentException extends NetworkNodeException { }
        public static class ReconfiguringNodeException extends NetworkNodeException { }
        public static class NodeIdMismatch extends NetworkNodeException { }
        public static class FunctionTopologyMismatch extends NetworkNodeException { }
        public static class BooleanNetworkNodeIdConfigurationException extends NetworkNodeException { }
        public static class IncomingArcAlredyPresent extends NetworkNodeException { }
        public static class NodesAndTopologyMismatch extends NetworkNodeException { }




    }

}
