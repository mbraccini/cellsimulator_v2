package exceptions;

public abstract class SimulatorExceptions extends RuntimeException {

    public static abstract class NetworkNodeException extends SimulatorExceptions {
        public static class NodeNotPresentException extends SimulatorExceptions { }
        public static class NodeAlreadyPresentException extends SimulatorExceptions { }

    }

}
