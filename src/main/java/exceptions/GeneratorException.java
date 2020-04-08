package exceptions;

public abstract class GeneratorException extends RuntimeException {

    public static abstract class FixedGeneratorException extends SimulatorExceptions {

        public static class IndicesValuesDimensionsMismatch extends GeneratorException {}
    }
}
