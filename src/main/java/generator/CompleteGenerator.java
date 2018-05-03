package generator;

import interfaces.sequences.Generator;
import interfaces.state.BinaryState;
import states.ImmutableBinaryState;
import states.States;

import java.math.BigInteger;
import java.util.BitSet;

public class CompleteGenerator implements Generator<BinaryState>{

    private final int nodesNumber;
    private BigInteger start;
    private final BigInteger end;
    private BitSet bitSetState;
    private BinaryState binaryState;

    public CompleteGenerator(int nodesNumber) {
        this.nodesNumber = nodesNumber;
        this.start = BigInteger.ZERO;

        this.end = BigInteger.valueOf(2).pow(nodesNumber);

        this.bitSetState = new BitSet(nodesNumber);
    }

    @Override
    public BinaryState nextSample() {
        if (this.start.compareTo(this.end) < 0) { //corrisponde a if (this.sample < this.combinations){
            this.start = this.start.add(BigInteger.ONE);
            this.binaryState = new ImmutableBinaryState(this.nodesNumber, this.bitSetState);
            States.addOneToBitSet(this.bitSetState);
            return this.binaryState;
        }
        return null;
    }

    @Override
    public BigInteger totalNumberOfSamplesToBeGenerated() {
        return this.end;
    }

/*
    @Override
    public BinaryState get() {
        return nextSample();
    }


    Stream<BinaryState> values() {
        return StreamSupport.stream(new GeneratorSplitIterator(), false);
    }

    @Override
    public BigInteger cardinality() {
        return null;
    }

    @Override
    public BinaryState limitValue() {
        return null;
    }

    //@Override
    public Boolean hasNext() {
        return null;
    }

    //@Override
    public BinaryState next() {
        return nextSample();
    }

    private class GeneratorSplitIterator extends Spliterators.AbstractSpliterator<BinaryState> {


        protected GeneratorSplitIterator() {
            super(end.longValue(), 0);
        }

        @Override
        public boolean tryAdvance(Consumer<? super BinaryState> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            if (start.compareTo(end) >= 0) { //corrisponde a if (this.sample >= this.combinations){

                return false;
            }

            action.accept(nextSample());

            return true;
        }
    }

    /*@Override
    public Stream<BinaryState> stream() {
        return StreamSupport.stream(new GeneratorSplitIterator(), false);
    }

    @Override
    public SequenceIterator<BinaryState> iterator() {
        return new SequenceIteratorImpl(nodesNumber);
    }

    private class SequenceIteratorImpl implements SequenceIterator<BinaryState>{
        private final int nodesNumber;
        private BigInteger start;
        private final BigInteger end;
        private BitSet bitSetState;
        private BinaryState binaryState;

        public SequenceIteratorImpl(int nodesNumber) {
            this.nodesNumber = nodesNumber;
            this.start = BigInteger.ZERO;

            this.end = BigInteger.valueOf(2).pow(nodesNumber);

            this.bitSetState = new BitSet(nodesNumber);
        }

        @Override
        public BinaryState next() {
                this.start = this.start.add(BigInteger.ONE);
                this.binaryState = new ImmutableBinaryState(this.nodesNumber, this.bitSetState);
                States.addOneToBitSet(this.bitSetState);
                return this.binaryState;
        }

        @Override
        public boolean hasNext() {
            if (this.start.compareTo(this.end) >= 0) { //corrisponde a if (this.sample < this.combinations){
                return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {



        CompleteGenerator gen = new CompleteGenerator(3);

        //Stream.generate(gen).limit(10).forEach(System.out::println);

        //gen.values().forEach(System.out::println);


        //gen.values().forEach(System.out::println);


        Iterable<BinaryState> iterable1 = new Iterable<BinaryState>() {
            @Override
            public Iterator<BinaryState> iterator() {
                return gen.values().iterator();
            }
        };
        Iterable<BinaryState> iterable2 = new Iterable<BinaryState>() {
            @Override
            public Iterator<BinaryState> iterator() {
                return gen.values().iterator();
            }
        };
        Flowable<BinaryState> f1= Flowable.<BinaryState>fromIterable(iterable1);
        Flowable<BinaryState> f2= Flowable.<BinaryState>fromIterable(iterable2);



        f1.subscribe(System.out::println);

        UnboundedSequence<BinaryState> seq1 = new CompleteGenerator(2);
        //UnboundedSequence<BinaryState> seq2 = new CompleteGenerator(3);

        //seq1.seq().zip(seq2.seq()).forEach(System.out::println);



        System.out.println("o");

        seq1.seq().zip(seq1.stream()).forEach(System.out::println);

        seq1.observable().zipWith(seq1.observable(), (x,y)-> x.toString() + "/" + y.toString()).subscribe(System.out::println);
    }
*/

}

