package utility;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import interfaces.attractor.ImmutableAttractor;
import interfaces.attractor.ImmutableList;
import interfaces.state.Immutable;
import interfaces.state.State;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Files {
    /**
     * OS-independent file separator.
     */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String NEW_LINE = System.getProperty("line.separator");
    public static final String SEMICOLON = ";";
    public static final String COMMA = ",";

    private Files() {
    }

    /**
     * Read a file
     *
     * @param path
     * @return
     */
    public static String readFile(String path) {
        String string = "";
        String read = null;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(path));
            while ((read = in.readLine()) != null) {
                string += read;
                string += "\n";
            }
        } catch (IOException e) {
            System.err.println("There was a problem: " + e);
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return string;
    }

    /**
     * Write a string to a file
     *
     * @param filename
     * @param string
     */
    private static void writeStringToFile(String filename, String string) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(string);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeStringToFileUTF8(String filename, String string) {
        Path file = Paths.get(filename);
        BufferedWriter writer = null;
        try {
            writer = java.nio.file.Files.newBufferedWriter(file, StandardCharsets.UTF_8);
            writer.write(string);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create all the directories specified in the path (even the subfolders!)
     *
     * @param path
     */
    public static void createDirectories(String path) {
        try {
            java.nio.file.Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Serialize an object.
     *
     * @param o
     * @param filename
     */
    public static void serializeObject(Object o, String filename) {
        if (!filename.endsWith(".ser")) {
            filename = filename + ".ser";
        }
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
     * Deserialize an object specified by a file path.
     *
     * @param filename
     * @return
     */
    public static Object deserializeObject(String filename) {
        if (!filename.endsWith(".ser")) {
            filename = filename + ".ser";
        }
        Object ret = null;
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ret = in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException | ClassNotFoundException i) {
            i.printStackTrace();
        }
        return ret;
    }


    /**
     * read csvFile
     * @param
     */
    public static List<String[]> readCsv(String filename, char separator, boolean header) {
        if (!filename.endsWith(".csv")) filename = filename + ".csv";
        CSVReader reader = null;
        List<String[]> s = null;
        try {
            if (header)
                //Build reader instance
                reader = new CSVReader(new FileReader(filename), separator, '"', 1);
            else
                reader = new CSVReader(new FileReader(filename), separator, '"', 0);

            //Read all rows at once
            s = reader.readAll();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }


    /**
     * read csvFile
     * @param
     */
    public static Double[][] readCsvMatrix(String filename, char separator, boolean header) {
        List<String[]> s = readCsv(filename, separator, header);
        Double[][] matrix = new Double[s.size()][];

        for (int i = 0; i < s.size(); i++) {
            String[] p = s.get(i);
            matrix[i] = new Double[p.length];
            for (int j = 0; j < p.length; j++) {
                matrix[i][j] = Double.valueOf(p[j]);
            }
        }

        return matrix;
    }


    /**
     * write List of array to csv file
     * @param elements
     */
    public static void writeToCsv(List<String[]> elements, String filename) {
        if (!filename.endsWith(".csv")) filename = filename + ".csv";
        try{
            CSVWriter writer = new CSVWriter(new FileWriter(filename), ';', '"', '\\', "\n");
            writer.writeAll(elements);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMatrixToCsv(int[][] matrix, String filename) {
        List<String[]> s = Arrays.stream(matrix).map(x -> Arrays.stream(x).mapToObj(String::valueOf).toArray(String[]::new)).collect(Collectors.toList());
        writeToCsv(s, filename);
    }
    public static void writeMatrixToCsv(double[][] matrix, String filename) {
        List<String[]> s = Arrays.stream(matrix).map(x -> Arrays.stream(x).mapToObj(String::valueOf).toArray(String[]::new)).collect(Collectors.toList());
        writeToCsv(s, filename);
    }
    public static <T> void writeMatrixToCsv(T[][] matrix, String filename) {
        List<String[]> s = Arrays.stream(matrix).map(x -> Arrays.stream(x).map(Object::toString).toArray(String[]::new)).collect(Collectors.toList());
        writeToCsv(s, filename);
    }
    public static <T> void writeListsToCsv(List<List<T>> list, String filename) {
        List<String[]> s = list.stream().map(x -> x.stream().map(Object::toString).toArray(String[]::new)).collect(Collectors.toList());
        writeToCsv(s, filename);
    }


    /**
     * Writes an attractors list to a file.
     * @param attractors
     * @param filename
     */
    public static <T extends State> void writeAttractorsToReadableFile(final ImmutableList<ImmutableAttractor<T>> attractors, final String filename) {
        StringWriter writer = new StringWriter();
        List<T> statesInAttractor;
        for (ImmutableAttractor<T> attractor : attractors) {
            statesInAttractor = attractor.getStates();
            writer.write("[id: " + attractor.getId() + " Attractor] Length= " + attractor.getLength()
                    + ", BasinSize= " + (attractor.getBasin().isPresent() ? attractor.getBasin().get().getDimension() : "") + ":");
            writer.append(Files.NEW_LINE);

            for (int i = 0; i < attractor.getLength(); i++) {
                writer.write("s" + i + ":  " + statesInAttractor.get(i).toString());
                writer.append(Files.NEW_LINE);
            }
            writer.append(Files.NEW_LINE);
        }


        Files.writeStringToFileUTF8(filename, writer.toString());
    }

}
