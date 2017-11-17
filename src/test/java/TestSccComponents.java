import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import tes.SCCTarjanAlgorithm;


public class TestSccComponents {

	@Test
	public void TestSccComponents1() {
		Double[][] adjacencyMatrix = new Double[][] {
			//	  0	   1	2	 3	  4    5    6    7
				{0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},

				{0.0, 0.0, 0.5, 0.0, 0.5, 0.5, 0.0, 0.0},

				{0.0, 0.0, 0.0, 0.1, 0.0, 0.0, 0.2, 0.0},

				{0.0, 0.0, 0.5, 0.0, 0.0, 0.0, 0.0, 0.2},

				{0.3, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0},

				{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0},

				{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 0.0, 0.0},

				{0.0, 0.0, 0.0, 0.3, 0.0, 0.0, 0.5, 0.0}
			};
			SCCTarjanAlgorithm tar1 = new SCCTarjanAlgorithm(adjacencyMatrix);
			Set<List<Integer>> scc = tar1.getSCCComponents();
			scc.forEach(x -> Collections.sort(x)); // ordinati in maniera crescente per fare il confronto facilmente
			
			scc.stream().forEach(System.out::println);

			assertTrue("Deve contenere 3 scc component", scc.size() == 3);
			assertTrue("Deve contenere [0, 1 ,4]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(0, 1 ,4))));
			assertTrue("Deve contenere [5, 6]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(5, 6))));
			assertTrue("Deve contenere [2, 3, 7]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(2, 3, 7))));

	}
	
	@Test
	public void TestSccComponents2() {
		Double[][] adjacencyMatrix = new Double[][] {
			//	  0	    1	  2
				{0.88, 0.12, 0.0},

				{0.34, 0.66, 0.0},

				{0.55, 0.0, 0.45}

			};	
			
			SCCTarjanAlgorithm tar1 = new SCCTarjanAlgorithm(adjacencyMatrix);
			Set<List<Integer>> scc = tar1.getSCCComponents();
			scc.forEach(x -> Collections.sort(x)); // ordinati in maniera crescente per fare il confronto facilmente
			
			scc.stream().forEach(System.out::println);

			assertTrue("Deve contenere 2 scc component", scc.size() == 2);
			assertTrue("Deve contenere [0, 1]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(0, 1 ))));
			assertTrue("Deve contenere [2]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(2))));
	}
	
	@Test
	public void TestSccComponents3() {
		
		Double[][] adjacencyMatrix = new Double[][] {
			//	  0	    1	  2   3   4
				{0.0, 0.0, 0.3, 0.4, 0.0},

				{0.2, 0.0, 0.0, 0.0, 0.0},

				{0.0, 0.05, 0.0, 0.0, 0.0},
				
				{0.0, 0.0, 0.0, 0.0, 0.1},
				
				{0.0, 0.0, 0.0, 0.0, 0.0}

			};	
			SCCTarjanAlgorithm tar1 = new SCCTarjanAlgorithm(adjacencyMatrix);
			Set<List<Integer>> scc = tar1.getSCCComponents();
			scc.forEach(x -> Collections.sort(x)); // ordinati in maniera crescente per fare il confronto facilmente
			
			scc.stream().forEach(System.out::println);

			assertTrue("Deve contenere 3 scc component", scc.size() == 3);
			assertTrue("Deve contenere [0, 1, 2]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(0, 1 ,2))));
			assertTrue("Deve contenere [3]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(3))));
			assertTrue("Deve contenere [4]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(4))));

	}
}
