package simulator;

import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.dynamic.Dynamics;
import interfaces.pipeline.Result;
import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import interfaces.state.State;

public interface AttractorFinderOutput<T extends State> extends Result{

    ImmutableList<ImmutableAttractor<T>> attractors();
    Dynamics<T> dynamics();

    static class AttractorFinderOutputImpl<T extends State> implements AttractorFinderOutput<T>{

        private final ImmutableList<ImmutableAttractor<T>> att;
        private final Dynamics<T> dyn;


        public AttractorFinderOutputImpl(ImmutableList<ImmutableAttractor<T>> att , Dynamics<T> dyn) {
            this.att = att;
            this.dyn = dyn;
        }

        @Override
        public ImmutableList<ImmutableAttractor<T>> attractors() {
            return att;
        }

        @Override
        public Dynamics<T> dynamics() {
            return dyn;
        }

        @Override
        public String getStringRepresentation() {
            return "prova + \n" + att.toString();
        }

        @Override
        public String getFileRepresentation() {
            return null;
        }
    }

}
