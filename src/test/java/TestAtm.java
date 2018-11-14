import dynamic.SynchronousDynamicsImpl;
import experiments.selfLoop.MainSelfLoopsStatisticsNumberOfAttractors;
import generator.CompleteGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import utility.*;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
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
        RandomGenerator pseudoRandom;

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

        RandomGenerator rnd = RandomnessFactory.getPureRandomGenerator();
        int iterations = 30000;
        while (iterations > 0) {
            int matrixSize = rnd.nextInt(10) + 2;
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < matrixSize * matrixSize; i++) {
                list.add(rnd.nextInt(1000));
            }
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





    @Test
    public void TestRelativeStabilityIndex_1() {
        Double[][] occ =  {  {0.8, 0.2, 0.0},
                             {0.2, 0.5, 0.3},
                             {0.2, 0.4, 0.4}
                          };
        Atm<?> atm = AtmImpl.newInstance(null, occ);
        double delta = 0.01;
        assertArrayEquals("Arrays must be equals", MatrixUtility.stabilityIndexRelativeStabilityJoo(atm), new double[]{1,0.6, 0.1}, delta);
    }

    @Test
    public void TestRelativeStabilityIndex_2() {
        Double[][] occ =    {   {0.8, 0.2},
                                {0.2,0.8},};
        Atm<?> atm = AtmImpl.newInstance(null, occ);
        double delta = 0.01;
        assertArrayEquals("Arrays must be equals", MatrixUtility.stabilityIndexRelativeStabilityJoo(atm), new double[]{0.8,0.8}, delta);
    }

    @Test
    public void TestRelativeStabilityIndex_3() {
        Double[][] occ =  {  {0.2, 0.8},
                {1.0,0.0}
        };
        Atm<?> atm = AtmImpl.newInstance(null, occ);
        double delta = 0.01;
        assertArrayEquals("Arrays must be equals", MatrixUtility.stabilityIndexRelativeStabilityJoo(atm), new double[]{0.4, -0.2}, delta);
    }

    @Test
    public void TestRelativeStabilityIndex_4() {
        Double[][] occ =  {  {0.4, 0.2, 0.4, 0.0},
                            {0.2, 0.4, 0.0, 0.4},
                             {0.6, 0.0, 0.2, 0.2},
                            {0.0, 0.6, 0.2, 0.2},
        };
        Atm<?> atm = AtmImpl.newInstance(null, occ);
        double delta = 0.01;
        assertArrayEquals("Arrays must be equals", MatrixUtility.stabilityIndexRelativeStabilityJoo(atm), new double[]{0.6, 0.6, 0.0, 0.0}, delta);
    }


    @Test
    public void TestMaxMinDiagonal() {
        Double[][] occ = {{0.4, 0.2, 0.4, 0.0},
                            {0.2, 0.4, 0.0, 0.4},
                                {0.6, 0.0, 0.2, 0.2},
                                    {0.0, 0.6, 0.2, 0.2},
        };
        Double[][] occ1 = {{1.0, 0.2, 0.4, 0.0},
                {0.2, 0.4, 0.0, 0.4},
                {0.6, 0.0, 0.2, 0.2},
                {0.0, 0.6, 0.2, 0.0},
        };
        Double[][] occ2 = {{0.0, 0.2},
                {0.2, 0.0},
        };
        assertEquals(0.2, MatrixUtility.retrieveMinMaxDiagonal(occ)._1(),0.001);
        assertEquals(0.4, MatrixUtility.retrieveMinMaxDiagonal(occ)._2(),0.001);

        assertEquals(0.0, MatrixUtility.retrieveMinMaxDiagonal(occ1)._1(),0.001);
        assertEquals(1.0, MatrixUtility.retrieveMinMaxDiagonal(occ1)._2(),0.001);

        assertEquals(0.0, MatrixUtility.retrieveMinMaxDiagonal(occ2)._1(),0.001);
        assertEquals(0.0, MatrixUtility.retrieveMinMaxDiagonal(occ2)._2(),0.001);


    }
}