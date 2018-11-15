package states;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class States {
    private States() {
    }

    /**
     * Adds one to the BitSet object
     *
     * @param bitset
     */
    public static void addOneToBitSet(BitSet bitset) {
        int i = 0;
        for (; i < bitset.length(); i++) {
            if (bitset.get(i)) {
                bitset.clear(i);
            } else {
                break;
            }
        }
        bitset.set(i);

    }


    /**
     * Converts a BitSet into a long value.
     * @param bits
     * @return
     */
    public static long convert(BitSet bits) {
        long value = 0L;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }


    /**
     * Converts a long value into a new BitSet object.
     * @param value
     * @param numBits
     * @return
     */
    public static BitSet convert(long value, int numBits) {
        BitSet bits = new BitSet(numBits);
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

    public static List<Boolean> fromBitSetToBooleans(long value, int numBits){
        List<Boolean> l = new ArrayList<>();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                l.add(Boolean.TRUE);
            } else {
                l.add(Boolean.FALSE);
            }
            ++index;
            value = value >>> 1;
        }

        if (l.size() < numBits) { //trailing zeros
            int n = numBits - l.size();
            for (int i = 0; i < n; i++) {
                l.add(Boolean.FALSE);
            }
        }
        return l;
    }
}
