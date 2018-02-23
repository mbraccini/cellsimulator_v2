import org.junit.Test;
import utility.GenericUtility;
import utility.MatrixUtility;

import static org.junit.Assert.assertTrue;

public class TestMatrixUtility {

    /**
     * From this matrix:
     *
     * 0.7  0.3  0.0
     * 0.1  0.1  0.8
     * 0.0  0.4  0.6
     *
     * We must obtain:
     *
     * 0.1  0.8  0.1
     * 0.4  0.6  0.0
     * 0.3  0.0  0.7
     *
     */
    @Test
    public void TestReorderByDiagonalValues() {

        Double[][] matrix = {
                {0.7,  0.3,  0.0},
                {0.1,  0.1,  0.8},
                {0.0,  0.4,  0.6}
        };

        Number[][] m = MatrixUtility.reorderByDiagonalValues(matrix);

        GenericUtility.printMatrix(m);

        assertTrue("elm [0][0] non corretto", m[0][0].doubleValue() == 0.1);
        assertTrue("elm [0][1] non corretto", m[0][1].doubleValue() == 0.8);
        assertTrue("elm [0][2] non corretto", m[0][2].doubleValue() == 0.1);
        assertTrue("elm [1][0] non corretto", m[1][0].doubleValue() == 0.4);
        assertTrue("elm [1][1] non corretto", m[1][1].doubleValue() == 0.6);
        assertTrue("elm [1][2] non corretto", m[1][2].doubleValue() == 0.0);
        assertTrue("elm [2][0] non corretto", m[2][0].doubleValue() == 0.3);
        assertTrue("elm [2][1] non corretto", m[2][1].doubleValue() == 0.0);
        assertTrue("elm [2][2] non corretto", m[2][2].doubleValue() == 0.7);

    }



    /**
     * From this matrix:
     *
     * 0.5 0.2 0.1
     * 0.6 0.3 0.4
     * 0.7 0.2 0.1
     *
     * We must obtain:
     *
     * 0.1 0.2 0.7
     * 0.4 0.3 0.6
     * 0.1 0.2 0.5
     *
     */
    @Test
    public void TestReorderByDiagonalValues_2() {

        Double[][] matrix = {
                {0.5,  0.2,  0.1},
                {0.6,  0.3,  0.4},
                {0.7,  0.2,  0.1}
        };

        Number[][] m = MatrixUtility.reorderByDiagonalValues(matrix);

        GenericUtility.printMatrix(m);

        assertTrue("elm [0][0] non corretto", m[0][0].doubleValue() == 0.1);
        assertTrue("elm [0][1] non corretto", m[0][1].doubleValue() == 0.2);
        assertTrue("elm [0][2] non corretto", m[0][2].doubleValue() == 0.7);
        assertTrue("elm [1][0] non corretto", m[1][0].doubleValue() == 0.4);
        assertTrue("elm [1][1] non corretto", m[1][1].doubleValue() == 0.3);
        assertTrue("elm [1][2] non corretto", m[1][2].doubleValue() == 0.6);
        assertTrue("elm [2][0] non corretto", m[2][0].doubleValue() == 0.1);
        assertTrue("elm [2][1] non corretto", m[2][1].doubleValue() == 0.2);
        assertTrue("elm [2][2] non corretto", m[2][2].doubleValue() == 0.5);

    }

    /**
     * From this matrix:
     *
     * 0.5 0.2 0.1
     * 0.6 0.1 0.4
     * 0.7 0.2 0.3
     *
     * We must obtain:
     *
     * 0.1 0.4 0.6
     * 0.2 0.3 0.7
     * 0.2 0.1 0.5
     *
     */
    @Test
    public void TestReorderByDiagonalValues_3() {

        Double[][] matrix = {
                {0.5,  0.2,  0.1},
                {0.6,  0.1,  0.4},
                {0.7,  0.2,  0.3}
        };

        Number[][] m = MatrixUtility.reorderByDiagonalValues(matrix);

        GenericUtility.printMatrix(m);

        assertTrue("elm [0][0] non corretto", m[0][0].doubleValue() == 0.1);
        assertTrue("elm [0][1] non corretto", m[0][1].doubleValue() == 0.4);
        assertTrue("elm [0][2] non corretto", m[0][2].doubleValue() == 0.6);
        assertTrue("elm [1][0] non corretto", m[1][0].doubleValue() == 0.2);
        assertTrue("elm [1][1] non corretto", m[1][1].doubleValue() == 0.3);
        assertTrue("elm [1][2] non corretto", m[1][2].doubleValue() == 0.7);
        assertTrue("elm [2][0] non corretto", m[2][0].doubleValue() == 0.2);
        assertTrue("elm [2][1] non corretto", m[2][1].doubleValue() == 0.1);
        assertTrue("elm [2][2] non corretto", m[2][2].doubleValue() == 0.5);

    }


    /**
     * From this matrix:
     *
     * 0.5 0.2
     * 0.6 0.1
     *
     * We must obtain:
     *
     * 0.1 0.6
     * 0.2 0.5
     *
     */
    @Test
    public void TestReorderByDiagonalValues_4() {

        Double[][] matrix = {
                {0.5,  0.2},
                {0.6,  0.1}
        };

        Number[][] m = MatrixUtility.reorderByDiagonalValues(matrix);

        GenericUtility.printMatrix(m);

        assertTrue("elm [0][0] non corretto", m[0][0].doubleValue() == 0.1);
        assertTrue("elm [0][1] non corretto", m[0][1].doubleValue() == 0.6);
        assertTrue("elm [0][2] non corretto", m[1][0].doubleValue() == 0.2);
        assertTrue("elm [1][0] non corretto", m[1][1].doubleValue() == 0.5);

    }
}