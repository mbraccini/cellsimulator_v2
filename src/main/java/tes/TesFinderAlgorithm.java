package tes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Algorithm that compute the TESs starting from an adjacency matrix (and exploiting the computation of the Tarjans's strongly connected components).
 * @author michelebraccini
 *
 */
public class TesFinderAlgorithm {

	private SCCTarjanAlgorithm scc;
	private Set<List<Integer>> sccComponents;
	protected Set<List<Integer>> tesFound = new HashSet<>();
	protected Double[][] atm;

	public TesFinderAlgorithm(Double[][] adjacencyMatrix) {
		this.atm = adjacencyMatrix;
		this.scc = new SCCTarjanAlgorithm(atm);
		this.sccComponents = scc.getSCCComponents();
	}

	public Set<List<Integer>> call() {
		boolean outboundArc = false;

		for (List<Integer> sccComponentAnalyzed : sccComponents) {
			outboundArc = false;
			//System.out.println("componente "+sccComponentAnalyzed);

			for (Integer index : sccComponentAnalyzed) {
				for (Integer i : IntStream.range(0, this.atm.length).boxed().collect(Collectors.toList())){
					//System.out.println(""+index +", "+i+" ->> "+Arrays.asList(atm[index]));

					if (atm[index][i] > 0.0 && !sccComponentAnalyzed.stream().anyMatch(x -> x.intValue() == i.intValue())) {
						//se ha un arco in uscita che porta ad un elemento che non è nella stessa scc a cui lui appartiene quello non
						// è un TES e lo rimuoviamo dal Set!
						//System.out.println("C'è arco in uscita");
						outboundArc = true;
						break;
					}
				}
				if (outboundArc) {
					// se outboundArc == true posso già terminare l'analisi di questa componente connessa escludendola dai TES trovati
					break;
				}
			}
			if (!outboundArc) {
				//se è false invece aggiungo la componente connessa ai TES trovati
				Collections.sort(sccComponentAnalyzed);
				tesFound.add(sccComponentAnalyzed);
			}
		}

		//tesFound.stream().forEach(System.out::println);
		return tesFound;
	}

}

