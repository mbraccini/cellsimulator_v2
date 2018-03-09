package interfaces.pipeline;

public interface Pipeline<IN, OUT> extends Pipe<IN, OUT> {

    static <M, N> Pipeline<M, N> start(Pipe<M, N> pipe) {
        return PipelineImpl.start(pipe);
    }

    <V> Pipeline<IN, V> add(Pipe<OUT, V> pipe);
}
