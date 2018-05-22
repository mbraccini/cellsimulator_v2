package experiments.selfLoop;

import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import io.jenetics.*;
import io.jenetics.util.ISeq;
import io.jenetics.util.IntRange;
import io.jenetics.util.Seq;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Iterator;

/**
 * Custom chromosome to model a BN topology with no incoming nodes replication and, possibly, avoiding self loops.
 */
public class TopologyFixedKBNChromosome implements NumericChromosome<Integer, IntegerGene>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final boolean selfLoopEnabled;
    protected IntegerChromosome internalChromosome;

    /**
     * Dovrebbero essere a due a due diversi in modo tale da non avere 2 self loop dello stesso tipo, es 3: 3,3
     */
    int k;

    protected TopologyFixedKBNChromosome(ISeq<IntegerGene> genes, int k, boolean selfLoopEnabled) {
        checkTopologyFeasibility(genes.length(), k);

        IntegerGene[] tempGenes = new IntegerGene[genes.length()];
        this.internalChromosome = IntegerChromosome.of(genes.toArray(tempGenes));
        this.k = k;
        this.selfLoopEnabled = selfLoopEnabled;
    }

    protected TopologyFixedKBNChromosome(Integer min, Integer max, IntRange lengthRange, int k, boolean selfLoopEnabled) {
        if (lengthRange.size() != 1) { //cioè vogliamo cromosomi di lunghezza fissa
            throw new VariableChromosomeLengthException();
        }

        checkTopologyFeasibility(lengthRange.getMin(), k);

        this.internalChromosome = new IntegerChromosome(min, max, lengthRange);
        this.k = k;
        this.selfLoopEnabled = selfLoopEnabled;
    }

    public TopologyFixedKBNChromosome(Integer min, Integer max, int length, int k, boolean selfLoopEnabled) {

        checkTopologyFeasibility(length, k);

        this.internalChromosome = new IntegerChromosome(min, max, length);
        this.k = k;
        this.selfLoopEnabled = selfLoopEnabled;
    }

    private void checkTopologyFeasibility(int length, int k) {
        if (length % k != 0) {
            throw new InfeasibleTopologyException();
        }
    }

    @Override
    public boolean isValid() {
        ISeq<IntegerGene> seq = internalChromosome.toSeq();
        for (int i = 0, block = 1; i <= length() - k; i += k, block++) {
            if (seq.subSeq(i, (k * block)).stream().distinct().count() != k) {
                //repeated incoming nodes
                return false;
            } else if (!selfLoopEnabled) {
                for (int j = i; j < i + k; j++) {
                    if (seq.get(j).getAllele().equals(block - 1)) {
                        //there is a self-loop
                        return false;
                    }
                }
            }
        }
        return true;

    }

    @Override
    public TopologyFixedKBNChromosome newInstance(final ISeq<IntegerGene> genes) {
        return new TopologyFixedKBNChromosome(genes, k, selfLoopEnabled);
    }

    @Override
    public TopologyFixedKBNChromosome newInstance() {
        return new TopologyFixedKBNChromosome(getMin(), getMax(), internalChromosome.lengthRange(), k, selfLoopEnabled);

    }

    public static TopologyFixedKBNChromosome of(int min, int max, int length, int k, boolean selfLoopEnabled) {
        return new TopologyFixedKBNChromosome(min, max, length, k, selfLoopEnabled);
    }

    public static TopologyFixedKBNChromosome of(IntRange range, int length, int k, boolean selfLoopEnabled) {
        return new TopologyFixedKBNChromosome(range.getMin(), range.getMax(), length, k, selfLoopEnabled);
    }

    @Override
    public IntegerGene getGene(int index) {
        return internalChromosome.getGene(index);
    }

    @Override
    public int length() {
        return internalChromosome.length();
    }

    @Override
    public ISeq<IntegerGene> toSeq() {
        return internalChromosome.toSeq();
    }


    @Override
    public Iterator<IntegerGene> iterator() {
        return internalChromosome.iterator();
    }

    @Override
    public int hashCode() {
        return internalChromosome.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o)
            return true;

        // null check
        if (o == null)
            return false;

        // type check and cast
        if (getClass() == o.getClass()) {
            TopologyFixedKBNChromosome that = (TopologyFixedKBNChromosome) o;
            return internalChromosome.equals(that.internalChromosome);
        }

        return false;
    }

    @Override
    public String toString() {
        return internalChromosome.toString();
    }

    static class VariableChromosomeLengthException extends RuntimeException {
    }

    static class InfeasibleTopologyException extends RuntimeException {
    }

    static class InvalidCreationMethod extends RuntimeException {
    }


    public static void main(String[] args) {

        IntegerChromosome z1 = IntegerChromosome.of(IntegerGene.of(1, 10), IntegerGene.of(0, 9));

        IntegerChromosome z2 = z1.newInstance((ISeq<IntegerGene>) Seq.of(IntegerGene.of(2, 0, 3), IntegerGene.of(3, 0, 3)));

        TopologyFixedKBNChromosome a = new TopologyFixedKBNChromosome(
                (ISeq<IntegerGene>) Seq.of(
                        IntegerGene.of(2, 0, 3),
                        IntegerGene.of(4, 0, 3)

                ), 2, true);

        TopologyFixedKBNChromosome b = new TopologyFixedKBNChromosome((ISeq<IntegerGene>) Seq.of(IntegerGene.of(1, 10), IntegerGene.of(0, 9)), 2, true);




        /*System.out.println(p.length());
        System.out.println(p.getMin());
        System.out.println(p.getMax());
        System.out.println(a);
        System.out.println(b);
        System.out.println(a.equals(b));
        System.out.println(z2);
        System.out.println(z3);
        System.out.println(z2.equals(z3));
        System.out.println(p1);
        System.out.println(p2);
        */

        TopologyFixedKBNChromosome p = new TopologyFixedKBNChromosome(0, 2, IntRange.of(6), 2, true);
        TopologyFixedKBNChromosome top = p.newInstance(
                (ISeq<IntegerGene>) Seq.of(
                        IntegerGene.of(0, 0, 2),
                        IntegerGene.of(1, 0, 2),
                        IntegerGene.of(0, 0, 2),
                        IntegerGene.of(2, 0, 2),
                        IntegerGene.of(1, 0, 2),
                        IntegerGene.of(0, 0, 2)
                ));

        IntegerChromosome fun = IntegerChromosome.of(IntRange.of(0,2), IntRange.of(3));


        Genotype<IntegerGene> gen = Genotype.of(top, fun);
        System.out.println(gen.getChromosome(0));
        System.out.println(gen.getChromosome(1));
        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = GeneticAlgFitness.fromGenotypeToBN(gen, 2);


        System.out.println(top.isValid());
        System.out.println(bn);



    }


}
