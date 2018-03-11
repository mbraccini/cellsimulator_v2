package interfaces.pipeline;

import interfaces.core.Lab;

import java.util.ArrayList;

public class ExperimentPipelineImpl<IN,OUT extends Result> extends PipelineImpl<IN,OUT> implements ExperimentPipeline<IN,OUT> {

    private Lab lab;

    protected ExperimentPipelineImpl(){
    }

    public static <M, N extends Result> Pipeline<M, N> start(Pipe<M, N> pipe, Lab lab) {
        ExperimentPipelineImpl<M,N> t = new ExperimentPipelineImpl<M,N>();
        t.pipes = new ArrayList<>();
        t.pipes.add(pipe);
        t.lab = lab;
        return t;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public OUT apply(IN input) {
        Object source = input;
        Result output = null;

        for (Pipe s: pipes) {
            output = (Result) s.apply(source);
            lab.publish(output); //publish to lab
            source = output;
        }

        return (OUT) output;
    }
}
