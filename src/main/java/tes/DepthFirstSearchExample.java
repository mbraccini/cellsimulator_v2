package tes;

import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;
/**
 * https://algs4.cs.princeton.edu/41graph/AllPaths.java.html
 */
public class DepthFirstSearchExample {
    public static final int PATH_LENGTH_GREATER_THAN = 1;

    private int adjacency_matrix[][];
    private boolean[] onPath;        // vertices in current path
    private Stack<Integer> path;     // the current path
    private int numberOfPaths;       // number of simple path
    private Combinations combinations;
    private Set<List<Integer>> paths;

    public DepthFirstSearchExample(int adjacency_matrix[][]) {
        this.adjacency_matrix = adjacency_matrix;
        this.onPath = new boolean[adjacency_matrix.length]; //all false
        this.path = new Stack<>();
        this.combinations = new Combinations(adjacency_matrix.length, 2);
        this.paths = new HashSet<>();
    }

    public Set<List<Integer>> computeAllPaths() {
        Iterator<int[]> iterator = this.combinations.iterator();
        while (iterator.hasNext()) {
            int[] comb = iterator.next();
            dfs(comb[0], comb[1]);
        }

        return paths.stream().filter(x -> x.size() > PATH_LENGTH_GREATER_THAN + 1).collect(Collectors.toSet());
    }


    private List<Integer> findNeighbours(int nodeIndex) {
        List<Integer> adjacent = new ArrayList<>();
        for (int i = 0; i < adjacency_matrix[nodeIndex].length; i++) {
            if (adjacency_matrix[nodeIndex][i] > 0.0) {
                adjacent.add(i);
            }
        }
        //System.out.println("index: "+nodeIndex+", neighbours: " +adjacent);
        return adjacent;
    }


    // use DFS
    private void dfs(int v, int t) {

        // add v to current path
        path.push(v);
        onPath[v] = true;

        // found path from s to t
        if (v == t) {
            System.out.println("Trovato: " + path);
            numberOfPaths++;
            this.paths.add(new ArrayList<>(path));
        }

        // consider all neighbors that would continue path with repeating a node
        else {
            for (int w : findNeighbours(v)) {
                if (!onPath[w])
                    dfs(w, t);
            }
        }

        // done exploring from v, so remove from path
        path.pop();
        onPath[v] = false;
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

        DepthFirstSearchExample paths = new DepthFirstSearchExample(adjacency_matrix);
        //dfsExample.dfs(0,3);
        System.out.println(paths.computeAllPaths().size());



    }


}