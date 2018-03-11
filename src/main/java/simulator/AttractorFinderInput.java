package simulator;

import interfaces.dynamic.Dynamics;
import interfaces.pipeline.Result;
import interfaces.sequences.Generator;
import interfaces.state.State;

public interface AttractorFinderInput<T extends State> {

    Generator<T> generator();

    Dynamics<T> dynamics();

    static class AttractorFinderInputImpl<T extends State> implements AttractorFinderInput<T>{

        private final Generator<T> gen;
        private final Dynamics<T> dyn;


        public AttractorFinderInputImpl(Generator<T> gen , Dynamics<T> dyn) {
            this.gen = gen;
            this.dyn = dyn;
        }

        @Override
        public Generator<T> generator() {
            return gen;
        }

        @Override
        public Dynamics<T> dynamics() {
            return dyn;
        }
    }
}

