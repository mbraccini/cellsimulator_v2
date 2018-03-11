package interfaces.core;

import interfaces.pipeline.Pipeline;

public interface Experiment<IN, OUT> {


    Pipeline<IN, OUT> getPipeline();



}
