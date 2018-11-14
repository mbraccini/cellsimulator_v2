package generator;

import interfaces.sequences.Generator;
import interfaces.state.State;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BagOfStatesGenerator<T extends State> implements Generator<T> {

    final protected Collection<T> states;
    final private Iterator<T> iterator;

    public BagOfStatesGenerator(Collection<T> states){
        this.states = states;
        this.iterator = states.iterator();
    }

    @Override
    public T nextSample() {
        if (this.iterator.hasNext()) {
            return this.iterator.next();
        }
        return null;
    }

    @Override
    public BigInteger totalNumberOfSamplesToBeGenerated() {
        return BigInteger.valueOf(states.size());
    }
}
