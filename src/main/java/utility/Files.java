package utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public static void writeStringToFile(String filename, String string) {
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

    public static void writeStringToFile2(String filename, String string) {
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
}
