package utility;

public class GenericUtility {
    private GenericUtility(){ }

    /**
     * Generic print matrix
     * @param matrix
     */
    public static <E> void printMatrix(E[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }


    public static void printMatrix(int[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }

    public static void printMatrix(double[][] matrix){
        System.out.println("---------------");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }

}
