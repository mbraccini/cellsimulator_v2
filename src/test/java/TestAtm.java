import dynamic.SynchronousDynamicsImpl;
import generator.CompleteGenerator;
import generator.RandomnessFactory;
import interfaces.attractor.Attractors;
import interfaces.network.BNClassic;
import interfaces.network.NodeDeterministic;
import interfaces.sequences.Generator;
import interfaces.dynamic.Dynamics;
import interfaces.state.BinaryState;
import interfaces.tes.Atm;
import network.BooleanNetworkFactory;
import noise.CompletePerturbations;
import org.junit.Test;
import simulator.AttractorsFinderService;
import tes.AtmImpl;
import utility.Constant;
import utility.Files;
import utility.GenericUtility;

import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class TestAtm {

    /**
     * Retrieves the path
     * @param path
     * @return
     */
    private String path(String path) {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        return file.getAbsolutePath();
    }

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
        String bnFilename = path(rootDirectory
                + Files.FILE_SEPARATOR
                + "self_loop_bn_1");
        Random pseudoRandom;

        BNClassic<BitSet, Boolean, NodeDeterministic<BitSet,Boolean>> bn = BooleanNetworkFactory.newNetworkFromFile(bnFilename);
        Generator<BinaryState> generator = new CompleteGenerator(bn.getNodesNumber());
        Dynamics<BinaryState> dynamics = new SynchronousDynamicsImpl(bn);
        Attractors<BinaryState> attractors = new AttractorsFinderService<BinaryState>().apply(generator, dynamics, true, true);
        System.out.println(attractors);
        Atm<BinaryState> atm = new CompletePerturbations().apply(attractors, dynamics, Constant.PERTURBATIONS_CUTOFF);
        GenericUtility.printMatrix(atm.getMatrix());

        Integer[][] occurrencesAtm = atm.getOccurrencesMatrix().get();
        assertTrue("Dimensione Atm non corretta", occurrencesAtm.length == 2);
        assertTrue("Dimensione Atm non corretta", occurrencesAtm[1].length == 2);
        assertTrue("Occorrenze non corrette elm [0][0]", occurrencesAtm[0][0] == 4);
        assertTrue("Occorrenze non corrette elm [0][1]", occurrencesAtm[0][1] == 2);
        assertTrue("Occorrenze non corrette elm [1][0]", occurrencesAtm[1][0] == 2);
        assertTrue("Occorrenze non corrette elm [1][1]", occurrencesAtm[1][1] == 1);


        Double[][] doubleAtm = atm.getMatrix();


        assertTrue("double elm [0][0] non corretto", doubleAtm[0][0] == 0.67);
        assertTrue("double elm [0][1] non corretto", doubleAtm[0][1] == 0.33);
        assertTrue("double elm [1][0] non corretto", doubleAtm[1][0] == 0.67);
        assertTrue("double elm [1][1] non corretto", doubleAtm[1][1] == 0.33);


        /* Test if the modified copy is reflected to the original atm */
        Double[][] doubleAtmCopy = atm.getMatrixCopy();

        doubleAtmCopy[0][0] = 0.1;

        assertTrue("double elm [0][0] non corretto", doubleAtm[0][0] == 0.67);

    }


    @Test
    public void atmSumsOfRows() {

        Random rnd = RandomnessFactory.getPureRandomGenerator();
        int iterations = 30000;
        while (iterations > 0) {
            int matrixSize = rnd.nextInt(10) + 2;
            List<Integer> list = rnd.ints(0, 1000).limit(matrixSize * matrixSize).boxed().collect(Collectors.toList());

            int[][] a = new int[matrixSize][matrixSize];

            int start = 0;
            for (int i = 0; i < matrixSize; i++) {
                a[i] = list.subList(start, start + matrixSize).stream().mapToInt(x -> x).toArray();
                start += matrixSize;
            }

            //GenericUtility.printMatrix(a);

            Double[][] atm = new AtmImpl<>(a, null).getMatrix();
            //GenericUtility.printMatrix(atm);

            iterations --;
        }
    }

}