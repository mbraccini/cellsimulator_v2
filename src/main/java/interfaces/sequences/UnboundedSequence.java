package interfaces.sequences;

import io.reactivex.Observable;
import org.jooq.lambda.Seq;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface UnboundedSequence<T> extends Iterable<T> {

    /*NO default Stream<T> stream() {
        final SequenceIterator<T> s = iterator();
        return Stream.generate( () -> s.next());
    }*/



    default Stream<T> stream() {
       // final SequenceIterator<T> s = iterator();
        return StreamSupport.stream(spliterator(), false);
    }

    default Seq<T> seq() {
        return Seq.seq(iterator());
    }


    default Observable<T> observable() {
        return Observable.fromIterable(this);
    }


    @Override
    Iterator<T> iterator();
}
