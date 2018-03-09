package interfaces.pipeline;

import java.util.ArrayList;
import java.util.List;

public class PipelineImpl<IN,OUT> implements Pipeline<IN,OUT>{

    private List<Pipe<?,?>> pipes;

    private PipelineImpl() { }

    public static <M, N> Pipeline<M, N> start(Pipe<M, N> pipe) {
        PipelineImpl<M,N> t = new PipelineImpl<M,N>();
        t.pipes = new ArrayList<>();
        t.pipes.add(pipe);
        return t;
    }

    @Override
    public <V> Pipeline<IN, V> add(Pipe<OUT, V> pipe) {
        PipelineImpl<IN,V> pipeline = new PipelineImpl<IN,V>();
        pipeline.pipes = new ArrayList<>(pipes);
        pipeline.pipes.add(pipe);
        return pipeline;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public OUT apply(IN input) {
        Object source = input;
        Object output = null;

        for (Pipe s: pipes) {
           output = s.apply(source);
           source = output;
        }

        return (OUT) output;
    }


    public static void main(String arg[]) {
        Pipe<Boolean, Boolean> not = x -> !x;
        Pipe<Boolean, Boolean> identity = x -> x;
        Pipe<Boolean, Boolean> fal = x -> false;
        Pipe<Boolean, Boolean> tru = x -> true;

        Pipe<Boolean, String> trutru = x -> "1";
        Pipe<String, Integer> pru = x -> Integer.valueOf(x);


        Pipeline<Boolean, String> p = Pipeline.start(not).add(identity).add(trutru);
        Pipeline<String, Integer> p1 = Pipeline.start(pru);

        p.add(p1);

        System.out.println(p.apply(true));

    }


}
