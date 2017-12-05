package tes;

import java.util.*;

public class PathSearchAlgorithm {
    public static final int PATH_LENGTH_GREATER_THAN = 1;

    private int adjacency_matrix[][];
    private boolean[] onPath;        // vertices in current path
    private Stack<Integer> path;     // the current path
    private int numberOfPaths;       // number of simple path
    private Set<List<Integer>> paths;
    private HashMap<Integer, List<Integer>> neighboursMemory;

    public PathSearchAlgorithm(int adjacency_matrix[][]) {
        this.adjacency_matrix = adjacency_matrix;
        this.onPath = new boolean[adjacency_matrix.length]; //all false
        this.path = new Stack<>();
        this.paths = new HashSet<>();
        this.neighboursMemory = new HashMap<>();
    }

    private List<Integer> findNeighbours(int nodeIndex) {
        List<Integer> adjacent = new ArrayList<>();
        for (int i = 0; i < adjacency_matrix[nodeIndex].length; i++) {
            if (adjacency_matrix[nodeIndex][i] > 0.0) {
                adjacent.add(i);
            }
        }
        return adjacent;
    }

    public void searchPath(int startingNode) {
        path.push(startingNode);
        onPath[startingNode] = true;

        while (!path.isEmpty()) {
            int element = path.peek();
            if (Objects.isNull(neighboursMemory.get(element))) {
                neighboursMemory.put(element, findNeighbours(element));
            }

            int nextElement = -1;
            if (!neighboursMemory.get(element).isEmpty()) {
                for (int idx = 0; idx < neighboursMemory.get(element).size(); idx++) {
                    if (!onPath[neighboursMemory.get(element).get(idx)]) {
                        nextElement = neighboursMemory.get(element).remove(idx);
                        break;
                    }
                }

            }

            if (nextElement == -1) {
                //PATH TROVATO
                this.paths.add(new ArrayList<>(path));
                path.pop();
                onPath[element] = false;
            } else {
                path.push(nextElement);
                onPath[nextElement] = true;
                element = nextElement;
            }
        }
    }

    public Set<List<Integer>> getPaths() {
        return paths;
    }

    public static void main(String arg[]) {


        int adjacency_matrix[][] = {
                {0, 1, 1, 0, 0, 0, 0},  // Node 1: 40
                {0, 0, 0, 1, 0, 0, 0},  // Node 2 :10
                {0, 1, 0, 1, 1, 1, 0},  // Node 3: 20
                {0, 0, 0, 0, 1, 0, 0},  // Node 4: 30
                {0, 0, 0, 0, 0, 0, 1},  // Node 5: 60
                {0, 0, 0, 0, 0, 0, 1},  // Node 6: 50
                {0, 0, 0, 0, 0, 0, 0},  // Node 7: 70
        };

        PathSearchAlgorithm paths = new PathSearchAlgorithm(adjacency_matrix);
        paths.searchPath(0);
        System.out.println(paths.getPaths());



    }


}
