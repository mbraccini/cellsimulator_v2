package exceptions;

public abstract class DynamicsException extends RuntimeException {

        public static abstract class KnockOutKnockInDynamics extends SimulatorExceptions {
            public static class InitialNodeValueNotSet extends KnockOutKnockInDynamics{}
        }
}
