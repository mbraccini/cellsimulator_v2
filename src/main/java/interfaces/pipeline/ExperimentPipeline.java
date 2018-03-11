package interfaces.pipeline;

import interfaces.core.Lab;

public interface ExperimentPipeline<IN,OUT extends Result> extends Pipeline<IN,OUT> {

    static <M, N extends Result> Pipeline<M, N> start(Pipe<M, N> pipe, Lab lab) {
        return ExperimentPipelineImpl.start(pipe, lab);
    }


}
