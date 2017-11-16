package tes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Tarjan's algorithm for finding the strongly connected components of an adjacency matrix.
 * @author michelebraccini
 *
 */
public class SCCTarjanAlgorithm {

	private boolean[] visited;       // visited[v] = has v been visited?
	private boolean[] onStack;			//per controllare se è sullo stack

	private int[] indices;
	private int[] lowlink;           // lowlink[v] = low number of v

	private Stack<Integer> stack;

	private Double[][] adjacencyMatrix;

	private Set<List<Integer>> sccComponents = new HashSet<>();

	private int index;


	public SCCTarjanAlgorithm(Double[][] adjacencyMatrix) {
		this.adjacencyMatrix = adjacencyMatrix;
		this.visited 	= new boolean[adjacencyMatrix.length];
		this.onStack 	= new boolean[adjacencyMatrix.length]; 

		this.lowlink	= new int[adjacencyMatrix.length];
		this.indices 	= new int[adjacencyMatrix.length];
		this.stack 		= new Stack<Integer>();


		for (int v = 0; v < adjacencyMatrix.length; v++) {
			if (!visited[v]){
				this.strongConnect(v);
			}
		}

	}

	private void strongConnect(int v) {
		this.visited[v] = true;
		this.indices[v] = index;
		this.lowlink[v] = index;
		index++;
		this.stack.push(v);
		this.onStack[v] = true;

		for(int w : getAdjacentVertices(v)){
			if(this.visited[w] == false){ //non ancora viistato w
				strongConnect(w);
				this.lowlink[v] = Math.min(this.lowlink[v], this.lowlink[w]);
			} else if (this.onStack[w] == true){ //w è sullo stack
				this.lowlink[v] = Math.min(this.lowlink[v], this.indices[w]);
			}
		}

		int p;
		//se v è un nodo root facciamo pop dello stack e generiamo un SCC
		if (this.lowlink[v] == this.indices[v]){
			List<Integer> scc = new ArrayList<>(); //nuova scc
			do{
				p = this.stack.pop();
				this.onStack[p] = false;
				scc.add(p);
			} while (p != v);
			this.sccComponents.add(scc);
		}

	}

	private List<Integer> getAdjacentVertices(int v){
		List<Integer> adjacent = new ArrayList<>();
		for(int i = 0; i< adjacencyMatrix.length; i++){
			if(adjacencyMatrix[v][i] > 0.0){
				adjacent.add(i);
			}
		}
		return adjacent;
	}

	public Set<List<Integer>> getSCCComponents(){
		return this.sccComponents;
	}

}
