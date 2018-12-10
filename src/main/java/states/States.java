package states;

import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
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

    /**
     * Hexadecimal String representation from a BitSet
     * @param bSet
     * @return
     */
    public static String bitSetToHex(BitSet bSet){
        return DatatypeConverter.printHexBinary(bSet.toByteArray());
    }

    /**
     * BitSet from an hexadecimal String representation
     * @param hex
     * @return
     */
    public static BitSet hexToBitSet(String hex){
        byte[] bArray = DatatypeConverter.parseHexBinary(hex);
        return BitSet.valueOf(bArray);
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


    // https://stackoverflow.com/questions/29526985/java-from-biginteger-to-bitset-and-back
    public static BitSet convertTo (BigInteger bi) {
        byte[] bia = bi.toByteArray();
        int l = bia.length;
        byte[] bsa = new byte[l+1];
        System.arraycopy(bia,0,bsa,0,l);
        bsa[l] = 0x01;
        return BitSet.valueOf(bsa);
    }

    public static BigInteger convertFrom (BitSet bs) {
        byte[] bsa = bs.toByteArray();
        int l = bsa.length-0x01;
        byte[] bia = new byte[l];
        System.arraycopy(bsa,0,bia,0,l);
        return new BigInteger(bia);
    }
}
