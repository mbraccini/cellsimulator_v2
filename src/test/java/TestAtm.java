import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import interfaces.attractor.Generator;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.dynamic.Dynamics;
import interfaces.network.BooleanNetwork;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.junit.Test;
import simulator.AttractorsFinderService;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class TestAtm {

    private String rootDirectory = "testing"
            + Files.FILE_SEPARATOR
            + "atm";

    /**
     * Attractors (tested with Boolnet):
     Attractor 1 is a simple attractor consisting of 2 state(s) and has a basin of 6 state(s):
     |--<--|
     V     |
     100   |
     011   |
     V     |
     |-->--|
     Genes are encoded in the following order: Gene0 Gene1 Gene2

     Attractor 2 is a simple attractor consisting of 1 state(s) and has a basin of 2 state(s):
     |--<--|
     V     |
     110   |
     V     |
     |-->--|
     Genes are encoded in the following order: Gene0 Gene1 Gene2


     ATM (manually checked):
           Att1 | Att2
     Att1|  4   |   2
     Att2|  2   |   1
     */
    @Test
    public void TestAtm() {

        /** BN from file "sync_bn" **/
        String bnFilename = rootDirectory
                + Files.FILE_SEPARATOR
                + "self_loop_bn_1";
        Random pseudoRandom;

        BooleanNetwork<BitSet, Boolean> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        ImmutableList<ImmutableAttractor<BinaryState>> attractors = new AttractorsFinderService<BinaryState>(generator, dynamics).call();
        System.out.println(attractors);
        CompletePerturbations cp = new CompletePerturbations(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
        Atm<BinaryState> atm = cp.call();
        GenericUtility.printMatrix(atm.getMatrix());

        Integer[][] occurrencesAtm = atm.getOccurrencesMatrix().get();
        assertTrue("Dimensione Atm non corretta", occurrencesAtm.length == 2);
        assertTrue("Dimensione Atm non corretta", occurrencesAtm[1].length == 2);
        assertTrue("Occorrenze non corrette elm [0][0]", occurrencesAtm[0][0] == 4);
        assertTrue("Occorrenze non corrette elm [0][1]", occurrencesAtm[0][1] == 2);
        assertTrue("Occorrenze non corrette elm [1][0]", occurrencesAtm[1][0] == 2);
        assertTrue("Occorrenze non corrette elm [1][1]", occurrencesAtm[1][1] == 1);


        BigDecimal[][] atmBD = atm.getMatrix();
        assertTrue("BigDecimal elm [0][0] non corretto", atmBD[0][0].compareTo(BigDecimal.valueOf(0.67)) == 0);
        assertTrue("BigDecimal elm [0][1] non corretto", atmBD[0][1].compareTo(BigDecimal.valueOf(0.33)) == 0);
        assertTrue("BigDecimal elm [1][0] non corretto", atmBD[1][0].compareTo(BigDecimal.valueOf(0.67)) == 0);
        assertTrue("BigDecimal elm [1][1] non corretto", atmBD[1][1].compareTo(BigDecimal.valueOf(0.33)) == 0);


        assertTrue("double elm [0][0] non corretto", atmBD[0][0].doubleValue() == 0.67);
        assertTrue("double elm [0][1] non corretto", atmBD[0][1].doubleValue() == 0.33);
        assertTrue("double elm [1][0] non corretto", atmBD[1][0].doubleValue() == 0.67);
        assertTrue("double elm [1][1] non corretto", atmBD[1][1].doubleValue() == 0.33);
    }
}