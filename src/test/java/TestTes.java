import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import tes.TesFinderAlgorithm;

public class TestTes {

	@Test
	public void TestTes1() {
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
			TesFinderAlgorithm tar1 = new TesFinderAlgorithm(adjacencyMatrix);
			Set<List<Integer>> scc = tar1.call();
			scc.forEach(x -> Collections.sort(x)); // ordinati in maniera crescente per fare il confronto facilmente

			scc.stream().forEach(System.out::println);

			assertTrue("Deve contenere 1 TES", scc.size() == 1);
			assertTrue("Deve contenere [5, 6]", scc.stream().anyMatch(x -> x.equals(Arrays.asList(5, 6))));

	}

}
