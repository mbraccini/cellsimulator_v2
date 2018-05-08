package interfaces.sequences;

import java.math.BigInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface BoundedSequence<T> extends UnboundedSequence<T> {

    BigInteger cardinality();

    T limitValue();

}
